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

    /** Crea el ejercicio con el identificador que dio el backend, su nombre y su grupo muscular. */
    public Ejercicio(Long id, String nombre, String grupoMuscular) {
        this.id = id;
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
    }

    /** Obtiene el identificador del ejercicio en el servidor, que es el que guarda la rutina. */
    public Long getId() {
        return id;
    }

    /** Obtiene el nombre visible del ejercicio. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene el grupo muscular al que pertenece el ejercicio. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }
}
