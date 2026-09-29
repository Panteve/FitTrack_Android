package ue.edu.co.fittrackandroid.entrenamiento.datos.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Base de datos local de FitTrack, construida con Room sobre SQLite.
 *
 * <p>Solo guarda el borrador del entrenamiento en curso, que pertenece al feature de
 * entrenamiento; por eso la base vive dentro de {@code entrenamiento/datos/local} y no
 * en una carpeta genérica de base de datos.
 *
 * <p>Se mantiene una sola instancia por proceso: Room no admite abrir la misma base
 * varias veces con configuraciones distintas, y así todas las pantallas comparten
 * la misma conexión.
 */
@Database(
        entities = {
                EntrenamientoBorradorEntity.class,
                EjercicioBorradorEntity.class,
                SerieBorradorEntity.class
        },
        version = 1,
        exportSchema = false)
public abstract class FitTrackDatabase extends RoomDatabase {

    private static final String NOMBRE_BASE_DATOS = "fittrack.db";

    private static FitTrackDatabase instancia;

    /** @return el DAO con todas las consultas del borrador del entrenamiento. */
    public abstract EntrenamientoBorradorDao borradorDao();

    /**
     * Abre la base de datos la primera vez y devuelve la misma instancia en las siguientes
     * llamadas. Se usa {@code synchronized} porque varias pantallas pueden pedirla a la vez.
     *
     * @param contexto contexto de la aplicación, para que la base sobreviva a los fragments.
     * @return la instancia única de la base de datos.
     */
    public static synchronized FitTrackDatabase obtenerInstancia(Context contexto) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(
                    contexto.getApplicationContext(),
                    FitTrackDatabase.class,
                    NOMBRE_BASE_DATOS
            ).build();
        }
        return instancia;
    }
}
