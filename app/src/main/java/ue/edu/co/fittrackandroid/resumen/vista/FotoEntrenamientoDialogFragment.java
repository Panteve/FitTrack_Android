package ue.edu.co.fittrackandroid.resumen.vista;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.resumen.datos.FotoEntrenamientoLocal;

/**
 * Muestra la foto del entrenamiento a pantalla completa.
 *
 * <p>La miniatura de la tarjeta de resumen es demasiado pequeña para juzgar una foto
 * de progreso, así que al tocarla se abre esta pantalla, con fondo negro y la imagen
 * completa. Se cierra tocando cualquier parte.
 *
 * <p>Solo se le pasa el identificador del entrenamiento, nunca los bytes de la foto:
 * el contenido se vuelve a leer del archivo que FitTrack ya guardó en su caché. Así se
 * evita llevar varios megabytes en los argumentos del fragmento, que pueden fallar al
 * guardarse si la foto es grande.
 */
public class FotoEntrenamientoDialogFragment extends DialogFragment {

    /** Etiqueta del fragmento al agregarlo al gestor de fragmentos. */
    public static final String ETIQUETA = "FotoEntrenamientoDialogFragment";

    private static final String ARG_ENTRENAMIENTO_ID = "entrenamientoId";

    /**
     * Crea la pantalla con el entrenamiento cuya foto se quiere ampliar.
     *
     * @param entrenamientoId identificador del entrenamiento
     * @return fragmento listo para mostrar
     */
    public static FotoEntrenamientoDialogFragment newInstance(Long entrenamientoId) {
        FotoEntrenamientoDialogFragment fragmento = new FotoEntrenamientoDialogFragment();
        Bundle argumentos = new Bundle();

        if (entrenamientoId != null) {
            argumentos.putLong(ARG_ENTRENAMIENTO_ID, entrenamientoId);
        }

        fragmento.setArguments(argumentos);
        return fragmento;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View contenido = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_foto_entrenamiento, null, false);

        ImageView imgFotoAmpliada = contenido.findViewById(
                R.id.imgFotoEntrenamientoAmpliada);
        cargarFotoAmpliada(imgFotoAmpliada);

        // Tocar la pantalla en cualquier parte la cierra, como en un visor de fotos.
        contenido.setOnClickListener(v -> dismiss());

        // Se usa el tema negro del sistema: esto es un visor de imágenes, no una
        // pantalla más de la app, así que no necesita los colores del proyecto.
        // El tema "Fullscreen" además esconde la barra de estado, para que la foto
        // ocupe toda la pantalla y no solo el área entre las barras del sistema.
        Dialog dialogo = new Dialog(
                requireContext(),
                android.R.style.Theme_Black_NoTitleBar_Fullscreen
        );

        // Sin esto la vista se queda suelta: el diálogo abriría vacío.
        dialogo.setContentView(contenido);

        return dialogo;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Por defecto el diálogo ocupa solo su contenido. Sin esto se vería un cuadro
        // pequeño en el centro en vez de la foto a pantalla completa.
        Dialog dialogo = getDialog();

        if (dialogo != null && dialogo.getWindow() != null) {
            dialogo.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        }
    }

    /**
     * Lee la foto guardada en el caché y la muestra a tamaño completo. Si no hay copia
     * local se cierra la pantalla: no hay nada que ver y es mejor salir solo que dejar
     * un rectángulo negro.
     */
    private void cargarFotoAmpliada(ImageView imgFotoAmpliada) {
        Bundle argumentos = getArguments();

        if (argumentos == null || !argumentos.containsKey(ARG_ENTRENAMIENTO_ID)) {
            dismiss();
            return;
        }

        Long entrenamientoId = argumentos.getLong(ARG_ENTRENAMIENTO_ID);
        FotoEntrenamientoLocal fotoLocal = new FotoEntrenamientoLocal(requireContext());
        byte[] contenidoFoto = fotoLocal.leerCopiaLocal(entrenamientoId);

        if (contenidoFoto == null) {
            dismiss();
            return;
        }

        // A pantalla completa se decodifica lo más grande posible. La foto ya viene
        // limitada al subirla, así que no se corre riesgo de cargar una imagen enorme.
        Bitmap foto = fotoLocal.cargarBitmapParaVistaPrevia(
                contenidoFoto,
                Integer.MAX_VALUE
        );

        if (foto == null) {
            dismiss();
            return;
        }

        imgFotoAmpliada.setImageBitmap(foto);
    }
}