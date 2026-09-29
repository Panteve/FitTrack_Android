package ue.edu.co.fittrackandroid.hoy.modelo;

import java.util.List;

public class RutinaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String diaSemana;
    private List<EjercisioEnRutina> ejercicios;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public List<EjercisioEnRutina> getEjercicios() {
        return ejercicios;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }
}
