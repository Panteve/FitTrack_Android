package ue.edu.co.fittrackandroid.resumen.datos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Administra el archivo de la fotografía del entrenamiento mientras está en el
 * dispositivo: lo crea para la cámara, lo valida, lo corrige y lo deja listo para
 * subirlo. También recibe la imagen que el usuario elige en el almacenamiento
 * externo.
 *
 * <p>Todo el trabajo se hace en el caché de la aplicación, que es privado y el sistema
 * limpia solo cuando falta espacio. Por eso no hace falta ningún permiso de
 * almacenamiento: ni para tomar la foto ni para leer el archivo elegido, porque
 * Android concede acceso únicamente al documento que el usuario selecciona.
 */
public final class FotoEntrenamientoLocal {

    private static final String DIRECTORIO_FOTOS = "fotos";
    private static final String PREFIJO_CAPTURA = "captura_";
    private static final String EXTENSION_JPEG = ".jpg";

    /**
     * Límite del backend. La imagen se reduce antes de guardarse, así que es una
     * medida de seguridad: nunca debería alcanzarse.
     */
    private static final int TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;

    private static final int TAMANO_BUFFER = 8 * 1024;

    /** Lado mayor de la imagen subida. Es más que suficiente para ver la foto. */
    private static final int LADO_MAXIMO = 1600;

    private static final int CALIDAD_INICIAL = 85;
    private static final int CALIDAD_MINIMA = 50;

    private final Context contextoAplicacion;
    private final File directorioFotos;
    private final String autoridadProveedor;

    /** Crea el administrador preparando el directorio del caché y la autoridad del FileProvider. */
    public FotoEntrenamientoLocal(Context context) {
        contextoAplicacion = context.getApplicationContext();
        directorioFotos = new File(contextoAplicacion.getCacheDir(), DIRECTORIO_FOTOS);
        // El mismo nombre que declara el FileProvider en el manifiesto, con el
        // applicationId del proyecto, para no dejar el valor escrito a mano.
        autoridadProveedor = contextoAplicacion.getPackageName() + ".fileprovider";
    }

    /**
     * Crea el archivo donde la cámara va a escribir la fotografía, devolviendo nulo
     * si no se pudo crear.
     */
    public File crearArchivoCaptura() {
        if (!directorioFotos.isDirectory() && !directorioFotos.mkdirs()) {
            return null;
        }

        try {
            File archivoCaptura = File.createTempFile(
                    PREFIJO_CAPTURA,
                    EXTENSION_JPEG,
                    directorioFotos
            );

            return archivoCaptura.isFile() ? archivoCaptura : null;
        } catch (IOException error) {
            return null;
        }
    }

    /**
     * Traduce el archivo de la captura a una dirección segura que la aplicación de
     * cámara puede escribir. Falla si el archivo está fuera de la ruta compartida.
     */
    public Uri obtenerUriParaCamara(File archivoCaptura) {
        return FileProvider.getUriForFile(
                contextoAplicacion,
                autoridadProveedor,
                archivoCaptura
        );
    }

    /**
     * Comprueba que la cámara sí escribió algo y que se puede decodificar: el archivo
     * tiene que existir, no estar vacío y ser una imagen legible.
     */
    public boolean esCapturaValida(File archivoCaptura) {
        if (archivoCaptura == null
                || !archivoCaptura.isFile()
                || archivoCaptura.length() == 0) {
            return false;
        }

        BitmapFactory.Options medidas = new BitmapFactory.Options();
        medidas.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivoCaptura.getAbsolutePath(), medidas);

        return medidas.outWidth > 0 && medidas.outHeight > 0;
    }

    /**
     * Prepara la fotografía para subirla: aplica la orientación que dejó la cámara,
     * reduce el tamaño si excede el límite y la vuelve a guardar como JPEG.
     *
     * <p>La captura original no se toca. Se escribe una copia y solo cuando esa copia
     * quedó bien se devuelve como resultado. Si algo sale mal se devuelve nulo.
     */
    public byte[] prepararParaSubir(File archivoCaptura) {
        if (!esCapturaValida(archivoCaptura)) {
            return null;
        }

        Bitmap imagen = decodificarReducida(archivoCaptura);
        if (imagen == null) {
            return null;
        }

        Bitmap enderezada = aplicarOrientacion(archivoCaptura, imagen);
        byte[] contenido = comprimirComoJpeg(enderezada);

        if (enderezada != imagen) {
            enderezada.recycle();
        }
        imagen.recycle();

        if (contenido == null || contenido.length == 0
                || contenido.length > TAMANO_MAXIMO_BYTES) {
            return null;
        }

        return contenido;
    }

    /**
     * Decodifica una copia reducida en lugar de la imagen original completa, para no
     * cargar en memoria una fotografía de la cámara de varios megapíxeles. Devuelve
     * nulo si la imagen no se pudo decodificar.
     */
    private Bitmap decodificarReducida(File archivoCaptura) {
        BitmapFactory.Options medidas = new BitmapFactory.Options();
        medidas.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivoCaptura.getAbsolutePath(), medidas);

        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = calcularMuestra(
                medidas.outWidth,
                medidas.outHeight
        );

        return BitmapFactory.decodeFile(archivoCaptura.getAbsolutePath(), opciones);
    }

    /**
     * Calcula el divisor más cercano que deja la imagen dentro del lado máximo
     * permitido sin quedarse demasiado pequeña.
     */
    private int calcularMuestra(int anchoOriginal, int altoOriginal) {
        return calcularMuestra(anchoOriginal, altoOriginal, LADO_MAXIMO);
    }

    /**
     * Calcula el divisor de decodificación para un tamaño de vista concreto, reduciendo
     * la imagen sin quedarnos sin resolución.
     */
    private int calcularMuestra(int anchoOriginal, int altoOriginal, int ladoObjetivo) {
        int ladoMayor = Math.max(anchoOriginal, altoOriginal);
        int ladoMaximo = Math.max(1, ladoObjetivo);
        int muestra = 1;

        while (ladoMayor / (muestra * 2) >= ladoMaximo) {
            muestra *= 2;
        }

        return muestra;
    }

    /**
     * Copia en el caché la imagen elegida en el almacenamiento externo.
     *
     * <p>No hace falta permiso de almacenamiento: al elegir el archivo, Android da
     * acceso únicamente a ese documento. La copia vive en el caché porque es privada
     * y el sistema puede limpiarla sin perder la foto que ya está en el backend.
     *
     * <p>Devuelve el archivo con la copia, o nulo si la imagen supera el tamaño permitido.
     * Si el proveedor no deja leerla o llegó vacía, avisa con una excepción de entrada y salida.
     */
    @Nullable
    public File copiarDesdeUri(Uri uri) throws IOException {
        if (uri == null) {
            throw new IOException("No se eligió ninguna imagen");
        }

        File archivoNuevo = crearArchivoCaptura();
        if (archivoNuevo == null) {
            throw new IOException("No se pudo crear el archivo temporal");
        }

        try {
            boolean cabeEnElLimite = copiarContenido(uri, archivoNuevo);
            if (!cabeEnElLimite) {
                eliminarCaptura(archivoNuevo);
                return null;
            }
        } catch (IOException | SecurityException error) {
            eliminarCaptura(archivoNuevo);
            throw error;
        }

        return archivoNuevo;
    }

    /**
     * Escribe en el archivo de destino todo lo que el proveedor entregue, sin dejar
     * que pase del límite del backend. Indica si la imagen cabe, y avisa con una
     * excepción de entrada y salida si no se puede leer o si llegó vacía.
     */
    private boolean copiarContenido(Uri uri, File destino) throws IOException {
        int bytesTotales = 0;

        try (InputStream entrada = contextoAplicacion
                .getContentResolver()
                .openInputStream(uri);
             FileOutputStream salida = new FileOutputStream(destino, false)) {

            if (entrada == null) {
                throw new IOException("No se pudo abrir la imagen seleccionada");
            }

            byte[] buffer = new byte[TAMANO_BUFFER];
            int bytesLeidos;

            while ((bytesLeidos = entrada.read(buffer)) != -1) {
                bytesTotales += bytesLeidos;
                if (bytesTotales > TAMANO_MAXIMO_BYTES) {
                    return false;
                }
                salida.write(buffer, 0, bytesLeidos);
            }
        }

        if (bytesTotales == 0) {
            throw new IOException("La imagen seleccionada está vacía");
        }

        return true;
    }

    /**
     * La cámara guarda la orientación en los metadatos, no en los píxeles. Si no se
     * aplica, una foto tomada en horizontal aparece de lado. Devuelve el mismo bitmap
     * cuando la foto ya estaba derecha.
     */
    private Bitmap aplicarOrientacion(File archivoCaptura, Bitmap imagen) {
        int rotacion = leerRotacion(archivoCaptura);

        if (rotacion == 0) {
            return imagen;
        }

        Matrix matriz = new Matrix();
        matriz.postRotate(rotacion);

        return Bitmap.createBitmap(
                imagen,
                0,
                0,
                imagen.getWidth(),
                imagen.getHeight(),
                matriz,
                true
        );
    }

    /**
     * Lee el metadato de orientación y lo traduce a grados, devolviendo cero si la foto
     * ya estaba derecha o si no se pudo leer el metadato.
     */
    private int leerRotacion(File archivoCaptura) {
        int orientacion;
        try {
            ExifInterface exif = new ExifInterface(archivoCaptura.getAbsolutePath());
            orientacion = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            );
        } catch (IOException | SecurityException error) {
            // Sin metadatos se muestra tal cual llegó.
            return 0;
        }

        switch (orientacion) {
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_TRANSPOSE:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Comprime la imagen como JPEG, reduciendo la calidad si todavía no cabe en el
     * límite del backend. Devuelve el contenido JPEG, o nulo si no se pudo comprimir.
     */
    private byte[] comprimirComoJpeg(Bitmap imagen) {
        int calidad = CALIDAD_INICIAL;

        while (calidad >= CALIDAD_MINIMA) {
            ByteArrayOutputStream memoria = new ByteArrayOutputStream();

            if (!imagen.compress(Bitmap.CompressFormat.JPEG, calidad, memoria)) {
                return null;
            }

            byte[] contenido = memoria.toByteArray();
            if (contenido.length <= TAMANO_MAXIMO_BYTES) {
                return contenido;
            }

            calidad -= 15;
        }

        return null;
    }

    /**
     * Decodifica una copia pequeña del contenido JPEG para mostrarla en la vista previa,
     * ajustada al espacio disponible. Devuelve nulo si el contenido no es una imagen legible.
     */
    public Bitmap cargarBitmapParaVistaPrevia(byte[] contenido, int ladoMaximo) {
        if (contenido == null || contenido.length == 0) {
            return null;
        }

        BitmapFactory.Options medidas = new BitmapFactory.Options();
        medidas.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(contenido, 0, contenido.length, medidas);

        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = calcularMuestra(
                medidas.outWidth,
                medidas.outHeight,
                ladoMaximo
        );

        return BitmapFactory.decodeByteArray(
                contenido,
                0,
                contenido.length,
                opciones
        );
    }

    /**
     * Escribe la fotografía en el caché para poder mostrarla sin volver a
     * descargarla. Si no se puede guardar, la copia anterior se conserva.
     *
     * <p>Cada entrenamiento tiene su propio archivo: la copia se llama con el
     * identificador para que la foto de uno no aparezca en el resumen de otro.
     *
     * <p>Si la copia no puede escribirse o no resulta ser una imagen válida, avisa con
     * una excepción de entrada y salida.
     */
    public void guardarCopiaLocal(Long entrenamientoId, byte[] contenido)
            throws IOException {
        if (entrenamientoId == null || entrenamientoId <= 0) {
            throw new IOException("El identificador del entrenamiento no es válido");
        }

        if (contenido == null || contenido.length == 0
                || contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new IOException("La foto no tiene un tamaño válido");
        }

        if (!directorioFotos.isDirectory() && !directorioFotos.mkdirs()) {
            throw new IOException("No se pudo crear el directorio de fotos");
        }

        File archivoTemporal = File.createTempFile(
                PREFIJO_CAPTURA + "subida",
                EXTENSION_JPEG,
                directorioFotos
        );
        File archivoDefinitivo = obtenerArchivoCopia(entrenamientoId);

        try {
            escribir(contenido, archivoTemporal);
            validarImagen(archivoTemporal);

            if (archivoDefinitivo.exists() && !archivoDefinitivo.delete()) {
                throw new IOException("No se pudo reemplazar la foto anterior");
            }

            if (!archivoTemporal.renameTo(archivoDefinitivo)) {
                throw new IOException("No se pudo guardar la foto");
            }
        } finally {
            if (archivoTemporal.exists()) {
                archivoTemporal.delete();
            }
        }
    }

    /**
     * Revisa si hay copia local sin cargarla. Sirve para no abrir un visor vacío:
     * solo mira el archivo en disco, no lee su contenido.
     */
    public boolean existeCopiaLocal(Long entrenamientoId) {
        if (entrenamientoId == null || entrenamientoId <= 0) {
            return false;
        }

        File archivo = obtenerArchivoCopia(entrenamientoId);

        return archivo.isFile()
                && archivo.length() > 0
                && archivo.length() <= TAMANO_MAXIMO_BYTES;
    }

    /** Devuelve el contenido de la copia local de ese entrenamiento, o nulo si no hay. */
    public byte[] leerCopiaLocal(Long entrenamientoId) {
        if (entrenamientoId == null || entrenamientoId <= 0) {
            return null;
        }

        File archivo = obtenerArchivoCopia(entrenamientoId);

        if (!archivo.isFile() || archivo.length() == 0
                || archivo.length() > TAMANO_MAXIMO_BYTES) {
            return null;
        }

        try (InputStream entrada = new FileInputStream(archivo);
             ByteArrayOutputStream memoria = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[TAMANO_BUFFER];
            int leidos;

            while ((leidos = entrada.read(buffer)) != -1) {
                memoria.write(buffer, 0, leidos);
            }

            return memoria.toByteArray();
        } catch (IOException error) {
            return null;
        }
    }

    /** Devuelve el archivo donde se guarda la copia local de un entrenamiento. */
    private File obtenerArchivoCopia(Long entrenamientoId) {
        return new File(
                directorioFotos,
                "entrenamiento_" + entrenamientoId + EXTENSION_JPEG
        );
    }

    /** Escribe el contenido en el archivo y fuerza a disco para que la copia no se pierda. */
    private void escribir(byte[] contenido, File destino) throws IOException {
        try (FileOutputStream salida = new FileOutputStream(destino, false)) {
            salida.write(contenido);
            salida.flush();
            salida.getFD().sync();
        }
    }

    /** Revisa que el archivo contenga una imagen decodificable y avisa si no lo es. */
    private void validarImagen(File archivo) throws IOException {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivo.getAbsolutePath(), opciones);

        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) {
            throw new IOException("El archivo no contiene una imagen válida");
        }
    }

    /** Borra la captura de la cámara una vez que ya no se necesita. */
    public void eliminarCaptura(File archivoCaptura) {
        if (archivoCaptura != null && archivoCaptura.exists()) {
            archivoCaptura.delete();
        }
    }
}
