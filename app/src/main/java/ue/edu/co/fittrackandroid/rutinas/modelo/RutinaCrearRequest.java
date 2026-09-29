package ue.edu.co.fittrackandroid.rutinas.modelo;

import java.util.List;

/**
 * Datos que se envían al backend para registrar una rutina nueva con
 * {@code POST /rutinas}.
 * La descripción es opcional y por ahora se envía como null, porque la pantalla
 * todavía no la pide. El día va como enum para que Gson lo escriba en mayúsculas
 * y sin tilde, tal como lo espera la API.
 */
public class RutinaCrearRequest {

    private final String nombre;
    private final String descripcion;
    private final DiaSemana diaSemana;
    private final List<RutinaEjercicioCrearRequest> ejercicios;

    /**
     * @param nombre nombre de la rutina, obligatorio.
     * @param descripcion descripción opcional, puede ir en null.
     * @param diaSemana día de entrenamiento elegido en el desplegable.
     * @param ejercicios ejercicios de la rutina con sus series, al menos uno.
     */
    public RutinaCrearRequest(String nombre, String descripcion, DiaSemana diaSemana,
                              List<RutinaEjercicioCrearRequest> ejercicios) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.diaSemana = diaSemana;
        this.ejercicios = ejercicios;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public List<RutinaEjercicioCrearRequest> getEjercicios() {
        return ejercicios;
    }
}
