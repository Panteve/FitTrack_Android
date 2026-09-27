package ue.edu.co.fittrackandroid.entrenamiento;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Estado de la sesión de entrenamiento que está en curso.
 *
 * <p>MainActivity mantiene una única referencia a este objeto mientras la aplicación siga viva,
 * de esa forma la sesión sobrevive a que el fragment se destruya al minimizar el entrenamiento
 * o al abrir el selector de ejercicios.
 *
 * <p>Los tiempos se guardan como instantes de {@link SystemClock#elapsedRealtime()} y nunca como
 * contadores que se van sumando, así el tiempo no se altera si cambia la hora del dispositivo.
 * Además se guarda el momento real de inicio, con {@link System#currentTimeMillis()}, que es
 * el que se muestra como día y hora en el resumen del entrenamiento terminado.
 * Esta primera versión no guarda nada en disco: si Android cierra el proceso, la sesión se pierde.
 */
public class EntrenamientoEnCurso {

    /** Instante en que empezó el entrenamiento, en milisegundos de reloj del sistema. */
    private final long instanteInicio;

    /** Momento real en que empezó el entrenamiento, para mostrar el día y la hora. */
    private final long fechaHoraInicio;

    /** Nombre de la rutina que se está entrenando; puede estar vacío si no se conoce. */
    private final String nombre;

    /** Ejercicios de la sesión, con sus series y el estado de cada una. */
    private final List<EjercicioEntrenamiento> ejercicios = new ArrayList<>();

    /** Instante en que termina el descanso actual, o cero si no hay descanso activo. */
    private long instanteFinDescanso = 0;

    /** Indica si la sesión sigue en curso. */
    private boolean activa = true;

    /**
     * @param nombre         nombre de la rutina que se está entrenando, o vacío si no se conoce.
     * @param instanteInicio momento en que comenzó el entrenamiento, con
     *                       {@link SystemClock#elapsedRealtime()}.
     * @param fechaHoraInicio momento real en que comenzó el entrenamiento, con
     *                        {@link System#currentTimeMillis()}.
     */
    public EntrenamientoEnCurso(String nombre, long instanteInicio, long fechaHoraInicio) {
        this.nombre = nombre;
        this.instanteInicio = instanteInicio;
        this.fechaHoraInicio = fechaHoraInicio;
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
    public String getNombre() {
        return nombre;
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
