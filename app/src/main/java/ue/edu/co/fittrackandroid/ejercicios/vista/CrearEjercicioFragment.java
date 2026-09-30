package ue.edu.co.fittrackandroid.ejercicios.vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.ejercicios.datos.EjercicioRepository;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioRequest;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para crear un ejercicio del usuario. Solo pide el nombre y el grupo muscular,
 * que son los únicos datos que el backend necesita, y los envía con un POST a /ejercicios.
 * Cuando el backend confirma la creación avisa a la pantalla anterior y regresa.
 */
public class CrearEjercicioFragment extends Fragment {

    /**
     * Clave del resultado que avisa que se creó un ejercicio. La escuchan las pantallas
     * que abrieron CrearEjercicioFragment, para volver a consultar sus ejercicios.
     */
    public static final String REQUEST_EJERCICIO_CREADO = "ejercicioCreado";

    /** El backend no admite nombres de más de 100 caracteres. */
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    /** Posición de la opción inicial del Spinner: todavía no es un grupo válido. */
    private static final int POSICION_SIN_GRUPO_SELECCIONADO = 0;

    private EditText etNombreEjercicio;
    private Spinner spGrupoMuscular;
    private TextView tvErrorNombreEjercicio;
    private TextView tvErrorGrupoMuscular;
    private ProgressBar pbGuardarEjercicio;

    private EjercicioRepository ejercicioRepository;
    private Call<EjercicioResponse> currentCallCrearEjercicio;

    /** Evita que se envíe una segunda petición mientras la primera sigue en vuelo. */
    private boolean guardandoEjercicio;

    public CrearEjercicioFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_ejercicio, container, false);

        inicializarVistas(view);
        inicializarDatos();
        configurarEventos();
        configurarLimpiarErrores();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        configurarToolbar();
    }

    /**
     * La acción GUARDAR pertenece a la toolbar de MainActivity, no a esta pantalla.
     * Se configura en onResume porque al volver desde otra pantalla la barra todavía
     * muestra la pantalla anterior.
     */
    private void configurarToolbar() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCrearEjercicio),
                true,
                getString(R.string.btnGuardar)
        );
        activity.setAccionToolbar(this::guardarEjercicio);
    }

    @Override
    public void onDestroyView() {
        // El guardado puede seguir en vuelo si el usuario se va de la pantalla.
        if (currentCallCrearEjercicio != null) {
            currentCallCrearEjercicio.cancel();
        }
        super.onDestroyView();
    }

    // ------------------------------------------------------------------ Preparación de la pantalla

    /** Busca las vistas de la pantalla y las guarda en los campos. */
    private void inicializarVistas(View view) {
        etNombreEjercicio = view.findViewById(R.id.etNombreEjercicio);
        spGrupoMuscular = view.findViewById(R.id.spGrupoMuscular);
        tvErrorNombreEjercicio = view.findViewById(R.id.tvErrorNombreEjercicio);
        tvErrorGrupoMuscular = view.findViewById(R.id.tvErrorGrupoMuscular);
        pbGuardarEjercicio = view.findViewById(R.id.pbGuardarEjercicio);
    }

    /** Prepara el repositorio y llena el Spinner con los grupos musculares permitidos. */
    private void inicializarDatos() {
        ejercicioRepository = new EjercicioRepository(requireContext());

        // createFromResource devuelve un ArrayAdapter<CharSequence> porque las opciones
        // se leen del arreglo de recursos.
        ArrayAdapter<CharSequence> adapterGrupoMuscular = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.spGrupoMuscular_opciones,
                android.R.layout.simple_spinner_item
        );
        adapterGrupoMuscular.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spGrupoMuscular.setAdapter(adapterGrupoMuscular);
    }

    /** Conecta la pantalla con las acciones: por ahora, elegir el grupo muscular. */
    private void configurarEventos() {
        spGrupoMuscular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                tvErrorGrupoMuscular.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {
                // No se usa: el Spinner siempre entrega la opción inicial.
            }
        });
    }

    /** Oculta el error del nombre mientras el usuario escribe. */
    private void configurarLimpiarErrores() {
        etNombreEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence texto, int inicio, int cantidad, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence texto, int inicio, int antes, int cantidad) {
            }

            @Override
            public void afterTextChanged(Editable texto) {
                tvErrorNombreEjercicio.setVisibility(View.GONE);
                etNombreEjercicio.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }

    // ------------------------------------------------------------------ Validación

    /**
     * Revisa el nombre recortado y el grupo muscular elegido, deja visible el error del
     * campo que falló y devuelve true solo si se puede enviar la petición al backend.
     */
    private boolean validarFormulario(String nombre) {
        if (nombre.isEmpty()) {
            mostrarErrorNombre(R.string.tvErrorNombreEjercicio);
            return false;
        }

        if (nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            mostrarErrorNombre(R.string.tvErrorNombreEjercicioLargo);
            return false;
        }

        if (spGrupoMuscular.getSelectedItemPosition() == POSICION_SIN_GRUPO_SELECCIONADO) {
            tvErrorGrupoMuscular.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    /** Muestra el mensaje de error del nombre y devuelve el foco a ese campo. */
    private void mostrarErrorNombre(@StringRes int mensajeError) {
        tvErrorNombreEjercicio.setText(mensajeError);
        tvErrorNombreEjercicio.setVisibility(View.VISIBLE);
        etNombreEjercicio.setBackgroundResource(R.drawable.bg_input_error);
        etNombreEjercicio.requestFocus();
    }

    // ------------------------------------------------------------------ Guardado

    /** Valida el formulario y, si está completo, manda el ejercicio al backend. */
    private void guardarEjercicio() {
        if (guardandoEjercicio) {
            return;
        }

        String nombre = etNombreEjercicio.getText().toString().trim();

        if (!validarFormulario(nombre)) {
            return;
        }

        EjercicioRequest ejercicioRequest = new EjercicioRequest(
                nombre,
                obtenerGrupoMuscularSeleccionado()
        );

        mostrarGuardando(true);
        currentCallCrearEjercicio = ejercicioRepository.crearEjercicio(ejercicioRequest);
        currentCallCrearEjercicio.enqueue(new Callback<EjercicioResponse>() {

            @Override
            public void onResponse(@NonNull Call<EjercicioResponse> call,
                                   @NonNull Response<EjercicioResponse> response) {
                if (!isAdded()) {
                    return;
                }

                // El backend responde 201 cuando el ejercicio quedó registrado.
                if (response.isSuccessful() && response.body() != null) {
                    procesarCreacionExitosa(response.body());
                    return;
                }

                mostrarGuardando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<EjercicioResponse> call,
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

    /** Obtiene el grupo muscular elegido, escrito como lo espera el backend. */
    private String obtenerGrupoMuscularSeleccionado() {
        CharSequence grupoMuscular = (CharSequence) spGrupoMuscular.getSelectedItem();
        return grupoMuscular.toString();
    }

    /**
     * Avisa que el ejercicio se creó, informa a la pantalla que abrió esta para que
     * recargue sus ejercicios y regresa a ella.
     */
    private void procesarCreacionExitosa(EjercicioResponse ejercicioCreado) {
        Toast.makeText(
                requireContext(),
                getString(R.string.tvEjercicioCreado, ejercicioCreado.getNombre()),
                Toast.LENGTH_SHORT
        ).show();

        getParentFragmentManager().setFragmentResult(
                REQUEST_EJERCICIO_CREADO,
                new Bundle()
        );

        ((MainActivity) requireActivity()).regresar();
    }

    /**
     * Muestra el estado de guardado: bloquea los campos y la acción GUARDAR mientras
     * espera la respuesta, y los devuelve si la petición falla.
     */
    private void mostrarGuardando(boolean guardando) {
        guardandoEjercicio = guardando;
        pbGuardarEjercicio.setVisibility(guardando ? View.VISIBLE : View.GONE);
        etNombreEjercicio.setEnabled(!guardando);
        spGrupoMuscular.setEnabled(!guardando);

        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCrearEjercicio),
                true,
                getString(guardando ? R.string.btnGuardar_loading : R.string.btnGuardar)
        );
        activity.setAccionToolbar(this::guardarEjercicio);
        activity.habilitarAccionToolbar(!guardando);
    }
}
