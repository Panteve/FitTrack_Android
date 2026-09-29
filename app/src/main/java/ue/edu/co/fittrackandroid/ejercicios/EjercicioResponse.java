package ue.edu.co.fittrackandroid.ejercicios;

/**
 * Respuesta de la API para un ejercicio creado por el usuario.
 * Solo guarda los datos necesarios para mostrarlo en la lista del perfil.
 */
public class EjercicioResponse {

    private Long id;
    private String nombre;
    private String grupoMuscular;

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
