package ue.edu.co.fittrackandroid.rutinas.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio agregado a la rutina que se está creando.
 * Guarda el identificador del ejercicio para poder relacionarlo con el que ya existe en el
 * backend al momento de guardar la rutina.
 * Al construirlo se agrega automáticamente la primera serie, tal como se ve en la pantalla.
 */
public class EjercicioRutinaEditable {

    private final Long idEjercicio;
    private final String nombre;
    private final String grupoMuscular;
    private final List<SerieRutina> series = new ArrayList<>();

    public EjercicioRutinaEditable(Long idEjercicio, String nombre, String grupoMuscular) {
        this.idEjercicio = idEjercicio;
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
        agregarSerie();
    }

    public Long getIdEjercicio() {
        return idEjercicio;
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
