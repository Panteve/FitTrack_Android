package ue.edu.co.fittrackandroid.entrenamiento.modelo;

import java.util.List;

/** Datos necesarios para crear un entrenamiento terminado. */
public class EntrenamientoCrearRequest {

    private final Long rutinaId;
    private final String fecha;
    private final int duracionMinutos;
    private final String notas;
    private final Integer seriesTotales;
    private final List<RegistroSerieRequest> series;

    /**
     * Crea la solicitud que espera {@code POST /entrenamientos}.
     *
     * @param rutinaId identificador de la rutina realizada
     * @param fecha fecha del entrenamiento en formato ISO
     * @param duracionMinutos duración total redondeada a minutos
     * @param notas notas opcionales
     * @param seriesTotales series que tenía la rutina, completas o pendientes
     * @param series series completadas
     */
    public EntrenamientoCrearRequest(Long rutinaId, String fecha, int duracionMinutos,
                                     String notas, Integer seriesTotales,
                                     List<RegistroSerieRequest> series) {
        this.rutinaId = rutinaId;
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
        this.notas = notas;
        this.seriesTotales = seriesTotales;
        this.series = series;
    }
}
