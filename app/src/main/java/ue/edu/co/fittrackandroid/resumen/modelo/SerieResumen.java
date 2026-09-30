package ue.edu.co.fittrackandroid.resumen.modelo;

/**
 * Modelo de una serie ya realizada dentro de un entrenamiento terminado.
 *
 * <p>A diferencia de {@link SerieEntrenamiento}, aquí el peso y las repeticiones ya son
 * números validados y todas las instancias representan series completadas, por eso no
 * necesitan un estado de "completada". Tampoco guardan el número visible de la serie:
 * ese número sale de la posición de la serie en la lista más uno.
 */
public class SerieResumen {

    private final double peso;
    private final int repeticiones;

    /** Crea una serie realizada con el peso levantado y las repeticiones completadas. */
    public SerieResumen(double peso, int repeticiones) {
        this.peso = peso;
        this.repeticiones = repeticiones;
    }

    /** Devuelve el peso realizado, en kilogramos. */
    public double getPeso() {
        return peso;
    }

    /** Devuelve las repeticiones realizadas. */
    public int getRepeticiones() {
        return repeticiones;
    }

    /**
     * Calcula el volumen de la serie: peso multiplicado por repeticiones.
     * Es la misma fórmula que usa el resumen del entrenamiento en curso.
     */
    public double calcularVolumen() {
        return peso * repeticiones;
    }
}
