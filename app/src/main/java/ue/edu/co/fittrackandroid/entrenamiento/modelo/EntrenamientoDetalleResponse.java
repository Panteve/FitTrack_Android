package ue.edu.co.fittrackandroid.entrenamiento.modelo;

import java.util.List;

/** Detalle completo de un entrenamiento guardado. */
public class EntrenamientoDetalleResponse {

    private Long id;
    private Long rutinaId;
    private String nombreRutina;
    private String fecha;
    private Integer duracionMinutos;
    private Integer seriesTotales;
    private String notas;
    private String fotoUrl;
    private List<RegistroSerieResponse> series;

    /** Devuelve el identificador del entrenamiento guardado. */
    public Long getId() {
        return id;
    }

    /** Devuelve el identificador de la rutina de la que salió el entrenamiento. */
    public Long getRutinaId() {
        return rutinaId;
    }

    /** Devuelve el nombre de la rutina entrenada. */
    public String getNombreRutina() {
        return nombreRutina;
    }

    /** Devuelve la fecha del entrenamiento tal como la envía el backend. */
    public String getFecha() {
        return fecha;
    }

    /** Devuelve la duración total del entrenamiento en minutos. */
    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    /**
     * Devuelve las series que tenía la rutina al terminar, completas o pendientes.
     * Los entrenamientos guardados antes de que existiera este campo llegan como nulo.
     */
    public Integer getSeriesTotales() {
        return seriesTotales;
    }

    /** Devuelve las notas que el usuario escribió en el entrenamiento. */
    public String getNotas() {
        return notas;
    }

    /** Devuelve la dirección de la fotografía del entrenamiento, o nulo si no tiene foto. */
    public String getFotoUrl() {
        return fotoUrl;
    }

    /** Devuelve el detalle de cada serie registrada en el entrenamiento. */
    public List<RegistroSerieResponse> getSeries() {
        return series;
    }
}
