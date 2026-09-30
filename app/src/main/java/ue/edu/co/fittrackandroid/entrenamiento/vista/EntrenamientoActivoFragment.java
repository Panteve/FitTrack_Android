package ue.edu.co.fittrackandroid.entrenamiento.vista;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.entrenamiento.datos.EntrenamientoRepository;
import ue.edu.co.fittrackandroid.entrenamiento.datos.PreferenciasEntrenamientoDataStore;
import ue.edu.co.fittrackandroid.entrenamiento.datos.local.EntrenamientoBorradorRepository;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EjercicioEntrenamiento;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoCrearRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoDetalleResponse;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoEnCurso;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.RegistroSerieRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.SerieEntrenamiento;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Pantalla del entrenamiento en curso.
 *
 * <p>Muestra el resumen de la sesión (duración, volumen y series), la lista de ejercicios
 * con sus series y el temporizador de descanso entre series.
 *
 * <p>La sesión completa vive en {@link EntrenamientoEnCurso}, que es propiedad de MainActivity.
 * Por eso el tiempo y los datos se conservan al minimizar el entrenamiento, aunque el fragment
 * y sus vistas se destruyan.
 */
public class EntrenamientoActivoFragment extends Fragment
        implements EntrenamientoEjercicioAdapter.EscuchaEntrenamiento  {

    /** Tiempo de descanso con el que arranca cada serie, hasta leer la preferencia. */
    private static final int SEGUNDOS_DESCANSO_BASE =
            PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_PREDETERMINADO;

    /** Pasos de ajuste del temporizador de descanso. */
    private static final int AJUSTE_DESCANSO_SEGUNDOS = 15;

    /** Cada cuánto se refrescan los dos relojes de la pantalla. */
    private static final long INTERVALO_RELOJ_MS = 1000;

    /**
     * Espera antes de guardar el peso y las repeticiones, para no escribir en la base de
     * datos por cada tecla que el usuario pulse.
     */
    private static final long ESPERA_GUARDADO_SERIES_MS = 400;

    private MainActivity activity;
    private EntrenamientoEnCurso entrenamiento;
    private EntrenamientoEjercicioAdapter adapter;
    private EntrenamientoRepository entrenamientoRepository;
    private EntrenamientoBorradorRepository borradorRepository;
    private PreferenciasEntrenamientoDataStore preferenciasEntrenamiento;
    private Call<EntrenamientoDetalleResponse> currentCall;
    private boolean guardandoEntrenamiento;

    /**
     * Descanso con el que empieza cada serie. Se reemplaza por la preferencia que el
     * usuario configuró en el perfil; si esa lectura falla se queda el valor seguro de
     * tres minutos.
     */
    private int segundosDescansoPredeterminado = SEGUNDOS_DESCANSO_BASE;

    /** Lectura de la preferencia, que se cancela al destruirse la vista. */
    private final CompositeDisposable suscripcionesDescanso = new CompositeDisposable();

    /**
     * Se pone en true cuando el entrenamiento ya se guardó en el backend o el usuario lo
     * descartó. A partir de ese momento MainActivity ya borró el borrador local, así que la
     * pantalla no debe volver a escribir en él.
     */
    private boolean borradorResuelto;

    /** Ejercicios cuyas series cambiaron y que aún no se han escrito en el borrador. */
    private final List<EjercicioEntrenamiento> ejerciciosPorGuardar = new ArrayList<>();

    private TextView tvDuracionEntrenamiento;
    private TextView tvVolumenEntrenamiento;
    private TextView tvSeriesEntrenamiento;
    private RecyclerView rvEjerciciosEntrenamiento;
    private Button btnDescartarEntrenamiento;
    private View layoutDescansoEntrenamiento;
    private TextView tvTiempoDescanso;
    private Button btnRestarDescanso;
    private Button btnSumarDescanso;
    private Button btnOmitirDescanso;

    private final Handler handler = new Handler(Looper.getMainLooper());

    /** Refresca la duración del entrenamiento una vez por segundo. */
    private final Runnable runnableRelojEntrenamiento = new Runnable() {
        @Override
        public void run() {
            actualizarDuracion();
            handler.postDelayed(this, INTERVALO_RELOJ_MS);
        }
    };

    /** Avanza la cuenta regresiva del descanso una vez por segundo. */
    private final Runnable runnableDescanso = new Runnable() {
        @Override
        public void run() {
            avanzarCuentaDescanso();
        }
    };

    /** Escribe las series que el usuario editó, una vez que dejó de escribir. */
    private final Runnable runnableGuardarSeries = new Runnable() {
        @Override
        public void run() {
            guardarSeriesPendientes();
        }
    };

    public EntrenamientoActivoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) requireActivity();
        // La sesión la conserva MainActivity. Si el proceso se cerró, MainActivity la recuperó
        // del borrador local de Room antes de abrir esta pantalla, con sus ejercicios, sus
        // series, el descanso y el tiempo transcurrido.
        entrenamiento = activity.obtenerEntrenamientoEnCurso();
        entrenamientoRepository = new EntrenamientoRepository(requireContext());
        borradorRepository = new EntrenamientoBorradorRepository(requireContext());
        preferenciasEntrenamiento =
                PreferenciasEntrenamientoDataStore.obtenerInstancia(requireContext());
    }

    /**
     * Lee de DataStore la duración del descanso configurada en el perfil. La lectura llega
     * en un hilo de trabajo, así que solo se guarda el valor: la cuenta regresiva usa la
     * variable cuando el usuario complete una serie. Si la lectura falla se conserva el
     * valor seguro de tres minutos.
     */
    private void cargarDescansoPredeterminado() {
        suscripcionesDescanso.add(
                preferenciasEntrenamiento.obtenerSegundosDescanso().subscribe(
                        segundosDescanso -> segundosDescansoPredeterminado = segundosDescanso,
                        error -> segundosDescansoPredeterminado = SEGUNDOS_DESCANSO_BASE
                ));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrenamiento_activo, container, false);
        aplicarEspacioParaBarraDelSistema(view);

        // Si no hay sesión en curso no hay nada que pintar: onResume devuelve a la pantalla anterior.
        if (entrenamiento != null) {
            inicializarVistas(view);
            configurarRecyclerView();
            configurarAcciones();
            recalcularResumen();
            actualizarEstadoDescanso();

            // La preferencia se consulta una sola vez, al entrar a la sesión. Si el usuario
            // la cambia en el perfil después, se aplicará al siguiente entrenamiento que abra.
            cargarDescansoPredeterminado();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registrarAtrasoQueMinimiza();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Si no hay sesión en curso no hay nada que mostrar: se vuelve a la pantalla anterior.
        if (entrenamiento == null) {
            activity.regresar();
            return;
        }

        configurarToolbar();
        activity.ocultarNavegacionInferior();

        reanudarActualizacionesVisuales();
    }

    @Override
    public void onPause() {
        super.onPause();
        detenerActualizacionesVisuales();
        // Al salir de la pantalla se escribe lo que estaba esperando, para que minimizar el
        // entrenamiento no dependa solo de lo que MainActivity tiene en memoria.
        guardarCambiosPendientes();
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        guardandoEntrenamiento = false;
        cancelarCallbacks();
        // La lectura de la preferencia ya no importa: el valor quedó en la variable de la
        // pantalla o se perdió con la vista.
        suscripcionesDescanso.clear();
        super.onDestroyView();
    }

    /**
     * Durante el entrenamiento la navegación inferior de la app está oculta, así que esta
     * pantalla llega hasta el borde inferior de la pantalla, donde también viven los botones
     * de navegación del sistema. Aquí se reserva ese espacio para que ni el panel de descanso
     * ni el botón de descartar queden por debajo de ellos.
     */
    private void aplicarEspacioParaBarraDelSistema(View raiz) {
        int espacioInferiorInicial = raiz.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (vista, insets) -> {
            Insets barrasDelSistema = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(vista.getPaddingLeft(), vista.getPaddingTop(), vista.getPaddingRight(),
                    espacioInferiorInicial + barrasDelSistema.bottom);
            return insets;
        });
    }

    private void inicializarVistas(View view) {
        tvDuracionEntrenamiento = view.findViewById(R.id.tvDuracionEntrenamiento);
        tvVolumenEntrenamiento = view.findViewById(R.id.tvVolumenEntrenamiento);
        tvSeriesEntrenamiento = view.findViewById(R.id.tvSeriesEntrenamiento);
        rvEjerciciosEntrenamiento = view.findViewById(R.id.rvEjerciciosEntrenamiento);
        btnDescartarEntrenamiento = view.findViewById(R.id.btnDescartarEntrenamiento);
        layoutDescansoEntrenamiento = view.findViewById(R.id.layoutDescansoEntrenamiento);
        tvTiempoDescanso = view.findViewById(R.id.tvTiempoDescanso);
        btnRestarDescanso = view.findViewById(R.id.btnRestarDescanso);
        btnSumarDescanso = view.findViewById(R.id.btnSumarDescanso);
        btnOmitirDescanso = view.findViewById(R.id.btnOmitirDescanso);
    }

    private void configurarRecyclerView() {
        adapter = new EntrenamientoEjercicioAdapter(entrenamiento.getEjercicios(), this);
        rvEjerciciosEntrenamiento.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosEntrenamiento.setAdapter(adapter);
    }

    private void configurarAcciones() {
        btnDescartarEntrenamiento.setOnClickListener(v -> confirmarDescartarEntrenamiento());
        btnRestarDescanso.setOnClickListener(v -> restarTiempoDescanso());
        btnSumarDescanso.setOnClickListener(v -> sumarTiempoDescanso());
        btnOmitirDescanso.setOnClickListener(v -> omitirDescanso());
    }

    /**
     * Toolbar de la sesión: la flecha hacia arriba se reemplaza por el chevron hacia abajo,
     * que minimiza el entrenamiento, y la acción derecha termina la sesión.
     */
    private void configurarToolbar() {
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloEntrenamientoActivo),
                true,
                getString(R.string.btnTerminarEntrenamiento)
        );
        activity.mostrarControlMinimizarEntrenamiento(this::minimizarEntrenamiento);
        activity.setAccionToolbar(this::confirmarTerminarEntrenamiento);
        activity.habilitarAccionToolbar(true);
    }

    /** El botón físico de retroceso también minimiza, nunca abandona la sesión. */
    private void registrarAtrasoQueMinimiza() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        minimizarEntrenamiento();
                    }
                });
    }

    /** Vuelve a poner en marcha los relojes al regresar a la sesión minimizada. */
    private void reanudarActualizacionesVisuales() {
        // El tiempo sigue contando aunque la pantalla no esté visible, porque se calcula
        // siempre desde el instante en que empezó la sesión.
        handler.removeCallbacks(runnableRelojEntrenamiento);
        handler.post(runnableRelojEntrenamiento);

        actualizarEstadoDescanso();
    }

    private void detenerActualizacionesVisuales() {
        handler.removeCallbacks(runnableRelojEntrenamiento);
        handler.removeCallbacks(runnableDescanso);
    }

    private void cancelarCallbacks() {
        handler.removeCallbacksAndMessages(null);
    }

    // ------------------------------------------------------------------ Reloj general

    private void actualizarDuracion() {
        tvDuracionEntrenamiento.setText(entrenamiento.getTiempoTranscurrido());
    }

    // ------------------------------------------------------------------ Resumen

    /**
     * Recalcula las tres métricas del resumen recorriendo todos los modelos desde cero.
     * No se acumulan contadores, así desmarcar una serie o quitar un ejercicio siempre
     * deja el resumen correcto.
     */
    private void recalcularResumen() {
        actualizarDuracion();
        tvVolumenEntrenamiento.setText(getString(R.string.tvVolumenEntrenamiento,
                formatearVolumen(sumarVolumen())));
        tvSeriesEntrenamiento.setText(getString(R.string.tvSeriesEntrenamiento,
                contarSeriesCompletadas(), contarSeriesTotales()));
    }

    /** @return el volumen de todas las series completadas, en kilogramos. */
    private double sumarVolumen() {
        double volumenTotal = 0;

        for (EjercicioEntrenamiento ejercicio : entrenamiento.getEjercicios()) {
            for (SerieEntrenamiento serie : ejercicio.getSeries()) {
                volumenTotal += serie.calcularVolumen();
            }
        }

        return volumenTotal;
    }

    private int contarSeriesCompletadas() {
        int seriesCompletadas = 0;

        for (EjercicioEntrenamiento ejercicio : entrenamiento.getEjercicios()) {
            for (SerieEntrenamiento serie : ejercicio.getSeries()) {
                if (serie.isCompletada()) {
                    seriesCompletadas++;
                }
            }
        }

        return seriesCompletadas;
    }

    private int contarSeriesTotales() {
        int seriesTotales = 0;

        for (EjercicioEntrenamiento ejercicio : entrenamiento.getEjercicios()) {
            seriesTotales += ejercicio.getCantidadSeries();
        }

        return seriesTotales;
    }

    /** Muestra el volumen sin decimales cuando no hacen falta: 600 kg, 22.5 kg. */
    private String formatearVolumen(double volumenTotal) {
        DecimalFormat formato = new DecimalFormat("#0.##",
                DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return formato.format(volumenTotal);
    }

    // ------------------------------------------------------------------ Descanso

    /** Arranca el descanso configurado en el perfil después de completar una serie. */
    private void iniciarDescanso() {
        entrenamiento.setInstanteFinDescanso(
                SystemClock.elapsedRealtime() + segundosDescansoPredeterminado * 1000L);

        // El fin del descanso también se guarda, para que la cuenta regresiva se pueda
        // restaurar aunque Android cierre el proceso mientras está corriendo.
        guardarDescanso();

        tvTiempoDescanso.setText(
                EntrenamientoEnCurso.formatearTiempo(segundosDescansoPredeterminado));
        layoutDescansoEntrenamiento.setVisibility(View.VISIBLE);

        handler.removeCallbacks(runnableDescanso);
        handler.post(runnableDescanso);
    }

    /**
     * Resta quince segundos al descanso sin dejar que el tiempo sea negativo. Solo cambia
     * el temporizador que está corriendo: la preferencia del perfil no se toca.
     */
    private void restarTiempoDescanso() {
        int segundosRestantes = entrenamiento.getSegundosDescansoRestantes() - AJUSTE_DESCANSO_SEGUNDOS;
        if (segundosRestantes < 0) {
            segundosRestantes = 0;
        }

        aplicarTiempoDescanso(segundosRestantes);
    }

    /** Suma quince segundos al descanso, sin imponer un máximo. */
    private void sumarTiempoDescanso() {
        aplicarTiempoDescanso(
                entrenamiento.getSegundosDescansoRestantes() + AJUSTE_DESCANSO_SEGUNDOS);
    }

    /** Fija el tiempo restante del descanso a partir de ahora. */
    private void aplicarTiempoDescanso(int segundosRestantes) {
        if (segundosRestantes <= 0) {
            detenerDescanso();
            return;
        }

        entrenamiento.setInstanteFinDescanso(
                SystemClock.elapsedRealtime() + segundosRestantes * 1000L);
        guardarDescanso();
        tvTiempoDescanso.setText(EntrenamientoEnCurso.formatearTiempo(segundosRestantes));
    }

    /** Detiene la cuenta regresiva y oculta el panel. */
    private void omitirDescanso() {
        detenerDescanso();
    }

    /** Avanza un segundo la cuenta del descanso y la oculta cuando llega a cero. */
    private void avanzarCuentaDescanso() {
        int segundosRestantes = entrenamiento.getSegundosDescansoRestantes();

        if (segundosRestantes <= 0) {
            detenerDescanso();
            return;
        }

        tvTiempoDescanso.setText(EntrenamientoEnCurso.formatearTiempo(segundosRestantes));
        handler.postDelayed(runnableDescanso, INTERVALO_RELOJ_MS);
    }

    /**
     * Muestra el panel de descanso si todavía queda tiempo, o lo oculta si el descanso
     * terminó mientras el usuario estaba en otra pantalla.
     */
    private void actualizarEstadoDescanso() {
        if (!entrenamiento.hayDescansoActivo()) {
            detenerDescanso();
            return;
        }

        int segundosRestantes = entrenamiento.getSegundosDescansoRestantes();
        tvTiempoDescanso.setText(EntrenamientoEnCurso.formatearTiempo(segundosRestantes));
        layoutDescansoEntrenamiento.setVisibility(View.VISIBLE);

        handler.removeCallbacks(runnableDescanso);
        handler.post(runnableDescanso);
    }

    private void detenerDescanso() {
        handler.removeCallbacks(runnableDescanso);

        // La pantalla llama a este método también cuando no había descanso, por ejemplo al
        // entrar de nuevo a la sesión: solo se guarda cuando de verdad había uno corriendo.
        boolean habiaDescanso = entrenamiento.getInstanteFinDescanso() > 0;

        entrenamiento.detenerDescanso();
        layoutDescansoEntrenamiento.setVisibility(View.GONE);

        if (habiaDescanso) {
            guardarDescanso();
        }
    }

    // ------------------------------------------------------------------ Borrador local

    /**
     * Guarda el estado del descanso en el borrador local. Aquí también se guardarían las notas
     * de la sesión cuando la pantalla tenga un campo para escribirlas.
     */
    private void guardarDescanso() {
        if (borradorResuelto) {
            return;
        }
        borradorRepository.guardarDatosEntrenamiento(entrenamiento);
    }

    /**
     * Espera a que el usuario termine de escribir antes de guardar el peso y las repeticiones,
     * para no escribir en la base de datos por cada tecla.
     *
     * @param ejercicio ejercicio cuyas series cambiaron.
     */
    private void programarGuardadoSeries(EjercicioEntrenamiento ejercicio) {
        if (!ejerciciosPorGuardar.contains(ejercicio)) {
            ejerciciosPorGuardar.add(ejercicio);
        }

        handler.removeCallbacks(runnableGuardarSeries);
        handler.postDelayed(runnableGuardarSeries, ESPERA_GUARDADO_SERIES_MS);
    }

    /** Escribe las series de los ejercicios que estaban esperando. */
    private void guardarSeriesPendientes() {
        if (borradorResuelto || ejerciciosPorGuardar.isEmpty()) {
            return;
        }

        List<EjercicioEntrenamiento> ejercicios = new ArrayList<>(ejerciciosPorGuardar);
        ejerciciosPorGuardar.clear();

        for (EjercicioEntrenamiento ejercicio : ejercicios) {
            borradorRepository.guardarSeries(ejercicio);
        }
    }

    /**
     * Escribe sin esperar lo que estaba pendiente. Se usa al salir de la pantalla para que
     * la última edición del usuario no se pierda al minimizar el entrenamiento.
     */
    private void guardarCambiosPendientes() {
        handler.removeCallbacks(runnableGuardarSeries);
        guardarSeriesPendientes();

        if (!borradorResuelto) {
            borradorRepository.guardarDatosEntrenamiento(entrenamiento);
        }
    }

    // ------------------------------------------------------------------ Ejercicios y series

    @Override
    public void onAgregarSerie(int posicionEjercicio) {
        EjercicioEntrenamiento ejercicio = obtenerEjercicio(posicionEjercicio);
        if (ejercicio == null) {
            return;
        }

        // La serie nueva solo pertenece a ese ejercicio: ningún otro cambia.
        ejercicio.agregarSerie();
        adapter.actualizarEjercicio(posicionEjercicio);
        recalcularResumen();

        if (!borradorResuelto) {
            borradorRepository.guardarSeries(ejercicio);
        }
    }

    @Override
    public void onQuitarEjercicio(int posicionEjercicio) {
        EjercicioEntrenamiento ejercicio = obtenerEjercicio(posicionEjercicio);
        if (ejercicio == null) {
            return;
        }

        confirmarQuitarEjercicio(ejercicio);
    }

    @Override
    public void onEstadoSerieCambiado(int posicionEjercicio, int posicionSerie, boolean completada) {
        recalcularResumen();

        EjercicioEntrenamiento ejercicio = obtenerEjercicio(posicionEjercicio);
        SerieEntrenamiento serie = obtenerSerie(ejercicio, posicionSerie);
        if (serie != null && !borradorResuelto) {
            // Se guardan todas las series del ejercicio porque el check también conserva
            // cambios de peso o repeticiones que el usuario hizo sin pasar por el watcher.
            borradorRepository.guardarSeries(ejercicio);
        }

        // El descanso solo arranca al completar una serie, nunca al desmarcarlo.
        if (completada) {
            iniciarDescanso();
        }
    }

    @Override
    public void onDatosSerieCambiados(EjercicioEntrenamiento ejercicio) {
        recalcularResumen();

        if (!borradorResuelto) {
            programarGuardadoSeries(ejercicio);
        }
    }

    /**
     * @return el ejercicio que está en la posición indicada, o null si la lista ya cambió y
     *         esa posición ya no corresponde a ningún ejercicio.
     */
    private EjercicioEntrenamiento obtenerEjercicio(int posicionEjercicio) {
        List<EjercicioEntrenamiento> ejercicios = entrenamiento.getEjercicios();
        if (posicionEjercicio < 0 || posicionEjercicio >= ejercicios.size()) {
            return null;
        }
        return ejercicios.get(posicionEjercicio);
    }

    /**
     * @return la serie que está en la posición indicada, o null si en ese lugar ya no hay
     *         ninguna serie.
     */
    private SerieEntrenamiento obtenerSerie(EjercicioEntrenamiento ejercicio, int posicionSerie) {
        if (ejercicio == null) {
            return null;
        }

        List<SerieEntrenamiento> series = ejercicio.getSeries();
        if (posicionSerie < 0 || posicionSerie >= series.size()) {
            return null;
        }
        return series.get(posicionSerie);
    }

    // ------------------------------------------------------------------ Diálogos

    /** Pide confirmación antes de quitar un ejercicio y todas sus series. */
    private void confirmarQuitarEjercicio(EjercicioEntrenamiento ejercicio) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloQuitarEjercicio)
                .setMessage(getString(R.string.tvMensajeQuitarEjercicio, ejercicio.getNombre()))
                .setPositiveButton(R.string.btnConfirmarQuitarEjercicio,
                        (dialogo, cual) -> quitarEjercicio(ejercicio))
                .setNegativeButton(R.string.btnCancelarQuitarEjercicio, null)
                .show();
    }

    private void quitarEjercicio(EjercicioEntrenamiento ejercicio) {
        int posicionEjercicio = entrenamiento.getEjercicios().indexOf(ejercicio);
        if (posicionEjercicio < 0) {
            return;
        }

        // Quitar el ejercicio borra también sus series, y con ellas su volumen.
        adapter.quitarEjercicio(posicionEjercicio);
        recalcularResumen();

        if (!borradorResuelto) {
            // En el borrador local el ejercicio se elimina con su clave foránea en cascada,
            // así que sus series desaparecen también.
            borradorRepository.eliminarEjercicio(ejercicio);
        }
    }

    /** Pide confirmación antes de terminar, y solo si hay al menos una serie completada. */
    private void confirmarTerminarEntrenamiento() {
        if (guardandoEntrenamiento) {
            return;
        }

        int seriesCompletadas = contarSeriesCompletadas();
        if (seriesCompletadas == 0) {
            Toast.makeText(requireContext(), R.string.tvMensajeTerminarSinSeries,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String mensaje = getString(R.string.tvMensajeTerminarEntrenamiento,
                entrenamiento.getTiempoTranscurrido(),
                formatearVolumen(sumarVolumen()),
                seriesCompletadas,
                contarSeriesTotales());

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloTerminarEntrenamiento)
                .setMessage(mensaje)
                .setPositiveButton(R.string.btnConfirmarTerminarEntrenamiento,
                        (dialogo, cual) -> terminarEntrenamiento())
                .setNegativeButton(R.string.btnCancelarTerminarEntrenamiento, null)
                .show();
    }

    private void terminarEntrenamiento() {
        EntrenamientoCrearRequest request = crearSolicitudEntrenamiento();

        // Se escribe lo que estaba pendiente antes de enviar la sesión al backend.
        guardarCambiosPendientes();
        detenerActualizacionesVisuales();
        mostrarGuardandoEntrenamiento(true);

        currentCall = entrenamientoRepository.crearEntrenamiento(request);
        currentCall.enqueue(new Callback<EntrenamientoDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<EntrenamientoDetalleResponse> call,
                                   @NonNull Response<EntrenamientoDetalleResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    // El backend ya guardó la sesión, así que el borrador local dejó de hacer
                    // falta y la pantalla no debe volver a escribir en él.
                    borradorResuelto = true;

                    // El identificador lo crea el backend: es el único válido para consultar
                    // o borrar después el entrenamiento guardado.
                    activity.mostrarResumenEntrenamiento(response.body().getId());
                    return;
                }

                restaurarDespuesDeErrorGuardado();
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<EntrenamientoDetalleResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                restaurarDespuesDeErrorGuardado();
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /** Construye el cuerpo requerido por el backend con las series completadas. */
    private EntrenamientoCrearRequest crearSolicitudEntrenamiento() {
        List<RegistroSerieRequest> seriesCompletadas = new ArrayList<>();

        for (EjercicioEntrenamiento ejercicio : entrenamiento.getEjercicios()) {
            List<SerieEntrenamiento> series = ejercicio.getSeries();

            for (int posicionSerie = 0; posicionSerie < series.size(); posicionSerie++) {
                SerieEntrenamiento serie = series.get(posicionSerie);
                if (!serie.isCompletada() || !serie.tieneDatosValidos()) {
                    continue;
                }

                int numeroSerie = serie.getNumeroSerie() > 0
                        ? serie.getNumeroSerie()
                        : posicionSerie + 1;
                seriesCompletadas.add(new RegistroSerieRequest(
                        ejercicio.getRutinaEjercicioId(),
                        numeroSerie,
                        serie.getRepeticiones(),
                        serie.getPeso()));
            }
        }

        long duracionSegundos = entrenamiento.getSegundosTranscurridos();
        int duracionMinutos = (int) Math.max(1, (duracionSegundos + 59) / 60);
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date(entrenamiento.getFechaHoraInicio()));

        return new EntrenamientoCrearRequest(
                entrenamiento.getIdRutina(),
                fecha,
                duracionMinutos,
                entrenamiento.getNotas(),
                contarSeriesTotales(),
                seriesCompletadas);
    }

    /** Bloquea o restaura las acciones mientras se guarda el entrenamiento. */
    private void mostrarGuardandoEntrenamiento(boolean guardando) {
        guardandoEntrenamiento = guardando;
        btnDescartarEntrenamiento.setEnabled(!guardando);

        if (guardando) {
            activity.mostrarToolbarSecundaria(
                    getString(R.string.tvToolbarTituloEntrenamientoActivo),
                    true,
                    getString(R.string.btnTerminarEntrenamiento_loading));
            activity.mostrarControlMinimizarEntrenamiento(this::minimizarEntrenamiento);
            activity.setAccionToolbar(this::confirmarTerminarEntrenamiento);
            activity.habilitarAccionToolbar(false);
        } else {
            configurarToolbar();
        }
    }

    /** Conserva la sesión y permite reintentar cuando la API rechaza el guardado. */
    private void restaurarDespuesDeErrorGuardado() {
        mostrarGuardandoEntrenamiento(false);
        reanudarActualizacionesVisuales();
    }

    /** Pide confirmación antes de descartar la sesión y todo lo registrado en ella. */
    private void confirmarDescartarEntrenamiento() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloDescartarEntrenamiento)
                .setMessage(R.string.tvMensajeDescartarEntrenamiento)
                .setPositiveButton(R.string.btnConfirmarDescartarEntrenamiento,
                        (dialogo, cual) -> descartarEntrenamiento())
                .setNegativeButton(R.string.btnCancelarDescartarEntrenamiento, null)
                .show();
    }

    private void descartarEntrenamiento() {
        // MainActivity borra el borrador local de esta cuenta al cerrar la sesión.
        borradorResuelto = true;
        detenerActualizacionesVisuales();
        activity.cerrarEntrenamientoEnCurso();
    }

    /** El chevron hacia abajo solo minimiza: no termina ni descarta la sesión. */
    private void minimizarEntrenamiento() {
        if (guardandoEntrenamiento) {
            return;
        }

        // Se escribe lo que estaba esperando: si Android cierra el proceso mientras el
        // entrenamiento está minimizado, la sesión se recupera tal como quedó.
        guardarCambiosPendientes();
        detenerActualizacionesVisuales();
        activity.minimizarEntrenamiento();
    }
}
