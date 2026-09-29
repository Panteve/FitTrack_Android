package ue.edu.co.fittrackandroid.rutinas.modelo;

/**
 * Modelo de una serie que el usuario está escribiendo dentro de un ejercicio de la rutina.
 * El peso y las repeticiones se guardan como texto porque el usuario los digita en la pantalla
 * y todavía no se han validado.
 */
public class SerieRutina {

    private String pesoObjetivo = "";
    private String repeticiones = "";

    public String getPesoObjetivo() {
        return pesoObjetivo;
    }

    public void setPesoObjetivo(String pesoObjetivo) {
        this.pesoObjetivo = pesoObjetivo;
    }

    public String getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(String repeticiones) {
        this.repeticiones = repeticiones;
    }
}
