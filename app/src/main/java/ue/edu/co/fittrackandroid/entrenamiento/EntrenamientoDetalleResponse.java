package ue.edu.co.fittrackandroid.entrenamiento;

import java.util.List;

/** Detalle completo de un entrenamiento guardado. */
public class EntrenamientoDetalleResponse {

    private Long id;
    private Long rutinaId;
    private String nombreRutina;
    private String fecha;
    private Integer duracionMinutos;
    private String notas;
    private String fotoUrl;
    private List<RegistroSerieResponse> series;

    public Long getId() {
        return id;
    }

    public Long getRutinaId() {
        return rutinaId;
    }

    public String getNombreRutina() {
        return nombreRutina;
    }

    public String getFecha() {
        return fecha;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public String getNotas() {
        return notas;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public List<RegistroSerieResponse> getSeries() {
        return series;
    }
}
