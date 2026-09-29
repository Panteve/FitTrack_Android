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
     * La relación cruza la tabla {@code ejercicio_borrador} como puente: el Junction
     * empareja {@code entrenamiento_borrador.id} con {@code ejercicio_borrador.entrenamientoId}
     * y {@code serie_borrador.ejercicioBorradorId} con {@code ejercicio_borrador.id}.
     */
    @Relation(entity = SerieBorradorEntity.class,
            parentColumn = "id",
            entityColumn = "ejercicioBorradorId",
            associateBy = @Junction(
                    value = EjercicioBorradorEntity.class,
                    parentColumn = "entrenamientoId",
                    entityColumn = "id"))
    public List<SerieBorradorEntity> series;

    /** @return la fila principal del borrador, o null si la consulta no devolvió nada. */
    public EntrenamientoBorradorEntity getEntrenamiento() {
        return entrenamiento;
    }
}
