package ue.edu.co.fittrackandroid.perfil.vista;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.ejercicios.datos.EjercicioRepository;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.ejercicios.vista.CrearEjercicioFragment;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.perfil.datos.FotoPerfilLocal;
import ue.edu.co.fittrackandroid.perfil.datos.PerfilRepository;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarNombreRequest;
import ue.edu.co.fittrackandroid.perfil.modelo.FotoPerfilResponse;
import ue.edu.co.fittrackandroid.perfil.modelo.UsuarioResponse;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Pantalla de perfil: foto, datos personales, ejercicios creados por el usuario, cambio de
 * contraseña y cierre de sesión. El nombre y el correo salen de la sesión guardada en
 * SharedPreferences, el mismo origen que usa el saludo de Inicio. La foto se guarda en el
 * backend y se conserva como archivo privado asociado al identificador del usuario.
 * Los ejercicios se consultan al backend.
 */
public class PerfilFragment extends Fragment {

    private static final int TAMANO_MAXIMO_FOTO_BYTES = 5 * 1024 * 1024;
    private static final int TAMANO_BUFFER_FOTO = 8 * 1024;
    private static final String TIPO_JPEG = "image/jpeg";
    private static final String TIPO_PNG = "image/png";

    // El registro de usuario admite nombres de hasta 255 caracteres; en Perfil se
    // aplica la misma regla para no enviar un nombre que el backend rechace.
    private static final int LONGITUD_MAXIMA_NOMBRE = 255;

    private ActivityResultLauncher<String[]> selectorImagen;

    private ImageView imgFotoPerfil;
    private ImageButton btnIconoCambiarFoto;
    private Button btnCambiarFoto;
    private Button btnQuitarFoto;
    private EditText etNombrePerfil;
    private TextView tvCorreoPerfil;
    private Button btnGuardarNombre;
    private Button btnNuevoEjercicio;
    private Button btnCambiarContrasena;
    private Button btnCerrarSesion;
    private Button btnBorrarCuenta;
    private TextView tvTituloLista;
    private ProgressBar pbCargaEjerciciosPerfil;
    private TextView tvSinEjerciciosPerfil;
    private RecyclerView rvEjerciciosPerfil;
    private SesionManager sesionManager;
    private FotoPerfilLocal fotoPerfilLocal;
    private Long usuarioId;
    private EjercicioRepository ejercicioRepository;
    private PerfilRepository perfilRepository;
    private Call<List<EjercicioResponse>> currentCallEjercicios;
    private Call<Void> currentCallCambiarNombre;
    private Call<UsuarioResponse> currentCallEliminarUsuario;
    private boolean eliminandoCuenta;
    private Call<FotoPerfilResponse> currentCallGuardarFoto;
    private Call<Void> currentCallQuitarFoto;
    private boolean guardandoNombre;
    private boolean actualizandoFoto;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El selector debe registrarse en onCreate, antes de que exista la vista.
        selectorImagen = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::subirFotoPerfil);
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
        usuarioId = sesionManager.obtenerUsuarioId();
        fotoPerfilLocal = new FotoPerfilLocal(requireContext());
        ejercicioRepository = new EjercicioRepository(requireContext());
        perfilRepository = new PerfilRepository(requireContext());

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
        btnQuitarFoto = view.findViewById(R.id.btnQuitarFoto);
        etNombrePerfil = view.findViewById(R.id.etNombrePerfil);
        tvCorreoPerfil = view.findViewById(R.id.tvCorreoPerfil);
        btnGuardarNombre = view.findViewById(R.id.btnGuardarNombre);
        btnNuevoEjercicio = view.findViewById(R.id.btnNuevoEjercicio);
        btnCambiarContrasena = view.findViewById(R.id.btnCambiarContrasena);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnBorrarCuenta = view.findViewById(R.id.btnBorrarCuenta);
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
     * Muestra el nombre y el correo guardados en la sesión, e intenta restaurar la copia
     * privada de la foto del usuario.
     */
    private void cargarDatosPerfil() {
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
        btnQuitarFoto.setOnClickListener(v -> quitarFoto());
        btnGuardarNombre.setOnClickListener(v -> guardarNombre());
        btnNuevoEjercicio.setOnClickListener(v -> abrirCrearEjercicio());
        btnCambiarContrasena.setOnClickListener(v -> abrirCambiarContrasena());
        btnCerrarSesion.setOnClickListener(v -> confirmarCierreSesion());
        btnBorrarCuenta.setOnClickListener(v -> confirmarBorradoCuenta());

        configurarLimpiarErrorNombre();
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
        if (actualizandoFoto) {
            return;
        }
        selectorImagen.launch(new String[]{"image/*"});
    }

    /**
     * Valida la imagen elegida y la sube como multipart. La copia local cambia
     * únicamente después de que el backend confirma que la guardó.
     *
     * @param uri dirección de la imagen elegida, o null si el usuario canceló
     */
    private void subirFotoPerfil(Uri uri) {
        if (uri == null || actualizandoFoto) {
            return;
        }

        if (usuarioId == null || usuarioId <= 0) {
            Toast.makeText(
                    requireContext(),
                    R.string.error_sesion_expirada,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String tipoContenido = requireContext().getContentResolver().getType(uri);
        if (!TIPO_JPEG.equals(tipoContenido) && !TIPO_PNG.equals(tipoContenido)) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnCambiarFoto_error_formato,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        byte[] contenidoFoto;
        try {
            contenidoFoto = leerContenidoFoto(uri);
        } catch (IOException | SecurityException error) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnCambiarFoto_error_lectura,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (contenidoFoto == null) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnCambiarFoto_error_tamano,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (contenidoFoto.length == 0) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnCambiarFoto_error_lectura,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        MediaType mediaType = MediaType.get(tipoContenido);

        String nombreArchivo = TIPO_PNG.equals(tipoContenido)
                ? "foto_perfil.png"
                : "foto_perfil.jpg";
        RequestBody cuerpoFoto = RequestBody.create(contenidoFoto, mediaType);
        MultipartBody.Part parteFoto = MultipartBody.Part.createFormData(
                "foto",
                nombreArchivo,
                cuerpoFoto
        );

        mostrarActualizandoFoto(true, false);
        currentCallGuardarFoto = perfilRepository.guardarFoto(parteFoto);
        currentCallGuardarFoto.enqueue(new Callback<FotoPerfilResponse>() {
            @Override
            public void onResponse(@NonNull Call<FotoPerfilResponse> call,
                                   @NonNull Response<FotoPerfilResponse> response) {
                if (!isAdded() || getView() == null) {
                    return;
                }

                if (response.isSuccessful()) {
                    if (response.body() != null
                            && response.body().getFotoPerfilUrl() != null
                            && !response.body().getFotoPerfilUrl().trim().isEmpty()) {
                        try {
                            fotoPerfilLocal.guardarDesdeBytes(usuarioId, contenidoFoto);
                        } catch (IOException error) {
                            mostrarActualizandoFoto(false, false);
                            Toast.makeText(
                                    requireContext(),
                                    R.string.btnCambiarFoto_error_guardado_local,
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        mostrarFotoLocal();
                        mostrarActualizandoFoto(false, false);
                        Toast.makeText(
                                requireContext(),
                                R.string.btnCambiarFoto_confirmacion,
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    mostrarActualizandoFoto(false, false);
                    Toast.makeText(
                            requireContext(),
                            R.string.error_respuesta_invalida,
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                mostrarActualizandoFoto(false, false);
                mostrarErrorFoto(response.code());
            }

            @Override
            public void onFailure(@NonNull Call<FotoPerfilResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded() || getView() == null) {
                    return;
                }

                mostrarActualizandoFoto(false, false);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /**
     * Lee la imagen sin permitir que se mantenga en memoria un archivo mayor de 5 MB.
     *
     * @param uri dirección entregada por el selector de documentos
     * @return contenido de la imagen, o null cuando supera el tamaño permitido
     * @throws IOException si el proveedor no permite leer la imagen
     */
    @Nullable
    private byte[] leerContenidoFoto(Uri uri) throws IOException {
        try (InputStream entrada = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            if (entrada == null) {
                throw new IOException("No se pudo abrir la imagen seleccionada");
            }

            byte[] buffer = new byte[TAMANO_BUFFER_FOTO];
            int bytesLeidos;
            int bytesTotales = 0;

            while ((bytesLeidos = entrada.read(buffer)) != -1) {
                bytesTotales += bytesLeidos;
                if (bytesTotales > TAMANO_MAXIMO_FOTO_BYTES) {
                    return null;
                }
                salida.write(buffer, 0, bytesLeidos);
            }

            return salida.toByteArray();
        }
    }

    /** Pide confirmación antes de quitar la foto del backend. */
    private void quitarFoto() {
        if (actualizandoFoto) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloQuitarFoto)
                .setMessage(R.string.tvMensajeQuitarFoto)
                .setPositiveButton(
                        R.string.btnConfirmarQuitarFoto,
                        (dialogo, cual) -> eliminarFotoPerfil()
                )
                .setNegativeButton(R.string.btnCancelarQuitarFoto, null)
                .show();
    }

    /** Envía la eliminación y limpia la foto local únicamente tras recibir HTTP 204. */
    private void eliminarFotoPerfil() {
        if (actualizandoFoto) {
            return;
        }

        if (usuarioId == null || usuarioId <= 0) {
            Toast.makeText(
                    requireContext(),
                    R.string.error_sesion_expirada,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        mostrarActualizandoFoto(true, true);
        currentCallQuitarFoto = perfilRepository.quitarFoto();
        currentCallQuitarFoto.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (!isAdded() || getView() == null) {
                    return;
                }

                if (response.isSuccessful()) {
                    fotoPerfilLocal.eliminar(usuarioId);
                    mostrarFotoPorDefecto();
                    mostrarActualizandoFoto(false, true);
                    Toast.makeText(
                            requireContext(),
                            R.string.btnQuitarFoto_confirmacion,
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                mostrarActualizandoFoto(false, true);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), response.code())
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded() || getView() == null) {
                    return;
                }

                mostrarActualizandoFoto(false, true);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /**
     * Bloquea las tres acciones de fotografía mientras una petición está en vuelo.
     *
     * @param actualizando true mientras se espera la respuesta del backend
     * @param quitando true cuando la operación actual es la eliminación
     */
    private void mostrarActualizandoFoto(boolean actualizando, boolean quitando) {
        actualizandoFoto = actualizando;
        imgFotoPerfil.setEnabled(!actualizando);
        btnIconoCambiarFoto.setEnabled(!actualizando);
        btnCambiarFoto.setEnabled(!actualizando);
        btnQuitarFoto.setEnabled(!actualizando);

        btnCambiarFoto.setText(
                actualizando && !quitando
                        ? R.string.btnCambiarFoto_loading
                        : R.string.btnCambiarFoto
        );
        btnQuitarFoto.setText(
                actualizando && quitando
                        ? R.string.btnQuitarFoto_loading
                        : R.string.btnQuitarFoto
        );
    }

    /** Muestra mensajes específicos para los rechazos de formato o tamaño. */
    private void mostrarErrorFoto(int codigoRespuesta) {
        if (codigoRespuesta == 413) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnCambiarFoto_error_tamano,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (codigoRespuesta == 415) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnCambiarFoto_error_formato,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ManejadorErroresApi
                .obtenerToast(requireContext(), codigoRespuesta)
                .show();
    }

    /** Vuelve a mostrar la copia privada, o la imagen predeterminada si no está disponible. */
    private void restaurarFotoPerfil() {
        mostrarFotoLocal();
    }

    /** Decodifica una versión ajustada al tamaño del avatar y la muestra en pantalla. */
    private void mostrarFotoLocal() {
        if (usuarioId == null || usuarioId <= 0) {
            mostrarFotoPorDefecto();
            return;
        }

        int tamanoAvatar = getResources().getDimensionPixelSize(R.dimen.avatar_size);
        Bitmap fotoPerfil = fotoPerfilLocal.cargarBitmap(
                usuarioId,
                tamanoAvatar,
                tamanoAvatar
        );
        if (fotoPerfil == null) {
            mostrarFotoPorDefecto();
            return;
        }

        // Sin relleno para que la foto ocupe todo el círculo.
        imgFotoPerfil.setPadding(0, 0, 0, 0);
        imgFotoPerfil.setImageBitmap(fotoPerfil);
    }

    private void mostrarFotoPorDefecto() {
        int relleno = getResources().getDimensionPixelSize(R.dimen.avatar_icon_padding);
        imgFotoPerfil.setPadding(relleno, relleno, relleno, relleno);
        imgFotoPerfil.setImageResource(R.drawable.ic_person_teal);
    }

    /**
     * Valida el nombre escrito y pide al backend que lo actualice. El nombre solo se
     * guarda en la sesión cuando el backend confirma el cambio con un 204, de modo que
     * un fallo no deja el nombre guardado con un valor que el servidor no conoce.
     * Si algo sale mal, el texto escrito se conserva para que el usuario reintente.
     */
    private void guardarNombre() {
        // Bloqueo de reintentos: el botón se deshabilita mientras la petición está en vuelo.
        if (guardandoNombre) {
            return;
        }

        String nombreUsuario = etNombrePerfil.getText().toString().trim();

        if (!validarNombre(nombreUsuario)) {
            return;
        }

        CambiarNombreRequest cambiarNombreRequest = new CambiarNombreRequest(nombreUsuario);

        mostrarGuardandoNombre(true);

        currentCallCambiarNombre = perfilRepository.cambiarNombre(cambiarNombreRequest);
        currentCallCambiarNombre.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                // El backend responde 204 sin cuerpo, así que solo importa el código.
                if (response.isSuccessful()) {
                    procesarNombreActualizado(nombreUsuario);
                    return;
                }

                mostrarGuardandoNombre(false);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), response.code())
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                mostrarGuardandoNombre(false);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /**
     * Revisa el nombre ya recortado y muestra el error correspondiente si no sirve.
     *
     * @param nombreUsuario nombre escrito por el usuario, sin espacios de los extremos.
     * @return true si el nombre se puede enviar al backend.
     */
    private boolean validarNombre(String nombreUsuario) {
        if (nombreUsuario.isEmpty()) {
            etNombrePerfil.setError(getString(R.string.etNombrePerfil_error));
            etNombrePerfil.requestFocus();
            return false;
        }

        if (nombreUsuario.length() > LONGITUD_MAXIMA_NOMBRE) {
            etNombrePerfil.setError(getString(R.string.etNombrePerfil_error_largo));
            etNombrePerfil.requestFocus();
            return false;
        }

        etNombrePerfil.setError(null);
        return true;
    }

    /**
     * Bloquea el formulario mientras se guarda el nombre, para no enviar dos veces, y
     * cambia el texto del botón para que se vea que la petición está en curso.
     *
     * @param guardando true si la llamada está en vuelo, false si ya terminó.
     */
    private void mostrarGuardandoNombre(boolean guardando) {
        guardandoNombre = guardando;
        etNombrePerfil.setEnabled(!guardando);
        btnGuardarNombre.setEnabled(!guardando);
        btnGuardarNombre.setText(
                guardando
                        ? R.string.btnGuardarNombre_loading
                        : R.string.btnGuardarNombre
        );
    }

    /**
     * Guarda en la sesión el nombre que el backend ya confirmó y lo deja visible.
     * El saludo de Inicio lee el mismo nombre, así que ya sale actualizado al volver.
     * El usuario se queda en el perfil, no se regresa a otra pantalla.
     *
     * @param nombreUsuario nombre ya validado y confirmado por el backend.
     */
    private void procesarNombreActualizado(String nombreUsuario) {
        sesionManager.guardarNombre(nombreUsuario);
        etNombrePerfil.setText(nombreUsuario);
        etNombrePerfil.setError(null);
        mostrarGuardandoNombre(false);

        Toast.makeText(requireContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show();
    }

    /** Oculta el error del nombre mientras el usuario escribe. */
    private void configurarLimpiarErrorNombre() {
        etNombrePerfil.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence texto, int inicio, int cantidad, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence texto, int inicio, int antes, int cantidad) {
            }

            @Override
            public void afterTextChanged(Editable texto) {
                // Solo se limpia el error: no se valida ni se llama al backend en cada tecla.
                etNombrePerfil.setError(null);
            }
        });
    }

    /**
     * Pide confirmación antes de borrar la cuenta. Todavía no hace nada: el backend
     * no expone el endpoint y el aviso de la pantalla le dice al usuario que la
     * acción aún no está disponible.
     */
    private void confirmarBorradoCuenta() {
        if (eliminandoCuenta) {
            return;
        }
        // decidirá si además se limpia la sesión con sesionManager.cerrarSesion().
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloBorrarCuenta)
                .setMessage(R.string.tvMensajeBorrarCuenta)
                .setPositiveButton(R.string.btnConfirmarBorrarCuenta, (dialogo, cual) ->
                        eliminarCuenta())
                .setNegativeButton(R.string.btnCancelarBorrarCuenta, null)
                .show();
    }

    private void eliminarCuenta() {

        if (eliminandoCuenta) {
            return;
        }

        eliminandoCuenta = true;
        btnBorrarCuenta.setEnabled(false);

        currentCallEliminarUsuario = perfilRepository.eliminarUsuario();

        currentCallEliminarUsuario.enqueue(new Callback<UsuarioResponse>() {

            @Override
            public void onResponse(
                    @NonNull Call<UsuarioResponse> call,
                    @NonNull Response<UsuarioResponse> response) {

                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    procesarCuentaEliminada();
                    return;
                }

                eliminandoCuenta = false;
                btnBorrarCuenta.setEnabled(true);

                ManejadorErroresApi
                        .obtenerToast(requireContext(), response.code())
                        .show();
            }

            @Override
            public void onFailure(
                    @NonNull Call<UsuarioResponse> call,
                    @NonNull Throwable throwable) {

                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                eliminandoCuenta = false;
                btnBorrarCuenta.setEnabled(true);

                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    private void procesarCuentaEliminada() {

        Toast.makeText(
                requireContext(),
                "Cuenta eliminada correctamente",
                Toast.LENGTH_SHORT
        ).show();

        ((MainActivity) requireActivity()).cerrarSesion();
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

    @Override
    public void onDestroyView() {
        if (currentCallEjercicios != null) {
            currentCallEjercicios.cancel();
        }

        // Si la pantalla se cierra mientras se guarda el nombre, la llamada se cancela
        // para no intentar tocar vistas de un Fragment que ya no está visible.
        if (currentCallCambiarNombre != null) {
            currentCallCambiarNombre.cancel();
        }

        if (currentCallGuardarFoto != null) {
            currentCallGuardarFoto.cancel();
        }

        if (currentCallQuitarFoto != null) {
            currentCallQuitarFoto.cancel();
        }

        if (currentCallEliminarUsuario != null) {
            currentCallEliminarUsuario.cancel();
        }

        actualizandoFoto = false;

        super.onDestroyView();
    }
}
