package ue.edu.co.fittrackandroid.perfil.modelo;

/**
 * Datos enviados para actualizar el nombre del usuario autenticado.
 */
public class CambiarNombreRequest {

    private final String nuevoNombre;

    /** Crea la solicitud con el nombre que el usuario quiere dejar. */
    public CambiarNombreRequest(String nuevoNombre) {
        this.nuevoNombre = nuevoNombre;
    }

    /** Devuelve el nombre nuevo que se quiere establecer. */
    public String getNuevoNombre() {
        return nuevoNombre;
    }
}
