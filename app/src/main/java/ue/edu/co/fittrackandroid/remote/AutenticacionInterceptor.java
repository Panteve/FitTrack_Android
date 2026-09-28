package ue.edu.co.fittrackandroid.remote;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AutenticacionInterceptor implements Interceptor {
    private final SesionManager sesionManager;

    public AutenticacionInterceptor(SesionManager sesionManager) {
        this.sesionManager = sesionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request solicitudOriginal = chain.request();
        String token = sesionManager.obtenerAccessToken();

        if (token == null || token.isEmpty()) {
            return chain.proceed(solicitudOriginal);
        }

        Request solicitudConToken = solicitudOriginal.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(solicitudConToken);
    }

}
