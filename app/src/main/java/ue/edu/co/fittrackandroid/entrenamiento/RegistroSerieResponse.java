package ue.edu.co.fittrackandroid.entrenamiento;

/** Detalle de una serie guardada en un entrenamiento. */
public class RegistroSerieResponse {

    private Long id;
    private Long rutinaEjercicioId;
    private Long ejercicioId;
    private String nombreEjercicio;
    private String grupoMuscular;
    private Integer ordenEjercicio;
    private Integer numeroSerie;
    private Integer repeticiones;
    private Double peso;

    public Long getRutinaEjercicioId() {
        return rutinaEjercicioId;
    }

    public Long getEjercicioId() {
        return ejercicioId;
    }

    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    /** @return el grupo muscular principal del ejercicio, o null si el backend no lo envía. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    /** @return la posición del ejercicio dentro de la rutina, con la que llega la lista. */
    public Integer getOrdenEjercicio() {
        return ordenEjercicio;
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
