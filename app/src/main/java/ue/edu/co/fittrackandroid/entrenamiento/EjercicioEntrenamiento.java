package ue.edu.co.fittrackandroid.entrenamiento;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio dentro de un entrenamiento en curso.
 * Conserva su identificador, nombre y las series editables de la sesión.
 */
public class EjercicioEntrenamiento {

    private final String nombre;
    private final Long rutinaEjercicioId;
    private final List<SerieEntrenamiento> series = new ArrayList<>();

    /**
     * Crea un ejercicio con las series recibidas desde la rutina.
     *
     * @param rutinaEjercicioId identificador del bloque dentro de la rutina
     * @param nombre nombre visible del ejercicio
     * @param series series iniciales del ejercicio; puede ser {@code null}
     */
    public EjercicioEntrenamiento(Long rutinaEjercicioId, String nombre,
                                 List<SerieEntrenamiento> series) {
        this.nombre = nombre;
        this.rutinaEjercicioId = rutinaEjercicioId;
        if (series != null) {
            this.series.addAll(series);
        }
    }

    /** @return el nombre visible del ejercicio. */
    public String getNombre() {
        return nombre;
    }

    /** @return el identificador del bloque de ejercicio dentro de la rutina. */
    public Long getRutinaEjercicioId() {
        return rutinaEjercicioId;
    }

    /** @return las series editables del ejercicio. */
    public List<SerieEntrenamiento> getSeries() {
        return series;
    }

    /**
     * Agrega una serie vacía al final del ejercicio.
     *
     * @return la serie recién agregada.
     */
    public SerieEntrenamiento agregarSerie() {
        int numeroSerie = series.size() + 1;
        SerieEntrenamiento serieNueva = new SerieEntrenamiento(numeroSerie, 0, 0);
        series.add(serieNueva);
        return serieNueva;
    }

    /** @return cuántas series tiene el ejercicio, completas o no. */
    public int getCantidadSeries() {
        return series.size();
    }
}
