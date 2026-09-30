package ue.edu.co.fittrackandroid.entrenamiento.modelo;

/** Datos enviados para actualizar únicamente las notas de un entrenamiento. */
public class EntrenamientoActualizarNotasRequest {

    private final String notas;

    /**
     * Crea la solicitud parcial de actualización con las notas nuevas, que pueden ser
     * un texto vacío para eliminar las que había.
     */
    public EntrenamientoActualizarNotasRequest(String notas) {
        this.notas = notas;
    }

    /** Obtiene las notas que se enviarán al backend. */
    public String getNotas() {
        return notas;
    }
}
