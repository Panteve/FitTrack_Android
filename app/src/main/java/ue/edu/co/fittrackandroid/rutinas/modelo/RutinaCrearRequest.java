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
     * Crea el cuerpo del envío con el nombre obligatorio de la rutina, la descripción
     * opcional, el día de entrenamiento elegido en el desplegable y los ejercicios con
     * sus series, que deben ser al menos uno.
     */
    public RutinaCrearRequest(String nombre, String descripcion, DiaSemana diaSemana,
                              List<RutinaEjercicioCrearRequest> ejercicios) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.diaSemana = diaSemana;
        this.ejercicios = ejercicios;
    }

    /** Obtiene el nombre obligatorio de la rutina. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene la descripción opcional de la rutina, que puede venir en null. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Obtiene el día de entrenamiento escolhido en el desplegable. */
    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    /** Obtiene los ejercicios de la rutina con sus series, listos para enviarse. */
    public List<RutinaEjercicioCrearRequest> getEjercicios() {
        return ejercicios;
    }
}
