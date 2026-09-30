package ue.edu.co.fittrackandroid.entrenamiento.datos.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Fila de la tabla {@code serie_borrador}: una serie editada por el usuario durante la sesión.
 *
 * <p>La clave foránea hacia {@link EjercicioBorradorEntity} está declarada con borrado
 * en cascada, así que al quitar un ejercicio del borrador desaparecen también sus series.
 */
@Entity(
        tableName = "serie_borrador",
        foreignKeys = @ForeignKey(
                entity = EjercicioBorradorEntity.class,
                parentColumns = "id",
                childColumns = "ejercicioBorradorId",
                onDelete = ForeignKey.CASCADE))
public class SerieBorradorEntity {

    /** Clave primaria que genera SQLite cuando la fila es nueva. */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Ejercicio al que pertenece la serie. */
    public long ejercicioBorradorId;

    /** Orden de la serie dentro del ejercicio. */
    public int numeroSerie;

    /** Peso registrado en la serie. */
    public double peso;

    /** Repeticiones registradas en la serie. */
    public int repeticiones;

    /** Indica si el usuario ya completó la serie. */
    public boolean completada;

    /** Crea la fila de la serie con el peso, las repeticiones y si el usuario ya la completó. */
    public SerieBorradorEntity(long id, long ejercicioBorradorId, int numeroSerie, double peso,
                               int repeticiones, boolean completada) {
        this.id = id;
        this.ejercicioBorradorId = ejercicioBorradorId;
        this.numeroSerie = numeroSerie;
        this.peso = peso;
        this.repeticiones = repeticiones;
        this.completada = completada;
    }
}
