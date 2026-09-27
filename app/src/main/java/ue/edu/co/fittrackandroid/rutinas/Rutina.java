package ue.edu.co.fittrackandroid.rutinas;

import java.util.List;

/**
 * Modelo de un plan de entrenamiento: nombre, resumen y la lista de sus ejercicios.
 */
public class Rutina {

    // TODO: Agregar un identificador persistente para consultar, editar o eliminar la rutina.
    private final String nombre;
    private final String resumen;
    private final List<EjercicioRutina> ejercicios;

    public Rutina(String nombre, String resumen, List<EjercicioRutina> ejercicios) {
        this.nombre = nombre;
        this.resumen = resumen;
        this.ejercicios = ejercicios;
    }

    public String getNombre() {
        return nombre;
    }

    public String getResumen() {
        return resumen;
    }

    public List<EjercicioRutina> getEjercicios() {
        return ejercicios;
    }
}
