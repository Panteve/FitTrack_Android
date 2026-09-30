package ue.edu.co.fittrackandroid.ejercicios.modelo;

/**
 * Datos que se envían al backend para registrar un ejercicio creado por el usuario.
 * El backend solo necesita el nombre y el grupo muscular al que pertenece.
 */
public class EjercicioRequest {

    private String nombre;
    private String grupoMuscular;

    /** Crea el cuerpo del envío con el nombre del ejercicio y el grupo muscular elegido. */
    public EjercicioRequest(String nombre, String grupoMuscular) {
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
    }

    /** Obtiene el nombre del ejercicio que se está creando o modificando. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene el grupo muscular del ejercicio que se está creando o modificando. */
    public String getGrupoMuscular() {
        return grupoMuscular;
    }
}
