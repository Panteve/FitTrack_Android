package ue.edu.co.fittrackandroid.resumen.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio realizado dentro de un entrenamiento terminado.
 *
 * <p>Solo guarda lo que se va a mostrar: el nombre, el grupo muscular y las series
 * completadas. No tiene listeners, estado editable ni referencias a vistas.
 */
public class EjercicioResumen {

    private final String nombre;
    private final String grupoMuscular;
    private final List<SerieResumen> series;

    /** Crea un ejercicio del resumen con las series completadas que ya trae la sesión. */
    public EjercicioResumen(String nombre, String grupoMuscular, List<SerieResumen> series) {
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
        // Se copia la lista para que el resumen no dependa de la sesión que lo creó.
        this.series = new ArrayList<>(series);
    }

    /** Devuelve el nombre del ejercicio. */
    public String getNombre() {
        return nombre;
    }

    /** Devuelve el grupo muscular principal del ejercicio. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    /** Devuelve las series completadas del ejercicio. */
    public List<SerieResumen> getSeries() {
        return series;
    }

    /** Devuelve cuántas series completadas tiene el ejercicio. */
    public int getCantidadSeries() {
        return series.size();
    }
}
