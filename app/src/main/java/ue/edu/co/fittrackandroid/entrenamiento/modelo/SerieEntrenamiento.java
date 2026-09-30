package ue.edu.co.fittrackandroid.entrenamiento.modelo;

public class SerieEntrenamiento {

    /**
     * Identificador de la fila que guarda esta serie en Room; cero mientras todavía no se ha
     * escrito en la base de datos.
     */
    private long idBorrador = 0;

    private double peso = 0.0;
    private int repeticiones = 0;
    private int numeroSerie = 0;
    private boolean completada = false;

    /** Crea una serie vacía, la que usa el usuario cuando agrega una nueva durante la sesión. */
    public SerieEntrenamiento() {
    }

    /** Crea la serie con el número que le toca dentro del ejercicio y los valores iniciales. */
    public SerieEntrenamiento(int numeroSerie, double peso, int repeticiones) {
        this.numeroSerie = numeroSerie;
        this.peso = peso;
        this.repeticiones = repeticiones;
    }

    /** Crea la serie completa, como se recupera de la base de datos local o del backend. */
    public SerieEntrenamiento(boolean completada, double peso, int repeticiones, int numeroSerie) {
        this.completada = completada;
        this.peso = peso;
        this.repeticiones = repeticiones;
        this.numeroSerie = numeroSerie;
    }

    /** Devuelve el número que ocupa la serie dentro de su ejercicio. */
    public int getNumeroSerie() {
        return numeroSerie;
    }

    /** Guarda el número de la serie, que se vuelve a escribir cada vez que el ejercicio se renumera. */
    public void setNumeroSerie(int numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    /** Devuelve cuántas repeticiones registró el usuario en la serie. */
    public int getRepeticiones() {
        return repeticiones;
    }

    /** Guarda las repeticiones escritas por el usuario para la serie. */
    public void setRepeticiones(int repeticiones) {
        this.repeticiones = repeticiones;
    }

    /** Devuelve el peso que el usuario registró en la serie. */
    public double getPeso() {
        return peso;
    }

    /** Guarda el peso escrito por el usuario para la serie. */
    public void setPeso(double peso) {
        this.peso = peso;
    }

    /** Indica si el usuario ya marcó la serie como completada. */
    public boolean isCompletada() {
        return completada;
    }

    /** Marca o desmarca la serie como completada según lo que indique el usuario. */
    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    /** Devuelve el identificador de la fila que guarda la serie en la base de datos local, o cero si todavía no se ha guardado. */
    public long getIdBorrador() {
        return idBorrador;
    }

    /** Guarda el identificador que Room asignó a esta serie al escribirla en la base de datos local. */
    public void setIdBorrador(long idBorrador) {
        this.idBorrador = idBorrador;
    }

    /**
     * Revisa que el peso sea un número igual o mayor que cero y que las repeticiones
     * sean un entero mayor que cero. El cero se acepta en el peso porque sirve
     * para los ejercicios de peso corporal.
     */
    public boolean tieneDatosValidos() {
        return esPesoValido() && sonRepeticionesValidas();
    }

    /** Indica si el peso escrito es un número igual o mayor que cero. */
    public boolean esPesoValido() {
        if(peso < 0) {
            return false;
        }
        return true;
    }

    /** Indica si las repeticiones escritas son un entero mayor que cero. */
    public boolean sonRepeticionesValidas() {
        if(repeticiones <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Calcula el volumen de la serie: peso multiplicado por repeticiones.
     * Una serie que todavía no está completada, o que tiene valores inválidos,
     * no aporta nada al volumen del entrenamiento y se cuenta como cero.
     */
    public double calcularVolumen() {
        if (!completada || !tieneDatosValidos()) {
            return 0;
        }

        return getPeso() * getRepeticiones();
    }
}
