package ue.edu.co.fittrackandroid.rutinas;

/**
 * Modelo de un ejercicio dentro de un plan de entrenamiento.
 */
public class EjercicioRutina {

    private final String nombre;
    private final String series;

    public EjercicioRutina(String nombre, String series) {
        this.nombre = nombre;
        this.series = series;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSeries() {
        return series;
    }
}
