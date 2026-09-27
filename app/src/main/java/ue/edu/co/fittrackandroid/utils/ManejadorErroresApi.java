package ue.edu.co.fittrackandroid.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import ue.edu.co.fittrackandroid.R;

/**
 * Convierte errores de la API y de conexión en mensajes reutilizables.
 */
public final class ManejadorErroresApi {

    private static final int CODIGO_DEMASIADAS_SOLICITUDES = 429;
    private static final int PRIMER_ERROR_SERVIDOR = 500;
    private static final int ULTIMO_ERROR_SERVIDOR = 599;

    private ManejadorErroresApi() {
    }

    /**
     * Crea un Toast para un código de respuesta HTTP.
     *
     * @param context contexto utilizado para crear el Toast
     * @param codigoRespuesta código HTTP devuelto por el servidor
     * @return Toast preparado para mostrar el error
     */
    public static Toast obtenerToast(@NonNull Context context, int codigoRespuesta) {
        return Toast.makeText(
                context.getApplicationContext(),
                obtenerMensaje(codigoRespuesta),
                Toast.LENGTH_SHORT
        );
    }

    /**
     * Crea un Toast para un fallo sin respuesta HTTP.
     *
     * @param context contexto utilizado para crear el Toast
     * @param throwable fallo recibido por Retrofit
     * @return Toast preparado para mostrar el error
     */
    public static Toast obtenerToast(
            @NonNull Context context,
            @NonNull Throwable throwable
    ) {
        return Toast.makeText(
                context.getApplicationContext(),
                obtenerMensaje(throwable),
                Toast.LENGTH_SHORT
        );
    }

    @StringRes
    private static int obtenerMensaje(int codigoRespuesta) {
        switch (codigoRespuesta) {
            case HttpURLConnection.HTTP_BAD_REQUEST:
                return R.string.error_solicitud_invalida;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return R.string.error_sesion_expirada;
            case HttpURLConnection.HTTP_FORBIDDEN:
                return R.string.error_sin_permisos;
            case HttpURLConnection.HTTP_NOT_FOUND:
                return R.string.error_recurso_no_encontrado;
            case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                return R.string.error_tiempo_espera;
            case CODIGO_DEMASIADAS_SOLICITUDES:
                return R.string.error_demasiadas_solicitudes;
            default:
                if (codigoRespuesta >= PRIMER_ERROR_SERVIDOR
                        && codigoRespuesta <= ULTIMO_ERROR_SERVIDOR) {
                    return R.string.error_servidor;
                }

                return R.string.error_desconocido;
        }
    }

    @StringRes
    private static int obtenerMensaje(@NonNull Throwable throwable) {
        if (throwable instanceof SocketTimeoutException) {
            return R.string.error_tiempo_espera;
        }

        if (throwable instanceof IOException) {
            return R.string.error_conexion;
        }

        return R.string.error_respuesta_invalida;
    }
}
