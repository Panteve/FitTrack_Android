package ue.edu.co.fittrackandroid.ejercicios;

/**
 * Modelo de un ejercicio mostrado en la lista.
 */
public class Ejercicio {

    private final String nombre;
    private final String subtitulo;
    private final String grupoMuscular;

    public Ejercicio(String nombre, String subtitulo, String grupoMuscular) {
        this.nombre = nombre;
        this.subtitulo = subtitulo;
        this.grupoMuscular = grupoMuscular;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }
}
