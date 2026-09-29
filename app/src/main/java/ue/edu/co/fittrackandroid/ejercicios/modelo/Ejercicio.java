package ue.edu.co.fittrackandroid.ejercicios.modelo;

/**
 * Modelo de un ejercicio mostrado en la lista.
 * Guarda el identificador que entrega el backend, porque la rutina debe guardar la relación
 * real con el ejercicio y no depender de su nombre visible.
 * Como dato secundario solo guarda el grupo muscular al que pertenece.
 */
public class Ejercicio {

    private final Long id;
    private final String nombre;
    private final String grupoMuscular;

    public Ejercicio(Long id, String nombre, String grupoMuscular) {
        this.id = id;
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }
}
