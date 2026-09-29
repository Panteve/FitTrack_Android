package ue.edu.co.fittrackandroid.rutinas.modelo;

/**
 * Días de la semana que acepta el backend para una rutina.
 * El nombre de cada constante es exactamente el valor que espera la API, por eso
 * Gson envía el día escrito en mayúsculas y sin tilde (LUNES, MIERCOLES, SABADO...).
 */
public enum DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO
}
