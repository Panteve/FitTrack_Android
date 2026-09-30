package ue.edu.co.fittrackandroid.entrenamiento.datos;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Preferencias de entrenamiento guardadas con Preferences DataStore.
 *
 * <p>Aquí vive la duración del descanso predeterminado. Es un dato pequeño del usuario en
 * su dispositivo: solo necesita un número entero, sin tablas, sin consultas complejas y
 * sin sincronización con el backend. Por eso no se guarda en Room ni en la sesión.
 *
 * <p>La preferencia se edita desde el perfil, pero la clase vive aquí porque es la
 * configuración que usa el entrenamiento activo. Los datos de la sesión siguen en
 * {@code SesionManager}, con SharedPreferences.
 *
 * <p>DataStore permite una sola instancia por archivo y por proceso: si dos pantallas
 * construyeran su propio DataStore sobre el mismo archivo, la segunda fallaría. Por eso
 * la clase es un singleton, al igual que la base de datos de Room.
 *
 * <p>Se usa la versión de RxJava 3 porque el proyecto está escrito en Java: permite leer
 * y escribir sin bloquear el hilo principal y sin introducir archivos Kotlin.
 */
public class PreferenciasEntrenamientoDataStore {

    /** Descanso de tres minutos, el valor con el que arranca la aplicación. */
    public static final int SEGUNDOS_DESCANSO_PREDETERMINADO = 180;

    /** No se puede bajar de medio minuto. */
    public static final int SEGUNDOS_DESCANSO_MINIMO = 30;

    /** No se puede pasar de diez minutos. */
    public static final int SEGUNDOS_DESCANSO_MAXIMO = 600;

    /** Cuánto suma o resta cada pulsación de los botones del perfil. */
    public static final int PASO_AJUSTE_DESCANSO = 15;

    /**
     * Nombre del archivo de DataStore. Al construirlo, la librería agrega la extensión
     * y el archivo real queda como preferencias_entrenamiento.preferences_pb.
     */
    private static final String NOMBRE_ARCHIVO = "preferencias_entrenamiento";

    private static final Preferences.Key<Integer> CLAVE_SEGUNDOS_DESCANSO =
            PreferencesKeys.intKey("segundos_descanso_predeterminado");

    private static PreferenciasEntrenamientoDataStore instancia;

    private final RxDataStore<Preferences> dataStore;

    /**
     * Crea las preferencias abriendo el archivo de DataStore sobre el contexto de la
     * aplicación, porque el archivo debe sobrevivir a los fragments que lo consultan.
     */
    private PreferenciasEntrenamientoDataStore(Context contexto) {
        // Se usa el contexto de la aplicación: el DataStore debe sobrevivir a los fragments.
        dataStore = new RxPreferenceDataStoreBuilder(
                contexto.getApplicationContext(),
                NOMBRE_ARCHIVO
        ).build();
    }

    /**
     * Crea el DataStore la primera vez y devuelve la misma instancia en las siguientes
     * llamadas. El método está sincronizado porque varias pantallas pueden pedirla a la vez.
     */
    public static synchronized PreferenciasEntrenamientoDataStore obtenerInstancia(
            Context contexto) {
        if (instancia == null) {
            instancia = new PreferenciasEntrenamientoDataStore(contexto);
        }
        return instancia;
    }

    /**
     * Lee los segundos de descanso guardados, ya limitados al rango permitido. Si el
     * usuario nunca ajustó la preferencia se devuelve el valor predeterminado.
     */
    public Single<Integer> obtenerSegundosDescanso() {
        return dataStore.data()
                .map(preferencias -> {
                    Integer segundosGuardados = preferencias.get(CLAVE_SEGUNDOS_DESCANSO);
                    if (segundosGuardados == null) {
                        return SEGUNDOS_DESCANSO_PREDETERMINADO;
                    }
                    return limitarASeRangoPermitido(segundosGuardados);
                })
                .firstOrError();
    }

    /**
     * Guarda la duración del descanso predeterminado. El valor se limita al rango
     * permitido antes de escribirlo, así no puede quedar un descanso negativo o excesivo.
     * La operación se confirma cuando el dato quedó en el archivo.
     */
    public Completable guardarSegundosDescanso(int segundosDescanso) {
        int segundosLimitados = limitarASeRangoPermitido(segundosDescanso);

        return dataStore.updateDataAsync(preferenciasGuardadas -> {
            MutablePreferences preferenciasNuevas =
                    preferenciasGuardadas.toMutablePreferences();
            preferenciasNuevas.set(CLAVE_SEGUNDOS_DESCANSO, segundosLimitados);
            return Single.just(preferenciasNuevas);
        }).ignoreElement();
    }

    /**
     * Deja el descanso dentro del rango que la pantalla de perfil permite elegir,
     * devolviendo el mismo valor si es válido o el límite más cercano si no lo es.
     */
    private static int limitarASeRangoPermitido(int segundosDescanso) {
        if (segundosDescanso < SEGUNDOS_DESCANSO_MINIMO) {
            return SEGUNDOS_DESCANSO_MINIMO;
        }

        if (segundosDescanso > SEGUNDOS_DESCANSO_MAXIMO) {
            return SEGUNDOS_DESCANSO_MAXIMO;
        }

        return segundosDescanso;
    }
}
