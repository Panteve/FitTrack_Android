package ue.edu.co.fittrackandroid.rutinas.modelo;

/**
 * Serie que el backend devuelve dentro de una rutina ya guardada.
 * Es la contraparte de {@link RutinaSerieCrearRequest}: aquí el peso y las repeticiones
 * llegan como números validados y además viene el identificador de la serie en el servidor.
 * Gson la llena sola al leer el JSON, por eso necesita constructor vacío y setters.
 */
public class RutinaSerieResponse {
    private Long id;
    private int numeroSerie;
    private int repeticionesObjetivo;
    private double pesoObjetivo;

    /** Crea la serie vacía para que Gson la llene sola al leer el JSON de la respuesta. */
    public RutinaSerieResponse() {
    }

    /** Obtiene el identificador que la serie tiene en el servidor. */
    public Long getId() {
        return id;
    }

    /** Cambia el identificador que la serie tiene en el servidor. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Obtiene las repeticiones objetivo que el backend guardó para la serie. */
    public int getRepeticionesObjetivo() {
        return repeticionesObjetivo;
    }

    /** Cambia las repeticiones objetivo de la serie. */
    public void setRepeticionesObjetivo(int repeticionesObjetivo) {
        this.repeticionesObjetivo = repeticionesObjetivo;
    }

    /** Obtiene la posición de la serie dentro del ejercicio de la rutina. */
    public int getNumeroSerie() {
        return numeroSerie;
    }

    /** Cambia la posición de la serie dentro del ejercicio de la rutina. */
    public void setNumeroSerie(int numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    /** Obtiene el peso objetivo que el backend guardó para la serie. */
    public double getPesoObjetivo() {
        return pesoObjetivo;
    }

    /** Cambia el peso objetivo de la serie. */
    public void setPesoObjetivo(double pesoObjetivo) {
        this.pesoObjetivo = pesoObjetivo;
    }
}
