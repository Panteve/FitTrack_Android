package ue.edu.co.fittrackandroid.resumen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Copia de solo lectura de un entrenamiento terminado.
 *
 * <p>Se crea al confirmar "Terminar", antes de cerrar la sesión en curso, y guarda
 * únicamente datos finales: nada de lo que hay aquí depende de las vistas editables del
 * entrenamiento activo. El volumen y la cantidad de series completadas no se almacenan
 * porque se calculan siempre desde las series copiadas, así no pueden quedar
 * inconsistentes entre sí.
 *
 * <p>También se puede reconstruir a partir del detalle guardado en el backend. En ese
 * caso el backend solo conserva el día, no la hora exacta, y por eso se marca con
 * {@code tieneHoraExacta} para no mostrar una hora que nunca existió.
 */
public class ResumenEntrenamiento {

    private final String nombre;
    private final long fechaHoraInicio;
    private final long duracionSegundos;
    private final int seriesTotales;
    private final boolean tieneHoraExacta;
    private final List<EjercicioResumen> ejercicios;

    /**
     * Crea el resumen de un entrenamiento terminado.
     *
     * @param nombre           nombre de la rutina, o vacío si no se conoce.
     * @param fechaHoraInicio  momento real en que empezó, con {@link System#currentTimeMillis()}.
     * @param duracionSegundos duración final de la sesión, en segundos.
     * @param seriesTotales    series que existían al terminar, completas o pendientes.
     * @param tieneHoraExacta  true si la fechaHoraInicio es el momento real de la sesión;
     *                         false si solo se conoce el día guardado por el backend.
     * @param ejercicios       ejercicios realizados con sus series completadas.
     */
    public ResumenEntrenamiento(String nombre, long fechaHoraInicio, long duracionSegundos,
                                int seriesTotales, boolean tieneHoraExacta,
                                List<EjercicioResumen> ejercicios) {
        this.nombre = nombre;
        this.fechaHoraInicio = fechaHoraInicio;
        this.duracionSegundos = duracionSegundos;
        this.seriesTotales = seriesTotales;
        this.tieneHoraExacta = tieneHoraExacta;
        // Se copia la lista para que el resumen no dependa de la sesión que lo creó.
        this.ejercicios = new ArrayList<>(ejercicios);
    }

    /**
     * @return el nombre de la rutina entrenada.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return el momento real de inicio, en milisegundos de reloj, para mostrar día y hora.
     */
    public long getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    /**
     * @return la duración final de la sesión, en segundos.
     */
    public long getDuracionSegundos() {
        return duracionSegundos;
    }

    /**
     * @return las series que existían al terminar, completas o pendientes.
     */
    public int getSeriesTotales() {
        return seriesTotales;
    }

    /**
     * @return true si se conoce la hora real de la sesión y se puede mostrar junto a la
     *         fecha; false si el resumen fue reconstruido desde el historial guardado,
     *         donde el backend solo conserva el día.
     */
    public boolean tieneHoraExacta() {
        return tieneHoraExacta;
    }

    /**
     * @return los ejercicios realizados, con sus series completadas.
     */
    public List<EjercicioResumen> getEjercicios() {
        return ejercicios;
    }

    /**
     * Suma el volumen de todas las series completadas del entrenamiento.
     * Es la misma fórmula del entrenamiento en curso: peso multiplicado por repeticiones.
     *
     * @return el volumen total, en kilogramos.
     */
    public double calcularVolumenTotal() {
        double volumenTotal = 0;

        for (EjercicioResumen ejercicio : ejercicios) {
            for (SerieResumen serie : ejercicio.getSeries()) {
                volumenTotal += serie.calcularVolumen();
            }
        }

        return volumenTotal;
    }

    /**
     * @return cuántas series se completaron durante el entrenamiento.
     */
    public int contarSeriesCompletadas() {
        int seriesCompletadas = 0;

        for (EjercicioResumen ejercicio : ejercicios) {
            seriesCompletadas += ejercicio.getCantidadSeries();
        }

        return seriesCompletadas;
    }

    /**
     * Reparte las series completadas entre los grupos musculares y arma la distribución
     * porcentual del resumen.
     *
     * <p>El porcentaje se calcula sobre las series completadas, porque son las que
     * realmente se trabajaron: porcentaje del grupo = sus series sobre el total, por 100.
     *
     * @return los grupos del porcentaje mayor al menor, o una lista vacía si no hay
     *         ninguna serie completada.
     */
    public List<GrupoMuscularResumen> calcularGruposMusculares() {
        List<GrupoMuscularResumen> grupos = new ArrayList<>();
        int totalSeriesCompletadas = contarSeriesCompletadas();

        // Sin series no hay porcentajes: se evita dividir entre cero.
        if (totalSeriesCompletadas == 0) {
            return grupos;
        }

        List<String> nombresGrupos = new ArrayList<>();
        List<Integer> seriesPorGrupo = new ArrayList<>();

        for (EjercicioResumen ejercicio : ejercicios) {
            String nombreGrupo = ejercicio.getGrupoMuscular();
            int posicionGrupo = nombresGrupos.indexOf(nombreGrupo);

            if (posicionGrupo < 0) {
                nombresGrupos.add(nombreGrupo);
                seriesPorGrupo.add(ejercicio.getCantidadSeries());
            } else {
                int seriesAcumuladas = seriesPorGrupo.get(posicionGrupo)
                        + ejercicio.getCantidadSeries();
                seriesPorGrupo.set(posicionGrupo, seriesAcumuladas);
            }
        }

        for (int posicion = 0; posicion < nombresGrupos.size(); posicion++) {
            int seriesDelGrupo = seriesPorGrupo.get(posicion);
            int porcentaje = (int) Math.round(seriesDelGrupo * 100.0 / totalSeriesCompletadas);
            grupos.add(new GrupoMuscularResumen(nombresGrupos.get(posicion), seriesDelGrupo, porcentaje));
        }

        Collections.sort(grupos, new Comparator<GrupoMuscularResumen>() {
            @Override
            public int compare(GrupoMuscularResumen grupoPrimero, GrupoMuscularResumen grupoSegundo) {
                return grupoSegundo.getPorcentaje() - grupoPrimero.getPorcentaje();
            }
        });

        return grupos;
    }
}
