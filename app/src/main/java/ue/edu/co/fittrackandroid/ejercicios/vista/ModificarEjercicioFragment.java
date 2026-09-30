package ue.edu.co.fittrackandroid.ejercicios.vista;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
 * Fragment para consultar, modificar y eliminar un ejercicio que el usuario ya tiene guardado.
 * Abre con los datos actuales del ejercicio, que se consultan con {@code GET /ejercicios/{id}};
 * solo se pasa el identificador porque el nombre y el grupo muscular pueden haber cambiado
 * desde que se cargó la lista del perfil.
 * Los cambios se envían con {@code PUT /ejercicios/{id}} y el borrado con
 * {@code DELETE /ejercicios/{id}}, que es lógico: el ejercicio deja de aparecer en el perfil
 * y en los ejercicios disponibles, pero no se borra de la base de datos.
 */
public class ModificarEjercicioFragment extends Fragment {

    /** Clave de los argumentos con el identificador del ejercicio que se está modificando. */
    private static final String ARG_EJERCICIO_ID = "ejercicioId";

    /** El backend no admite nombres de más de 100 caracteres. */
    private static final int LONGITUD_MAXIMA_NOMBRE = 100;

    /** Posición de la opción inicial del Spinner: todavía no es un grupo válido. */
    private static final int POSICION_SIN_GRUPO_SELECCIONADO = 0;

    private Long ejercicioId;
    private String nombreEjercicio = "";

    private EditText etNombreEjercicio;
    private Spinner spGrupoMuscular;
    private TextView tvErrorNombreEjercicio;
    private TextView tvErrorGrupoMuscular;
    private Button btnBorrarEjercicio;

    private View svModificarEjercicio;
    private View layoutErrorModificarEjercicio;
    private View layoutCargaModificarEjercicio;

    private EjercicioRepository ejercicioRepository;

    private Call<EjercicioResponse> currentCallDetalle;
    private Call<EjercicioResponse> currentCallActualizar;
    private Call<Void> currentCallEliminar;

    /** Indica que el detalle ya llegó y el formulario se puede modificar y guardar. */
    private boolean ejercicioCargado;

    /** Evita mandar una segunda petición mientras la primera sigue en vuelo. */
    private boolean procesandoPeticion;

    public ModificarEjercicioFragment() {
        // Required empty public constructor
    }

    /**
     * Crea la pantalla para un ejercicio concreto. El identificador viaja en los argumentos
     * porque el Fragment lo reconstruye Android cuando rota el dispositivo.
     */
    public static ModificarEjercicioFragment newInstance(Long ejercicioId) {
        ModificarEjercicioFragment fragment = new ModificarEjercicioFragment();
        Bundle argumentos = new Bundle();
        argumentos.putLong(ARG_EJERCICIO_ID, ejercicioId);
        fragment.setArguments(argumentos);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modificar_ejercicio, container, false);

        inicializarVistas(view);

        ejercicioRepository = new EjercicioRepository(requireContext());
        leerIdentificadorEjercicio();

        configurarSpinner();
        configurarEventos();
        cargarEjercicio();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // La toolbar se reconfigura aquí porque al volver de otra pantalla todavía
        // muestra la pantalla anterior.
        configurarToolbar();
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
        etNombreEjercicio = view.findViewById(R.id.etNombreEjercicio);
        spGrupoMuscular = view.findViewById(R.id.spGrupoMuscular);
        tvErrorNombreEjercicio = view.findViewById(R.id.tvErrorNombreEjercicio);
        tvErrorGrupoMuscular = view.findViewById(R.id.tvErrorGrupoMuscular);
        btnBorrarEjercicio = view.findViewById(R.id.btnBorrarEjercicio);
        svModificarEjercicio = view.findViewById(R.id.svModificarEjercicio);
        layoutErrorModificarEjercicio = view.findViewById(R.id.layoutErrorModificarEjercicio);
        layoutCargaModificarEjercicio = view.findViewById(R.id.layoutCargaModificarEjercicio);
    }

    /** Lee de los argumentos el identificador del ejercicio abierto desde el perfil. */
    private void leerIdentificadorEjercicio() {
        Bundle argumentos = getArguments();

        if (argumentos == null || !argumentos.containsKey(ARG_EJERCICIO_ID)) {
            ejercicioId = null;
            return;
        }

        ejercicioId = argumentos.getLong(ARG_EJERCICIO_ID);
    }

    /** Llena el Spinner con los mismos grupos musculares que permite Crear ejercicio. */
    private void configurarSpinner() {
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

    /** Conecta la pantalla con las acciones: escribir el nombre y elegir el grupo muscular. */
    private void configurarEventos() {
        btnBorrarEjercicio.setOnClickListener(v -> confirmarBorrado());

        // El reintento vuelve a consultar el ejercicio por su identificador.
        Button btnReintentarModificarEjercicio = layoutErrorModificarEjercicio
                .findViewById(R.id.btnReintentarModificarEjercicio);
        btnReintentarModificarEjercicio.setOnClickListener(v -> cargarEjercicio());

        spGrupoMuscular.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                tvErrorGrupoMuscular.setVisibility(View.GONE);
                // El grupo puede ser lo único que falte para poder guardar, por eso la
                // toolbar se revisa cada vez que cambia la opción elegida.
                if (isAdded()) {
                    configurarToolbar();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {
                // No se usa: el Spinner siempre entrega la opción inicial.
            }
        });

        etNombreEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence texto, int inicio, int cantidad, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence texto, int inicio, int antes, int cantidad) {
            }

            @Override
            public void afterTextChanged(Editable texto) {
                nombreEjercicio = texto.toString();
                tvErrorNombreEjercicio.setVisibility(View.GONE);
                etNombreEjercicio.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }

    // ------------------------------------------------------------------ Estados de la pantalla

    /** Deja visible únicamente el indicador mientras se consulta el ejercicio. */
    private void mostrarCargaInicial() {
        svModificarEjercicio.setVisibility(View.GONE);
        layoutErrorModificarEjercicio.setVisibility(View.GONE);
        layoutCargaModificarEjercicio.setVisibility(View.VISIBLE);
        configurarToolbar();
    }

    /** Muestra el formulario con el ejercicio ya cargado. */
    private void mostrarContenido() {
        layoutCargaModificarEjercicio.setVisibility(View.GONE);
        layoutErrorModificarEjercicio.setVisibility(View.GONE);
        svModificarEjercicio.setVisibility(View.VISIBLE);
        configurarToolbar();
    }

    /** Muestra el mensaje de error y deja el reintento disponible. */
    private void mostrarErrorCarga() {
        layoutCargaModificarEjercicio.setVisibility(View.GONE);
        svModificarEjercicio.setVisibility(View.GONE);
        layoutErrorModificarEjercicio.setVisibility(View.VISIBLE);
        configurarToolbar();
    }

    /**
     * Muestra o quita la capa que cubre la pantalla mientras espera una respuesta.
     */
    private void mostrarProcesando(boolean procesando) {
        procesandoPeticion = procesando;
        layoutCargaModificarEjercicio.setVisibility(procesando ? View.VISIBLE : View.GONE);
        configurarToolbar();
    }

    // ------------------------------------------------------------------ Toolbar

    /**
     * Coloca la toolbar de la pantalla: título, flecha de volver y la acción de guardar.
     * Guardar queda deshabilitado mientras el ejercicio no está cargado, no hay un grupo
     * muscular válido o hay una petición en vuelo, para no mandar datos a medias.
     */
    private void configurarToolbar() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloModificarEjercicio),
                true,
                getString(R.string.btnGuardarCambiosEjercicio)
        );
        activity.setAccionToolbar(this::guardarCambios);
        activity.habilitarAccionToolbar(puedeGuardar());
    }

    /**
     * Indica si el ejercicio ya llegó y hay un grupo muscular válido elegido, para
     * que la acción de la toolbar quede disponible.
     */
    private boolean puedeGuardar() {
        return ejercicioCargado
                && !procesandoPeticion
                && spGrupoMuscular.getSelectedItemPosition() != POSICION_SIN_GRUPO_SELECCIONADO;
    }

    // ------------------------------------------------------------------ Consulta del ejercicio

    /**
     * Consulta el ejercicio por su identificador y llena el formulario con lo que devuelve
     * la API. El backend solo deja consultar los ejercicios del usuario
     * autenticado, por eso un identificador inválido se trata como un error de carga.
     */
    private void cargarEjercicio() {
        mostrarCargaInicial();

        if (ejercicioId == null || ejercicioId <= 0) {
            mostrarErrorCarga();
            return;
        }

        // Una consulta anterior puede seguir en vuelo si se pulsa el reintento.
        if (currentCallDetalle != null) {
            currentCallDetalle.cancel();
        }

        currentCallDetalle = ejercicioRepository.getEjercicioById(ejercicioId);
        currentCallDetalle.enqueue(new Callback<EjercicioResponse>() {

            @Override
            public void onResponse(@NonNull Call<EjercicioResponse> call,
                                   @NonNull Response<EjercicioResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    cargarDatosEnFormulario(response.body());
                    mostrarContenido();
                    return;
                }

                mostrarErrorCarga();
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<EjercicioResponse> call,
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
     * Escribe en el formulario lo que tiene el ejercicio guardado: el nombre y el grupo
     * muscular. El grupo se busca por texto entre las opciones del desplegable, porque sus
     * posiciones dependen del arreglo de recursos.
     */
    private void cargarDatosEnFormulario(EjercicioResponse ejercicio) {
        nombreEjercicio = ejercicio.getNombre();
        etNombreEjercicio.setText(nombreEjercicio);

        seleccionarGrupoMuscular(ejercicio.getGrupoMuscular());

        ejercicioCargado = true;
    }

    /**
     * Marca en el Spinner el grupo muscular que ya tenía el ejercicio. Se recorre la lista
     * de opciones y se compara el texto, no la posición, para que funcione aunque el arreglo
     * de grupos cambie de orden.
     */
    private void seleccionarGrupoMuscular(String grupoMuscular) {
        if (grupoMuscular != null) {
            for (int posicion = 1; posicion < spGrupoMuscular.getCount(); posicion++) {
                String opcion = spGrupoMuscular.getItemAtPosition(posicion).toString();

                if (opcion.equalsIgnoreCase(grupoMuscular.trim())) {
                    spGrupoMuscular.setSelection(posicion);
                    return;
                }
            }
        }

        // El ejercicio trae un grupo que no está entre las opciones: se deja el texto de
        // ayuda y se avisa, porque sin un grupo válido no se puede guardar.
        spGrupoMuscular.setSelection(POSICION_SIN_GRUPO_SELECCIONADO);
        tvErrorGrupoMuscular.setVisibility(View.VISIBLE);
    }

    // ------------------------------------------------------------------ Validación

    /**
     * Revisa los datos escritos y deja visible el error del campo que falló.
     * Las reglas son las mismas de Crear ejercicio: el nombre es obligatorio, no puede
     * superar los 100 caracteres y el grupo muscular tiene que ser uno de los permitidos.
     * Los nombres repetidos no se revisan aquí porque el backend los admite.
     * Devuelve true solo si se puede enviar la petición al backend.
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

    /** Muestra el mensaje de error del nombre y lo destaca en el campo. */
    private void mostrarErrorNombre(@StringRes int mensajeError) {
        tvErrorNombreEjercicio.setText(mensajeError);
        tvErrorNombreEjercicio.setVisibility(View.VISIBLE);
        etNombreEjercicio.setBackgroundResource(R.drawable.bg_input_error);
        etNombreEjercicio.requestFocus();
    }

    /** Obtiene el grupo muscular elegido, escrito como lo espera el backend. */
    private String obtenerGrupoMuscularSeleccionado() {
        CharSequence grupoMuscular = (CharSequence) spGrupoMuscular.getSelectedItem();
        return grupoMuscular.toString();
    }

    // ------------------------------------------------------------------ Guardado de los cambios

    /**
     * Valida el formulario y, si está completo, manda los cambios al backend.
     * Si la petición falla el formulario se conserva tal como estaba para que el
     * usuario pueda corregir lo que haga falta y volver a intentarlo.
     */
    private void guardarCambios() {
        // No se puede guardar y borrar al mismo tiempo, ni repetir la misma operación.
        if (procesandoPeticion || !ejercicioCargado) {
            return;
        }

        String nombre = nombreEjercicio.trim();

        if (!validarFormulario(nombre)) {
            return;
        }

        // El backend usa el mismo cuerpo para crear y para actualizar.
        EjercicioRequest ejercicioRequest = new EjercicioRequest(
                nombre,
                obtenerGrupoMuscularSeleccionado()
        );

        mostrarProcesando(true);
        currentCallActualizar = ejercicioRepository.actualizarEjercicio(
                ejercicioId,
                ejercicioRequest
        );
        currentCallActualizar.enqueue(new Callback<EjercicioResponse>() {

            @Override
            public void onResponse(@NonNull Call<EjercicioResponse> call,
                                   @NonNull Response<EjercicioResponse> response) {
                if (!isAdded()) {
                    return;
                }

                // El backend responde 200 cuando el ejercicio quedó actualizado.
                if (response.isSuccessful() && response.body() != null) {
                    procesarEjercicioActualizado(response.body());
                    return;
                }

                mostrarProcesando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<EjercicioResponse> call,
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
     * Avisa que el ejercicio quedó actualizado y regresa al perfil. No hace falta avisar
     * con un FragmentResult: PerfilFragment vuelve a consultar sus ejercicios en onResume.
     */
    private void procesarEjercicioActualizado(EjercicioResponse ejercicioActualizado) {
        Toast.makeText(
                requireContext(),
                getString(R.string.tvEjercicioActualizado, ejercicioActualizado.getNombre()),
                Toast.LENGTH_SHORT
        ).show();

        ((MainActivity) requireActivity()).regresar();
    }

    // ------------------------------------------------------------------ Borrado del ejercicio

    /** Pide confirmación antes de borrar: el borrado no se puede deshacer. */
    private void confirmarBorrado() {
        if (!isAdded()) {
            return;
        }

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloConfirmarBorrarEjercicio)
                .setMessage(getString(R.string.tvMensajeConfirmarBorrarEjercicio, nombreEjercicio))
                .setPositiveButton(R.string.btnConfirmarBorrarEjercicio,
                        (dialogoVisible, cual) -> borrarEjercicio())
                .setNegativeButton(R.string.btnCancelarBorrarEjercicio, null)
                .create();

        dialogo.show();
        // El botón de borrar queda en rojo para que se lea como una acción destructiva.
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.colorErrorText));
    }

    /**
     * Pide al backend eliminar el ejercicio. El borrado es lógico: el ejercicio deja de
     * aparecer en el perfil y entre los ejercicios disponibles, pero sigue existiendo en
     * la base de datos.
     */
    private void borrarEjercicio() {
        // No se puede borrar y guardar al mismo tiempo, ni repetir la misma operación.
        if (procesandoPeticion || !isAdded()) {
            return;
        }

        mostrarProcesando(true);
        currentCallEliminar = ejercicioRepository.eliminarEjercicio(ejercicioId);
        currentCallEliminar.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                // La respuesta no trae cuerpo (204), así que basta con que el código sea
                // correcto: no se puede leer response.body() en una llamada de tipo Void.
                if (response.isSuccessful()) {
                    procesarEjercicioEliminado();
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

                // La pantalla y el ejercicio se conservan para que el usuario pueda reintentar.
                mostrarProcesando(false);
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Avisa que el ejercicio se borró y regresa al perfil, donde la lista se vuelve a
     * consultar y el ejercicio ya no aparece.
     */
    private void procesarEjercicioEliminado() {
        Toast.makeText(
                requireContext(),
                getString(R.string.tvEjercicioEliminado),
                Toast.LENGTH_SHORT
        ).show();

        ((MainActivity) requireActivity()).regresar();
    }
}
