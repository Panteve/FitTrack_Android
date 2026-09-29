package ue.edu.co.fittrackandroid.rutinas.vista;

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
import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.rutinas.datos.RutinaRepository;
import ue.edu.co.fittrackandroid.rutinas.modelo.DiaSemana;
import ue.edu.co.fittrackandroid.rutinas.modelo.EjercicioRutinaEditable;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaEjercicioCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaSerieCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.SerieRutina;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para crear una rutina: nombre, día de entrenamiento, ejercicios elegidos y
 * series de cada ejercicio. Los datos se conservan en memoria mientras el usuario navega
 * dentro de esta pantalla y se envían a {@code POST /rutinas} al pulsar GUARDAR.
 * Cuando el backend confirma la creación avisa a RutinasFragment y regresa.
 */
public class CrearRutinaFragment extends Fragment
        implements CrearRutinaEjercicioAdapter.OnAgregarSerieListener {

    /**
     * Clave del resultado que avisa que se creó una rutina. La escucha RutinasFragment,
     * que es la pantalla que abrió esta, para volver a consultar sus rutinas.
     */
    public static final String REQUEST_RUTINA_CREADA = "rutinaCreada";

    /** El backend no admite nombres de más de 100 caracteres. */
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    /** Posición de la opción inicial del Spinner: todavía no es un día válido. */
    private static final int POSICION_SIN_DIA_SELECCIONADO = 0;

    private EditText etNombreRutinaNueva;
    private TextView tvErrorNombreRutinaNueva;
    private Spinner spDiaRutina;
    private TextView tvErrorDiaRutina;
    private Button btnAgregarPrimerEjercicio;
    private Button btnAgregarEjercicio;
    private View layoutRutinaSinEjercicios;
    private View layoutRutinaConEjercicios;
    private RecyclerView rvEjerciciosRutina;
    private View layoutCargaGuardarRutina;
    private CrearRutinaEjercicioAdapter adapter;

    // Estado actual de la rutina que se está creando.
    private final List<EjercicioRutinaEditable> listaEjercicios = new ArrayList<>();
    private String nombreRutina = "";

    /**
     * Posición del día elegido en el desplegable. Se guarda aparte porque al abrir el
     * selector de ejercicios las vistas se recrean y el Spinner vuelve a la primera opción.
     */
    private int posicionDiaSeleccionado = POSICION_SIN_DIA_SELECCIONADO;

    private RutinaRepository rutinaRepository;
    private Call<RutinaResponse> currentCallCrearRutina;

    /** Evita que se envíe una segunda petición mientras la primera sigue en vuelo. */
    private boolean guardandoRutina;

    public CrearRutinaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarResultadoEjercicio();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_rutina, container, false);

        inicializarVistas(view);
        // El repositorio se crea una sola vez por vista, igual que en el resto de pantallas.
        rutinaRepository = new RutinaRepository(requireContext());

        configurarRecyclerView();
        configurarAcciones();
        restaurarNombreEnVista();
        restaurarDiaEnVista();
        actualizarEstadoPantalla();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // La toolbar se reconfigura aquí porque al volver del selector de ejercicios
        // sigue mostrando la pantalla anterior.
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCrearRutina),
                true,
                getString(R.string.btnGuardarRutinaNueva)
        );
        activity.setAccionToolbar(this::guardarRutina);
    }

    @Override
    public void onDestroyView() {
        // El guardado puede seguir en vuelo si el usuario se va de la pantalla.
        if (currentCallCrearRutina != null) {
            currentCallCrearRutina.cancel();
        }
        super.onDestroyView();
    }

    // ------------------------------------------------------------------ Preparación de la pantalla

    /** Busca las vistas de la pantalla y las guarda en los campos. */
    private void inicializarVistas(View view) {
        etNombreRutinaNueva = view.findViewById(R.id.etNombreRutinaNueva);
        tvErrorNombreRutinaNueva = view.findViewById(R.id.tvErrorNombreRutinaNueva);
        spDiaRutina = view.findViewById(R.id.spDiaRutina);
        tvErrorDiaRutina = view.findViewById(R.id.tvErrorDiaRutina);
        btnAgregarPrimerEjercicio = view.findViewById(R.id.btnAgregarPrimerEjercicio);
        btnAgregarEjercicio = view.findViewById(R.id.btnAgregarEjercicio);
        layoutRutinaSinEjercicios = view.findViewById(R.id.layoutRutinaSinEjercicios);
        layoutRutinaConEjercicios = view.findViewById(R.id.layoutRutinaConEjercicios);
        rvEjerciciosRutina = view.findViewById(R.id.rvEjerciciosRutina);
        layoutCargaGuardarRutina = view.findViewById(R.id.layoutCargaGuardarRutina);
    }

    private void configurarRecyclerView() {
        adapter = new CrearRutinaEjercicioAdapter(listaEjercicios, this);
        rvEjerciciosRutina.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosRutina.setAdapter(adapter);
    }

    private void configurarAcciones() {
        btnAgregarPrimerEjercicio.setOnClickListener(v -> abrirSelectorEjercicios());
        btnAgregarEjercicio.setOnClickListener(v -> abrirSelectorEjercicios());
        limpiarErrorNombreAlEscribir();
        configurarDiaRutina();
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

    private void abrirSelectorEjercicios() {
        ((MainActivity) requireActivity()).mostrarSelectorEjercicios();
    }

    /**
     * Agrega el ejercicio elegido en el selector. Cada ejercicio entra con su identificador
     * y con su primera serie. El mismo ejercicio se puede agregar varias veces, por ejemplo
     * con series diferentes.
     */
    private void agregarEjercicioSeleccionado(Long idEjercicio, String nombreEjercicio,
                                              String grupoMuscular) {
        // TODO: Mostrar un error si el ejercicio fue eliminado antes de agregarlo a la rutina.
        if (nombreEjercicio == null || adapter == null) {
            return;
        }

        adapter.agregarEjercicio(new EjercicioRutinaEditable(idEjercicio, nombreEjercicio, grupoMuscular));
        actualizarEstadoPantalla();
    }

    @Override
    public void onAgregarSerie(int posicionEjercicio) {
        listaEjercicios.get(posicionEjercicio).agregarSerie();
        adapter.actualizarEjercicio(posicionEjercicio);
    }

    /** Alterna entre el estado vacío y el estado con ejercicios. */
    private void actualizarEstadoPantalla() {
        boolean hayEjercicios = !listaEjercicios.isEmpty();
        layoutRutinaSinEjercicios.setVisibility(hayEjercicios ? View.GONE : View.VISIBLE);
        layoutRutinaConEjercicios.setVisibility(hayEjercicios ? View.VISIBLE : View.GONE);
    }

    private void restaurarNombreEnVista() {
        if (etNombreRutinaNueva.getText().length() == 0 && !nombreRutina.isEmpty()) {
            etNombreRutinaNueva.setText(nombreRutina);
        }
    }

    /** Vuelve a marcar el día que el usuario había elegido antes de abrir el selector. */
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

    private void limpiarErrorNombreAlEscribir() {
        etNombreRutinaNueva.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int cantidad, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int cantidad) {
            }

            @Override
            public void afterTextChanged(Editable texto) {
                nombreRutina = texto.toString();
                tvErrorNombreRutinaNueva.setVisibility(View.GONE);
                etNombreRutinaNueva.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }

    // ------------------------------------------------------------------ Guardado

    /**
     * Valida el formulario y, si está completo, manda la rutina al backend.
     * Si la petición falla el formulario se conserva tal como estaba para que el
     * usuario pueda corregir lo que haga falta y volver a intentarlo.
     */
    private void guardarRutina() {
        if (guardandoRutina) {
            return;
        }

        if (!validarRutina()) {
            return;
        }

        // La conversión de los campos vacíos a cero se hace aquí, ya validado el formulario.
        RutinaCrearRequest rutinaCrearRequest = crearRutinaRequest();

        mostrarGuardando(true);
        currentCallCrearRutina = rutinaRepository.crearRutina(rutinaCrearRequest);
        currentCallCrearRutina.enqueue(new Callback<RutinaResponse>() {

            @Override
            public void onResponse(@NonNull Call<RutinaResponse> call,
                                   @NonNull Response<RutinaResponse> response) {
                if (!isAdded()) {
                    return;
                }

                // El backend responde 201 cuando la rutina quedó registrada.
                if (response.isSuccessful() && response.body() != null) {
                    procesarRutinaCreada(response.body());
                    return;
                }

                mostrarGuardando(false);

                // El 409 solo llega de este endpoint: el usuario ya tiene una rutina
                // con ese mismo nombre, así que el error se señala en el campo nombre.
                if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                    mostrarErrorNombre(R.string.tvErrorNombreRutinaNuevaDuplicado);
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
                mostrarGuardando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Avisa que la rutina se creó, informa a RutinasFragment para que vuelva a
     * consultar la lista y regresa a esa pantalla.
     */
    private void procesarRutinaCreada(RutinaResponse rutinaCreada) {
        Toast.makeText(
                requireContext(),
                getString(R.string.tvRutinaCreada, rutinaCreada.getNombre()),
                Toast.LENGTH_SHORT
        ).show();

        getParentFragmentManager().setFragmentResult(
                REQUEST_RUTINA_CREADA,
                new Bundle()
        );

        ((MainActivity) requireActivity()).regresar();
    }

    /**
     * Muestra el estado de guardado: la capa oscura cubre la pantalla y bloquea la
     * acción GUARDAR mientras espera la respuesta, y todo se devuelve si la petición falla.
     *
     * @param guardando true mientras la petición está en vuelo.
     */
    private void mostrarGuardando(boolean guardando) {
        guardandoRutina = guardando;
        layoutCargaGuardarRutina.setVisibility(guardando ? View.VISIBLE : View.GONE);

        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCrearRutina),
                true,
                getString(guardando ? R.string.btnGuardar_loading : R.string.btnGuardarRutinaNueva)
        );
        activity.setAccionToolbar(this::guardarRutina);
        activity.habilitarAccionToolbar(!guardando);
    }

    // ------------------------------------------------------------------ Validación

    /**
     * Revisa el nombre, el día, los ejercicios y las series antes de enviar la rutina.
     * El nombre, el día y al menos un ejercicio son obligatorios, y cada ejercicio
     * necesita su identificador y al menos una serie. El peso y las repeticiones de
     * cada serie son opcionales: si el usuario no escribió nada se envían en cero, y si
     * escribió tiene que ser un valor válido. Los nombres repetidos no se revisan aquí:
     * el backend los rechaza con un 409 cuando ya existe una rutina con ese nombre.
     *
     * @return true si la rutina se puede enviar al backend.
     */
    private boolean validarRutina() {
        if (nombreRutina.trim().isEmpty()) {
            mostrarErrorNombre(R.string.tvErrorNombreRutinaNueva);
            return false;
        }

        if (nombreRutina.trim().length() > LONGITUD_MAXIMA_NOMBRE) {
            mostrarErrorNombre(R.string.tvErrorNombreRutinaNuevaLargo);
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
        tvErrorNombreRutinaNueva.setText(mensajeError);
        tvErrorNombreRutinaNueva.setVisibility(View.VISIBLE);
        etNombreRutinaNueva.setBackgroundResource(R.drawable.bg_input_error);
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
     * Los órdenes y los números de serie salen de la posición de cada elemento, por eso
     * siempre empiezan en uno y nunca se repiten. Ninguna serie se elimina aunque esté vacía:
     * el usuario la agregó a propósito y se envía con peso y repeticiones en cero.
     *
     * @return datos listos para enviar a {@code POST /rutinas}.
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
                null,
                obtenerDiaSeleccionado(),
                ejerciciosRequest
        );
    }

    /**
     * @return el día escrito por el usuario, o null si sigue en la opción inicial.
     */
    private DiaSemana obtenerDiaSeleccionado() {
        int posicionSeleccionada = spDiaRutina.getSelectedItemPosition();

        if (posicionSeleccionada == POSICION_SIN_DIA_SELECCIONADO) {
            return null;
        }

        return DiaSemana.values()[posicionSeleccionada - 1];
    }

    /**
     * Convierte el peso escrito en un número para la petición. Un campo vacío vale cero
     * porque el backend no admite nulos. Solo se llama después de validar el formulario.
     *
     * @param pesoEscrito peso tal como lo escribió el usuario.
     * @return peso listo para enviar.
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
     *
     * @param repeticionesEscritas repeticiones tal como las escribió el usuario.
     * @return repeticiones listas para enviar.
     */
    private int convertirRepeticiones(String repeticionesEscritas) {
        String repeticionesNormalizadas = repeticionesEscritas.trim();

        if (repeticionesNormalizadas.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(repeticionesNormalizadas);
    }
}
