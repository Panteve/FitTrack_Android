package ue.edu.co.fittrackandroid.rutinas.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio agregado a la rutina que se está creando o modificando.
 * Guarda el identificador del ejercicio para poder relacionarlo con el que ya existe en el
 * backend al momento de guardar la rutina.
 * Al construirlo se agrega automáticamente la primera serie, tal como se ve en la pantalla.
 * Si el ejercicio ya venía con series, se usa el constructor que las recibe y la pantalla
 * se abre con todo lo que la rutina tenía guardado.
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

    /**
     * Construye el ejercicio con las series que ya tenía en el backend, para abrir una
     * rutina existente con todo precargado. Solo se agrega la primera serie vacía cuando
     * la rutina llegó sin ninguna, porque en ese caso el usuario necesita una fila donde
     * escribir. Los identificadores internos de la rutina no se guardan: al actualizar,
     * el backend reemplaza la configuración completa de la rutina.
     *
     * @param idEjercicio identificador del ejercicio que ya existe.
     * @param nombre nombre del ejercicio.
     * @param grupoMuscular grupo muscular del ejercicio.
     * @param seriesExistentes series que ya tenía la rutina, con sus valores objetivo.
     */
    public EjercicioRutinaEditable(Long idEjercicio, String nombre, String grupoMuscular,
                                  List<SerieRutina> seriesExistentes) {
        this.idEjercicio = idEjercicio;
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;

        if (seriesExistentes != null) {
            series.addAll(seriesExistentes);
        }

        if (series.isEmpty()) {
            agregarSerie();
        }
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

    /**
     * Indica si el ejercicio tiene más de una serie. El backend exige que cada ejercicio
     * tenga al menos una, así que la última no se puede quitar.
     *
     * @return true si se puede quitar alguna serie.
     */
    public boolean puedeEliminarSerie() {
        return series.size() > 1;
    }

    /**
     * Quita una serie del ejercicio. Como el número de serie sale de la posición, las que
     * quedan se renumeran solas al redibujarse.
     *
     * @param posicionSerie posición de la serie dentro de este ejercicio.
     * @return true si la serie se eliminó, false si la posición no existe o si era la única.
     */
    public boolean eliminarSerie(int posicionSerie) {
        if (posicionSerie < 0 || posicionSerie >= series.size() || !puedeEliminarSerie()) {
            return false;
        }

        series.remove(posicionSerie);
        return true;
    }
}
