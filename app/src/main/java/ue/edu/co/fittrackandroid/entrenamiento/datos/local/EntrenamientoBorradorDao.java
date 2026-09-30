package ue.edu.co.fittrackandroid.entrenamiento.datos.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

/**
 * Consultas de Room sobre el borrador del entrenamiento en curso.
 *
 * <p>Ninguna de estas operaciones se llama desde el hilo principal: el repositorio
 * {@link EntrenamientoBorradorRepository} las ejecuta siempre en su hilo de base de datos.
 */
@Dao
public interface EntrenamientoBorradorDao {

    /**
     * Crea la fila principal de un entrenamiento.
     *
     * @param entrenamiento borrador que se va a guardar.
     * @return el identificador local que SQLite asignó a la fila.
     */
    @Insert
    long insertarEntrenamiento(EntrenamientoBorradorEntity entrenamiento);

    /**
     * Crea la fila de un ejercicio de la sesión.
     *
     * @param ejercicio ejercicio que se va a guardar.
     * @return el identificador local que SQLite asignó a la fila.
     */
    @Insert
    long insertarEjercicio(EjercicioBorradorEntity ejercicio);

    /** Crea una sola serie, por ejemplo la que el usuario agrega durante la sesión. */
    @Insert
    long insertarSerie(SerieBorradorEntity serie);

    /** Crea de una sola vez todas las series de un ejercicio recién agregado. */
    @Insert
    void insertarSeries(List<SerieBorradorEntity> series);

    /**
     * Recupera el borrador activo de una cuenta con sus ejercicios y sus series.
     * Solo puede existir un borrador activo por usuario.
     *
     * @param correoUsuario cuenta dueña del borrador.
     * @return el borrador completo, o null si esa cuenta no tiene ninguno.
     */
    @Transaction
    @Query("SELECT * FROM entrenamiento_borrador "
            + "WHERE correoUsuario = :correoUsuario AND activa = 1 LIMIT 1")
    EntrenamientoBorradorCompleto obtenerActivo(String correoUsuario);

    /** @return la fila principal del borrador, o null si ya no existe. */
    @Query("SELECT * FROM entrenamiento_borrador WHERE id = :entrenamientoId")
    EntrenamientoBorradorEntity obtenerEntrenamiento(long entrenamientoId);

    /** Guarda los cambios de una fila que ya existe, como las notas o el fin del descanso. */
    @Update
    void actualizarEntrenamiento(EntrenamientoBorradorEntity entrenamiento);

    /** Guarda los cambios de un ejercicio, por ejemplo si cambió su posición. */
    @Update
    void actualizarEjercicio(EjercicioBorradorEntity ejercicio);

    /** Guarda los cambios de una serie: peso, repeticiones y estado de completada. */
    @Update
    void actualizarSerie(SerieBorradorEntity serie);

    /**
     * Elimina el borrador de una cuenta. Por el borrado en cascada también se van sus
     * ejercicios y todas sus series.
     *
     * @param correoUsuario cuenta cuyo borrador se borra.
     */
    @Query("DELETE FROM entrenamiento_borrador WHERE correoUsuario = :correoUsuario")
    void eliminarBorradorDeUsuario(String correoUsuario);

    /**
     * Elimina un ejercicio del borrador. Sus series se borran solas por la clave foránea
     * con borrado en cascada.
     *
     * @param ejercicioBorradorId identificador local del ejercicio.
     */
    @Query("DELETE FROM ejercicio_borrador WHERE id = :ejercicioBorradorId")
    void eliminarEjercicio(long ejercicioBorradorId);

    /**
     * Elimina una sola serie del borrador, sin tocar las demás de su ejercicio.
     *
     * @param serieBorradorId identificador local de la serie.
     */
    @Query("DELETE FROM serie_borrador WHERE id = :serieBorradorId")
    void eliminarSerie(long serieBorradorId);
}
