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
     * Crea la solicitud que espera la API al guardar un entrenamiento, con la rutina
     * realizada,
     * la fecha en formato ISO, la duración ya redondeada a minutos, las notas opcionales
     * y el detalle de las series completadas.
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
