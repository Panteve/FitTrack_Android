package ue.edu.co.fittrackandroid.hoy.modelo;

import java.util.List;

import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaSerieResponse;

public class EjercisioEnRutina {
    private Long id;
    private Long ejercicioId;
    private String nombre;
    private String grupoMuscular;
    private int orden;
    private List<RutinaSerieResponse> series;

    /** Obtiene el identificador del ejercicio del catálogo al que apunta dentro de la rutina. */
    public Long getEjercicioId() {
        return ejercicioId;
    }

    /** Cambia el identificador del ejercicio del catálogo al que apunta dentro de la rutina. */
    public void setEjercicioId(Long ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    /** Obtiene el identificador de la relación entre el ejercicio y la rutina. */
    public Long getId() {
        return id;
    }

    /** Cambia el identificador de la relación entre el ejercicio y la rutina. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre del ejercicio que se muestra en la rutina. */
    public String getNombre() {
        return nombre;
    }

    /** Cambia el nombre del ejercicio de la rutina. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Obtiene el grupo muscular al que pertenece el ejercicio. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    /** Cambia el grupo muscular del ejercicio de la rutina. */
    public void setGrupoMuscular(String grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    /** Obtiene la posición del ejercicio dentro del orden de la rutina. */
    public int getOrden() {
        return orden;
    }

    /** Cambia la posición del ejercicio dentro del orden de la rutina. */
    public void setOrden(int orden) {
        this.orden = orden;
    }

    /** Obtiene las series planificadas para este ejercicio en la rutina. */
    public List<RutinaSerieResponse> getSeries() {
        return series;
    }

    /** Cambia las series planificadas para este ejercicio en la rutina. */
    public void setSeries(List<RutinaSerieResponse> series) {
        this.series = series;
    }
}
