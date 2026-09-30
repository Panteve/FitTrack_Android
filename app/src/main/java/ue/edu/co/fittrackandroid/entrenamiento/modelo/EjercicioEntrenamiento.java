package ue.edu.co.fittrackandroid.entrenamiento.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de un ejercicio dentro de un entrenamiento en curso.
 * Conserva su identificador, nombre y las series editables de la sesión.
 */
public class EjercicioEntrenamiento {

    /**
     * Identificador de la fila que guarda este ejercicio en Room; cero mientras todavía no se
     * ha escrito en la base de datos.
     */
    private long idBorrador = 0;

    private final String nombre;
    private final String grupoMuscular;
    private final Long rutinaEjercicioId;
    private final List<SerieEntrenamiento> series = new ArrayList<>();

    /**
     * Crea un ejercicio con las series recibidas desde la rutina.
     *
     * @param rutinaEjercicioId identificador del bloque dentro de la rutina
     * @param nombre nombre visible del ejercicio
     * @param grupoMuscular grupo muscular principal del ejercicio
     * @param series series iniciales del ejercicio; puede ser {@code null}
     */
    public EjercicioEntrenamiento(Long rutinaEjercicioId, String nombre, String grupoMuscular,
                                 List<SerieEntrenamiento> series) {
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
        this.rutinaEjercicioId = rutinaEjercicioId;
        if (series != null) {
            this.series.addAll(series);
        }
    }

    /** @return el nombre visible del ejercicio. */
    public String getNombre() {
        return nombre;
    }

    /** @return el grupo muscular principal del ejercicio. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    /** @return el identificador del bloque de ejercicio dentro de la rutina. */
    public Long getRutinaEjercicioId() {
        return rutinaEjercicioId;
    }

    /**
     * @return el identificador de la fila que guarda el ejercicio en la base de datos local,
     *         o cero si todavía no se ha guardado.
     */
    public long getIdBorrador() {
        return idBorrador;
    }

    /** @param idBorrador identificador que Room asignó a este ejercicio. */
    public void setIdBorrador(long idBorrador) {
        this.idBorrador = idBorrador;
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

    /**
     * Indica si el ejercicio tiene más de una serie. Un ejercicio siempre necesita al menos
     * una, así que la última no se puede quitar.
     *
     * @return true si se puede quitar alguna serie.
     */
    public boolean puedeEliminarSerie() {
        return series.size() > 1;
    }

    /**
     * Quita una serie del ejercicio y renumera las que quedan.
     *
     * <p>Aquí el renumerado es obligatorio: a diferencia de la creación de rutinas, el
     * entrenamiento guarda el número de cada serie y lo manda al backend, así que no basta
     * con mostrar la posición.
     *
     * @param posicionSerie posición de la serie dentro de este ejercicio.
     * @return la serie quitada, o null si la posición no existe o si era la única.
     */
    public SerieEntrenamiento eliminarSerie(int posicionSerie) {
        if (posicionSerie < 0 || posicionSerie >= series.size() || !puedeEliminarSerie()) {
            return null;
        }

        SerieEntrenamiento serieEliminada = series.remove(posicionSerie);
        renumerarSeries();

        return serieEliminada;
    }

    /** Vuelve a escribir el número de cada serie según el orden en que quedaron. */
    private void renumerarSeries() {
        for (int posicion = 0; posicion < series.size(); posicion++) {
            series.get(posicion).setNumeroSerie(posicion + 1);
        }
    }
}
