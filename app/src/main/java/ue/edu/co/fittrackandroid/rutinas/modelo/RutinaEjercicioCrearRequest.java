package ue.edu.co.fittrackandroid.rutinas.modelo;

import java.util.List;

/**
 * Ejercicio que se envía al backend dentro de una rutina nueva.
 * Se manda el identificador del ejercicio que ya existe, el orden que tendrá
 * en la rutina y la lista completa de sus series, aunque alguna se haya dejado vacía.
 */
public class RutinaEjercicioCrearRequest {

    private final Long ejercicioId;
    private final int orden;
    private final List<RutinaSerieCrearRequest> series;

    /**
     * Crea el ejercicio del envío con el identificador del ejercicio elegido en el
     * selector, la posición que tendrá dentro de la rutina empezando en 1 y sus
     * series con su número y sus valores objetivo.
     */
    public RutinaEjercicioCrearRequest(Long ejercicioId, int orden,
                                       List<RutinaSerieCrearRequest> series) {
        this.ejercicioId = ejercicioId;
        this.orden = orden;
        this.series = series;
    }

    /** Obtiene el identificador del ejercicio elegido en el selector. */
    public Long getEjercicioId() {
        return ejercicioId;
    }

    /** Obtiene la posición del ejercicio dentro de la rutina, empezando en 1. */
    public int getOrden() {
        return orden;
    }

    /** Obtiene las series del ejercicio con su número y sus valores objetivo. */
    public List<RutinaSerieCrearRequest> getSeries() {
        return series;
    }
}
