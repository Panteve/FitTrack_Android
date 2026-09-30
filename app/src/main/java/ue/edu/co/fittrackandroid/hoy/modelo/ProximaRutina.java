package ue.edu.co.fittrackandroid.hoy.modelo;

public class ProximaRutina {
    private final String nombre;
    private final Integer numeroDeEjercicios;
    private final Long id;


    /** Crea la rutina con su identificador, la cantidad de ejercicios que tiene y el nombre que se muestra. */
    public ProximaRutina(Long id, Integer numeroDeEjercicios, String nombre) {
        this.id = id;
        this.numeroDeEjercicios = numeroDeEjercicios;
        this.nombre = nombre;
    }

    /** Obtiene el nombre de la rutina que se sugiere para el día de hoy. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene el identificador de la rutina, necesario para abrir su detalle. */
    public Long getId() {
        return id;
    }

    /** Obtiene la cantidad de ejercicios que tiene la rutina propuesta. */
    public Integer getNumeroDeEjercicios() {
        return numeroDeEjercicios;
    }

}
