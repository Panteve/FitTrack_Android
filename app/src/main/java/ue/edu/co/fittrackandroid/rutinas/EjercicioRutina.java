package ue.edu.co.fittrackandroid.rutinas;

/**
 * Modelo de un ejercicio dentro de un plan de entrenamiento.
 */
public class EjercicioRutina {

    // TODO: Agregar el identificador persistente del ejercicio para poder consultar sus datos.
    private final String nombre;

    // TODO: Reemplazar este texto de presentación por una lista estructurada de series con
    // peso y repeticiones, de modo que el entrenamiento pueda recuperar los objetivos reales.
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
