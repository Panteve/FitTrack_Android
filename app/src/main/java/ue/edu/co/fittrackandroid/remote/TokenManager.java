package ue.edu.co.fittrackandroid.remote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda y proporciona los tokens de la sesión del usuario.
 */
public final class TokenManager {

    private static final String ARCHIVO_SESION = "sesion_usuario";
    private static final String CLAVE_ACCESS_TOKEN = "access_token";

    private final SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(
                        ARCHIVO_SESION,
                        Context.MODE_PRIVATE
                );
    }

    public void guardarTokens(
            String accessToken
    ) {
        sharedPreferences.edit()
                .putString(CLAVE_ACCESS_TOKEN, accessToken)
                .apply();
    }

    public String obtenerAccessToken() {
        return sharedPreferences.getString(
                CLAVE_ACCESS_TOKEN,
                null
        );
    }

    public void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
    }
}