package ue.edu.co.fittrackandroid.perfil.modelo;

/**
 * Datos enviados para actualizar el nombre del usuario autenticado.
 */
public class CambiarNombreRequest {

    private final String nuevoNombre;

    public CambiarNombreRequest(String nuevoNombre) {
        this.nuevoNombre = nuevoNombre;
    }

    public String getNuevoNombre() {
        return nuevoNombre;
    }
}
