package ue.edu.co.fittrackandroid.hoy;

import ue.edu.co.fittrackandroid.resumen.ResumenEntrenamiento;

/**
 * Modelo de un entrenamiento reciente, tal como se muestra en la fila de la lista
 * de últimos entrenamientos de la pantalla de inicio.
 */
public class UltimoEntrenamiento {

    private final String nombre;
    private final Integer id;
    private final String fecha;
    private final int duracionMinutos;

    private final ResumenEntrenamiento resumen;

    public UltimoEntrenamiento(int id, String nombre, String fecha, int duracionMinutos,
                               ResumenEntrenamiento resumen) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
        this.resumen = resumen;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public Integer getId() {
        return id;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public ResumenEntrenamiento getResumen() {
        return resumen;
    }
}
