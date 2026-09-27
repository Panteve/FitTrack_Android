package ue.edu.co.fittrackandroid.entrenamiento;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio dentro de un entrenamiento en curso.
 * Al construirlo se agrega automáticamente la primera serie, tal como se ve en la pantalla.
 */
public class EjercicioEntrenamiento {

    // TODO: Guardar el identificador persistente del ejercicio para no depender de su nombre
    // al registrar el historial o recuperar una sesión interrumpida.
    private final String nombre;
    private final String grupoMuscular;
    private final List<SerieEntrenamiento> series = new ArrayList<>();

    public EjercicioEntrenamiento(String nombre, String grupoMuscular) {
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
        agregarSerie();
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
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
