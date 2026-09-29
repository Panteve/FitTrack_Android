package ue.edu.co.fittrackandroid.entrenamiento;

import java.util.List;

/** Datos necesarios para guardar un entrenamiento terminado. */
public class EntrenamientoCrearRequest {

    private final Long rutinaId;
    private final String fecha;
    private final int duracionMinutos;
    private final String notas;
    private final List<RegistroSerieRequest> series;

    /**
     * Crea la solicitud que espera {@code POST /entrenamientos}.
     *
     * @param rutinaId identificador de la rutina realizada
     * @param fecha fecha del entrenamiento en formato ISO
     * @param duracionMinutos duración total redondeada a minutos
     * @param notas notas opcionales
     * @param series series completadas
     */
    public EntrenamientoCrearRequest(Long rutinaId, String fecha, int duracionMinutos,
                                     String notas, List<RegistroSerieRequest> series) {
        this.rutinaId = rutinaId;
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
        this.notas = notas;
        this.series = series;
    }
}
