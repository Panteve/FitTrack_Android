package ue.edu.co.fittrackandroid.rutinas;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio agregado a la rutina que se está creando.
 * Al construirlo se agrega automáticamente la primera serie, tal como se ve en la pantalla.
 */
public class EjercicioRutinaEditable {

    private final String nombre;
    private final String grupoMuscular;
    private final List<SerieRutina> series = new ArrayList<>();

    public EjercicioRutinaEditable(String nombre, String grupoMuscular) {
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

    public List<SerieRutina> getSeries() {
        return series;
    }

    /**
     * Agrega una serie vacía al final del ejercicio.
     *
     * @return la serie recién agregada.
     */
    public SerieRutina agregarSerie() {
        SerieRutina serieNueva = new SerieRutina();
        series.add(serieNueva);
        return serieNueva;
    }
}
