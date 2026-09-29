package ue.edu.co.fittrackandroid.entrenamiento.modelo;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Estado de la sesión de entrenamiento que está en curso.
 *
 * <p>MainActivity mantiene una única referencia a este objeto mientras la aplicación siga viva,
 * de esa forma la sesión sobrevive a que el fragment se destruya al minimizar el entrenamiento.
 *
 * <p>Los tiempos se guardan como instantes de {@link SystemClock#elapsedRealtime()} y nunca como
 * contadores que se van sumando, así el tiempo no se altera si cambia la hora del dispositivo.
 * Además se guarda el momento real de inicio, con {@link System#currentTimeMillis()}, que es
 * el que se muestra como día y hora en el resumen del entrenamiento terminado.
 *
 * <p>La sesión también se guarda en la base de datos local (Room) mientras está en curso, para
 * que no se pierda si Android cierra el proceso. Ese borrador se identifica con
 * {@link #getIdBorrador()}.
 */
public class EntrenamientoEnCurso {

    /**
     * Identificador de la fila que guarda esta sesión en Room; cero mientras todavía no se ha
     * escrito en la base de datos.
     */
    private long idBorrador = 0;

    /** Instante en que empezó el entrenamiento, en milisegundos de reloj del sistema. */
    private final long instanteInicio;

    /** Momento real en que empezó el entrenamiento, para mostrar el día y la hora. */
    private final long fechaHoraInicio;

    /** Nombre de la rutina que se está entrenando; puede estar vacío si no se conoce. */
    private final String nombreRutina;

    private String notas;

    /** Identificador de la rutina usada para iniciar la sesión. */
    private final Long idRutina;

    /** Ejercicios de la sesión, con sus series y el estado de cada una. */
    private final List<EjercicioEntrenamiento> ejercicios = new ArrayList<>();

    /** Instante en que termina el descanso actual, o cero si no hay descanso activo. */
    private long instanteFinDescanso = 0;

    /** Indica si la sesión sigue en curso. */
    private boolean activa = true;

    /**
     * @param idRutina identificador de la rutina que originó el entrenamiento
     * @param nombreRutina nombre de la rutina que se está entrenando, o vacío si no se conoce
     * @param instanteInicio momento en que comenzó el entrenamiento, con
     *                       {@link SystemClock#elapsedRealtime()}
     * @param fechaHoraInicio momento real en que comenzó el entrenamiento, con
     *                        {@link System#currentTimeMillis()}
     */
    public EntrenamientoEnCurso(Long idRutina, String nombreRutina, long instanteInicio,
                                long fechaHoraInicio) {
        this.idRutina = idRutina;
        this.nombreRutina = nombreRutina;
        this.instanteInicio = instanteInicio;
        this.fechaHoraInicio = fechaHoraInicio;
    }

    /** @return el identificador de la rutina que originó el entrenamiento. */
    public Long getIdRutina() {
        return idRutina;
    }

    /**
     * @return el identificador de la fila que guarda la sesión en la base de datos local,
     *         o cero si todavía no se ha guardado.
     */
    public long getIdBorrador() {
        return idBorrador;
    }

    /** @param idBorrador identificador que Room asignó al borrador de esta sesión. */
    public void setIdBorrador(long idBorrador) {
        this.idBorrador = idBorrador;
    }

    /** @return las notas opcionales registradas para el entrenamiento. */
    public String getNotas() {
        return notas;
    }

    /** @param notas notas opcionales del entrenamiento. */
    public void setNotas(String notas) {
        this.notas = notas;
    }

    public long getInstanteInicio() {
        return instanteInicio;
    }

    /**
     * @return el momento real de inicio, en milisegundos de reloj, para mostrar día y hora.
     */
    public long getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    /**
     * @return el nombre de la rutina entrenada, o una cadena vacía si no se conoce.
     */
    public String getNombreRutina() {
        return nombreRutina;
    }

    public List<EjercicioEntrenamiento> getEjercicios() {
        return ejercicios;
    }

    /** Agrega un ejercicio con su primera serie al final de la sesión. */
    public void agregarEjercicio(EjercicioEntrenamiento ejercicio) {
        ejercicios.add(ejercicio);
    }

    public boolean isActiva() {
        return activa;
    }

    /** Marca la sesión como terminada o descartada. */
    public void finalizar() {
        activa = false;
        instanteFinDescanso = 0;
    }

    public long getInstanteFinDescanso() {
        return instanteFinDescanso;
    }

    /** @param instanteFinDescanso instante en que termina el descanso actual. */
    public void setInstanteFinDescanso(long instanteFinDescanso) {
        this.instanteFinDescanso = instanteFinDescanso;
    }

    /** Cancela el descanso actual sin modificar el resto de la sesión. */
    public void detenerDescanso() {
        instanteFinDescanso = 0;
    }

    /** @return true si hay un descanso activo que todavía no terminó. */
    public boolean hayDescansoActivo() {
        return instanteFinDescanso > 0 && getSegundosDescansoRestantes() > 0;
    }

    /**
     * Calcula el tiempo ya transcurrido del entrenamiento a partir del instante guardado.
     * No hay un segundo cronómetro: siempre se recalcula desde el inicio real.
     *
     * @return segundos transcurridos desde que começou la sesión.
     */
    public long getSegundosTranscurridos() {
        long segundos = (SystemClock.elapsedRealtime() - instanteInicio) / 1000;
        return Math.max(0, segundos);
    }

    /**
     * @return segundos que le quedan al descanso actual, o cero si no hay descanso.
     */
    public int getSegundosDescansoRestantes() {
        if (instanteFinDescanso == 0) {
            return 0;
        }

        long milisegundosRestantes = instanteFinDescanso - SystemClock.elapsedRealtime();
        if (milisegundosRestantes <= 0) {
            return 0;
        }
        return (int) (milisegundosRestantes / 1000);
    }

    /** @return el tiempo transcurrido en el formato HH:MM:SS. */
    public String getTiempoTranscurrido() {
        return formatearTiempo(getSegundosTranscurridos());
    }

    /**
     * Convierte una cantidad de segundos al formato HH:MM:SS que usan el resumen,
     * el panel de descanso y la barra del entrenamiento minimizado.
     *
     * @param segundosTotales cantidad de segundos a mostrar.
     * @return el tiempo formateado, por ejemplo 00:18:34.
     */
    public static String formatearTiempo(long segundosTotales) {
        long segundos = Math.max(0, segundosTotales);
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long segundosRestantes = segundos % 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                horas, minutos, segundosRestantes);
    }
}
