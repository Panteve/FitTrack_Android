package ue.edu.co.fittrackandroid.entrenamiento;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.ejercicios.EjerciciosFragment;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Pantalla del entrenamiento en curso.
 *
 * <p>Muestra el resumen de la sesión (duración, volumen y series), la lista de ejercicios
 * con sus series y el temporizador de descanso entre series.
 *
 * <p>La sesión completa vive en {@link EntrenamientoEnCurso}, que es propiedad de MainActivity.
 * Por eso el tiempo y los datos se conservan al abrir el selector de ejercicios y al minimizar
 * el entrenamiento, aunque el fragment y sus vistas se destruyan.
 */
public class EntrenamientoActivoFragment extends Fragment
        implements EntrenamientoEjercicioAdapter.EscuchaEntrenamiento  {

    /** Tiempo base de cada descanso entre series: tres minutos. */
    private static final int SEGUNDOS_DESCANSO_BASE = 180;

    /** Pasos de ajuste del temporizador de descanso. */
    private static final int AJUSTE_DESCANSO_SEGUNDOS = 15;

    /** Cada cuánto se refrescan los dos relojes de la pantalla. */
    private static final long INTERVALO_RELOJ_MS = 1000;

    private MainActivity activity;
    private EntrenamientoEnCurso entrenamiento;
    private EntrenamientoEjercicioAdapter adapter;

    private TextView tvDuracionEntrenamiento;
    private TextView tvVolumenEntrenamiento;
    private TextView tvSeriesEntrenamiento;
    private RecyclerView rvEjerciciosEntrenamiento;
    private Button btnAgregarEjercicioEntrenamiento;
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
        // La sesión no se guarda en disco todavía: si el proceso muere, hay que empezar de nuevo.
        // TODO: Recuperar el entrenamiento al recrear el proceso cuando exista la persistencia.
        entrenamiento = activity.obtenerEntrenamientoEnCurso();
        registrarResultadoEjercicio();
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
        // No se restaura la navegación inferior aquí porque onPause también ocurre al abrir
        // el selector de ejercicios, que debe seguir sin barra inferior.
        detenerActualizacionesVisuales();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelarCallbacks();
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
        btnAgregarEjercicioEntrenamiento = view.findViewById(R.id.btnAgregarEjercicioEntrenamiento);
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
        btnAgregarEjercicioEntrenamiento.setOnClickListener(v -> abrirSelectorEjercicios());
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

    /** Escucha el ejercicio que envía EjerciciosFragment y lo agrega a la sesión. */
    private void registrarResultadoEjercicio() {
        getParentFragmentManager().setFragmentResultListener(
                EjerciciosFragment.REQUEST_SELECCION_EJERCICIO,
                this,
                (clave, resultado) -> {
                    String nombreEjercicio = resultado.getString(
                            EjerciciosFragment.RESULT_NOMBRE_EJERCICIO);
                    String grupoMuscular = resultado.getString(
                            EjerciciosFragment.RESULT_GRUPO_MUSCULAR);
                    agregarEjercicioSeleccionado(nombreEjercicio, grupoMuscular);
                });
    }

    /** Vuelve a poner en marcha los relojes después de abrir el selector o volver del minimized. */
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

    private void abrirSelectorEjercicios() {
        activity.mostrarSelectorEjercicios();
    }

    /** Agrega el ejercicio elegido en el selector, con su primera serie. */
    private void agregarEjercicioSeleccionado(String nombreEjercicio, String grupoMuscular) {
        // El resultado solo se procesa con la vista ya creada: las vistas se recrean cada
        // vez que la pantalla vuelve del selector, así que el adapter puede estar vacío.
        if (nombreEjercicio == null || adapter == null) {
            return;
        }

        // Cada ejercicio entra con su primera serie, igual que en la creación de rutinas.
        adapter.agregarEjercicio(new EjercicioEntrenamiento(nombreEjercicio, grupoMuscular));
        recalcularResumen();
    }

    @Override
    public void onAgregarSerie(int posicionEjercicio) {
        // La serie nueva solo pertenece a ese ejercicio: ningún otro cambia.
        entrenamiento.getEjercicios().get(posicionEjercicio).agregarSerie();
        adapter.actualizarEjercicio(posicionEjercicio);
        recalcularResumen();
    }

    @Override
    public void onQuitarEjercicio(int posicionEjercicio) {
        confirmarQuitarEjercicio(entrenamiento.getEjercicios().get(posicionEjercicio));
    }

    @Override
    public void onEstadoSerieCambiado(int posicionEjercicio, int posicionSerie, boolean completada) {
        recalcularResumen();

        // El descanso solo arranca al completar una serie, nunca al desmarcarlo.
        if (completada) {
            iniciarDescanso();
        }
    }

    @Override
    public void onDatosSerieCambiados() {
        recalcularResumen();
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
    }

    /** Pide confirmación antes de terminar, y solo si hay al menos una serie completada. */
    private void confirmarTerminarEntrenamiento() {
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
        // Se copian los datos finales antes de cerrar la sesión, porque después de esto
        // el entrenamiento en curso deja de existir.
        ResumenEntrenamiento resumen = crearResumenFinal();

        detenerActualizacionesVisuales();
        // TODO: Agregar el entrenamiento a los últimos entrenamientos de Inicio y al
        //       historial cuando exista la capa de persistencia.
        activity.mostrarResumenEntrenamiento(resumen);
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
                    seriesResumen.add(new SerieResumen(serie.obtenerPesoNumerico(),
                            serie.obtenerRepeticionesNumericas()));
                }
            }

            if (seriesResumen.isEmpty()) {
                continue;
            }

            ejerciciosResumen.add(new EjercicioResumen(ejercicio.getNombre(),
                    grupoMuscularDe(ejercicio), seriesResumen));
        }

        return new ResumenEntrenamiento(entrenamiento.getNombre(),
                entrenamiento.getFechaHoraInicio(),
                entrenamiento.getSegundosTranscurridos(),
                contarSeriesTotales(),
                ejerciciosResumen);
    }

    /**
     * @return el grupo muscular del ejercicio, o un nombre de respaldo cuando la sesión se
     *         inició desde una rutina que aún no traía ese dato.
     */
    private String grupoMuscularDe(EjercicioEntrenamiento ejercicio) {
        if (ejercicio.getGrupoMuscular() == null || ejercicio.getGrupoMuscular().trim().isEmpty()) {
            return getString(R.string.tvGrupoMuscularResumen_fallback);
        }
        return ejercicio.getGrupoMuscular();
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
        activity.cerrarEntrenamientoEnCurso();
    }

    /** El chevron hacia abajo solo minimiza: no termina ni descarta la sesión. */
    private void minimizarEntrenamiento() {
        detenerActualizacionesVisuales();
        activity.minimizarEntrenamiento();
    }
}
