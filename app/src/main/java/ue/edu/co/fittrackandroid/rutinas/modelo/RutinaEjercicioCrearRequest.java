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
     * @param ejercicioId identificador del ejercicio elegido en el selector.
     * @param orden posición del ejercicio dentro de la rutina, empezando en 1.
     * @param series series del ejercicio, con su número y sus valores objetivo.
     */
    public RutinaEjercicioCrearRequest(Long ejercicioId, int orden,
                                       List<RutinaSerieCrearRequest> series) {
        this.ejercicioId = ejercicioId;
        this.orden = orden;
        this.series = series;
    }

    public Long getEjercicioId() {
        return ejercicioId;
    }

    public int getOrden() {
        return orden;
    }

    public List<RutinaSerieCrearRequest> getSeries() {
        return series;
    }
}
