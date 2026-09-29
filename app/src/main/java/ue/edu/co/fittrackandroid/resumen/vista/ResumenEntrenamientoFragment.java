package ue.edu.co.fittrackandroid.resumen.vista;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.entrenamiento.datos.EntrenamientoRepository;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoDetalleResponse;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoEnCurso;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.RegistroSerieResponse;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.resumen.modelo.EjercicioResumen;
import ue.edu.co.fittrackandroid.resumen.modelo.GrupoMuscularResumen;
import ue.edu.co.fittrackandroid.resumen.modelo.ResumenEntrenamiento;
import ue.edu.co.fittrackandroid.resumen.modelo.SerieResumen;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Pantalla de solo lectura con el resultado de un entrenamiento terminado.
 *
 * <p>Muestra el nombre del entrenamiento, el día y la hora en que se realizó, las tres
 * métricas finales (duración, volumen y series), la distribución porcentual de los
 * grupos musculares trabajados y los ejercicios con sus series realizadas.
 *
 * <p>La pantalla se abre al confirmar "Terminar" y también al pulsar un registro de
 * "Últimos entrenamientos" en Inicio. En el primer caso MainActivity ya tiene el resumen
 * completo y se muestra de una vez. En el segundo solo llega el identificador, así que
 * aquí se consulta el detalle del entrenamiento y se reconstruye el resumen.
 *
 * <p>Al final del resumen aparece "BORRAR ENTRENAMIENTO". Esa acción solo existe sobre un
 * entrenamiento que ya está guardado en el backend, por eso el botón se oculta cuando no
 * hay un identificador válido. No tiene relación con descartar la sesión en curso ni con
 * cerrar la pantalla: el registro que se borra aquí ya no se puede recuperar.
 *
 * <p>No hay ningún temporizador: todos los datos ya son finales.
 */
public class ResumenEntrenamientoFragment extends Fragment {

    /**
     * Clave del resultado que avisa que el entrenamiento fue borrado. La escucha
     * HomeFragment, la pantalla que puede tener abierto el historial, para que vuelva a
     * consultar su información y el registro desaparezca de "Últimos entrenamientos".
     */
    public static final String REQUEST_ENTRENAMIENTO_ELIMINADO = "entrenamientoEliminado";

    private MainActivity activity;
    private ResumenEntrenamiento resumen;
    private Long idEntrenamiento;
    private EntrenamientoRepository entrenamientoRepository;
    private Call<EntrenamientoDetalleResponse> currentCallDetalle;
    private Call<Void> currentCallEliminar;
    private boolean eliminandoEntrenamiento;

    private ProgressBar pbCargaResumenEntrenamiento;
    private TextView tvErrorResumenEntrenamiento;
    private Button btnReintentarResumenEntrenamiento;
    private Button btnBorrarEntrenamiento;
    private View layoutErrorResumenEntrenamiento;
    private ScrollView svResumenEntrenamiento;
    private TextView tvNombreResumenEntrenamiento;
    private TextView tvFechaResumenEntrenamiento;
    private TextView tvDuracionResumenEntrenamiento;
    private TextView tvVolumenResumenEntrenamiento;
    private TextView tvSeriesResumenEntrenamiento;
    private TextView tvResumenSinEjercicios;
    private LinearLayout layoutGruposMuscularesResumen;
    private RecyclerView rvEjerciciosResumen;

    public ResumenEntrenamientoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) requireActivity();
        // El resumen todavía no se guarda en disco: solo existe mientras la app siga viva,
        // o se reconstruye con el detalle que devuelve el backend.
        resumen = activity.obtenerResumenEntrenamientoActual();
        idEntrenamiento = activity.obtenerIdEntrenamientoResumenActual();
        entrenamientoRepository = new EntrenamientoRepository(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen_entrenamiento, container, false);

        inicializarVistas(view);
        btnReintentarResumenEntrenamiento.setOnClickListener(v -> cargarResumenDesdeApi());
        btnBorrarEntrenamiento.setOnClickListener(v -> confirmarBorradoEntrenamiento());

        if (resumen != null) {
            // El resumen ya venía construido: no hace falta ninguna consulta.
            mostrarResumen();
        } else if (idEntrenamiento != null) {
            cargarResumenDesdeApi();
        } else {
            // Sin resumen ni identificador no hay nada que recuperar ni que reintentar.
            mostrarEstadoError();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        configurarToolbar();
    }

    private void inicializarVistas(View view) {
        pbCargaResumenEntrenamiento = view.findViewById(R.id.pbCargaResumenEntrenamiento);
        layoutErrorResumenEntrenamiento = view.findViewById(R.id.layoutErrorResumenEntrenamiento);
        tvErrorResumenEntrenamiento = view.findViewById(R.id.tvErrorResumenEntrenamiento);
        btnReintentarResumenEntrenamiento = view.findViewById(R.id.btnReintentarResumenEntrenamiento);
        btnBorrarEntrenamiento = view.findViewById(R.id.btnBorrarEntrenamiento);
        svResumenEntrenamiento = view.findViewById(R.id.svResumenEntrenamiento);
        tvNombreResumenEntrenamiento = view.findViewById(R.id.tvNombreResumenEntrenamiento);
        tvFechaResumenEntrenamiento = view.findViewById(R.id.tvFechaResumenEntrenamiento);
        tvDuracionResumenEntrenamiento = view.findViewById(R.id.tvDuracionResumenEntrenamiento);
        tvVolumenResumenEntrenamiento = view.findViewById(R.id.tvVolumenResumenEntrenamiento);
        tvSeriesResumenEntrenamiento = view.findViewById(R.id.tvSeriesResumenEntrenamiento);
        tvResumenSinEjercicios = view.findViewById(R.id.tvResumenSinEjercicios);
        layoutGruposMuscularesResumen = view.findViewById(R.id.layoutGruposMuscularesResumen);
        rvEjerciciosResumen = view.findViewById(R.id.rvEjerciciosResumen);
    }

    /**
     * Toolbar de la pantalla: la flecha vuelve a la pantalla anterior y no hay ninguna
     * acción a la derecha. Como el entrenamiento ya terminó, se muestran la navegación
     * inferior y se oculta la isla.
     */
    private void configurarToolbar() {
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloResumenEntrenamiento),
                true,
                null
        );
        activity.mostrarNavegacionInferior();
        activity.ocultarIslaEntrenamiento();
    }

    // ------------------------------------------------------------------ Estados de la pantalla

    /** Deja visible únicamente el indicador mientras se consulta el detalle. */
    private void mostrarEstadoCarga() {
        pbCargaResumenEntrenamiento.setVisibility(View.VISIBLE);
        layoutErrorResumenEntrenamiento.setVisibility(View.GONE);
        svResumenEntrenamiento.setVisibility(View.GONE);
    }

    /** Muestra el mensaje de error y deja el reintento disponible. */
    private void mostrarEstadoError() {
        pbCargaResumenEntrenamiento.setVisibility(View.GONE);
        svResumenEntrenamiento.setVisibility(View.GONE);
        layoutErrorResumenEntrenamiento.setVisibility(View.VISIBLE);
    }

    /** Muestra el resumen completo y esconde los estados de carga y error. */
    private void mostrarResumen() {
        pbCargaResumenEntrenamiento.setVisibility(View.GONE);
        layoutErrorResumenEntrenamiento.setVisibility(View.GONE);
        svResumenEntrenamiento.setVisibility(View.VISIBLE);

        mostrarCabecera();
        mostrarMetricas();
        mostrarGruposMusculares();
        mostrarEjercicios();
        actualizarBotonBorrar();
    }

    // ------------------------------------------------------------------ Borrado del entrenamiento

    /**
     * Solo se puede borrar un entrenamiento que el backend ya guardó, así que el botón
     * aparece únicamente cuando hay un identificador válido. Si por un estado inesperado
     * no lo hay, se oculta: nunca se intenta eliminar con un identificador nulo.
     */
    private void actualizarBotonBorrar() {
        boolean tieneIdValido = idEntrenamiento != null
                && idEntrenamiento > 0;

        btnBorrarEntrenamiento.setVisibility(
                tieneIdValido ? View.VISIBLE : View.GONE
        );
    }

    /**
     * Pide confirmación antes de borrar el entrenamiento guardado. El borrado no se puede
     * deshacer, así que nunca se envía sin preguntar.
     */
    private void confirmarBorradoEntrenamiento() {
        if (eliminandoEntrenamiento || idEntrenamiento == null || idEntrenamiento <= 0) {
            return;
        }

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloConfirmarBorrarEntrenamiento)
                .setMessage(getString(
                        R.string.tvMensajeConfirmarBorrarEntrenamiento,
                        obtenerNombreEntrenamiento()
                ))
                .setPositiveButton(
                        R.string.btnConfirmarBorrarEntrenamiento,
                        (dialogoVisible, cual) -> borrarEntrenamiento()
                )
                .setNegativeButton(
                        R.string.btnCancelarBorrarEntrenamiento,
                        null
                )
                .create();

        dialogo.show();
        // El botón de borrar queda en rojo para que se lea como una acción destructiva.
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.colorErrorText));
    }

    /**
     * @return el nombre del entrenamiento tal como lo muestra la cabecera. Si el nombre
     *         llegó vacío se usa el mismo nombre de respaldo que ella.
     */
    private String obtenerNombreEntrenamiento() {
        if (resumen == null) {
            return getString(R.string.tvNombreResumenEntrenamiento_fallback);
        }

        String nombre = resumen.getNombre();

        if (nombre == null || nombre.trim().isEmpty()) {
            return getString(R.string.tvNombreResumenEntrenamiento_fallback);
        }

        return nombre;
    }

    /**
     * Pide al backend eliminar el entrenamiento con {@code DELETE /entrenamientos/{id}}.
     * La eliminación es lógica: el registro deja de aparecer en el historial y ya no se
     * puede consultar, pero sigue existiendo en la base de datos.
     */
    private void borrarEntrenamiento() {
        // Se revisa el identificador otra vez: el usuario pudo abrir el diálogo, cancelar
        // y volver a intentarlo, y no puede haber dos borrados al mismo tiempo.
        if (eliminandoEntrenamiento || idEntrenamiento == null || idEntrenamiento <= 0) {
            return;
        }

        mostrarEliminando(true);

        currentCallEliminar = entrenamientoRepository
                .eliminarEntrenamiento(idEntrenamiento);

        currentCallEliminar.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                // El endpoint responde 204 sin cuerpo, así que basta con que el código
                // sea correcto: no hay ningún body que revisar.
                if (response.isSuccessful()) {
                    procesarEntrenamientoEliminado();
                    return;
                }

                // El resumen y el identificador se conservan para que el usuario pueda
                // volver a intentarlo.
                mostrarEliminando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                mostrarEliminando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Avisa que el entrenamiento se borró, informa a HomeFragment para que vuelva a
     * consultar su información (donde el registro ya no aparecerá) y regresa a la
     * pantalla anterior. El resumen se olvida de MainActivity para que no quede en
     * memoria un entrenamiento que ya no existe.
     */
    private void procesarEntrenamientoEliminado() {
        Toast.makeText(
                requireContext(),
                R.string.tvEntrenamientoEliminado,
                Toast.LENGTH_SHORT
        ).show();

        getParentFragmentManager().setFragmentResult(
                REQUEST_ENTRENAMIENTO_ELIMINADO,
                new Bundle()
        );

        activity.limpiarResumenEntrenamientoActual();
        activity.regresar();
    }

    /**
     * Muestra u oculta el indicador de proceso. Reutiliza el mismo indicador de carga
     * del detalle: mientras se borra no tiene sentido dejar el resumen en pantalla.
     *
     * @param eliminando true mientras la petición de borrado está en vuelo.
     */
    private void mostrarEliminando(boolean eliminando) {
        eliminandoEntrenamiento = eliminando;
        btnBorrarEntrenamiento.setEnabled(!eliminando);

        pbCargaResumenEntrenamiento.setVisibility(eliminando ? View.VISIBLE : View.GONE);
        layoutErrorResumenEntrenamiento.setVisibility(View.GONE);
        svResumenEntrenamiento.setVisibility(eliminando ? View.GONE : View.VISIBLE);
    }

    // ------------------------------------------------------------------ Consulta del detalle

    /**
     * Consulta el entrenamiento guardado y reconstruye su resumen.
     * Se usa al abrir un registro del historial, que solo trae el identificador.
     */
    private void cargarResumenDesdeApi() {
        if (idEntrenamiento == null) {
            mostrarEstadoError();
            return;
        }

        mostrarEstadoCarga();
        currentCallDetalle = entrenamientoRepository.getEntrenamientoById(idEntrenamiento);

        currentCallDetalle.enqueue(new Callback<EntrenamientoDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<EntrenamientoDetalleResponse> call,
                                   @NonNull Response<EntrenamientoDetalleResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    resumen = convertirAResumen(response.body());
                    mostrarResumen();
                    return;
                }

                mostrarEstadoError();
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<EntrenamientoDetalleResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                mostrarEstadoError();
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Arma el resumen de solo lectura a partir del detalle guardado por el backend.
     *
     * <p>La lista de series ya llega ordenada por {@code ordenEjercicio} y
     * {@code numeroSerie}, así que se recorre en ese orden y se cierra un ejercicio cada
     * vez que cambia el {@code rutinaEjercicioId}. Se agrupa por ese identificador y no por
     * el del ejercicio porque una misma rutina puede repetir un ejercicio en dos bloques
     * distintos.
     */
    private ResumenEntrenamiento convertirAResumen(EntrenamientoDetalleResponse detalle) {
        List<RegistroSerieResponse> series = detalle.getSeries() == null
                ? new ArrayList<>()
                : detalle.getSeries();
        List<EjercicioResumen> ejercicios = new ArrayList<>();
        agruparSeriesEnEjercicios(series, ejercicios);

        // Los entrenamientos antiguos no tienen total guardado: se usa lo que llegó.
        int seriesTotales = detalle.getSeriesTotales() != null
                ? detalle.getSeriesTotales()
                : series.size();

        int duracionMinutos = detalle.getDuracionMinutos() == null
                ? 0
                : detalle.getDuracionMinutos();
        long duracionSegundos = duracionMinutos * 60L;

        // El backend solo conserva el día del entrenamiento, no la hora en que se hizo.
        return new ResumenEntrenamiento(
                detalle.getNombreRutina(),
                convertirFecha(detalle.getFecha()),
                duracionSegundos,
                seriesTotales,
                false,
                ejercicios);
    }

    /**
     * Recorre las series en el orden recibido y crea un ejercicio por cada bloque.
     *
     * <p>Un bloque termina cuando cambia el {@code rutinaEjercicioId}, porque una misma
     * rutina puede repetir un ejercicio en dos lugares distintos y hay que mostrarlos por
     * separado. Al cambiar se cierra el ejercicio con las series acumuladas y se empieza
     * el siguiente.
     */
    private void agruparSeriesEnEjercicios(List<RegistroSerieResponse> series,
                                           List<EjercicioResumen> ejercicios) {
        List<SerieResumen> seriesDelEjercicio = new ArrayList<>();
        RegistroSerieResponse primeraSerie = null;
        Long rutinaEjercicioActual = null;

        for (RegistroSerieResponse serie : series) {
            boolean empiezaNuevoEjercicio = primeraSerie != null
                    && !Objects.equals(rutinaEjercicioActual, serie.getRutinaEjercicioId());

            if (empiezaNuevoEjercicio) {
                ejercicios.add(crearEjercicioResumen(primeraSerie, seriesDelEjercicio));
                seriesDelEjercicio = new ArrayList<>();
                primeraSerie = null;
            }

            if (primeraSerie == null) {
                primeraSerie = serie;
                rutinaEjercicioActual = serie.getRutinaEjercicioId();
            }

            double peso = serie.getPeso() == null ? 0 : serie.getPeso();
            int repeticiones = serie.getRepeticiones() == null ? 0 : serie.getRepeticiones();
            seriesDelEjercicio.add(new SerieResumen(peso, repeticiones));
        }

        if (primeraSerie != null) {
            ejercicios.add(crearEjercicioResumen(primeraSerie, seriesDelEjercicio));
        }
    }

    /** Crea un ejercicio del resumen con el nombre y el grupo muscular de su primera serie. */
    private EjercicioResumen crearEjercicioResumen(RegistroSerieResponse primeraSerie,
                                                   List<SerieResumen> series) {
        return new EjercicioResumen(
                primeraSerie.getNombreEjercicio(),
                obtenerGrupoMuscular(primeraSerie.getGrupoMuscular()),
                series);
    }

    /** @return el grupo muscular recibido, o el de respaldo si el backend no lo envió. */
    private String obtenerGrupoMuscular(String grupoMuscular) {
        if (grupoMuscular == null || grupoMuscular.trim().isEmpty()) {
            return getString(R.string.tvGrupoMuscularResumen_fallback);
        }

        return grupoMuscular;
    }

    /**
     * Convierte la fecha "yyyy-MM-dd" del backend a milisegundos para poder reutilizar el
     * formateo de la cabecera. La hora de ese valor no se muestra: el resumen reconstruido
     * no la conoce.
     */
    private long convertirFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return System.currentTimeMillis();
        }

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        try {
            return formato.parse(fecha).getTime();
        } catch (ParseException error) {
            // Si el backend no envía una fecha legible se usa el momento actual como respaldo.
            return System.currentTimeMillis();
        }
    }

    // ------------------------------------------------------------------ Cabecera

    /** Muestra el nombre del entrenamiento y el día y la hora reales en que se hizo. */
    private void mostrarCabecera() {
        String nombre = resumen.getNombre();

        // Si la sesión se inició sin nombre de rutina se usa un nombre de respaldo.
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = getString(R.string.tvNombreResumenEntrenamiento_fallback);
        }

        tvNombreResumenEntrenamiento.setText(nombre);

        if (resumen.tieneHoraExacta()) {
            tvFechaResumenEntrenamiento.setText(getString(R.string.tvFechaResumenEntrenamiento,
                    formatearFechaResumen(), formatearHoraResumen()));
            return;
        }

        // Del historial solo se conoce el día, así que no se inventa una hora.
        tvFechaResumenEntrenamiento.setText(
                getString(R.string.tvFechaResumenEntrenamiento_sin_hora, formatearFechaResumen()));
    }

    /** @return el día del entrenamiento, por ejemplo "Sábado, 26 de septiembre". */
    private String formatearFechaResumen() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM", new Locale("es", "CO"));
        String fecha = formato.format(new Date(resumen.getFechaHoraInicio()));
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }

    /** @return la hora del entrenamiento, por ejemplo "6:30 p. m.". */
    private String formatearHoraResumen() {
        SimpleDateFormat formato = new SimpleDateFormat("h:mm a", new Locale("es", "CO"));
        return formato.format(new Date(resumen.getFechaHoraInicio()));
    }

    // ------------------------------------------------------------------ Métricas

    /** Muestra la duración, el volumen y las series completadas sobre las totales. */
    private void mostrarMetricas() {
        tvDuracionResumenEntrenamiento.setText(
                EntrenamientoEnCurso.formatearTiempo(resumen.getDuracionSegundos()));

        tvVolumenResumenEntrenamiento.setText(getString(R.string.tvVolumenResumenEntrenamiento,
                formatearVolumen(resumen.calcularVolumenTotal())));

        tvSeriesResumenEntrenamiento.setText(getString(R.string.tvSeriesResumenEntrenamiento,
                resumen.contarSeriesCompletadas(), resumen.getSeriesTotales()));
    }

    /** Muestra el volumen sin decimales cuando no hacen falta: 600 kg, 22.5 kg. */
    private String formatearVolumen(double volumenTotal) {
        DecimalFormat formato = new DecimalFormat("#0.##",
                DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return formato.format(volumenTotal);
    }

    // ------------------------------------------------------------------ Grupos musculares

    /**
     * Muestra una fila por cada grupo muscular trabajado, de mayor a menor porcentaje.
     * Las filas se inflan aquí porque siempre son pocas.
     */
    private void mostrarGruposMusculares() {
        layoutGruposMuscularesResumen.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        List<GrupoMuscularResumen> grupos = resumen.calcularGruposMusculares();

        for (GrupoMuscularResumen grupo : grupos) {
            View filaGrupo = inflater.inflate(R.layout.item_grupo_muscular_resumen,
                    layoutGruposMuscularesResumen, false);

            TextView tvNombreGrupo = filaGrupo.findViewById(R.id.tvNombreGrupoMuscularResumen);
            TextView tvPorcentajeGrupo = filaGrupo.findViewById(R.id.tvPorcentajeGrupoMuscularResumen);
            ProgressBar pbGrupo = filaGrupo.findViewById(R.id.pbGrupoMuscularResumen);

            tvNombreGrupo.setText(grupo.getNombre());
            tvPorcentajeGrupo.setText(getString(R.string.tvPorcentajeGrupoMuscularResumen,
                    grupo.getPorcentaje()));
            pbGrupo.setProgress(grupo.getPorcentaje());

            layoutGruposMuscularesResumen.addView(filaGrupo);
        }
    }

    // ------------------------------------------------------------------ Ejercicios

    /** Muestra los ejercicios realizados y ajusta el mensaje cuando no hay ninguno. */
    private void mostrarEjercicios() {
        List<EjercicioResumen> ejercicios = resumen.getEjercicios();
        boolean hayEjercicios = !ejercicios.isEmpty();

        rvEjerciciosResumen.setVisibility(hayEjercicios ? View.VISIBLE : View.GONE);
        tvResumenSinEjercicios.setVisibility(hayEjercicios ? View.GONE : View.VISIBLE);

        if (!hayEjercicios) {
            return;
        }

        rvEjerciciosResumen.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosResumen.setAdapter(new ResumenEjercicioAdapter(ejercicios));
    }

    @Override
    public void onDestroyView() {
        if (currentCallDetalle != null) {
            currentCallDetalle.cancel();
        }

        if (currentCallEliminar != null) {
            currentCallEliminar.cancel();
        }

        // Al cancelar las llamadas ya no queda ninguna petición en vuelo, así que el
        // bloqueo puede desaparecer si las vistas se recrean. El resumen y el
        // identificador NO se limpian aquí: solo se olvidan cuando el borrado se
        // confirma, porque destruye la vista también al girar el dispositivo.
        eliminandoEntrenamiento = false;
        super.onDestroyView();
    }
}
