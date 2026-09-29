package ue.edu.co.fittrackandroid.rutinas;

import java.util.List;

public class RutinasResponse {
    private final String id;
    private final String nombre;
    private final String resumen;
    private final String descripcion;
    private final String diaSemana;
    private final List<String> ejercicios;

    public RutinasResponse(String descripcion, String id, String nombre, String resumen, String diaSemana, List<String> ejercicios) {
        this.descripcion = descripcion;
        this.id = id;
        this.nombre = nombre;
        this.resumen = resumen;
        this.diaSemana = diaSemana;
        this.ejercicios = ejercicios;
    }

    public String getNombre() {
        return nombre;
    }

    public String getResumen() {
        return resumen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public List<String> getEjercicios() {
        return ejercicios;
    }




}
