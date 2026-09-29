package ue.edu.co.fittrackandroid.rutinas.modelo;

import java.util.List;

/**
 * Modelo de un plan de entrenamiento: nombre, resumen y la lista de sus ejercicios.
 */
public class Rutina {

    // TODO: Agregar un identificador persistente para consultar, editar o eliminar la rutina.
    private final String id;
    private final String nombre;
    private final String resumen;
    private final String descripcion;
    private final String diaSemana;
    private final List<String> ejercicios;

    public Rutina(String descripcion, String id, String nombre, String resumen, String diaSemana, List<String> ejercicios) {
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
