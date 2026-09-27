package ue.edu.co.fittrackandroid;

/**
 * Modelo de un entrenamiento reciente, tal como se muestra en la fila de la lista
 * de últimos entrenamientos de la pantalla de inicio.
 */
public class UltimoEntrenamiento {

    private final String nombre;
    private final String fecha;
    private final int duracionMinutos;

    /**
     * Crea un entrenamiento reciente.
     *
     * @param nombre           nombre de la rutina, por ejemplo "Pierna y abdomen".
     * @param fecha            fecha ya formateada, por ejemplo "24 de mayo".
     * @param duracionMinutos  duración del entrenamiento en minutos.
     */
    public UltimoEntrenamiento(String nombre, String fecha, int duracionMinutos) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
    }

    /**
     * @return el nombre de la rutina.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return la fecha ya formateada para mostrar.
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * @return la duración del entrenamiento en minutos.
     */
    public int getDuracionMinutos() {
        return duracionMinutos;
    }
}
