package ue.edu.co.fittrackandroid.entrenamiento;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio dentro de un entrenamiento en curso.
 * Al construirlo se agrega automáticamente la primera serie, tal como se ve en la pantalla.
 */
public class EjercicioEntrenamiento {

    private final String nombre;
    private final int id;
    private final List<SerieEntrenamiento> series = new ArrayList<>();

    public EjercicioEntrenamiento(int id, String nombre, List<SerieEntrenamiento> series) {
        this.nombre = nombre;
        this.id = id;
        this.series.addAll(series);
    }

    public String getNombre() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public List<SerieEntrenamiento> getSeries() {
        return series;
    }

    /**
     * Agrega una serie vacía al final del ejercicio.
     *
     * @return la serie recién agregada.
     */
    public SerieEntrenamiento agregarSerie() {
        SerieEntrenamiento serieNueva = new SerieEntrenamiento();
        series.add(serieNueva);
        return serieNueva;
    }

    /** @return cuántas series tiene el ejercicio, completas o no. */
    public int getCantidadSeries() {
        return series.size();
    }
}
