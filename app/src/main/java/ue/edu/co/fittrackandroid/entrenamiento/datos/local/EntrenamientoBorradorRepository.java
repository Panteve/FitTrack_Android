package ue.edu.co.fittrackandroid.entrenamiento.datos.local;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ue.edu.co.fittrackandroid.entrenamiento.modelo.EjercicioEntrenamiento;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoEnCurso;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.SerieEntrenamiento;

/**
 * Guarda y recupera el borrador del entrenamiento en curso usando la base de datos local
 * de Room.
 *
 * <p>El borrador es solo eso: la sesión que el usuario tiene abierta. Los entrenamientos ya
 * terminados se guardan en el backend, no aquí.
 *
 * <p>Ninguna operación toca el hilo principal: todas se entregan a un único hilo de
 * base de datos, que además las ejecuta en orden. La única excepción es la lectura, que
 * vuelve al hilo principal porque su resultado lo usa la interfaz.
 *
 * <p>Los tiempos se guardan como instantes de reloj real
 * ({@link System#currentTimeMillis()}), nunca como valores de
 * {@link SystemClock#elapsedRealtime()}, porque estos últimos se reinician al apagar el
 * dispositivo y harían que la duración recuperada fuera absurda.
 */
public class EntrenamientoBorradorRepository {

    private static final String ETIQUETA = "BorradorEntrenamiento";

    /**
     * Un solo hilo para todas las operaciones locales. Así las escrituras se respetan
     * en el orden en que se piden y nunca se pisan entre sí.
     */
    private static final ExecutorService EJECUTOR = Executors.newSingleThreadExecutor();

    /** Entrega los resultados en el hilo principal, que es el único que puede tocar la interfaz. */
    private static final Handler HILO_PRINCIPAL = new Handler(Looper.getMainLooper());

    /** Avisa cuándo terminó de leer el borrador guardado de una cuenta. */
    public interface EscuchaBorrador {

        /** Avisa que la sesión quedó recuperada, o que esa cuenta no tenía borrador guardado. */
        void alCargarBorrador(EntrenamientoEnCurso entrenamiento);
    }

    private final FitTrackDatabase baseDeDatos;
    private final EntrenamientoBorradorDao dao;

    /** Crea el repositorio abriendo la base de datos local y guardando su DAO de borradores. */
    public EntrenamientoBorradorRepository(Context contexto) {
        baseDeDatos = FitTrackDatabase.obtenerInstancia(contexto);
        dao = baseDeDatos.borradorDao();
    }

    // ------------------------------------------------------------------ Crear

    /**
     * Guarda por completo una sesión que recién empieza: la fila del entrenamiento, sus
     * ejercicios y las series de cada uno. Todo se escribe en una sola transacción, así que
     * nunca queda un borrador a medio guardar.
     *
     * <p>Si la cuenta ya tenía un borrador, se reemplaza: solo puede haber uno activo.
     */
    public void guardarBorradorInicial(String correoUsuario, EntrenamientoEnCurso entrenamiento) {
        if (correoUsuario == null || entrenamiento == null) {
            return;
        }

        EJECUTOR.execute(() -> {
            try {
                baseDeDatos.runInTransaction(
                        () -> escribirBorradorCompleto(correoUsuario, entrenamiento));
            } catch (RuntimeException excepcion) {
                Log.e(ETIQUETA, "No se pudo guardar el borrador del entrenamiento", excepcion);
            }
        });
    }

    /** Borra el borrador anterior de la cuenta y escribe la sesión completa, ejercicio por ejercicio. */
    private void escribirBorradorCompleto(String correoUsuario,
                                          EntrenamientoEnCurso entrenamiento) {
        dao.eliminarBorradorDeUsuario(correoUsuario);

        EntrenamientoBorradorEntity borrador = EntrenamientoBorradorEntity.nuevoBorrador(
                correoUsuario,
                entrenamiento.getIdRutina(),
                textoONulo(entrenamiento.getNombreRutina()),
                textoONulo(entrenamiento.getNotas()),
                entrenamiento.getFechaHoraInicio(),
                obtenerFechaHoraFinDescanso(entrenamiento.getInstanteFinDescanso())
        );
        borrador.id = dao.insertarEntrenamiento(borrador);
        entrenamiento.setIdBorrador(borrador.id);

        List<EjercicioEntrenamiento> ejercicios = entrenamiento.getEjercicios();
        for (int posicion = 0; posicion < ejercicios.size(); posicion++) {
            escribirEjercicioConSeries(borrador.id, posicion, ejercicios.get(posicion));
        }
    }

    /** Inserta un ejercicio que todavía no existe y todas sus series. */
    private void escribirEjercicioConSeries(long entrenamientoId, int posicion,
                                            EjercicioEntrenamiento ejercicio) {
        long ejercicioId = dao.insertarEjercicio(
                crearEjercicio(0, entrenamientoId, posicion, ejercicio));
        ejercicio.setIdBorrador(ejercicioId);

        List<SerieBorradorEntity> series = new ArrayList<>();
        List<SerieEntrenamiento> seriesDelEjercicio = ejercicio.getSeries();

        for (int posicionSerie = 0; posicionSerie < seriesDelEjercicio.size(); posicionSerie++) {
            series.add(crearSerie(0, ejercicioId, posicionSerie, seriesDelEjercicio.get(posicionSerie)));
        }

        dao.insertarSeries(series);
    }

    // ------------------------------------------------------------------ Leer

    /**
     * Busca el borrador activo de la cuenta y lo devuelve ya convertido a
     * EntrenamientoEnCurso, con sus ejercicios, sus series y el descanso en curso.
     * El resultado llega en el hilo principal; si la cuenta no tiene borrador, llega null
     * y la aplicación sigue con el comportamiento de siempre.
     */
    public void cargarBorradorActivo(String correoUsuario, EscuchaBorrador escucha) {
        if (correoUsuario == null || escucha == null) {
            return;
        }

        EJECUTOR.execute(() -> {
            EntrenamientoEnCurso entrenamiento = null;

            try {
                EntrenamientoBorradorCompleto borrador = dao.obtenerActivo(correoUsuario);
                if (borrador != null && borrador.entrenamiento != null) {
                    entrenamiento = convertirABorrador(borrador);
                }
            } catch (RuntimeException excepcion) {
                Log.e(ETIQUETA, "No se pudo leer el borrador del entrenamiento", excepcion);
            }

            EntrenamientoEnCurso borradorRecuperado = entrenamiento;
            HILO_PRINCIPAL.post(() -> escucha.alCargarBorrador(borradorRecuperado));
        });
    }

    /**
     * Convierte las filas de Room en la sesión que usa la aplicación.
     *
     * <p>Las listas que devuelve Room no vienen ordenadas, así que los ejercicios se ordenan
     * por su posición y las series por su número. Los dos cronómetros de la pantalla usan
     * el tiempo transcurrido desde el arranque del sistema, y como ese valor cambia al
     * reiniciar el dispositivo, aquí se reconstruyen a partir de los instantes de reloj
     * real guardados.
     */
    private EntrenamientoEnCurso convertirABorrador(EntrenamientoBorradorCompleto borradorCompleto) {
        List<EjercicioBorradorEntity> ejerciciosGuardados =
                new ArrayList<>(borradorCompleto.ejercicios);
        Collections.sort(ejerciciosGuardados,
                Comparator.comparingInt(ejercicio -> ejercicio.posicion));

        Map<Long, List<SerieBorradorEntity>> seriesPorEjercicio =
                agruparSeriesPorEjercicio(borradorCompleto.series);

        EntrenamientoBorradorEntity borrador = borradorCompleto.entrenamiento;
        long duracionTranscurrida = Math.max(0, System.currentTimeMillis() - borrador.fechaHoraInicio);

        EntrenamientoEnCurso entrenamiento = new EntrenamientoEnCurso(
                borrador.idRutina,
                borrador.nombreRutina,
                SystemClock.elapsedRealtime() - duracionTranscurrida,
                borrador.fechaHoraInicio
        );
        entrenamiento.setIdBorrador(borrador.id);
        entrenamiento.setNotas(textoONuloANull(borrador.notas));

        for (EjercicioBorradorEntity ejercicioGuardado : ejerciciosGuardados) {
            List<SerieBorradorEntity> seriesGuardadas =
                    seriesPorEjercicio.get(ejercicioGuardado.id);
            if (seriesGuardadas == null) {
                seriesGuardadas = new ArrayList<>();
            }

            List<SerieEntrenamiento> series = new ArrayList<>();
            for (SerieBorradorEntity serieGuardada : seriesGuardadas) {
                series.add(convertirSerie(serieGuardada));
            }

            EjercicioEntrenamiento ejercicio = new EjercicioEntrenamiento(
                    ejercicioGuardado.rutinaEjercicioId,
                    ejercicioGuardado.nombre,
                    textoONuloANull(ejercicioGuardado.grupoMuscular),
                    series
            );
            ejercicio.setIdBorrador(ejercicioGuardado.id);
            entrenamiento.agregarEjercicio(ejercicio);
        }

        restaurarFinDescanso(entrenamiento, borrador.fechaHoraFinDescanso);
        return entrenamiento;
    }

    private SerieEntrenamiento convertirSerie(SerieBorradorEntity serieGuardada) {
        SerieEntrenamiento serie = new SerieEntrenamiento(
                serieGuardada.completada,
                serieGuardada.peso,
                serieGuardada.repeticiones,
                serieGuardada.numeroSerie
        );
        serie.setIdBorrador(serieGuardada.id);
        return serie;
    }

    /** Agrupa las series por ejercicio y deja cada grupo ordenado por número de serie. */
    private Map<Long, List<SerieBorradorEntity>> agruparSeriesPorEjercicio(
            List<SerieBorradorEntity> seriesGuardadas) {
        Map<Long, List<SerieBorradorEntity>> seriesPorEjercicio = new HashMap<>();

        for (SerieBorradorEntity serie : seriesGuardadas) {
            List<SerieBorradorEntity> seriesDelEjercicio =
                    seriesPorEjercicio.get(serie.ejercicioBorradorId);
            if (seriesDelEjercicio == null) {
                seriesDelEjercicio = new ArrayList<>();
                seriesPorEjercicio.put(serie.ejercicioBorradorId, seriesDelEjercicio);
            }
            seriesDelEjercicio.add(serie);
        }

        for (List<SerieBorradorEntity> seriesDelEjercicio : seriesPorEjercicio.values()) {
            Collections.sort(seriesDelEjercicio,
                    Comparator.comparingInt(serie -> serie.numeroSerie));
        }

        return seriesPorEjercicio;
    }

    // ------------------------------------------------------------------ Actualizar

    /**
     * Guarda los datos de todas las series de un ejercicio: peso, repeticiones y estado
     * de completada. También sirve para registrar una serie que el usuario acaba de agregar,
     * porque las que todavía no tienen fila propia se insertan.
     */
    public void guardarSeries(EjercicioEntrenamiento ejercicio) {
        if (ejercicio == null || ejercicio.getIdBorrador() == 0) {
            return;
        }

        long ejercicioId = ejercicio.getIdBorrador();
        EJECUTOR.execute(() -> {
            try {
                baseDeDatos.runInTransaction(() -> escribirSeries(ejercicio, ejercicioId));
            } catch (RuntimeException excepcion) {
                Log.e(ETIQUETA, "No se pudieron guardar las series del ejercicio", excepcion);
            }
        });
    }

    /**
     * Inserta o actualiza cada serie del ejercicio. Las que todavía no tienen fila propia se
     * insertan y se les guarda el identificador que Room les asigna.
     */
    private void escribirSeries(EjercicioEntrenamiento ejercicio, long ejercicioId) {
        List<SerieEntrenamiento> series = ejercicio.getSeries();

        for (int posicion = 0; posicion < series.size(); posicion++) {
            SerieEntrenamiento serie = series.get(posicion);
            SerieBorradorEntity fila = crearSerie(serie.getIdBorrador(), ejercicioId, posicion, serie);

            if (serie.getIdBorrador() == 0) {
                serie.setIdBorrador(dao.insertarSerie(fila));
            } else {
                dao.actualizarSerie(fila);
            }
        }
    }

    /**
     * Guarda los datos que pertenecen a la sesión completa y no a una serie concreta:
     * las notas y el descanso que está corriendo.
     */
    public void guardarDatosEntrenamiento(EntrenamientoEnCurso entrenamiento) {
        if (entrenamiento == null || entrenamiento.getIdBorrador() == 0) {
            return;
        }

        long entrenamientoId = entrenamiento.getIdBorrador();
        EJECUTOR.execute(() -> {
            try {
                EntrenamientoBorradorEntity borrador = dao.obtenerEntrenamiento(entrenamientoId);
                if (borrador == null) {
                    return;
                }

                borrador.notas = textoONulo(entrenamiento.getNotas());
                borrador.fechaHoraFinDescanso =
                        obtenerFechaHoraFinDescanso(entrenamiento.getInstanteFinDescanso());
                dao.actualizarEntrenamiento(borrador);
            } catch (RuntimeException excepcion) {
                Log.e(ETIQUETA, "No se pudieron guardar los datos de la sesión", excepcion);
            }
        });
    }

    /**
     * Quita un ejercicio del borrador guardado. Sus series se borran solas gracias a la
     * clave foránea con borrado en cascada.
     */
    public void eliminarEjercicio(EjercicioEntrenamiento ejercicio) {
        if (ejercicio == null || ejercicio.getIdBorrador() == 0) {
            return;
        }

        long ejercicioId = ejercicio.getIdBorrador();
        EJECUTOR.execute(() -> {
            try {
                dao.eliminarEjercicio(ejercicioId);
            } catch (RuntimeException excepcion) {
                Log.e(ETIQUETA, "No se pudo eliminar el ejercicio del borrador", excepcion);
            }
        });
    }

    /**
     * Quita una serie del borrador guardado y deja el número de las que quedan seguido.
     *
     * <p>Guardar las series otra vez es lo que renumera: el método crearSerie guarda el
     * número que tiene cada serie en memoria, que el modelo ya dejó consecutivo al quitar
     * la anterior. El borrado y la reescritura van en la misma transacción, para que el
     * borrador nunca quede con números repetidos o con un hueco.
     */
    public void eliminarSerie(EjercicioEntrenamiento ejercicio, SerieEntrenamiento serie) {
        if (ejercicio == null || serie == null || serie.getIdBorrador() == 0) {
            return;
        }

        long ejercicioId = ejercicio.getIdBorrador();
        long serieId = serie.getIdBorrador();
        EJECUTOR.execute(() -> {
            try {
                baseDeDatos.runInTransaction(() -> {
                    dao.eliminarSerie(serieId);
                    escribirSeries(ejercicio, ejercicioId);
                });
            } catch (RuntimeException excepcion) {
                Log.e(ETIQUETA, "No se pudo eliminar la serie del borrador", excepcion);
            }
        });
    }

    // ------------------------------------------------------------------ Eliminar

    /**
     * Borra el borrador completo de una cuenta: entrenamiento, ejercicios y series.
     *
     * <p>Se borra por correo y no por identificador de fila para que también funcione cuando
     * la sesión ya no está en memoria, por ejemplo al cerrar sesión. Así el borrador de una
     * cuenta nunca se confunde con el de otra.
     */
    public void eliminarBorrador(String correoUsuario) {
        if (correoUsuario == null) {
            return;
        }

        EJECUTOR.execute(() -> {
            try {
                dao.eliminarBorradorDeUsuario(correoUsuario);
            } catch (RuntimeException excepcion) {
                Log.e(ETIQUETA, "No se pudo eliminar el borrador del entrenamiento", excepcion);
            }
        });
    }

    // ------------------------------------------------------------------ Utilidades

    /** Traduce el ejercicio de la pantalla a la fila que se guarda en la tabla, en la posición indicada. */
    private EjercicioBorradorEntity crearEjercicio(long id, long entrenamientoId, int posicion,
                                                   EjercicioEntrenamiento ejercicio) {
        return new EjercicioBorradorEntity(
                id,
                entrenamientoId,
                ejercicio.getRutinaEjercicioId(),
                textoONulo(ejercicio.getNombre()),
                textoONulo(ejercicio.getGrupoMuscular()),
                posicion
        );
    }

    /**
     * Traduce la serie de la pantalla a la fila que se guarda en la tabla. Si la serie todavía
     * no tiene número propio, se le asigna el siguiente según la posición que ocupa.
     */
    private SerieBorradorEntity crearSerie(long id, long ejercicioId, int posicion,
                                            SerieEntrenamiento serie) {
        int numeroSerie = serie.getNumeroSerie() > 0 ? serie.getNumeroSerie() : posicion + 1;
        return new SerieBorradorEntity(
                id,
                ejercicioId,
                numeroSerie,
                serie.getPeso(),
                serie.getRepeticiones(),
                serie.isCompletada()
        );
    }

    /**
     * Traduce el fin del descanso, que la pantalla maneja con el reloj del sistema, al
     * instante de reloj real que sí sobrevive a que el proceso se cierre. Si no hay
     * descanso en curso, o si su tiempo ya venció, se guarda cero.
     */
    private long obtenerFechaHoraFinDescanso(long instanteFinDescanso) {
        if (instanteFinDescanso <= 0) {
            return 0;
        }

        long milisegundosRestantes = instanteFinDescanso - SystemClock.elapsedRealtime();
        if (milisegundosRestantes <= 0) {
            return 0;
        }

        return System.currentTimeMillis() + milisegundosRestantes;
    }

    /**
     * Devuelve a la sesión el descanso que estaba corriendo. Si el tiempo restante ya se
     * venció, el descanso simplemente no se restaura.
     */
    private void restaurarFinDescanso(EntrenamientoEnCurso entrenamiento,
                                      long fechaHoraFinDescanso) {
        if (fechaHoraFinDescanso <= 0) {
            return;
        }

        long milisegundosRestantes = fechaHoraFinDescanso - System.currentTimeMillis();
        if (milisegundosRestantes <= 0) {
            return;
        }

        entrenamiento.setInstanteFinDescanso(SystemClock.elapsedRealtime() + milisegundosRestantes);
    }

    /** Devuelve el texto, o una cadena vacía si es nulo, porque SQLite no guarda valores nulos. */
    private String textoONulo(String texto) {
        if (texto == null) {
            return "";
        }
        return texto;
    }

    /** Devuelve nulo si el texto está vacío, para devolverlo al modelo tal como estaba. */
    private String textoONuloANull(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }
        return texto;
    }
}
