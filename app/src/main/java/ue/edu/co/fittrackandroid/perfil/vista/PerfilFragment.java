package ue.edu.co.fittrackandroid.perfil.vista;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.ejercicios.datos.EjercicioRepository;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.ejercicios.vista.CrearEjercicioFragment;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Pantalla de perfil: foto, datos personales, ejercicios creados por el usuario, cambio de
 * contraseña y cierre de sesión. El nombre y el correo salen de la sesión guardada en
 * SharedPreferences, el mismo origen que usa el saludo de Inicio; la foto sí se guarda
 * localmente en las preferencias del perfil. Los ejercicios se consultan al backend.
 */
public class PerfilFragment extends Fragment {

    private static final String PREFERENCIAS_PERFIL = "preferencias_perfil";
    private static final String CLAVE_URI_FOTO_PERFIL = "uri_foto_perfil";

    private ActivityResultLauncher<String[]> selectorImagen;

    private ImageView imgFotoPerfil;
    private ImageButton btnIconoCambiarFoto;
    private Button btnCambiarFoto;
    private EditText etNombrePerfil;
    private TextView tvCorreoPerfil;
    private Button btnGuardarNombre;
    private Button btnNuevoEjercicio;
    private Button btnCambiarContrasena;
    private Button btnCerrarSesion;
    private TextView tvTituloLista;
    private ProgressBar pbCargaEjerciciosPerfil;
    private TextView tvSinEjerciciosPerfil;
    private RecyclerView rvEjerciciosPerfil;
    private SesionManager sesionManager;
    private EjercicioRepository ejercicioRepository;
    private Call<List<EjercicioResponse>> currentCallEjercicios;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El selector debe registrarse en onCreate, antes de que exista la vista.
        selectorImagen = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::guardarFotoPerfil);
        registrarResultadoEjercicioCreado();
    }

    /**
     * Escucha el resultado de CrearEjercicioFragment y vuelve a consultar los ejercicios
     * del usuario, para que el recién creado aparezca sin tener que salir del perfil.
     */
    private void registrarResultadoEjercicioCreado() {
        getParentFragmentManager().setFragmentResultListener(
                CrearEjercicioFragment.REQUEST_EJERCICIO_CREADO,
                this,
                (clave, resultado) -> {
                    if (!isAdded() || ejercicioRepository == null) {
                        return;
                    }
                    cargarEjercicios();
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        inicializarVistas(view);
        sesionManager = new SesionManager(requireContext());
        ejercicioRepository = new EjercicioRepository(requireContext());

        // La lista de ejercicios se consulta en onResume, no aquí: cargarDatosPerfil()
        // sigue siendo el responsable de los datos de la sesión y de la foto local.
        cargarDatosPerfil();
        configurarAcciones();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // El perfil es una pestaña raíz: la toolbar debe verse siempre como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();

        // Se consulta en onResume para que, al volver desde CrearEjercicioFragment o desde
        // ModificarEjercicioFragment, la lista muestre los cambios o el ejercicio borrado.
        cargarEjercicios();
    }

    /** Busca las vistas de la pantalla y las guarda en los campos. */
    private void inicializarVistas(View view) {
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        btnIconoCambiarFoto = view.findViewById(R.id.btnIconoCambiarFoto);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        etNombrePerfil = view.findViewById(R.id.etNombrePerfil);
        tvCorreoPerfil = view.findViewById(R.id.tvCorreoPerfil);
        btnGuardarNombre = view.findViewById(R.id.btnGuardarNombre);
        btnNuevoEjercicio = view.findViewById(R.id.btnNuevoEjercicio);
        btnCambiarContrasena = view.findViewById(R.id.btnCambiarContrasena);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        tvTituloLista = view.findViewById(R.id.tvTituloLista);
        pbCargaEjerciciosPerfil = view.findViewById(R.id.pbCargaEjerciciosPerfil);
        tvSinEjerciciosPerfil = view.findViewById(R.id.tvSinEjerciciosPerfil);
        rvEjerciciosPerfil = view.findViewById(R.id.rvEjerciciosPerfil);

        // La lista vive dentro del desplazamiento general del perfil, por eso no debe
        // intentar desplazarse por separado.
        rvEjerciciosPerfil.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosPerfil.setNestedScrollingEnabled(false);
        rvEjerciciosPerfil.setHasFixedSize(false);
    }

    /**
     * Muestra el nombre y el correo guardados en la sesión, e intenta restaurar la foto
     * elegida anteriormente.
     */
    private void cargarDatosPerfil() {
        // TODO: Consultar la foto del usuario autenticado al backend, cuando exista.

        // TODO: Dejar de compartir estas preferencias entre todas las cuentas del dispositivo.
        // La foto local debe asociarse al identificador del usuario.

        // El nombre es editable y vive en la sesión, el mismo origen que usa el saludo de Inicio.
        etNombrePerfil.setText(sesionManager.obtenerNombre());

        // El correo no es editable: solo se muestra el que se usó para iniciar sesión.
        String correoUsuario = sesionManager.obtenerCorreo();
        if (correoUsuario == null || correoUsuario.trim().isEmpty()) {
            tvCorreoPerfil.setText(R.string.tvCorreoPerfil);
        } else {
            tvCorreoPerfil.setText(correoUsuario);
        }

        restaurarFotoPerfil();
    }

    /** Conecta los botones y la foto con las acciones de la pantalla. */
    private void configurarAcciones() {
        imgFotoPerfil.setOnClickListener(v -> abrirSelectorImagen());
        btnIconoCambiarFoto.setOnClickListener(v -> abrirSelectorImagen());
        btnCambiarFoto.setOnClickListener(v -> abrirSelectorImagen());
        btnGuardarNombre.setOnClickListener(v -> guardarNombre());
        btnNuevoEjercicio.setOnClickListener(v -> abrirCrearEjercicio());
        btnCambiarContrasena.setOnClickListener(v -> abrirCambiarContrasena());
        btnCerrarSesion.setOnClickListener(v -> confirmarCierreSesion());
    }

    /** Consulta los ejercicios creados por el usuario y los muestra en la tarjeta. */
    private void cargarEjercicios() {
        // Una consulta anterior puede seguir en vuelo si la pantalla se abrió de nuevo.
        if (currentCallEjercicios != null) {
            currentCallEjercicios.cancel();
        }

        mostrarCargaEjercicios();
        currentCallEjercicios = ejercicioRepository.getMisEjercicios();
        currentCallEjercicios.enqueue(new Callback<List<EjercicioResponse>>() {

            @Override
            public void onResponse(@NonNull Call<List<EjercicioResponse>> call,
                                   @NonNull Response<List<EjercicioResponse>> response) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    mostrarErrorEjercicios();
                    ManejadorErroresApi
                            .obtenerToast(requireContext(), response.code())
                            .show();
                    return;
                }

                List<EjercicioResponse> ejerciciosRecibidos = response.body();
                if (ejerciciosRecibidos.isEmpty()) {
                    mostrarEjerciciosVacios();
                    return;
                }

                mostrarEjercicios(ejerciciosRecibidos);
            }

            @Override
            public void onFailure(@NonNull Call<List<EjercicioResponse>> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                mostrarErrorEjercicios();
                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /** Deja visible únicamente el indicador mientras se consulta la API. */
    private void mostrarCargaEjercicios() {
        tvTituloLista.setText(getString(R.string.tvTituloLista, 0));
        pbCargaEjerciciosPerfil.setVisibility(View.VISIBLE);
        tvSinEjerciciosPerfil.setVisibility(View.GONE);
        rvEjerciciosPerfil.setVisibility(View.GONE);
    }

    /** Muestra los ejercicios recibidos en el RecyclerView. */
    private void mostrarEjercicios(List<EjercicioResponse> ejerciciosRecibidos) {
        tvTituloLista.setText(
                getString(R.string.tvTituloLista, ejerciciosRecibidos.size()));
        pbCargaEjerciciosPerfil.setVisibility(View.GONE);
        tvSinEjerciciosPerfil.setVisibility(View.GONE);
        rvEjerciciosPerfil.setAdapter(new PerfilEjercicioAdapter(
                ejerciciosRecibidos,
                this::abrirModificarEjercicio
        ));
        rvEjerciciosPerfil.setVisibility(View.VISIBLE);
    }

    /** Avisa que el usuario todavía no ha creado ningún ejercicio. */
    private void mostrarEjerciciosVacios() {
        tvTituloLista.setText(getString(R.string.tvTituloLista, 0));
        pbCargaEjerciciosPerfil.setVisibility(View.GONE);
        rvEjerciciosPerfil.setAdapter(null);
        rvEjerciciosPerfil.setVisibility(View.GONE);
        tvSinEjerciciosPerfil.setText(R.string.tvSinEjerciciosPerfil);
        tvSinEjerciciosPerfil.setVisibility(View.VISIBLE);
    }

    /** Presenta un estado estable cuando no fue posible cargar los ejercicios. */
    private void mostrarErrorEjercicios() {
        pbCargaEjerciciosPerfil.setVisibility(View.GONE);
        rvEjerciciosPerfil.setAdapter(null);
        rvEjerciciosPerfil.setVisibility(View.GONE);
        tvSinEjerciciosPerfil.setText(R.string.tvSinEjerciciosPerfil_error);
        tvSinEjerciciosPerfil.setVisibility(View.VISIBLE);
    }

    /** Abre la pantalla de crear un ejercicio nuevo. */
    private void abrirCrearEjercicio() {
        ((MainActivity) requireActivity()).mostrarCrearEjercicio();
    }

    /**
     * Abre la pantalla para consultar, modificar o borrar un ejercicio propio.
     * Solo viaja el identificador: el nombre y el grupo muscular actual se consultan
     * en el backend, así que la pantalla nunca trabaja con datos que ya podían quedar
     * viejos.
     */
    private void abrirModificarEjercicio(EjercicioResponse ejercicio) {
        ((MainActivity) requireActivity()).mostrarModificarEjercicio(ejercicio.getId());
    }

    /** Abre la pantalla de cambiar la contraseña de la cuenta. */
    private void abrirCambiarContrasena() {
        ((MainActivity) requireActivity()).mostrarCambiarContrasena();
    }

    /** Abre el selector de imágenes del sistema, filtrando solo por imágenes. */
    private void abrirSelectorImagen() {
        selectorImagen.launch(new String[]{"image/*"});
    }

    /**
     * Guarda la imagen elegida y la muestra.
     *
     * @param uri  dirección de la imagen elegida, o null si el usuario canceló.
     */
    private void guardarFotoPerfil(Uri uri) {
        if (uri == null) {
            return;
        }

        // TODO: Validar el tipo y tamaño de la imagen, subirla al almacenamiento definido
        // para el perfil y manejar los estados de carga, éxito y error.

        // Se pide permiso persistente para poder volver a leer la imagen al abrir el perfil.
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            // Algunos proveedores no lo entregan; la foto se podrá ver mientras la app siga abierta.
            // TODO: Informar al usuario o copiar la imagen al almacenamiento interno para no
            // conservar una URI que dejará de estar disponible al reiniciar la aplicación.
        }

        obtenerPreferencias().edit().putString(CLAVE_URI_FOTO_PERFIL, uri.toString()).apply();
        mostrarFoto(uri);
    }

    /** Vuelve a mostrar la foto guardada, o la predeterminada si ya no se puede leer. */
    private void restaurarFotoPerfil() {
        String uriFoto = obtenerPreferencias().getString(CLAVE_URI_FOTO_PERFIL, "");
        if (uriFoto.isEmpty()) {
            return; // El layout ya muestra la foto predeterminada.
        }
        mostrarFoto(Uri.parse(uriFoto));
    }

    private void mostrarFoto(Uri uri) {
        if (!puedeLeerse(uri)) {
            mostrarFotoPorDefecto();
            return;
        }
        // Sin relleno para que la foto ocupe todo el círculo.
        imgFotoPerfil.setPadding(0, 0, 0, 0);
        imgFotoPerfil.setImageURI(uri);
    }

    private void mostrarFotoPorDefecto() {
        int relleno = getResources().getDimensionPixelSize(R.dimen.avatar_icon_padding);
        imgFotoPerfil.setPadding(relleno, relleno, relleno, relleno);
        imgFotoPerfil.setImageResource(R.drawable.ic_person_teal);
    }

    private boolean puedeLeerse(Uri uri) {
        try (InputStream datos = requireContext().getContentResolver().openInputStream(uri)) {
            return datos != null;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    /** Valida y guarda el nombre escrito por el usuario. */
    private void guardarNombre() {
        String nombreUsuario = etNombrePerfil.getText().toString().trim();

        if (nombreUsuario.isEmpty()) {
            etNombrePerfil.setError(getString(R.string.etNombrePerfil_error));
            etNombrePerfil.requestFocus();
            return;
        }

        // TODO: Actualizar el nombre en la fuente de datos del usuario y guardarlo solo
        // después de confirmar el resultado. Mientras se guarda, deshabilitar el botón
        // para evitar solicitudes duplicadas y conservar el texto si ocurre un error.
        // Se guarda junto al correo para no perderlo, porque los dos viven en la sesión.
        sesionManager.guardarInfoPersonal(nombreUsuario, sesionManager.obtenerCorreo());

        // El saludo de Inicio lee el mismo nombre, así que ya sale actualizado.
        etNombrePerfil.setError(null);
        Toast.makeText(requireContext(), "Nombre guardado", Toast.LENGTH_SHORT).show();
    }

    /** Pide confirmación antes de cerrar la sesión. */
    private void confirmarCierreSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloCerrarSesion)
                .setMessage(R.string.tvMensajeCerrarSesion)
                .setPositiveButton(R.string.btnConfirmarCerrarSesion,
                        (dialogo, cual) -> ((MainActivity) requireActivity()).cerrarSesion())
                .setNegativeButton(R.string.btnCancelarCerrarSesion, null)
                .show();
    }

    private SharedPreferences obtenerPreferencias() {
        return requireContext().getSharedPreferences(PREFERENCIAS_PERFIL, Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroyView() {
        if (currentCallEjercicios != null) {
            currentCallEjercicios.cancel();
        }
        super.onDestroyView();
    }
}
