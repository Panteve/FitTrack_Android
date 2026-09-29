package ue.edu.co.fittrackandroid.ejercicios.vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.ejercicios.datos.EjercicioRepository;
import ue.edu.co.fittrackandroid.ejercicios.modelo.Ejercicio;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjerciciosDisponiblesResponse;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment selector de ejercicios: buscador, filtros por grupo muscular y las dos listas de
 * ejercicios disponibles, primero los del usuario y después los del sistema, siempre por
 * separado. Al tocar un ejercicio lo devuelve a la pantalla que lo abrió y regresa.
 */
public class EjerciciosFragment extends Fragment {

    /**
     * Claves usadas para devolver el ejercicio elegido a la pantalla que abrió el selector.
     * Quien las escucha puede ser CrearRutinaFragment o EntrenamientoActivoFragment.
     */
    public static final String REQUEST_SELECCION_EJERCICIO = "seleccionEjercicio";
    public static final String RESULT_ID_EJERCICIO = "idEjercicio";
    public static final String RESULT_NOMBRE_EJERCICIO = "nombreEjercicio";
    public static final String RESULT_GRUPO_MUSCULAR = "grupoMuscular";

    private EditText etBuscarEjercicio;
    private TextView tvChipTodos;
    private TextView tvChipPecho;
    private TextView tvChipEspalda;
    private TextView tvChipPierna;
    private TextView tvChipBrazo;

    // Estados generales de la pantalla
    private ProgressBar pbCargaEjercicios;
    private View layoutErrorCargaEjercicios;
    private Button btnReintentarCargaEjercicios;
    private NestedScrollView svEjercicios;

    // Sección de los ejercicios del usuario
    private TextView tvSinMisEjercicios;
    private RecyclerView rvMisEjercicios;

    // Sección de los ejercicios del sistema
    private TextView tvSinEjerciciosSistema;
    private RecyclerView rvEjerciciosSistema;

    // Un adapter por sección: cada lista mantiene la suya y así nunca se mezclan.
    private EjercicioAdapter adapterEjerciciosSistema;
    private EjercicioAdapter adapterMisEjercicios;

    private EjercicioRepository ejercicioRepository;
    private Call<EjerciciosDisponiblesResponse> currentCallEjercicios;

    // Permite saber si las vistas existen antes de volver a pintar la pantalla.
    private boolean vistaCreada;

    // TODO: Guardar y restaurar estos filtros si el Fragment o el proceso se recrean.
    // Estado actual de los filtros: el buscador y el chip seleccionado se combinan.
    private String textoBusqueda = "";
    private String grupoMuscularSeleccionado = null;

    public EjerciciosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarResultadoEjercicioCreado();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ejercicios, container, false);

        inicializarVistas(view);
        ejercicioRepository = new EjercicioRepository(requireContext());

        configurarListas();
        configurarBuscador();
        configurarChips();
        restaurarFiltrosEnVista();

        vistaCreada = true;
        cargarEjerciciosDisponibles();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Solo es un selector: la flecha de la toolbar regresa a la pantalla que lo abrió.
        // La derecha queda libre, así que ahí va CREAR para registrar un ejercicio nuevo.
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloSeleccionarEjercicio),
                true,
                null
        );
        activity.mostrarAccionSecundariaToolbar(getString(R.string.btnCrearEjercicio));
        activity.setAccionSecundariaToolbar(this::abrirCrearEjercicio);
    }

    /**
     * Escucha el resultado de CrearEjercicioFragment y vuelve a consultar los ejercicios,
     * para que el recién creado aparezca de una vez en MIS EJERCICIOS.
     * El buscador y el filtro no se tocan: siguen igual en los campos del fragment.
     */
    private void registrarResultadoEjercicioCreado() {
        getParentFragmentManager().setFragmentResultListener(
                CrearEjercicioFragment.REQUEST_EJERCICIO_CREADO,
                this,
                (clave, resultado) -> {
                    if (!isAdded() || !vistaCreada) {
                        return;
                    }
                    cargarEjerciciosDisponibles();
                });
    }

    @Override
    public void onDestroyView() {
        vistaCreada = false;
        // La consulta puede seguir en vuelo si el usuario se va de la pantalla.
        if (currentCallEjercicios != null) {
            currentCallEjercicios.cancel();
        }
        super.onDestroyView();
    }

    private void inicializarVistas(View view) {
        etBuscarEjercicio = view.findViewById(R.id.etBuscarEjercicio);
        tvChipTodos = view.findViewById(R.id.tvChipTodos);
        tvChipPecho = view.findViewById(R.id.tvChipPecho);
        tvChipEspalda = view.findViewById(R.id.tvChipEspalda);
        tvChipPierna = view.findViewById(R.id.tvChipPierna);
        tvChipBrazo = view.findViewById(R.id.tvChipBrazo);

        pbCargaEjercicios = view.findViewById(R.id.pbCargaEjercicios);
        layoutErrorCargaEjercicios = view.findViewById(R.id.layoutErrorCargaEjercicios);
        btnReintentarCargaEjercicios = view.findViewById(R.id.btnReintentarCargaEjercicios);
        svEjercicios = view.findViewById(R.id.svEjercicios);

        tvSinEjerciciosSistema = view.findViewById(R.id.tvSinEjerciciosSistema);
        rvEjerciciosSistema = view.findViewById(R.id.rvEjerciciosSistema);

        tvSinMisEjercicios = view.findViewById(R.id.tvSinMisEjercicios);
        rvMisEjercicios = view.findViewById(R.id.rvMisEjercicios);
    }

    /** Prepara las dos listas y el reintento. El desplazamiento lo maneja svEjercicios. */
    private void configurarListas() {
        rvEjerciciosSistema.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMisEjercicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnReintentarCargaEjercicios.setOnClickListener(v -> cargarEjerciciosDisponibles());
    }

    private void configurarBuscador() {
        etBuscarEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textoBusqueda = s.toString();
                aplicarFiltros();
            }
        });
    }

    private void configurarChips() {
        tvChipTodos.setOnClickListener(v -> seleccionarChip(tvChipTodos, null));
        tvChipPecho.setOnClickListener(v -> seleccionarChip(tvChipPecho, grupoPecho()));
        tvChipEspalda.setOnClickListener(v -> seleccionarChip(tvChipEspalda, grupoEspalda()));
        tvChipPierna.setOnClickListener(v -> seleccionarChip(tvChipPierna, grupoPierna()));
        tvChipBrazo.setOnClickListener(v -> seleccionarChip(tvChipBrazo, grupoBrazo()));
    }

    // ------------------------------------------------------------------ Estados de la pantalla

    /** Deja visible únicamente el indicador mientras se consulta la API. */
    private void mostrarEstadoCarga() {
        pbCargaEjercicios.setVisibility(View.VISIBLE);
        layoutErrorCargaEjercicios.setVisibility(View.GONE);
        svEjercicios.setVisibility(View.GONE);
    }

    /** Muestra el mensaje de error y deja el reintento disponible. */
    private void mostrarErrorCarga() {
        pbCargaEjercicios.setVisibility(View.GONE);
        svEjercicios.setVisibility(View.GONE);
        layoutErrorCargaEjercicios.setVisibility(View.VISIBLE);
    }

    /**
     * Muestra las dos listas ya separadas. Cada una recibe su propio adapter con el mismo
     * callback, y si una llega vacía conserva su mensaje para que la otra siga visible.
     */
    private void mostrarEjerciciosDisponibles(EjerciciosDisponiblesResponse ejerciciosDisponibles) {
        List<Ejercicio> ejerciciosSistema = convertirAEjercicios(
                ejerciciosDisponibles.getEjerciciosSistema());
        List<Ejercicio> misEjercicios = convertirAEjercicios(
                ejerciciosDisponibles.getMisEjercicios());

        adapterEjerciciosSistema = new EjercicioAdapter(
                ejerciciosSistema, this::seleccionarEjercicio);
        adapterMisEjercicios = new EjercicioAdapter(
                misEjercicios, this::seleccionarEjercicio);

        rvEjerciciosSistema.setAdapter(adapterEjerciciosSistema);
        rvMisEjercicios.setAdapter(adapterMisEjercicios);

        pbCargaEjercicios.setVisibility(View.GONE);
        layoutErrorCargaEjercicios.setVisibility(View.GONE);
        svEjercicios.setVisibility(View.VISIBLE);

        // Puede haber un filtro escrito antes de que llegaran los ejercicios.
        aplicarFiltros();
    }

    // ------------------------------------------------------------------ Consulta al backend

    /**
     * Consulta los ejercicios disponibles y los muestra en sus dos secciones.
     * El backend ya devuelve los ejercicios del sistema y los del usuario por separado, así
     * que aquí no se mezclan y cada lista conserva el orden en que llegó.
     */
    private void cargarEjerciciosDisponibles() {
        // Una consulta anterior puede seguir en vuelo si se reintenta o se vuelve a abrir.
        if (currentCallEjercicios != null) {
            currentCallEjercicios.cancel();
        }

        mostrarEstadoCarga();

        currentCallEjercicios = ejercicioRepository.getEjerciciosDisponibles();

        currentCallEjercicios.enqueue(new Callback<EjerciciosDisponiblesResponse>() {

            @Override
            public void onResponse(@NonNull Call<EjerciciosDisponiblesResponse> call,
                                   @NonNull Response<EjerciciosDisponiblesResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    mostrarEjerciciosDisponibles(response.body());
                    return;
                }

                mostrarErrorCarga();
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<EjerciciosDisponiblesResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                mostrarErrorCarga();
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Convierte la lista que llegó de la API al modelo que usa el adapter.
     * Si el backend no envía alguna de las listas se entrega vacía, para que la sección
     * muestre su mensaje sin fallar.
     *
     * @param ejerciciosRecibidos lista de ejercicios recibida del backend.
     * @return lista de ejercicios en el modelo de la pantalla.
     */
    private List<Ejercicio> convertirAEjercicios(List<EjercicioResponse> ejerciciosRecibidos) {
        List<Ejercicio> listaEjercicios = new ArrayList<>();

        if (ejerciciosRecibidos == null) {
            return listaEjercicios;
        }

        for (EjercicioResponse ejercicio : ejerciciosRecibidos) {
            listaEjercicios.add(new Ejercicio(
                    ejercicio.getId(),
                    ejercicio.getNombre(),
                    ejercicio.getGrupoMuscular()));
        }

        return listaEjercicios;
    }

    // ------------------------------------------------------------------ Buscador y filtros

    /**
     * Vuelve a escribir la búsqueda y a remarcar el chip que estaban activos. Al ir a crear
     * un ejercicio y volver, las vistas se recrean pero el filtro sigue guardado en el
     * fragment, así que hay que volver a reflejarlo o la lista saldría filtrada sin que
     * se vea por qué.
     */
    private void restaurarFiltrosEnVista() {
        if (!textoBusqueda.isEmpty()) {
            etBuscarEjercicio.setText(textoBusqueda);
        }

        if (grupoPecho().equals(grupoMuscularSeleccionado)) {
            seleccionarChip(tvChipPecho, grupoPecho());
        } else if (grupoEspalda().equals(grupoMuscularSeleccionado)) {
            seleccionarChip(tvChipEspalda, grupoEspalda());
        } else if (grupoPierna().equals(grupoMuscularSeleccionado)) {
            seleccionarChip(tvChipPierna, grupoPierna());
        } else if (grupoBrazo().equals(grupoMuscularSeleccionado)) {
            seleccionarChip(tvChipBrazo, grupoBrazo());
        } else {
            seleccionarChip(tvChipTodos, null);
        }
    }

    /** Marca el chip pulsado y aplica su filtro a las dos listas. */
    private void seleccionarChip(TextView seleccionado, String grupoMuscular) {
        List<TextView> listaChips = Arrays.asList(
                tvChipTodos, tvChipPecho, tvChipEspalda, tvChipPierna, tvChipBrazo);

        for (TextView chip : listaChips) {
            boolean estaSeleccionado = chip == seleccionado;
            chip.setBackgroundResource(estaSeleccionado
                    ? R.drawable.bg_filter_chip_selected
                    : R.drawable.bg_filter_chip);
            chip.setTextColor(requireContext().getColor(estaSeleccionado
                    ? R.color.colorOnPrimary
                    : R.color.colorTextStrong));
        }

        grupoMuscularSeleccionado = grupoMuscular;
        aplicarFiltros();
    }

    // Los nombres de los grupos salen de los mismos recursos que muestran los chips y que
    // envía el usuario al crear un ejercicio, así nunca se escriben a mano.

    private String grupoPecho() {
        return getString(R.string.tvChipPecho);
    }

    private String grupoEspalda() {
        return getString(R.string.tvChipEspalda);
    }

    private String grupoPierna() {
        return getString(R.string.tvChipPierna);
    }

    private String grupoBrazo() {
        return getString(R.string.tvChipBrazo);
    }

    /**
     * Aplica la búsqueda y el grupo muscular a las dos listas. Cada sección se filtra por
     * separado: una puede quedar sin coincidencias sin afectar a la otra.
     */
    private void aplicarFiltros() {
        if (adapterEjerciciosSistema != null) {
            adapterEjerciciosSistema.filtrar(
                    textoBusqueda,
                    grupoMuscularSeleccionado);
        }

        if (adapterMisEjercicios != null) {
            adapterMisEjercicios.filtrar(
                    textoBusqueda,
                    grupoMuscularSeleccionado);
        }

        actualizarEstadosDeBusqueda();
    }

    /** Muestra el mensaje de cada sección cuando su lista quedó sin resultados. */
    private void actualizarEstadosDeBusqueda() {
        boolean hayEjerciciosSistema = adapterEjerciciosSistema != null
                && adapterEjerciciosSistema.getItemCount() > 0;
        boolean hayMisEjercicios = adapterMisEjercicios != null
                && adapterMisEjercicios.getItemCount() > 0;

        rvEjerciciosSistema.setVisibility(hayEjerciciosSistema ? View.VISIBLE : View.GONE);
        tvSinEjerciciosSistema.setVisibility(hayEjerciciosSistema ? View.GONE : View.VISIBLE);

        rvMisEjercicios.setVisibility(hayMisEjercicios ? View.VISIBLE : View.GONE);
        tvSinMisEjercicios.setVisibility(hayMisEjercicios ? View.GONE : View.VISIBLE);
    }

    // ------------------------------------------------------------------ Selección

    /**
     * Devuelve el ejercicio elegido a quien abrió el selector y regresa a esa pantalla.
     * Se envían el identificador y los textos sueltos porque los modelos no implementan
     * Parcelable ni Serializable.
     */
    private void seleccionarEjercicio(Ejercicio ejercicio) {
        Bundle datosEjercicio = new Bundle();

        datosEjercicio.putLong(RESULT_ID_EJERCICIO, ejercicio.getId());
        datosEjercicio.putString(RESULT_NOMBRE_EJERCICIO, ejercicio.getNombre());
        datosEjercicio.putString(RESULT_GRUPO_MUSCULAR, ejercicio.getGrupoMuscular());

        getParentFragmentManager().setFragmentResult(REQUEST_SELECCION_EJERCICIO, datosEjercicio);
        ((MainActivity) requireActivity()).regresar();
    }

    /**
     * Abre la pantalla de crear ejercicio. Se navega con retroceso, así que al guardar el
     * ejercicio el usuario vuelve aquí, al selector, con el buscador y el filtro como estaban.
     */
    private void abrirCrearEjercicio() {
        ((MainActivity) requireActivity()).mostrarCrearEjercicio();
    }
}
