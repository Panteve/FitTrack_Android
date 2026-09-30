package ue.edu.co.fittrackandroid.hoy.modelo;

import java.util.List;

public class RutinaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String diaSemana;
    private List<EjercisioEnRutina> ejercicios;

    /** Obtiene el identificador de la rutina. */
    public Long getId() {
        return id;
    }

    /** Cambia el identificador de la rutina. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene el nombre de la rutina. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene los ejercicios de la rutina con sus series ya resueltas. */
    public List<EjercisioEnRutina> getEjercicios() {
        return ejercicios;
    }

    /** Cambia el nombre de la rutina. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Obtiene la descripción que el usuario escribió para la rutina. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Cambia la descripción de la rutina. */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Obtiene el día de la semana asignado a la rutina. */
    public String getDiaSemana() {
        return diaSemana;
    }

    /** Cambia el día de la semana asignado a la rutina. */
    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }
}
