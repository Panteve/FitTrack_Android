package ue.edu.co.fittrackandroid.entrenamiento.modelo;

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

    /** Devuelve el identificador del bloque de la rutina al que pertenece la serie. */
    public Long getRutinaEjercicioId() {
        return rutinaEjercicioId;
    }

    /** Devuelve el identificador del ejercicio guardado en el backend. */
    public Long getEjercicioId() {
        return ejercicioId;
    }

    /** Devuelve el nombre del ejercicio al que pertenece la serie. */
    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    /** Devuelve el grupo muscular principal del ejercicio, o nulo si el backend no lo envía. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    /** Devuelve la posición del ejercicio dentro de la rutina, con la que llega la lista. */
    public Integer getOrdenEjercicio() {
        return ordenEjercicio;
    }

    /** Devuelve el número que ocupa la serie dentro del ejercicio. */
    public Integer getNumeroSerie() {
        return numeroSerie;
    }

    /** Devuelve las repeticiones registradas en la serie. */
    public Integer getRepeticiones() {
        return repeticiones;
    }

    /** Devuelve el peso registrado en la serie. */
    public Double getPeso() {
        return peso;
    }
}
