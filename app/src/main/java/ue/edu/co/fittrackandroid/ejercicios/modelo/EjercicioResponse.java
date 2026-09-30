package ue.edu.co.fittrackandroid.ejercicios.modelo;

/**
 * Respuesta de la API para un ejercicio creado por el usuario.
 * Solo guarda los datos necesarios para mostrarlo en la lista del perfil.
 */
public class EjercicioResponse {

    private Long id;
    private String nombre;
    private String grupoMuscular;

    /** Obtiene el identificador del ejercicio en el servidor. */
    public Long getId() {
        return id;
    }

    /** Obtiene el nombre del ejercicio tal como lo devolvió el backend. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene el grupo muscular al que pertenece el ejercicio. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }
}
