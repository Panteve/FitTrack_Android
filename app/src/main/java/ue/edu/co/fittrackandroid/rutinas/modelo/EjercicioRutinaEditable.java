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

    /** Crea el ejercicio con su identificador, nombre y grupo muscular, y le agrega la primera serie vacía. */
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

    /** Obtiene el identificador del ejercicio del catálogo, necesario para guardar la rutina. */
    public Long getIdEjercicio() {
        return idEjercicio;
    }

    /** Obtiene el nombre del ejercicio que se muestra en la tarjeta. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene el grupo muscular del ejercicio. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    /** Obtiene la lista viva de series del ejercicio, para leerlas y modificarlas mientras se edita. */
    public List<SerieRutina> getSeries() {
        return series;
    }

    /**
     * Agrega una serie vacía al final del ejercicio y la devuelve para que la pantalla
     * la pueda rellenar.
     */
    public SerieRutina agregarSerie() {
        SerieRutina serieNueva = new SerieRutina();
        series.add(serieNueva);
        return serieNueva;
    }

    /**
     * Indica si el ejercicio tiene más de una serie. El backend exige que cada ejercicio
     * tenga al menos una, así que la última no se puede quitar.
     */
    public boolean puedeEliminarSerie() {
        return series.size() > 1;
    }

    /**
     * Quita una serie del ejercicio y devuelve si pudo hacerlo. Como el número de serie
     * sale de la posición, las que quedan se renumeran solas al redibujarse. Devuelve
     * false si la posición no existe o si se intentó quitar la única serie.
     */
    public boolean eliminarSerie(int posicionSerie) {
        if (posicionSerie < 0 || posicionSerie >= series.size() || !puedeEliminarSerie()) {
            return false;
        }

        series.remove(posicionSerie);
        return true;
    }
}
