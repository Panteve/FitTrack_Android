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
     * Crea el entrenamiento en curso con los datos de la rutina desde la que se empezó.
     * Guarda el instante de inicio en el reloj del sistema y también el momento real
     * para poder mostrarlo después en el resumen del entrenamiento.
     */
    public EntrenamientoEnCurso(Long idRutina, String nombreRutina, long instanteInicio,
                                long fechaHoraInicio) {
        this.idRutina = idRutina;
        this.nombreRutina = nombreRutina;
        this.instanteInicio = instanteInicio;
        this.fechaHoraInicio = fechaHoraInicio;
    }

    /** Devuelve el identificador de la rutina que originó el entrenamiento. */
    public Long getIdRutina() {
        return idRutina;
    }

    /** Devuelve el identificador de la fila que guarda la sesión en la base de datos local, o cero si todavía no se ha guardado. */
    public long getIdBorrador() {
        return idBorrador;
    }

    /** Guarda el identificador que Room asignó al borrador de esta sesión al escribirlo en la base de datos local. */
    public void setIdBorrador(long idBorrador) {
        this.idBorrador = idBorrador;
    }

    /** Devuelve las notas opcionales registradas para el entrenamiento. */
    public String getNotas() {
        return notas;
    }

    /** Guarda las notas que el usuario escribe durante el entrenamiento. */
    public void setNotas(String notas) {
        this.notas = notas;
    }

    /** Devuelve el instante de inicio del entrenamiento medido con el reloj del sistema. */
    public long getInstanteInicio() {
        return instanteInicio;
    }

    /** Devuelve el momento real de inicio, en milisegundos de reloj, para mostrar día y hora en el resumen. */
    public long getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    /** Devuelve el nombre de la rutina entrenada, o una cadena vacía si no se conoce. */
    public String getNombreRutina() {
        return nombreRutina;
    }

    /** Devuelve la lista de ejercicios que forman la sesión, con sus series editables. */
    public List<EjercicioEntrenamiento> getEjercicios() {
        return ejercicios;
    }

    /** Agrega un ejercicio con su primera serie al final de la sesión. */
    public void agregarEjercicio(EjercicioEntrenamiento ejercicio) {
        ejercicios.add(ejercicio);
    }

    /** Indica si la sesión sigue en curso y no ha sido finalizada. */
    public boolean isActiva() {
        return activa;
    }

    /** Marca la sesión como terminada o descartada y cancela cualquier descanso activo. */
    public void finalizar() {
        activa = false;
        instanteFinDescanso = 0;
    }

    /** Devuelve el instante en que termina el descanso actual, medido con el reloj del sistema, o cero si no hay descanso activo. */
    public long getInstanteFinDescanso() {
        return instanteFinDescanso;
    }

    /** Guarda el instante en que termina el descanso actual, medido con el reloj del sistema. */
    public void setInstanteFinDescanso(long instanteFinDescanso) {
        this.instanteFinDescanso = instanteFinDescanso;
    }

    /** Cancela el descanso actual sin modificar el resto de la sesión. */
    public void detenerDescanso() {
        instanteFinDescanso = 0;
    }

    /** Indica si hay un descanso activo que todavía no terminó. */
    public boolean hayDescansoActivo() {
        return instanteFinDescanso > 0 && getSegundosDescansoRestantes() > 0;
    }

    /**
     * Calcula el tiempo ya transcurrido del entrenamiento a partir del instante guardado.
     * No hay un segundo cronómetro: siempre se recalcula desde el inicio real.
     */
    public long getSegundosTranscurridos() {
        long segundos = (SystemClock.elapsedRealtime() - instanteInicio) / 1000;
        return Math.max(0, segundos);
    }

    /** Devuelve los segundos que le quedan al descanso actual, o cero si no hay descanso o si ya terminó. */
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

    /** Devuelve el tiempo transcurrido en el formato HH:MM:SS. */
    public String getTiempoTranscurrido() {
        return formatearTiempo(getSegundosTranscurridos());
    }

    /**
     * Convierte una cantidad de segundos al formato HH:MM:SS que usan el resumen,
     * el panel de descanso y la barra del entrenamiento minimizado.
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
