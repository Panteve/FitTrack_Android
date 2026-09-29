package ue.edu.co.fittrackandroid.remote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda y proporciona los tokens y los datos personales de la sesión del usuario.
 */
public final class SesionManager {

    private static final String ARCHIVO_SESION = "sesion_usuario";

    private static final String CLAVE_ACCESS_TOKEN = "access_token";
    private static final String CLAVE_USUARIO_ID = "usuario_id";
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

    /**
     * Guarda los datos necesarios para identificar y mostrar la cuenta activa.
     *
     * @param usuarioId identificador confirmado por el backend
     * @param nombre nombre visible del usuario
     * @param correo correo usado para iniciar sesión
     */
    public void guardarInfoPersonal(Long usuarioId, String nombre, String correo) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario no es válido");
        }

        sharedPreferences.edit()
                .putLong(CLAVE_USUARIO_ID, usuarioId)
                .putString(CLAVE_NOMBRE, nombre)
                .putString(CLAVE_CORREO, correo)
                .apply();
    }

    /**
     * Actualiza únicamente el nombre almacenado en la sesión.
     *
     * @param nombre nuevo nombre confirmado por el backend
     */
    public void guardarNombre(String nombre) {
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

    /**
     * @return identificador del usuario autenticado, o null si no existe una sesión válida.
     */
    public Long obtenerUsuarioId() {
        long usuarioId = sharedPreferences.getLong(CLAVE_USUARIO_ID, -1L);
        return usuarioId > 0 ? usuarioId : null;
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
