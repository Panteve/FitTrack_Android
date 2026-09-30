package ue.edu.co.fittrackandroid.rutinas.modelo;

import java.util.List;

public class RutinasResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String diaSemana;
    private List<String> ejercicios;

    /** Obtiene el identificador de la rutina en la lista del servidor. */
    public Long getId() {
        return id;
    }

    /** Obtiene el nombre de la rutina tal como se muestra en la lista. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene la descripción de la rutina tal como se muestra en la lista. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Obtiene el día de la semana asignado a la rutina. */
    public String getDiaSemana() {
        return diaSemana;
    }

    /** Obtiene los nombres de los ejercicios que tiene la rutina. */
    public List<String> getEjercicios() {
        return ejercicios;
    }

    /** Obtiene la cantidad de ejercicios de la rutina, o cero cuando el backend no envió la lista. */
    public int getCantidadEjercicios() {
        return ejercicios == null ? 0 : ejercicios.size();
    }
}
