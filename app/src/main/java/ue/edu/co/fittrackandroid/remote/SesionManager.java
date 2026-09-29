package ue.edu.co.fittrackandroid.remote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda y proporciona los tokens y los datos personales de la sesión del usuario.
 */
public final class SesionManager {

    private static final String ARCHIVO_SESION = "sesion_usuario";

    private static final String CLAVE_ACCESS_TOKEN = "access_token";
    private static final String CLAVE_NOMBRE = "nombre";
    private static final String CLAVE_CORREO = "correo";

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

    public void guardarInfoPersonal(String nombre, String correo){
        sharedPreferences.edit()
                .putString(CLAVE_NOMBRE, nombre)
                .putString(CLAVE_CORREO, correo)
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

    /**
     * @return el correo con el que inició sesión, o null si todavía no se ha guardado.
     */
    public String obtenerCorreo() {
        return sharedPreferences.getString(
                CLAVE_CORREO,
                null
        );
    }

    public void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
    }
}