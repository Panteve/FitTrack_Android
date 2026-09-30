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

    /** Crea el plan de entrenamiento con su descripción, nombre, resumen, día asignado y nombres de ejercicios. */
    public Rutina(String descripcion, String id, String nombre, String resumen, String diaSemana, List<String> ejercicios) {
        this.descripcion = descripcion;
        this.id = id;
        this.nombre = nombre;
        this.resumen = resumen;
        this.diaSemana = diaSemana;
        this.ejercicios = ejercicios;
    }

    /** Obtiene el nombre del plan de entrenamiento. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene el resumen breve del plan de entrenamiento. */
    public String getResumen() {
        return resumen;
    }

    /** Obtiene la descripción que el usuario escribió para la rutina. */
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
}
