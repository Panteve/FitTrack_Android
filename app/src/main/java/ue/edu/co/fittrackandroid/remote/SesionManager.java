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

    /** Crea el administrador de sesión abriendo el archivo de preferencias donde se guardan los datos del usuario. */
    public SesionManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(
                        ARCHIVO_SESION,
                        Context.MODE_PRIVATE
                );
    }

    /** Guarda el token de acceso que el servidor devuelve al iniciar sesión, para poder enviarlo después en las peticiones. */
    public void guardarTokens(
            String accessToken
    ) {
        sharedPreferences.edit()
                .putString(CLAVE_ACCESS_TOKEN, accessToken)
                .apply();
    }

    /** Guarda el identificador, el nombre y el correo confirmados por el servidor, y rechaza la operación si el identificador no es válido. */
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

    /** Actualiza únicamente el nombre almacenado en la sesión, dejando intactos el token y el resto de datos. */
    public void guardarNombre(String nombre) {
        sharedPreferences.edit()
                .putString(CLAVE_NOMBRE, nombre)
                .apply();
    }

    /** Obtiene el token de acceso guardado en la sesión, o null cuando todavía no se ha iniciado sesión. */
    public String obtenerAccessToken() {
        return sharedPreferences.getString(
                CLAVE_ACCESS_TOKEN,
                null
        );
    }

    /** Obtiene el nombre visible del usuario activo, o un texto por defecto si todavía no se ha guardado. */
    public String obtenerNombre() {
        return sharedPreferences.getString(
                CLAVE_NOMBRE,
                "Sin nombre"
        );
    }

    /** Obtiene el identificador del usuario autenticado, o null si no existe una sesión válida. */
    public Long obtenerUsuarioId() {
        long usuarioId = sharedPreferences.getLong(CLAVE_USUARIO_ID, -1L);
        return usuarioId > 0 ? usuarioId : null;
    }

    /** Obtiene el correo con el que inició sesión el usuario, o null si todavía no se ha guardado. */
    public String obtenerCorreo() {
        return sharedPreferences.getString(
                CLAVE_CORREO,
                null
        );
    }

    /** Borra todos los datos guardados de la sesión para dejar la aplicación como si el usuario nunca hubiera iniciado sesión. */
    public void cerrarSesion() {
        sharedPreferences.edit().clear().apply();
    }
}
