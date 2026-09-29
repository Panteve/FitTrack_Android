package ue.edu.co.fittrackandroid.remote;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Agrega el token de sesión a las solicitudes protegidas y excluye el inicio de sesión.
 */
public class AutenticacionInterceptor implements Interceptor {
    private static final String RUTA_LOGIN = "/auth/login";

    private final SesionManager sesionManager;

    /**
     * Crea el interceptor con acceso a la sesión guardada.
     *
     * @param sesionManager administrador que proporciona el token actual
     */
    public AutenticacionInterceptor(SesionManager sesionManager) {
        this.sesionManager = sesionManager;
    }

    /** {@inheritDoc} */
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request solicitudOriginal = chain.request();

        if (esSolicitudLogin(solicitudOriginal)) {
            return chain.proceed(solicitudOriginal);
        }

        String token = sesionManager.obtenerAccessToken();

        if (token == null || token.isEmpty()) {
            return chain.proceed(solicitudOriginal);
        }

        Request solicitudConToken = solicitudOriginal.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(solicitudConToken);
    }

    /**
     * Indica si la solicitud corresponde al inicio de sesión, que debe enviarse sin token.
     *
     * @param solicitud solicitud HTTP que se va a enviar
     * @return {@code true} cuando la ruta termina en {@code /auth/login}
     */
    private boolean esSolicitudLogin(Request solicitud) {
        return solicitud.url().encodedPath().endsWith(RUTA_LOGIN);
    }

}
