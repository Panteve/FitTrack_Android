package ue.edu.co.fittrackandroid.rutinas.vista;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.ejercicios.vista.EjerciciosFragment;
import ue.edu.co.fittrackandroid.hoy.modelo.EjercisioEnRutina;
import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.rutinas.datos.RutinaRepository;
import ue.edu.co.fittrackandroid.rutinas.modelo.DiaSemana;
import ue.edu.co.fittrackandroid.rutinas.modelo.EjercicioRutinaEditable;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaEjercicioCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaSerieCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaSerieResponse;
import ue.edu.co.fittrackandroid.rutinas.modelo.SerieRutina;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para consultar, modificar y eliminar una rutina que ya existe.
 * La pantalla se ve igual que Crear rutina, pero abre con el nombre, el día, los ejercicios
 * y las series que ya estaban guardados. Los cambios se envían con {@code PUT /rutinas/{id}}
 * y el borrado con {@code DELETE /rutinas/{id}}, que es lógico: la rutina deja de aparecer
 * en la lista pero no se borra de la base de datos.
 * Cuando el backend confirma cualquiera de las dos acciones avisa a RutinasFragment y regresa.
 */
public class ModificarRutinaFragment extends Fragment
        implements CrearRutinaEjercicioAdapter.EscuchaCrearRutina {

    /**
     * Clave del resultado que avisa que la rutina se modificó o se borró. La escucha
     * RutinasFragment, que es la pantalla que abrió esta, para volver a consultar sus rutinas.
     */
    public static final String REQUEST_RUTINA_MODIFICADA = "rutinaModificada";

    /** Clave de los argumentos con el identificador de la rutina que se está modificando. */
    private static final String ARG_RUTINA_ID = "rutinaId";

    /** El backend no admite nombres de más de 100 caracteres. */
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    /** Posición de la opción inicial del Spinner: todavía no es un día válido. */
    private static final int POSICION_SIN_DIA_SELECCIONADO = 0;

    private EditText etNombreRutina;
    private TextView tvErrorNombreRutina;
    private Spinner spDiaRutina;
    private TextView tvErrorDiaRutina;
    private View layoutRutinaSinEjercicios;
    private View layoutRutinaConEjercicios;
    private RecyclerView rvEjerciciosRutina;
    private Button btnAgregarPrimerEjercicio;
    private Button btnAgregarEjercicio;
    private Button btnBorrarRutina;
    private Button btnReintentarModificarRutina;
    private View svModificarRutina;
    private View layoutErrorModificarRutina;
    private View layoutCargaModificarRutina;

    // Datos de la rutina que se está modificando.
    private Long rutinaId;
    private String descripcionRutina;
    private String nombreRutina = "";

    /**
     * Posición del día en el desplegable. Se guarda aparte porque al abrir el selector de
     * ejercicios las vistas se recrean y el Spinner vuelve a la primera opción.
     */
    private int posicionDiaSeleccionado = POSICION_SIN_DIA_SELECCIONADO;

    private final List<EjercicioRutinaEditable> listaEjercicios = new ArrayList<>();

    private RutinaRepository rutinaRepository;
    private CrearRutinaEjercicioAdapter adapter;

    private Call<RutinaResponse> currentCallDetalle;
    private Call<RutinaResponse> currentCallActualizar;
    private Call<Void> currentCallEliminar;

    /** Indica que el detalle ya llegó y el formulario se puede modificar y guardar. */
    private boolean rutinaCargada;

    /** Evita mandar una segunda petición mientras la primera sigue en vuelo. */
    private boolean procesandoPeticion;

    public ModificarRutinaFragment() {
        // Required empty public constructor
    }

    /**
     * Crea la pantalla para una rutina concreta. El identificador viaja en los argumentos
     * porque el Fragment lo reconstruye Android cuando rota el dispositivo.
     */
    public static ModificarRutinaFragment newInstance(Long rutinaId) {
        ModificarRutinaFragment fragment = new ModificarRutinaFragment();
        Bundle argumentos = new Bundle();
        argumentos.putLong(ARG_RUTINA_ID, rutinaId);
        fragment.setArguments(argumentos);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarResultadoEjercicio();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modificar_rutina, container, false);

        inicializarVistas(view);

        rutinaRepository = new RutinaRepository(requireContext());
        leerIdentificadorRutina();

        configurarRecyclerView();
        configurarAcciones();
        restaurarNombreEnVista();
        restaurarDiaEnVista();

        // El detalle se consulta una sola vez. Al volver del selector de ejercicios las
        // vistas se recrean, pero la rutina ya está cargada y no se vuelve a pedir, porque
        // esa respuesta borraría los cambios que el usuario todavía no ha guardado.
        if (rutinaCargada) {
            mostrarEstadoContenido();
        } else {
            cargarRutina();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // La toolbar se reconfigura aquí porque al volver del selector de ejercicios
        // sigue mostrando la pantalla anterior.
        configurarToolbar(procesandoPeticion);
    }

    @Override
    public void onDestroyView() {
        // Cualquiera de las tres peticiones puede seguir en vuelo si el usuario se va.
        if (currentCallDetalle != null) {
            currentCallDetalle.cancel();
        }
        if (currentCallActualizar != null) {
            currentCallActualizar.cancel();
        }
        if (currentCallEliminar != null) {
            currentCallEliminar.cancel();
        }

        // Al cancelar las llamadas ya no queda ninguna petición en vuelo, así que la capa
        // de proceso puede volver a mostrarse si las vistas se recrean.
        procesandoPeticion = false;
        super.onDestroyView();
    }

    // ------------------------------------------------------------------ Preparación de la pantalla

    /** Busca las vistas de la pantalla y las guarda en los campos. */
    private void inicializarVistas(View view) {
        etNombreRutina = view.findViewById(R.id.etNombreRutina);
        tvErrorNombreRutina = view.findViewById(R.id.tvErrorNombreRutina);
        spDiaRutina = view.findViewById(R.id.spDiaRutina);
        tvErrorDiaRutina = view.findViewById(R.id.tvErrorDiaRutina);
        layoutRutinaSinEjercicios = view.findViewById(R.id.layoutRutinaSinEjercicios);
        layoutRutinaConEjercicios = view.findViewById(R.id.layoutRutinaConEjercicios);
        rvEjerciciosRutina = view.findViewById(R.id.rvEjerciciosRutina);
        btnAgregarPrimerEjercicio = view.findViewById(R.id.btnAgregarPrimerEjercicio);
        btnAgregarEjercicio = view.findViewById(R.id.btnAgregarEjercicio);
        btnBorrarRutina = view.findViewById(R.id.btnBorrarRutina);
        btnReintentarModificarRutina = view.findViewById(R.id.btnReintentarModificarRutina);
        svModificarRutina = view.findViewById(R.id.svModificarRutina);
        layoutErrorModificarRutina = view.findViewById(R.id.layoutErrorModificarRutina);
        layoutCargaModificarRutina = view.findViewById(R.id.layoutCargaModificarRutina);
    }

    /** Crea el adapter de ejercicios y lo conecta con la lista vertical de la pantalla. */
    private void configurarRecyclerView() {
        adapter = new CrearRutinaEjercicioAdapter(listaEjercicios, this);
        rvEjerciciosRutina.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosRutina.setAdapter(adapter);
    }

    /** Conecta los botones de la pantalla, incluido el de borrar y el de reintentar la consulta. */
    private void configurarAcciones() {
        btnAgregarPrimerEjercicio.setOnClickListener(v -> abrirSelectorEjercicios());
        btnAgregarEjercicio.setOnClickListener(v -> abrirSelectorEjercicios());
        btnBorrarRutina.setOnClickListener(v -> confirmarBorrado());
        btnReintentarModificarRutina.setOnClickListener(v -> cargarRutina());
        limpiarErrorNombreAlEscribir();
        configurarDiaRutina();
    }

    /** Lee de los argumentos el identificador de la rutina abierta desde la lista. */
    private void leerIdentificadorRutina() {
        Bundle argumentos = getArguments();

        if (argumentos == null || !argumentos.containsKey(ARG_RUTINA_ID)) {
            rutinaId = null;
            return;
        }

        rutinaId = argumentos.getLong(ARG_RUTINA_ID);
    }

    /** Escucha el ejercicio que envía EjerciciosFragment y lo agrega a la rutina. */
    private void registrarResultadoEjercicio() {
        getParentFragmentManager().setFragmentResultListener(
                EjerciciosFragment.REQUEST_SELECCION_EJERCICIO,
                this,
                (clave, resultado) -> {
                    Long idEjercicio = resultado.getLong(
                            EjerciciosFragment.RESULT_ID_EJERCICIO);
                    String nombreEjercicio = resultado.getString(
                            EjerciciosFragment.RESULT_NOMBRE_EJERCICIO);
                    String grupoMuscular = resultado.getString(
                            EjerciciosFragment.RESULT_GRUPO_MUSCULAR);

                    agregarEjercicioSeleccionado(idEjercicio, nombreEjercicio, grupoMuscular);
                });
    }

    /** Abre la pantalla de ejercicios para que el usuario elija cuál agregar a la rutina. */
    private void abrirSelectorEjercicios() {
        ((MainActivity) requireActivity()).mostrarSelectorEjercicios();
    }

    /**
     * Agrega el ejercicio elegido en el selector. Se conservan los ejercicios que ya estaban
     * cargados: el nuevo entra al final y con su primera serie, igual que en Crear rutina.
     * El mismo ejercicio se puede agregar varias veces, por ejemplo con series diferentes.
     */
    private void agregarEjercicioSeleccionado(Long idEjercicio, String nombreEjercicio,
                                              String grupoMuscular) {
        // TODO: Mostrar un error si el ejercicio fue eliminado antes de agregarlo a la rutina.
        // Sin la rutina cargada no se puede agregar nada: la respuesta del backend llenaría
        // el formulario de nuevo y se perdería lo que el usuario ya eligió.
        if (nombreEjercicio == null || adapter == null || !rutinaCargada) {
            return;
        }

        adapter.agregarEjercicio(new EjercicioRutinaEditable(idEjercicio, nombreEjercicio, grupoMuscular));
        actualizarEstadoPantalla();
    }

    @Override
    public void onAgregarSerie(int posicionEjercicio) {
        EjercicioRutinaEditable ejercicio = obtenerEjercicio(posicionEjercicio);

        if (ejercicio != null) {
            ejercicio.agregarSerie();
            adapter.actualizarEjercicio(posicionEjercicio);
        }
    }

    @Override
    public void onQuitarSerie(int posicionEjercicio, int posicionSerie) {
        EjercicioRutinaEditable ejercicio = obtenerEjercicio(posicionEjercicio);

        // La última serie no se quita: el modelo lo impide porque el backend exige que
        // cada ejercicio tenga al menos una.
        if (ejercicio != null && ejercicio.eliminarSerie(posicionSerie)) {
            adapter.actualizarEjercicio(posicionEjercicio);
        }
    }

    @Override
    public void onQuitarEjercicio(int posicionEjercicio) {
        EjercicioRutinaEditable ejercicio = obtenerEjercicio(posicionEjercicio);

        if (ejercicio != null) {
            confirmarQuitarEjercicio(ejercicio);
        }
    }

    /**
     * Pide confirmación antes de quitar un ejercicio con todas sus series. El cambio queda
     * solo en el formulario: el backend se entera cuando el usuario pulsa GUARDAR, así que
     * si sale de la pantalla sin guardar la rutina sigue como estaba.
     */
    private void confirmarQuitarEjercicio(EjercicioRutinaEditable ejercicio) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloQuitarEjercicioRutina)
                .setMessage(getString(R.string.tvMensajeQuitarEjercicioRutina, ejercicio.getNombre()))
                .setPositiveButton(R.string.btnConfirmarQuitarEjercicioRutina,
                        (dialogo, cual) -> quitarEjercicio(ejercicio))
                .setNegativeButton(R.string.btnCancelarQuitarEjercicioRutina, null)
                .show();
    }

    /** Quita la tarjeta del ejercicio del formulario y vuelve a decidir qué estado mostrar. */
    private void quitarEjercicio(EjercicioRutinaEditable ejercicio) {
        int posicionEjercicio = listaEjercicios.indexOf(ejercicio);

        if (posicionEjercicio < 0) {
            return;
        }

        adapter.quitarEjercicio(posicionEjercicio);
        actualizarEstadoPantalla();
    }

    /**
     * Busca un ejercicio por su posición. Devuelve null si la posición ya no corresponde
     * a ningún ejercicio, por ejemplo cuando la tarjeta se recycló al quitar otra.
     */
    private EjercicioRutinaEditable obtenerEjercicio(int posicionEjercicio) {
        if (posicionEjercicio < 0 || posicionEjercicio >= listaEjercicios.size()) {
            return null;
        }

        return listaEjercicios.get(posicionEjercicio);
    }

    /** Alterna entre el estado vacío y el estado con ejercicios. */
    private void actualizarEstadoPantalla() {
        boolean hayEjercicios = !listaEjercicios.isEmpty();
        layoutRutinaSinEjercicios.setVisibility(hayEjercicios ? View.GONE : View.VISIBLE);
        layoutRutinaConEjercicios.setVisibility(hayEjercicios ? View.VISIBLE : View.GONE);
    }

    /** Vuelve a escribir el nombre de la rutina cuando la vista se recreó y el campo quedó vacío. */
    private void restaurarNombreEnVista() {
        if (etNombreRutina.getText().length() == 0 && !nombreRutina.isEmpty()) {
            etNombreRutina.setText(nombreRutina);
        }
    }

    /** Vuelve a marcar el día que la rutina ya tenía o el que eligió el usuario. */
    private void restaurarDiaEnVista() {
        if (posicionDiaSeleccionado != POSICION_SIN_DIA_SELECCIONADO) {
            spDiaRutina.setSelection(posicionDiaSeleccionado);
        }
    }

    /**
     * Guarda el día elegido y oculta el error del desplegable.
     * La opción inicial también se guarda, porque es la que vale cuando el usuario
     * todavía no ha escogido nada.
     */
    private void configurarDiaRutina() {
        spDiaRutina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                posicionDiaSeleccionado = posicion;
                tvErrorDiaRutina.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {
                // No se usa: el Spinner siempre entrega la opción inicial.
            }
        });
    }

    /** Guarda lo que el usuario escribe en el nombre para no perderlo y oculta el error del campo. */
    private void limpiarErrorNombreAlEscribir() {
        etNombreRutina.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int cantidad, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int cantidad) {
            }

            @Override
            public void afterTextChanged(Editable texto) {
                nombreRutina = texto.toString();
                tvErrorNombreRutina.setVisibility(View.GONE);
                etNombreRutina.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }

    // ------------------------------------------------------------------ Estados de la pantalla

    /** Deja visible únicamente el indicador mientras se consulta el detalle de la rutina. */
    private void mostrarEstadoCargaInicial() {
        svModificarRutina.setVisibility(View.GONE);
        layoutErrorModificarRutina.setVisibility(View.GONE);
        layoutCargaModificarRutina.setVisibility(View.VISIBLE);
        configurarToolbar(procesandoPeticion);
    }

    /** Muestra el formulario con la rutina ya cargada. */
    private void mostrarEstadoContenido() {
        layoutCargaModificarRutina.setVisibility(View.GONE);
        layoutErrorModificarRutina.setVisibility(View.GONE);
        svModificarRutina.setVisibility(View.VISIBLE);
        actualizarEstadoPantalla();
        configurarToolbar(procesandoPeticion);
    }

    /** Muestra el mensaje de error y deja el reintento disponible. */
    private void mostrarEstadoError() {
        layoutCargaModificarRutina.setVisibility(View.GONE);
        svModificarRutina.setVisibility(View.GONE);
        layoutErrorModificarRutina.setVisibility(View.VISIBLE);
        configurarToolbar(procesandoPeticion);
    }

    /**
     * Coloca la toolbar de la pantalla: título, flecha de volver y la acción de guardar.
     * Guardar queda deshabilitado mientras la rutina no está cargada o hay una petición en
     * vuelo, para no mandar datos a medias.
     */
    private void configurarToolbar(boolean procesando) {
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloModificarRutina),
                true,
                getString(procesando ? R.string.btnGuardar_loading : R.string.btnGuardarCambiosRutina)
        );
        activity.setAccionToolbar(this::guardarCambios);
        activity.habilitarAccionToolbar(rutinaCargada && !procesando);
    }

    // ------------------------------------------------------------------ Consulta del detalle

    /**
     * Consulta la rutina por su identificador y llena el formulario con lo que devuelve
     * la API.
     */
    private void cargarRutina() {
        mostrarEstadoCargaInicial();

        if (rutinaId == null) {
            mostrarEstadoError();
            return;
        }

        // Una consulta anterior puede seguir en vuelo si se pulsa el reintento.
        if (currentCallDetalle != null) {
            currentCallDetalle.cancel();
        }

        currentCallDetalle = rutinaRepository.getRutinaById(rutinaId);
        currentCallDetalle.enqueue(new Callback<RutinaResponse>() {

            @Override
            public void onResponse(@NonNull Call<RutinaResponse> call,
                                   @NonNull Response<RutinaResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    cargarDatosEnFormulario(response.body());
                    mostrarEstadoContenido();
                    return;
                }

                mostrarEstadoError();
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<RutinaResponse> call,
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
     * Escribe en el formulario lo que tiene la rutina guardada: nombre, día, ejercicios y
     * series. El orden es el mismo que envía el backend.
     */
    private void cargarDatosEnFormulario(RutinaResponse rutina) {
        nombreRutina = rutina.getNombre();
        etNombreRutina.setText(nombreRutina);

        // La pantalla no muestra la descripción, pero se guarda y se reenvía al guardar:
        // mandarla en null borraría la que el usuario ya tenía escrita.
        descripcionRutina = rutina.getDescripcion();

        // El backend escribe el día en mayúsculas, que es exactamente el nombre del enum.
        if (rutina.getDiaSemana() != null) {
            DiaSemana diaSemana = DiaSemana.valueOf(rutina.getDiaSemana());
            // La posición cero del desplegable es el texto de ayuda, por eso el día va una más.
            posicionDiaSeleccionado = diaSemana.ordinal() + 1;
            spDiaRutina.setSelection(posicionDiaSeleccionado);
        }

        listaEjercicios.clear();

        if (rutina.getEjercicios() != null) {
            for (EjercisioEnRutina ejercicio : rutina.getEjercicios()) {
                listaEjercicios.add(new EjercicioRutinaEditable(
                        ejercicio.getEjercicioId(),
                        ejercicio.getNombre(),
                        ejercicio.getGrupoMuscular(),
                        convertirSeries(ejercicio.getSeries())
                ));
            }
        }

        // La lista del RecyclerView es la misma que usa el adapter, así que hay que
        // redibujarla entera: llegaron todos los ejercicios de golpe.
        adapter.notifyDataSetChanged();
        actualizarEstadoPantalla();
        rutinaCargada = true;
    }

    /**
     * Convierte las series que llegaron del backend en las series que edita la pantalla.
     * El peso y las repeticiones viajan como números y aquí se escriben como texto en los
     * campos; un cero se muestra tal cual para que la serie se vea siempre.
     */
    private List<SerieRutina> convertirSeries(
            List<RutinaSerieResponse> seriesRecibidas) {
        List<SerieRutina> seriesConvertidas = new ArrayList<>();

        if (seriesRecibidas == null) {
            return seriesConvertidas;
        }

        for (RutinaSerieResponse serieRecibida : seriesRecibidas) {
            SerieRutina serie = new SerieRutina();
            serie.setRepeticiones(String.valueOf(serieRecibida.getRepeticionesObjetivo()));
            serie.setPesoObjetivo(convertirPesoATexto(serieRecibida.getPesoObjetivo()));
            seriesConvertidas.add(serie);
        }

        return seriesConvertidas;
    }

    /** Escribe el peso de una serie guardada como texto, sin el ".0" de los pesos enteros. */
    private String convertirPesoATexto(double pesoObjetivo) {
        String peso = String.valueOf(pesoObjetivo);

        if (peso.endsWith(".0")) {
            peso = peso.substring(0, peso.length() - 2);
        }

        return peso;
    }

    // ------------------------------------------------------------------ Guardado de los cambios

    /**
     * Valida el formulario y, si está completo, manda los cambios al backend.
     * Si la petición falla el formulario se conserva tal como estaba para que el
     * usuario pueda corregir lo que haga falta y volver a intentarlo.
     */
    private void guardarCambios() {
        // No se puede guardar y borrar al mismo tiempo, ni repetir la misma operación.
        if (procesandoPeticion || !rutinaCargada) {
            return;
        }

        if (!validarRutina()) {
            return;
        }

        // La conversión de los campos vacíos a cero se hace aquí, ya validado el formulario.
        RutinaCrearRequest rutinaCrearRequest = crearRutinaRequest();

        mostrarProcesando(true);
        currentCallActualizar = rutinaRepository.actualizarRutina(rutinaId, rutinaCrearRequest);
        currentCallActualizar.enqueue(new Callback<RutinaResponse>() {

            @Override
            public void onResponse(@NonNull Call<RutinaResponse> call,
                                   @NonNull Response<RutinaResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    procesarRutinaActualizada(response.body());
                    return;
                }

                mostrarProcesando(false);

                // El 409 solo llega de este endpoint: el usuario ya tiene otra rutina con
                // ese mismo nombre, así que el error se señala en el campo nombre.
                if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                    mostrarErrorNombre(R.string.tvErrorNombreRutinaDuplicado);
                    return;
                }

                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<RutinaResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                // El formulario queda como estaba para que el usuario pueda reintentar.
                mostrarProcesando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Avisa que la rutina quedó actualizada, informa a RutinasFragment para que vuelva a
     * consultar la lista y regresa a esa pantalla.
     */
    private void procesarRutinaActualizada(RutinaResponse rutinaActualizada) {
        Toast.makeText(
                requireContext(),
                getString(R.string.tvRutinaActualizada, rutinaActualizada.getNombre()),
                Toast.LENGTH_SHORT
        ).show();

        getParentFragmentManager().setFragmentResult(
                REQUEST_RUTINA_MODIFICADA,
                new Bundle()
        );

        ((MainActivity) requireActivity()).regresar();
    }

    // ------------------------------------------------------------------ Borrado de la rutina

    /** Pide confirmación antes de borrar: el borrado no se puede deshacer. */
    private void confirmarBorrado() {
        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloConfirmarBorrarRutina)
                .setMessage(getString(R.string.tvMensajeConfirmarBorrarRutina, nombreRutina))
                .setPositiveButton(R.string.btnConfirmarBorrarRutina, (dialogoVisible, cual) -> borrarRutina())
                .setNegativeButton(R.string.btnCancelarBorrarRutina, null)
                .create();

        dialogo.show();
        // El botón de borrar queda en rojo para que se lea como una acción destructiva.
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.colorErrorText));
    }

    /**
     * Pide al backend eliminar la rutina. El borrado es lógico: la rutina deja de aparecer
     * en la lista, pero sigue existiendo en la base de datos.
     */
    private void borrarRutina() {
        if (procesandoPeticion) {
            return;
        }

        mostrarProcesando(true);
        currentCallEliminar = rutinaRepository.eliminarRutina(rutinaId);
        currentCallEliminar.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                // La respuesta no trae cuerpo, así que basta con que el código sea correcto.
                if (response.isSuccessful()) {
                    procesarRutinaEliminada();
                    return;
                }

                mostrarProcesando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                // La pantalla y la rutina se conservan para que el usuario pueda reintentar.
                mostrarProcesando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Avisa que la rutina se borró, informa a RutinasFragment para que vuelva a consultar
     * la lista (donde ya no aparecerá) y regresa a esa pantalla.
     */
    private void procesarRutinaEliminada() {
        Toast.makeText(
                requireContext(),
                getString(R.string.tvRutinaEliminada),
                Toast.LENGTH_SHORT
        ).show();

        getParentFragmentManager().setFragmentResult(
                REQUEST_RUTINA_MODIFICADA,
                new Bundle()
        );

        ((MainActivity) requireActivity()).regresar();
    }

    /**
     * Muestra o quita la capa que cubre la pantalla mientras espera una respuesta.
     */
    private void mostrarProcesando(boolean procesando) {
        procesandoPeticion = procesando;
        layoutCargaModificarRutina.setVisibility(procesando ? View.VISIBLE : View.GONE);
        configurarToolbar(procesando);
    }

    // ------------------------------------------------------------------ Validación

    /**
     * Revisa el nombre, el día, los ejercicios y las series antes de enviar la rutina.
     * El nombre, el día y al menos un ejercicio son obligatorios, y cada ejercicio
     * necesita su identificador y al menos una serie. El peso y las repeticiones de
     * cada serie son opcionales: si el usuario no escribió nada se envían en cero, y si
     * escribió tiene que ser un valor válido. Los nombres repetidos no se revisan aquí:
     * el backend los rechaza con un 409 cuando ya existe una rutina con ese nombre.
     * Devuelve true solo si la rutina se puede enviar.
     */
    private boolean validarRutina() {
        if (nombreRutina.trim().isEmpty()) {
            mostrarErrorNombre(R.string.tvErrorNombreRutina);
            return false;
        }

        if (nombreRutina.trim().length() > LONGITUD_MAXIMA_NOMBRE) {
            mostrarErrorNombre(R.string.tvErrorNombreRutinaLargo);
            return false;
        }

        if (spDiaRutina.getSelectedItemPosition() == POSICION_SIN_DIA_SELECCIONADO) {
            tvErrorDiaRutina.setVisibility(View.VISIBLE);
            return false;
        }

        if (listaEjercicios.isEmpty()) {
            Toast.makeText(requireContext(), "Agrega al menos un ejercicio a la rutina",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        for (EjercicioRutinaEditable ejercicio : listaEjercicios) {
            List<SerieRutina> series = ejercicio.getSeries();

            // El backend relaciona cada ejercicio con uno que ya existe, por eso
            // el identificador que llegó del selector tiene que ser válido.
            if (ejercicio.getIdEjercicio() == null || ejercicio.getIdEjercicio() <= 0) {
                Toast.makeText(requireContext(),
                        "No se pudo identificar " + ejercicio.getNombre(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            if (series.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Agrega al menos una serie en " + ejercicio.getNombre(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            for (int posicion = 0; posicion < series.size(); posicion++) {
                SerieRutina serie = series.get(posicion);
                String numeroSerie = String.valueOf(posicion + 1);

                // El peso y las repeticiones son opcionales: solo se revisan si el usuario
                // escribió algo, así una serie puede guardarse con los campos vacíos.
                if (!esPesoValido(serie.getPesoObjetivo())) {
                    Toast.makeText(requireContext(),
                            "Revisa el peso de la serie " + numeroSerie + " de " + ejercicio.getNombre(),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!sonRepeticionesValidas(serie.getRepeticiones())) {
                    Toast.makeText(requireContext(),
                            "Revisa las repeticiones de la serie " + numeroSerie + " de " + ejercicio.getNombre(),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    /** Muestra el mensaje de error del nombre y lo destaca en el campo. */
    private void mostrarErrorNombre(@StringRes int mensajeError) {
        tvErrorNombreRutina.setText(mensajeError);
        tvErrorNombreRutina.setVisibility(View.VISIBLE);
        etNombreRutina.setBackgroundResource(R.drawable.bg_input_error);
    }

    /**
     * Revisa el peso de una serie. El campo puede quedar vacío, pero si el usuario escribió
     * algo tiene que ser un número igual o mayor que cero (el cero sirve para el peso corporal).
     */
    private boolean esPesoValido(String pesoObjetivo) {
        String peso = pesoObjetivo.trim();
        if (peso.isEmpty()) {
            return true;
        }

        try {
            // En algunos teclados el separador decimal es la coma.
            return Double.parseDouble(peso.replace(',', '.')) >= 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    /**
     * Revisa las repeticiones de una serie. El campo puede quedar vacío, pero si el usuario
     * escribió algo tiene que ser un número entero mayor o igual que cero.
     */
    private boolean sonRepeticionesValidas(String repeticiones) {
        String numeroRepeticiones = repeticiones.trim();
        if (numeroRepeticiones.isEmpty()) {
            return true;
        }

        try {
            return Integer.parseInt(numeroRepeticiones) >= 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    // ------------------------------------------------------------------ Construcción de la petición

    /**
     * Traduce lo que hay en pantalla a la petición que espera el backend.
     * El cuerpo es el mismo de la creación porque el backend reemplaza la configuración
     * completa de la rutina por la que se envía. La descripción no se edita aquí, pero se
     * reenvía tal como estaba para no borrarla.
     * Solo se envían los ejercicios y las series que quedaron en el formulario, así que lo
     * que el usuario quitó se elimina de verdad al guardar, y al salir sin guardar la
     * rutina del backend queda como estaba. Los órdenes y los números de serie salen de la
     * posición de cada elemento, por eso siempre empiezan en uno y nunca se repiten.
     * Una serie vacía sí se envía: el usuario la agregó a propósito y va con peso y
     * repeticiones en cero.
     */
    private RutinaCrearRequest crearRutinaRequest() {
        List<RutinaEjercicioCrearRequest> ejerciciosRequest = new ArrayList<>();

        for (int posicionEjercicio = 0;
             posicionEjercicio < listaEjercicios.size();
             posicionEjercicio++) {
            EjercicioRutinaEditable ejercicio = listaEjercicios.get(posicionEjercicio);
            List<RutinaSerieCrearRequest> seriesRequest = new ArrayList<>();

            for (int posicionSerie = 0;
                 posicionSerie < ejercicio.getSeries().size();
                 posicionSerie++) {
                SerieRutina serie = ejercicio.getSeries().get(posicionSerie);

                seriesRequest.add(new RutinaSerieCrearRequest(
                        posicionSerie + 1,
                        convertirRepeticiones(serie.getRepeticiones()),
                        convertirPeso(serie.getPesoObjetivo())
                ));
            }

            ejerciciosRequest.add(new RutinaEjercicioCrearRequest(
                    ejercicio.getIdEjercicio(),
                    posicionEjercicio + 1,
                    seriesRequest
            ));
        }

        return new RutinaCrearRequest(
                nombreRutina.trim(),
                descripcionRutina,
                obtenerDiaSeleccionado(),
                ejerciciosRequest
        );
    }

    /**
     * Obtiene el día escrito por el usuario, o null si sigue en la opción inicial.
     */
    private DiaSemana obtenerDiaSeleccionado() {
        int posicionSeleccionada = spDiaRutina.getSelectedItemPosition();

        if (posicionSeleccionada == POSICION_SIN_DIA_SELECCIONADO) {
            return null;
        }

        return DiaSemana.values()[posicionSeleccionada - 1];
    }

    /**
     * Convierte el peso escrito en un número para la petición, reemplazando la coma
     * decimal por punto. Un campo vacío vale cero porque el backend no admite nulos.
     * Solo se llama después de validar el formulario.
     */
    private double convertirPeso(String pesoEscrito) {
        String pesoNormalizado = pesoEscrito.trim().replace(',', '.');

        if (pesoNormalizado.isEmpty()) {
            return 0.0;
        }

        return Double.parseDouble(pesoNormalizado);
    }

    /**
     * Convierte las repeticiones escritas en un número para la petición. Un campo vacío
     * vale cero porque el backend no admite nulos. Solo se llama después de validar el
     * formulario.
     */
    private int convertirRepeticiones(String repeticionesEscritas) {
        String repeticionesNormalizadas = repeticionesEscritas.trim();

        if (repeticionesNormalizadas.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(repeticionesNormalizadas);
    }
}
