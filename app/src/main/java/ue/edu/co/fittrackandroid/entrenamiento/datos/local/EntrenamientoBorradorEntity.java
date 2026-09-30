package ue.edu.co.fittrackandroid.entrenamiento.datos.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Fila de la tabla {@code entrenamiento_borrador}: el borrador del entrenamiento
 * que el usuario tiene abierto.
 *
 * <p>Room lee y escribe directamente en los campos de la clase, por eso son públicos
 * y el constructor los recibe todos.
 *
 * <p>Los tiempos se guardan como instantes de reloj real ({@link System#currentTimeMillis()}).
 * No se guarda el valor de {@link android.os.SystemClock#elapsedRealtime()} porque se reinicia
 * al apagar el dispositivo y no serviría para volver a calcular la duración del entrenamiento.
 */
@Entity(tableName = "entrenamiento_borrador")
public class EntrenamientoBorradorEntity {

    /** Clave primaria que genera SQLite cuando la fila es nueva. */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Dueño del borrador: una cuenta nunca recupera el entrenamiento de otra. */
    public String correoUsuario;

    /** Identificador de la rutina en el backend; puede ser nulo si no se conoce. */
    public Long idRutina;

    /** Nombre de la rutina tal como se muestra durante la sesión. */
    public String nombreRutina;

    /** Notas escritas por el usuario; se guarda como cadena vacía cuando no hay ninguna. */
    public String notas;

    /** Momento real en que empezó el entrenamiento, en milisegundos de reloj. */
    public long fechaHoraInicio;

    /** Momento real en que termina el descanso actual, o cero si no hay descanso. */
    public long fechaHoraFinDescanso;

    /** Vale true mientras el borrador se pueda recuperar. */
    public boolean activa;

    /** Crea la fila del borrador con todos los campos que Room guarda en la tabla. */
    public EntrenamientoBorradorEntity(long id, String correoUsuario, Long idRutina,
                                       String nombreRutina, String notas, long fechaHoraInicio,
                                       long fechaHoraFinDescanso, boolean activa) {
        this.id = id;
        this.correoUsuario = correoUsuario;
        this.idRutina = idRutina;
        this.nombreRutina = nombreRutina;
        this.notas = notas;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFinDescanso = fechaHoraFinDescanso;
        this.activa = activa;
    }

    /**
     * Prepara la fila de un entrenamiento que recién empieza: todavía no tiene identificador
     * local, hereda la rutina y las notas iniciales indicadas y queda marcada como activa
     * para poder recuperarla.
     */
    public static EntrenamientoBorradorEntity nuevoBorrador(
            String correoUsuario,
            Long idRutina,
            String nombreRutina,
            String notas,
            long fechaHoraInicio,
            long fechaHoraFinDescanso) {
        return new EntrenamientoBorradorEntity(
                0,
                correoUsuario,
                idRutina,
                nombreRutina,
                notas,
                fechaHoraInicio,
                fechaHoraFinDescanso,
                true);
    }
}
