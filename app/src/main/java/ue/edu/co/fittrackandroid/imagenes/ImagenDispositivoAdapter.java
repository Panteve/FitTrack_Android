package ue.edu.co.fittrackandroid.imagenes;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.LruCache;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ue.edu.co.fittrackandroid.R;

/**
 * Cuadrícula de la galería de FitTrack. Las miniaturas se leen fuera del hilo
 * principal y se guardan en una caché pequeña en memoria, para no repetir la lectura
 * cada vez que el usuario desplaza la lista. Nunca se carga la fotografía completa
 * dentro del RecyclerView.
 */
public class ImagenDispositivoAdapter extends RecyclerView.Adapter<ImagenDispositivoAdapter.VistaImagen> {

    /** Columnas de la cuadrícula. También la usa la galería para el layout manager. */
    public static final int NUMERO_COLUMNAS = 3;

    /** Lado aproximado de la miniatura que se pide al sistema. */
    private static final int LADO_MINIATURA = 256;

    /** Interfaz para avisar que el usuario tocó una imagen. */
    public interface OnImagenClickListener {
        void onImagenClick(ImagenDispositivo imagen);
    }

    private final List<ImagenDispositivo> imagenes;
    private final OnImagenClickListener escucha;
    private final ContentResolver contentResolver;
    private final int tamanioCelda;
    private final ExecutorService ejecutorMiniaturas = Executors.newFixedThreadPool(3);
    private final LruCache<Long, Bitmap> cacheMiniaturas;

    /**
     * @param contexto contexto para leer las miniaturas y medir la pantalla
     * @param imagenes lista inicial de imágenes; se actualiza con {@link #actualizarImagenes}
     * @param escucha  acción a ejecutar cuando se toca una imagen
     */
    public ImagenDispositivoAdapter(Context contexto, List<ImagenDispositivo> imagenes,
                                    OnImagenClickListener escucha) {
        this.imagenes = imagenes;
        this.escucha = escucha;
        this.contentResolver = contexto.getContentResolver();
        this.tamanioCelda = contexto.getResources().getDisplayMetrics().widthPixels
                / NUMERO_COLUMNAS;

        // La caché se mide en kilobytes para no reservar más de una fracción de la
        // memoria disponible del proceso.
        int memoriaDisponibleKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        cacheMiniaturas = new LruCache<Long, Bitmap>(memoriaDisponibleKb / 16) {
            @Override
            protected int sizeOf(Long clave, Bitmap miniatura) {
                return miniatura.getAllocationByteCount() / 1024;
            }
        };
    }

    /** Interfaz del ViewHolder, para distinguir el nombre del estilo del paquete. */
    public class VistaImagen extends RecyclerView.ViewHolder {

        final ImageView imgImagenDispositivo;
        /** Identificador de la imagen que debe mostrar el contenedor en este momento. */
        long imagenEsperada = -1;

        VistaImagen(@NonNull View itemView) {
            super(itemView);
            imgImagenDispositivo = itemView.findViewById(R.id.imgImagenDispositivo);
        }
    }

    @NonNull
    @Override
    public VistaImagen onCreateViewHolder(@NonNull ViewGroup padre, int tipoVista) {
        View vista = LayoutInflater.from(padre.getContext())
                .inflate(R.layout.item_imagen_dispositivo, padre, false);
        VistaImagen contenedor = new VistaImagen(vista);

        contenedor.itemView.setOnClickListener(v -> {
            int posicion = contenedor.getBindingAdapterPosition();
            if (posicion != RecyclerView.NO_POSITION) {
                escucha.onImagenClick(imagenes.get(posicion));
            }
        });

        return contenedor;
    }

    @Override
    public void onBindViewHolder(@NonNull VistaImagen contenedor, int posicion) {
        // Cada celda es cuadrada: el alto se calcula una sola vez con el ancho de pantalla.
        ViewGroup.LayoutParams parametros = contenedor.itemView.getLayoutParams();
        parametros.height = tamanioCelda;
        contenedor.itemView.setLayoutParams(parametros);

        ImagenDispositivo imagen = imagenes.get(posicion);
        contenedor.imagenEsperada = imagen.getId();

        Bitmap enCache = cacheMiniaturas.get(imagen.getId());
        if (enCache != null) {
            contenedor.imgImagenDispositivo.setImageBitmap(enCache);
            return;
        }

        // Sin miniatura todavía: se deja el fondo del recuadro mientras se lee.
        contenedor.imgImagenDispositivo.setImageDrawable(null);

        ejecutorMiniaturas.execute(() -> {
            Bitmap miniatura = leerMiniatura(imagen);
            if (miniatura == null) {
                return;
            }

            cacheMiniaturas.put(imagen.getId(), miniatura);

            contenedor.itemView.post(() -> {
                // El contenedor puede haberse reutilizado para otra imagen mientras
                // se leía la miniatura: solo se pinta si sigue esperando la misma.
                if (contenedor.imagenEsperada == imagen.getId()) {
                    contenedor.imgImagenDispositivo.setImageBitmap(miniatura);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return imagenes.size();
    }

    /** Reemplaza la lista mostrada, por ejemplo cuando vuelve a consultarse MediaStore. */
    public void actualizarImagenes(List<ImagenDispositivo> nuevasImagenes) {
        imagenes.clear();
        imagenes.addAll(nuevasImagenes);
        notifyDataSetChanged();
    }

    /** Detiene las lecturas pendientes. Lo llama la galería cuando se cierra. */
    public void liberarRecursos() {
        ejecutorMiniaturas.shutdownNow();
    }

    /**
     * Lee una miniatura pequeña. Desde Android 10 se usa la miniatura que el propio
     * sistema genera con {@code ContentResolver.loadThumbnail}; en versiones anteriores
     * se decodifica una copia reducida con BitmapFactory.
     *
     * @param imagen imagen de la que se quiere la miniatura
     * @return miniatura lista para la celda, o null si no se pudo leer
     */
    private Bitmap leerMiniatura(ImagenDispositivo imagen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                return contentResolver.loadThumbnail(
                        imagen.getUri(),
                        new Size(LADO_MINIATURA, LADO_MINIATURA),
                        null
                );
            } catch (IOException | SecurityException error) {
                return null;
            }
        }

        return decodificarReducida(imagen);
    }

    /**
     * Decodifica la imagen con un divisor que la deja cerca de {@link #LADO_MINIATURA},
     * en lugar de cargar la fotografía completa en memoria.
     *
     * @param imagen imagen de la que se quiere la miniatura
     * @return miniatura reducida, o null si no se pudo decodificar
     */
    private Bitmap decodificarReducida(ImagenDispositivo imagen) {
        BitmapFactory.Options medidas = new BitmapFactory.Options();
        medidas.inJustDecodeBounds = true;

        try (InputStream entrada = contentResolver.openInputStream(imagen.getUri())) {
            if (entrada == null) {
                return null;
            }
            BitmapFactory.decodeStream(entrada, null, medidas);
        } catch (IOException | SecurityException error) {
            return null;
        }

        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = calcularMuestra(medidas.outWidth, medidas.outHeight);

        try (InputStream entrada = contentResolver.openInputStream(imagen.getUri())) {
            if (entrada == null) {
                return null;
            }
            return BitmapFactory.decodeStream(entrada, null, opciones);
        } catch (IOException | SecurityException error) {
            return null;
        }
    }

    /**
     * @return el divisor de decodificación que deja la imagen cerca del tamaño de miniatura.
     */
    private int calcularMuestra(int anchoOriginal, int altoOriginal) {
        int ladoMayor = Math.max(anchoOriginal, altoOriginal);
        int muestra = 1;

        while (ladoMayor / (muestra * 2) >= LADO_MINIATURA) {
            muestra *= 2;
        }

        return muestra;
    }
}