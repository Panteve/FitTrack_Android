package ue.edu.co.fittrackandroid.ejercicios.modelo;

import java.util.List;

/**
 * Respuesta de la API con los ejercicios disponibles para el usuario.
 * El backend los separa en dos listas: los que trae el sistema y los que creó el usuario.
 * Se mantienen separadas para no mezclarlas al mostrarlas.
 */
public class EjerciciosDisponiblesResponse {

    private List<EjercicioResponse> ejerciciosSistema;
    private List<EjercicioResponse> misEjercicios;

    public List<EjercicioResponse> getEjerciciosSistema() {
        return ejerciciosSistema;
    }

    public List<EjercicioResponse> getMisEjercicios() {
        return misEjercicios;
    }
}
