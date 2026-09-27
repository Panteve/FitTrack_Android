package ue.edu.co.fittrackandroid.ejercicios;

/**
 * Modelo de un ejercicio mostrado en la lista.
 * Como dato secundario solo guarda el grupo muscular al que pertenece.
 */
public class Ejercicio {

    // TODO: Agregar el identificador persistente para seleccionar, editar y eliminar el
    // ejercicio correcto sin depender de su nombre visible.
    private final String nombre;
    private final String grupoMuscular;

    // TODO: Incorporar el tipo de equipo, peso y repeticiones sugeridas, y la referencia de
    // multimedia que se capturan en CrearEjercicioFragment.

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
