package ue.edu.co.fittrackandroid.resumen;

/**
 * Modelo de un grupo muscular dentro de la distribución porcentual del resumen.
 *
 * <p>Cada ejercicio tiene un solo grupo muscular principal, así que todas sus series
 * completadas se cuentan para ese grupo. Es una clase de datos sencilla, sin jerarquías.
 */
public class GrupoMuscularResumen {

    private final String nombre;
    private final int seriesCompletadas;
    private final int porcentaje;

    /**
     * Crea un grupo muscular del resumen.
     *
     * @param nombre            nombre del grupo muscular.
     * @param seriesCompletadas series completadas que se le adjudicaron.
     * @param porcentaje        porcentaje sobre el total de series completadas.
     */
    public GrupoMuscularResumen(String nombre, int seriesCompletadas, int porcentaje) {
        this.nombre = nombre;
        this.seriesCompletadas = seriesCompletadas;
        this.porcentaje = porcentaje;
    }

    /**
     * @return el nombre del grupo muscular.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return las series completadas de este grupo.
     */
    public int getSeriesCompletadas() {
        return seriesCompletadas;
    }

    /**
     * @return el porcentaje de series completadas de este grupo, ya redondeado.
     */
    public int getPorcentaje() {
        return porcentaje;
    }
}
