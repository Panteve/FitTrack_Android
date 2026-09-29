package ue.edu.co.fittrackandroid.entrenamiento;

/** Detalle de una serie guardada en un entrenamiento. */
public class RegistroSerieResponse {

    private Long id;
    private Long rutinaEjercicioId;
    private Long ejercicioId;
    private String nombreEjercicio;
    private Integer ordenEjercicio;
    private Integer numeroSerie;
    private Integer repeticiones;
    private Double peso;

    public Long getRutinaEjercicioId() {
        return rutinaEjercicioId;
    }

    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    public Integer getNumeroSerie() {
        return numeroSerie;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public Double getPeso() {
        return peso;
    }
}
