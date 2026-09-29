package ue.edu.co.fittrackandroid.hoy;

public class ProximaRutina {
    private final String nombre;
    private final Integer numeroDeEjercicios;
    private final Integer id;


    public ProximaRutina(Integer id, Integer numeroDeEjercicios, String nombre) {
        this.id = id;
        this.numeroDeEjercicios = numeroDeEjercicios;
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNumeroDeEjercicios() {
        return numeroDeEjercicios;
    }

}
