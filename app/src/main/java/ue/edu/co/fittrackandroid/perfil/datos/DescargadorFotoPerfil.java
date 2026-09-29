package ue.edu.co.fittrackandroid.perfil.datos;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Descarga en segundo plano la foto firmada recibida durante el inicio de sesión. */
public final class DescargadorFotoPerfil {

    private static final int TIEMPO_CONEXION_MILISEGUNDOS = 10_000;
    private static final int TIEMPO_LECTURA_MILISEGUNDOS = 15_000;
    private static final int TAMANO_MAXIMO_FOTO_BYTES = 5 * 1024 * 1024;
    private static final String TIPO_JPEG = "image/jpeg";
    private static final String TIPO_PNG = "image/png";

    private DescargadorFotoPerfil() {
    }

    /**
     * Descarga sin bloquear la navegación. Si algo falla, la copia anterior se conserva.
     *
     * @param fotoPerfilUrl URL firmada temporal
     * @param usuarioId identificador usado para nombrar la copia local
     * @param fotoPerfilLocal administrador del archivo privado
     */
    public static void descargar(
            String fotoPerfilUrl,
            Long usuarioId,
            FotoPerfilLocal fotoPerfilLocal) {
        if (fotoPerfilUrl == null || fotoPerfilUrl.isBlank()
                || usuarioId == null || usuarioId <= 0) {
            return;
        }

        ExecutorService ejecutor = Executors.newSingleThreadExecutor();
        ejecutor.execute(() -> {
            HttpURLConnection conexion = null;
            try {
                conexion = (HttpURLConnection) new URL(fotoPerfilUrl).openConnection();
                conexion.setConnectTimeout(TIEMPO_CONEXION_MILISEGUNDOS);
                conexion.setReadTimeout(TIEMPO_LECTURA_MILISEGUNDOS);
                conexion.setInstanceFollowRedirects(true);
                conexion.connect();

                int codigo = conexion.getResponseCode();
                if (codigo < HttpURLConnection.HTTP_OK
                        || codigo >= HttpURLConnection.HTTP_MULT_CHOICE) {
                    return;
                }

                if (!tipoPermitido(conexion.getContentType())) {
                    return;
                }

                int tamanoDeclarado = conexion.getContentLength();
                if (tamanoDeclarado > TAMANO_MAXIMO_FOTO_BYTES) {
                    return;
                }

                try (InputStream datos = conexion.getInputStream()) {
                    fotoPerfilLocal.guardarDesdeStream(usuarioId, datos);
                }
            } catch (IOException | SecurityException error) {
                // El inicio de sesión continúa y FotoPerfilLocal conserva la copia anterior.
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
                ejecutor.shutdown();
            }
        });
    }

    private static boolean tipoPermitido(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return true;
        }

        String tipoNormalizado = contentType
                .split(";", 2)[0]
                .trim()
                .toLowerCase(Locale.ROOT);
        return TIPO_JPEG.equals(tipoNormalizado) || TIPO_PNG.equals(tipoNormalizado);
    }
}
