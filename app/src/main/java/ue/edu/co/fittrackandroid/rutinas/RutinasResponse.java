package ue.edu.co.fittrackandroid.rutinas;

import java.util.List;

public class RutinasResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String diaSemana;
    private List<String> ejercicios;

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
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

    public int getCantidadEjercicios() {
        return ejercicios == null ? 0 : ejercicios.size();
    }
}
