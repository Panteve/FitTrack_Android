package ue.edu.co.fittrackandroid.perfil.modelo;

/**
 * Datos enviados para actualizar la contraseña del usuario autenticado.
 * Los nombres de los campos deben coincidir con el JSON que espera el backend.
 * Estas contraseñas solo viajan en la petición: nunca se guardan en el dispositivo.
 */
public class CambiarContrasenaRequest {

    private final String passwordActual;
    private final String passwordNueva;

    public CambiarContrasenaRequest(String passwordActual, String passwordNueva) {
        this.passwordActual = passwordActual;
        this.passwordNueva = passwordNueva;
    }

    public String getPasswordActual() {
        return passwordActual;
    }

    public String getPasswordNueva() {
        return passwordNueva;
    }
}
