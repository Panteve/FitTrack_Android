package ue.edu.co.fittrackandroid.entrenamiento.datos.local;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Fila de la tabla {@code ejercicio_borrador}: un ejercicio agregado a la sesión en curso.
 *
 * <p>La clave foránea hacia {@link EntrenamientoBorradorEntity} está declarada con
 * borrado en cascada, así que al eliminar el borrador desaparecen también sus ejercicios
 * y, con ellos, todas sus series.
 */
@Entity(
        tableName = "ejercicio_borrador",
        foreignKeys = @ForeignKey(
                entity = EntrenamientoBorradorEntity.class,
                parentColumns = "id",
                childColumns = "entrenamientoId",
                onDelete = ForeignKey.CASCADE))
public class EjercicioBorradorEntity {

    /** Clave primaria que genera SQLite cuando la fila es nueva. */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Entrenamiento al que pertenece el ejercicio. */
    public long entrenamientoId;

    /** Identificador del bloque del ejercicio dentro de la rutina; puede ser nulo. */
    public Long rutinaEjercicioId;

    /** Nombre visible del ejercicio. */
    public String nombre;

    /** Grupo muscular visible del ejercicio. */
    public String grupoMuscular;

    /** Orden del ejercicio dentro del entrenamiento, empezando en cero. */
    public int posicion;

    public EjercicioBorradorEntity(long id, long entrenamientoId, Long rutinaEjercicioId,
                                   String nombre, String grupoMuscular, int posicion) {
        this.id = id;
        this.entrenamientoId = entrenamientoId;
        this.rutinaEjercicioId = rutinaEjercicioId;
        this.nombre = nombre;
        this.grupoMuscular = grupoMuscular;
        this.posicion = posicion;
    }
}
