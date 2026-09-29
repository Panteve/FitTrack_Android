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
     * @param numeroSerie posición de la serie dentro del ejercicio, empezando en 1.
     * @param repeticionesObjetivo repeticiones de la serie, cero si el usuario no escribió nada.
     * @param pesoObjetivo peso de la serie, cero si el usuario no escribió nada.
     */
    public RutinaSerieCrearRequest(int numeroSerie, int repeticionesObjetivo, double pesoObjetivo) {
        this.numeroSerie = numeroSerie;
        this.repeticionesObjetivo = repeticionesObjetivo;
        this.pesoObjetivo = pesoObjetivo;
    }

    public int getNumeroSerie() {
        return numeroSerie;
    }

    public int getRepeticionesObjetivo() {
        return repeticionesObjetivo;
    }

    public double getPesoObjetivo() {
        return pesoObjetivo;
    }
}
