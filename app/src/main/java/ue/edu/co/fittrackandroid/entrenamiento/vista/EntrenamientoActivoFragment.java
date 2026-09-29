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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.entrenamiento.datos.EntrenamientoRepository;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EjercicioEntrenamiento;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoCrearRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoDetalleResponse;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoEnCurso;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.RegistroSerieRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.SerieEntrenamiento;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.resumen.modelo.EjercicioResumen;
import ue.edu.co.fittrackandroid.resumen.modelo.ResumenEntrenamiento;
import ue.edu.co.fittrackandroid.resumen.modelo.SerieResumen;
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

    /** Tiempo base de cada descanso entre series: tres minutos. */
    // TODO: Obtener esta duración desde la configuración del usuario o de la rutina,
    // en lugar de usar siempre un valor fijo para todos los ejercicios.
    private static final int SEGUNDOS_DESCANSO_BASE = 180;

    /** Pasos de ajuste del temporizador de descanso. */
    private static final int AJUSTE_DESCANSO_SEGUNDOS = 15;

    /** Cada cuánto se refrescan los dos relojes de la pantalla. */
    private static final long INTERVALO_RELOJ_MS = 1000;

    private MainActivity activity;
    private EntrenamientoEnCurso entrenamiento;
    private EntrenamientoEjercicioAdapter adapter;
    private EntrenamientoRepository entrenamientoRepository;
    private Call<EntrenamientoDetalleResponse> currentCall;
    private boolean guardandoEntrenamiento;

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

    public EntrenamientoActivoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) requireActivity();
        // TODO: Recuperar el entrenamiento activo desde el almacenamiento si MainActivity no
        // conserva la referencia, incluyendo ejercicios, series, descanso y tiempo transcurrido.
        entrenamiento = activity.obtenerEntrenamientoEnCurso();
        entrenamientoRepository = new EntrenamientoRepository(requireContext());
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
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        guardandoEntrenamiento = false;
        cancelarCallbacks();
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

    /** Arranca el descanso de tres minutos después de completar una serie. */
    private void iniciarDescanso() {
        entrenamiento.setInstanteFinDescanso(
                SystemClock.elapsedRealtime() + SEGUNDOS_DESCANSO_BASE * 1000L);

        // TODO: Persistir el instante de finalización del descanso para poder restaurar la
        // cuenta regresiva si Android cierra el proceso mientras está activa.

        tvTiempoDescanso.setText(EntrenamientoEnCurso.formatearTiempo(SEGUNDOS_DESCANSO_BASE));
        layoutDescansoEntrenamiento.setVisibility(View.VISIBLE);

        handler.removeCallbacks(runnableDescanso);
        handler.post(runnableDescanso);
    }

    /** Resta quince segundos al descanso sin dejar que el tiempo sea negativo. */
    private void restarTiempoDescanso() {
        int segundosRestantes = entrenamiento.getSegundosDescansoRestantes() - AJUSTE_DESCANSO_SEGUNDOS;
        if (segundosRestantes < 0) {
            segundosRestantes = 0;
        }

        aplicarTiempoDescanso(segundosRestantes);
    }

    /** Suma quince segundos al descanso, sin imponer un máximo. */
    private void sumarTiempoDescanso() {
        aplicarTiempoDescanso(entrenamiento.getSegundosDescansoRestantes() + AJUSTE_DESCANSO_SEGUNDOS);
    }

    /** Fija el tiempo restante del descanso a partir de ahora. */
    private void aplicarTiempoDescanso(int segundosRestantes) {
        if (segundosRestantes <= 0) {
            detenerDescanso();
            return;
        }

        entrenamiento.setInstanteFinDescanso(
                SystemClock.elapsedRealtime() + segundosRestantes * 1000L);
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
        entrenamiento.detenerDescanso();
        layoutDescansoEntrenamiento.setVisibility(View.GONE);
    }

    // ------------------------------------------------------------------ Ejercicios y series

    @Override
    public void onAgregarSerie(int posicionEjercicio) {
        // La serie nueva solo pertenece a ese ejercicio: ningún otro cambia.
        entrenamiento.getEjercicios().get(posicionEjercicio).agregarSerie();
        adapter.actualizarEjercicio(posicionEjercicio);
        recalcularResumen();

        // TODO: Persistir el cambio en el borrador del entrenamiento activo.
    }

    @Override
    public void onQuitarEjercicio(int posicionEjercicio) {
        confirmarQuitarEjercicio(entrenamiento.getEjercicios().get(posicionEjercicio));
    }

    @Override
    public void onEstadoSerieCambiado(int posicionEjercicio, int posicionSerie, boolean completada) {
        recalcularResumen();

        // TODO: Persistir el nuevo estado de la serie para recuperarlo si se cierra el proceso.

        // El descanso solo arranca al completar una serie, nunca al desmarcarlo.
        if (completada) {
            iniciarDescanso();
        }
    }

    @Override
    public void onDatosSerieCambiados() {
        recalcularResumen();

        // TODO: Guardar los cambios de peso y repeticiones con una espera corta para no
        // escribir en el almacenamiento por cada tecla pulsada.
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

        // TODO: Eliminar también el ejercicio del borrador persistido de la sesión.
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
        ResumenEntrenamiento resumen = crearResumenFinal();
        EntrenamientoCrearRequest request = crearSolicitudEntrenamiento();

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
                    activity.mostrarResumenEntrenamiento(resumen);
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

    /**
     * Arma la copia de solo lectura del entrenamiento terminado.
     *
     * <p>Solo se copian las series completadas y con valores válidos, y los ejercicios que
     * se quedan sin ninguna serie completada no aparecen en el resumen. El resultado no
     * guarda ninguna referencia a la sesión en curso, así que puede mostrarse aunque la
     * sesión ya haya sido borrada.
     */
    private ResumenEntrenamiento crearResumenFinal() {
        List<EjercicioResumen> ejerciciosResumen = new ArrayList<>();

        for (EjercicioEntrenamiento ejercicio : entrenamiento.getEjercicios()) {
            List<SerieResumen> seriesResumen = new ArrayList<>();

            for (SerieEntrenamiento serie : ejercicio.getSeries()) {
                if (serie.isCompletada() && serie.tieneDatosValidos()) {
                    seriesResumen.add(new SerieResumen(serie.getPeso(),
                            serie.getRepeticiones()));
                }
            }

            if (seriesResumen.isEmpty()) {
                continue;
            }

            String grupoMuscular = ejercicio.getGrupoMuscular();
            if (grupoMuscular == null || grupoMuscular.isBlank()) {
                grupoMuscular = getString(R.string.tvGrupoMuscularResumen_fallback);
            }

            ejerciciosResumen.add(new EjercicioResumen(
                    ejercicio.getNombre(),
                    grupoMuscular,
                    seriesResumen));
        }

        return new ResumenEntrenamiento(entrenamiento.getNombreRutina(),
                entrenamiento.getFechaHoraInicio(),
                entrenamiento.getSegundosTranscurridos(),
                contarSeriesTotales(),
                true,
                ejerciciosResumen);
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
        detenerActualizacionesVisuales();

        // TODO: Eliminar el borrador persistido del entrenamiento al confirmar el descarte.
        activity.cerrarEntrenamientoEnCurso();
    }

    /** El chevron hacia abajo solo minimiza: no termina ni descarta la sesión. */
    private void minimizarEntrenamiento() {
        if (guardandoEntrenamiento) {
            return;
        }

        detenerActualizacionesVisuales();

        // TODO: Confirmar que el estado más reciente quedó persistido antes de abandonar
        // la pantalla, para que minimizar no dependa solo de la memoria de MainActivity.
        activity.minimizarEntrenamiento();
    }
}
