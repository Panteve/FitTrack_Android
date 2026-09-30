package ue.edu.co.fittrackandroid.resumen.datos;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Descarga en segundo plano la fotografía que el backend tiene guardada para un
 * entrenamiento, de modo que al volver a abrirlo siga viéndose la vista previa.
 *
 * <p>La descarga nunca bloquea la pantalla. Si algo falla, la pantalla conserva lo
 * que ya tuviera y no avisa: la foto es un extra del resumen, no un dato que el
 * usuario haya pedido ver.
 */
public final class DescargadorFotoEntrenamiento {

    private static final int TIEMPO_CONEXION_MILISEGUNDOS = 10_000;
    private static final int TIEMPO_LECTURA_MILISEGUNDOS = 15_000;
    private static final int TAMANO_MAXIMO_FOTO_BYTES = 5 * 1024 * 1024;
    private static final int TAMANO_BUFFER = 8 * 1024;
    private static final String TIPO_JPEG = "image/jpeg";
    private static final String TIPO_PNG = "image/png";

    /** Impide instanciar la clase: todos sus métodos son estáticos. */
    private DescargadorFotoEntrenamiento() {
    }

    /**
     * Descarga la foto en segundo plano, guarda una copia local y la entrega ya en el hilo
     * principal, donde sí se puede tocar la vista previa. Si la dirección está vacía o el
     * entrenamiento no es válido, no se hace nada.
     */
    public static void descargar(
            Context context,
            Long entrenamientoId,
            String fotoUrl,
            ConsumerFoto alTerminar
    ) {
        if (fotoUrl == null || fotoUrl.isBlank()
                || entrenamientoId == null || entrenamientoId <= 0) {
            return;
        }

        Context contextoAplicacion = context.getApplicationContext();
        Handler handler = new Handler(Looper.getMainLooper());
        ExecutorService ejecutor = Executors.newSingleThreadExecutor();

        ejecutor.execute(() -> {
            byte[] contenido = leerFoto(fotoUrl);

            ejecutor.shutdown();

            if (contenido == null) {
                return;
            }

            try {
                // Se guarda una copia para no volver a descargar la misma imagen cada
                // vez que se abre el entrenamiento.
                new FotoEntrenamientoLocal(contextoAplicacion)
                        .guardarCopiaLocal(entrenamientoId, contenido);
            } catch (IOException error) {
                // La copia es una comodidad: si falla, la foto igual se muestra.
            }

            handler.post(() -> alTerminar.entregar(contenido));
        });
    }

    /** Devuelve el contenido de la foto, o nulo si no se pudo descargar. */
    private static byte[] leerFoto(String fotoUrl) {
        HttpURLConnection conexion = null;

        try {
            conexion = (HttpURLConnection) new URL(fotoUrl).openConnection();
            conexion.setConnectTimeout(TIEMPO_CONEXION_MILISEGUNDOS);
            conexion.setReadTimeout(TIEMPO_LECTURA_MILISEGUNDOS);
            conexion.setInstanceFollowRedirects(true);
            conexion.connect();

            int codigo = conexion.getResponseCode();
            if (codigo < HttpURLConnection.HTTP_OK
                    || codigo >= HttpURLConnection.HTTP_MULT_CHOICE) {
                return null;
            }

            if (!tipoPermitido(conexion.getContentType())) {
                return null;
            }

            try (InputStream datos = conexion.getInputStream();
                 ByteArrayOutputStream memoria = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[TAMANO_BUFFER];
                int leidos;
                int total = 0;

                while ((leidos = datos.read(buffer)) != -1) {
                    total += leidos;
                    if (total > TAMANO_MAXIMO_FOTO_BYTES) {
                        return null;
                    }
                    memoria.write(buffer, 0, leidos);
                }

                return memoria.toByteArray();
            }
        } catch (IOException | SecurityException error) {
            return null;
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    /** Revisa que la respuesta sea una imagen JPEG o PNG, o que no declare tipo. */
    private static boolean tipoPermitido(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return true;
        }

        String tipo = contentType
                .split(";", 2)[0]
                .trim()
                .toLowerCase(Locale.ROOT);

        return TIPO_JPEG.equals(tipo) || TIPO_PNG.equals(tipo);
    }

    /**
     * Entrega el resultado de la descarga en el hilo principal.
     *
     * <p>Es una interfaz propia y no un Consumer de Java porque el proyecto no usa
     * funciones lambda encadenadas; además así queda claro que el contenido puede
     * ser null cuando la descarga falla.
     */
    public interface ConsumerFoto {

        /** Avisa que terminó la descarga, con el contenido de la foto o con nulo si no sirvió. */
        void entregar(byte[] contenido);
    }
}
