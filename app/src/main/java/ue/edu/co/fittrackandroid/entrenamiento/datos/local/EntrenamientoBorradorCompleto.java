package ue.edu.co.fittrackandroid.entrenamiento.datos.local;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * Lectura completa de un borrador: el entrenamiento junto con sus ejercicios y sus series.
 *
 * <p>No es una tabla, es solo el resultado de las relaciones declaradas con {@link Relation}.
 * Room llena los tres campos en la misma transacción, por eso la consulta que lo devuelve
 * está marcada como {@code @Transaction} en {@link EntrenamientoBorradorDao}.
 *
 * <p>Room necesita escribir directamente en estas listas, por eso son públicas.
 */
public class EntrenamientoBorradorCompleto {

    /** Datos de la fila principal del borrador. */
    @Embedded
    public EntrenamientoBorradorEntity entrenamiento;

    /** Ejercicios del borrador. */
    @Relation(entity = EjercicioBorradorEntity.class,
            parentColumn = "id",
            entityColumn = "entrenamientoId")
    public List<EjercicioBorradorEntity> ejercicios;

    /**
     * Series de todos los ejercicios del borrador.
     * La relación cruza la tabla ejercicio_borrador como puente: el Junction
     * empareja entrenamiento_borrador.id con ejercicio_borrador.entrenamientoId
     * y serie_borrador.ejercicioBorradorId con ejercicio_borrador.id.
     */
    @Relation(entity = SerieBorradorEntity.class,
            parentColumn = "id",
            entityColumn = "ejercicioBorradorId",
            associateBy = @Junction(
                    value = EjercicioBorradorEntity.class,
                    parentColumn = "entrenamientoId",
                    entityColumn = "id"))
    public List<SerieBorradorEntity> series;

    /** Devuelve la fila principal del borrador, o nulo si la consulta no devolvió nada. */
    public EntrenamientoBorradorEntity getEntrenamiento() {
        return entrenamiento;
    }
}
