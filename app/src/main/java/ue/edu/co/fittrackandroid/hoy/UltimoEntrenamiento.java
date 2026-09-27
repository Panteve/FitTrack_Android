package ue.edu.co.fittrackandroid.hoy;

import ue.edu.co.fittrackandroid.resumen.ResumenEntrenamiento;

/**
 * Modelo de un entrenamiento reciente, tal como se muestra en la fila de la lista
 * de últimos entrenamientos de la pantalla de inicio.
 */
public class UltimoEntrenamiento {

    private final String nombre;
    private final String fecha;
    private final int duracionMinutos;

    /**
     * Resumen completo del entrenamiento, para poder abrir la pantalla de resultado.
     *
     * <p>Es una referencia al mismo resumen que se creó al terminar la sesión, no una copia
     * de sus ejercicios y series, para no duplicar esos datos.
     */
    private final ResumenEntrenamiento resumen;

    /**
     * Crea un entrenamiento reciente.
     *
     * @param nombre           nombre de la rutina, por ejemplo "Pierna y abdomen".
     * @param fecha            fecha ya formateada, por ejemplo "24 de mayo".
     * @param duracionMinutos  duración del entrenamiento en minutos.
     * @param resumen          resumen completo del entrenamiento, con sus ejercicios y series.
     */
    public UltimoEntrenamiento(String nombre, String fecha, int duracionMinutos,
                               ResumenEntrenamiento resumen) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
        this.resumen = resumen;
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

    /**
     * @return el resumen completo del entrenamiento, o null si todavía no se guardó.
     *         TODO: Cuando exista el historial guardado, este campo se reemplaza por el
     *         identificador del entrenamiento y el resumen se carga desde el almacenamiento.
     */
    public ResumenEntrenamiento getResumen() {
        return resumen;
    }
}
