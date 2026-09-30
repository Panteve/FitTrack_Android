package ue.edu.co.fittrackandroid.entrenamiento.modelo;

/** Respuesta recibida después de subir la foto de un entrenamiento. */
public class EntrenamientoFotoResponse {

    private Long entrenamientoId;
    private String fotoUrl;

    /** @return identificador del entrenamiento al que quedó asociada la foto. */
    public Long getEntrenamientoId() {
        return entrenamientoId;
    }

    /** @return dirección de la foto ya guardada en el backend. */
    public String getFotoUrl() {
        return fotoUrl;
    }
}
