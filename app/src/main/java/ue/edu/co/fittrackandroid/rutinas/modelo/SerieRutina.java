package ue.edu.co.fittrackandroid.rutinas.modelo;

/**
 * Modelo de una serie que el usuario está escribiendo dentro de un ejercicio de la rutina.
 * El peso y las repeticiones se guardan como texto porque el usuario los digita en la pantalla
 * y todavía no se han validado.
 */
public class SerieRutina {

    private String pesoObjetivo = "";
    private String repeticiones = "";

    /** Obtiene el peso que el usuario digitó para la serie, todavía como texto sin validar. */
    public String getPesoObjetivo() {
        return pesoObjetivo;
    }

    /** Cambia el peso digitado por el usuario para la serie. */
    public void setPesoObjetivo(String pesoObjetivo) {
        this.pesoObjetivo = pesoObjetivo;
    }

    /** Obtiene las repeticiones que el usuario digitó para la serie, todavía como texto sin validar. */
    public String getRepeticiones() {
        return repeticiones;
    }

    /** Cambia las repeticiones digitadas por el usuario para la serie. */
    public void setRepeticiones(String repeticiones) {
        this.repeticiones = repeticiones;
    }
}
