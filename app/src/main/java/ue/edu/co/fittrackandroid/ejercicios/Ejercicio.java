package ue.edu.co.fittrackandroid.ejercicios;

/**
 * Modelo de un ejercicio mostrado en la lista.
 * Como dato secundario solo guarda el grupo muscular al que pertenece.
 */
public class Ejercicio {

    private final String nombre;
    private final String grupoMuscular;

    public Ejercicio(String nombre, String grupoMuscular) {
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }
}
