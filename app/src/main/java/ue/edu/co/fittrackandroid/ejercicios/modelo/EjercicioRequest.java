package ue.edu.co.fittrackandroid.ejercicios.modelo;

/**
 * Datos que se envían al backend para registrar un ejercicio creado por el usuario.
 * El backend solo necesita el nombre y el grupo muscular al que pertenece.
 */
public class EjercicioRequest {

    private String nombre;
    private String grupoMuscular;

    public EjercicioRequest(String nombre, String grupoMuscular) {
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
