package ue.edu.co.fittrackandroid.hoy.modelo;

import ue.edu.co.fittrackandroid.resumen.modelo.ResumenEntrenamiento;

/**
 * Modelo de un entrenamiento reciente, tal como se muestra en la fila de la lista
 * de últimos entrenamientos de la pantalla de inicio.
 */
public class UltimoEntrenamiento {

    private final String nombre;
    private final Long id;
    private final String fecha;
    private final int duracionMinutos;

    private final ResumenEntrenamiento resumen;

    /** Crea el entrenamiento reciente con su identificador, nombre, fecha, duración y el resumen de lo que se entrenó. */
    public UltimoEntrenamiento(Long id, String nombre, String fecha, int duracionMinutos,
                               ResumenEntrenamiento resumen) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
        this.resumen = resumen;
    }

    /** Obtiene el nombre de la rutina con la que se hizo el entrenamiento. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene la fecha en la que se realizó el entrenamiento, ya formateada por el backend. */
    public String getFecha() {
        return fecha;
    }

    /** Obtiene el identificador del entrenamiento, necesario para abrir su resumen. */
    public Long getId() {
        return id;
    }

    /** Obtiene la duración del entrenamiento en minutos. */
    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    /** Obtiene el resumen con los ejercicios y series registrados en el entrenamiento. */
    public ResumenEntrenamiento getResumen() {
        return resumen;
    }
}
