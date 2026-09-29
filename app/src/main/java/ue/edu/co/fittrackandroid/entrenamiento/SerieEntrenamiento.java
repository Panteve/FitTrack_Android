package ue.edu.co.fittrackandroid.entrenamiento;

public class SerieEntrenamiento {

    private double peso = 0.0;
    private int repeticiones = 0;
    private int numeroSerie = 0;
    private boolean completada = false;

    public SerieEntrenamiento() {
    }

    public SerieEntrenamiento(int numeroSerie, double peso, int repeticiones) {
        this.numeroSerie = numeroSerie;
        this.peso = peso;
        this.repeticiones = repeticiones;
    }

    public SerieEntrenamiento(boolean completada, double peso, int repeticiones, int numeroSerie) {
        this.completada = completada;
        this.peso = peso;
        this.repeticiones = repeticiones;
        this.numeroSerie = numeroSerie;
    }

    public int getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(int numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(int repeticiones) {
        this.repeticiones = repeticiones;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    /**
     * Revisa que el peso sea un número igual o mayor que cero y que las repeticiones
     * sean un entero mayor que cero. El cero se acepta en el peso porque sirve
     * para los ejercicios de peso corporal.
     *
     * @return true si la serie tiene los dos valores usables.
     */
    public boolean tieneDatosValidos() {
        return esPesoValido() && sonRepeticionesValidas();
    }

    /** @return true si el peso escrito es un número igual o mayor que cero. */
    public boolean esPesoValido() {
        if(peso < 0) {
            return false;
        }
        return true;
    }

    /** @return true si las repeticiones escritas son un entero mayor que cero. */
    public boolean sonRepeticionesValidas() {
        if(repeticiones <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Calcula el volumen de la serie: peso multiplicado por repeticiones.
     * Una serie que todavía no está completada, o que tiene valores inválidos,
     * no aporta nada al volumen del entrenamiento.
     *
     * @return el volumen de la serie, o cero si todavía no cuenta.
     */
    public double calcularVolumen() {
        if (!completada || !tieneDatosValidos()) {
            return 0;
        }

        return getPeso() * getRepeticiones();
    }
}
