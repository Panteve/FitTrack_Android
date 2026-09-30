package ue.edu.co.fittrackandroid.rutinas.modelo;

/**
 * Serie que se envía al backend al crear una rutina.
 * El número de serie es consecutivo y empieza en uno; el peso y las repeticiones
 * llegan como cero cuando el usuario dejó el campo vacío, porque el backend
 * no admite nulos en esos campos.
 */
public class RutinaSerieCrearRequest {

    private final int numeroSerie;
    private final int repeticionesObjetivo;
    private final double pesoObjetivo;

    /**
     * Crea la serie con su posición dentro del ejercicio empezando en 1 y sus valores
     * objetivo, que llegan en cero cuando el usuario dejó el campo vacío.
     */
    public RutinaSerieCrearRequest(int numeroSerie, int repeticionesObjetivo, double pesoObjetivo) {
        this.numeroSerie = numeroSerie;
        this.repeticionesObjetivo = repeticionesObjetivo;
        this.pesoObjetivo = pesoObjetivo;
    }

    /** Obtiene la posición de la serie dentro del ejercicio, empezando en 1. */
    public int getNumeroSerie() {
        return numeroSerie;
    }

    /** Obtiene las repeticiones objetivo de la serie, en cero si el usuario no escribió nada. */
    public int getRepeticionesObjetivo() {
        return repeticionesObjetivo;
    }

    /** Obtiene el peso objetivo de la serie, en cero si el usuario no escribió nada. */
    public double getPesoObjetivo() {
        return pesoObjetivo;
    }
}
