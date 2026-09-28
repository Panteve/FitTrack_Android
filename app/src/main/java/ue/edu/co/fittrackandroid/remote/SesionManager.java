package ue.edu.co.fittrackandroid.remote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda y proporciona los tokens de la sesión del usuario.
 */
public final class SesionManager {

    private static final String ARCHIVO_SESION = "sesion_usuario";

    private static final String CLAVE_ACCESS_TOKEN = "access_token";
    private static final String CLAVE_NOMBRE = "nombre";

    private final SharedPreferences sharedPreferences;

    public SesionManager(Context context) {
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

    public void guardarNombre(String nombre){
        sharedPreferences.edit()
                .putString(CLAVE_NOMBRE, nombre)
                .apply();
    }

    public String obtenerAccessToken() {
        return sharedPreferences.getString(
                CLAVE_ACCESS_TOKEN,
                null
        );
    }

    public String obtenerNombre() {
        return sharedPreferences.getString(
                CLAVE_NOMBRE,
                "Sin nombre"
        );
    }

    public void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
    }
}