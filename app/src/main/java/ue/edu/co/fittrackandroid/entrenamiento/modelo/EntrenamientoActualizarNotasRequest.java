package ue.edu.co.fittrackandroid.entrenamiento.modelo;

/** Datos enviados para actualizar únicamente las notas de un entrenamiento. */
public class EntrenamientoActualizarNotasRequest {

    private final String notas;

    /**
     * Crea la solicitud parcial de actualización.
     *
     * @param notas notas nuevas, o texto vacío para eliminarlas
     */
    public EntrenamientoActualizarNotasRequest(String notas) {
        this.notas = notas;
    }

    /**
     * Obtiene las notas que se enviarán al backend.
     *
     * @return notas nuevas
     */
    public String getNotas() {
        return notas;
    }
}
