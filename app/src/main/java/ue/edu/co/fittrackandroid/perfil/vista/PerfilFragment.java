package ue.edu.co.fittrackandroid.perfil.vista;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.ejercicios.datos.EjercicioRepository;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.ejercicios.vista.CrearEjercicioFragment;
import ue.edu.co.fittrackandroid.imagenes.GaleriaImagenesFragment;
import ue.edu.co.fittrackandroid.imagenes.PermisosImagenes;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.entrenamiento.datos.PreferenciasEntrenamientoDataStore;
import ue.edu.co.fittrackandroid.perfil.datos.FotoPerfilLocal;
import ue.edu.co.fittrackandroid.perfil.datos.PerfilRepository;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarNombreRequest;
import ue.edu.co.fittrackandroid.perfil.modelo.FotoPerfilResponse;
import ue.edu.co.fittrackandroid.perfil.modelo.UsuarioResponse;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Pantalla de perfil: foto, datos personales, configuración del entrenamiento, ejercicios
 * creados por el usuario, cambio de contraseña y cierre de sesión. El nombre y el correo
 * salen de la sesión guardada en SharedPreferences, el mismo origen que usa el saludo de
 * Inicio. La foto se guarda en el backend y se conserva como archivo privado asociado al
 * identificador del usuario. La duración del descanso predeterminado sale de DataStore.
 * Los ejercicios se consultan al backend.
 */
public class PerfilFragment extends Fragment {

    private static final int TAMANO_MAXIMO_FOTO_BYTES = 5 * 1024 * 1024;
    private static final int TAMANO_BUFFER_FOTO = 8 * 1024;
    private static final String TIPO_JPEG = "image/jpeg";
    private static final String TIPO_PNG = "image/png";

    /**
     * Opacidad de los botones de descanso que ya llegaron a su límite. Android no cambia
     * la apariencia de un Button al deshabilitarlo cuando el botón tiene su propio color,
     * así que se baja la opacidad a mano para que se note que no se puede seguir pulsando.
     */
    private static final float ALFA_BOTON_DESHABILITADO = 0.4f;

    // El registro de usuario admite nombres de hasta 255 caracteres; en Perfil se
    // aplica la misma regla para no enviar un nombre que el backend rechace.
    private static final int LONGITUD_MAXIMA_NOMBRE = 255;

    private ActivityResultLauncher<String[]> permisoGaleriaLauncher;

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
    private TextView tvTiempoDescansoPredeterminado;
    private Button btnRestarDescansoPredeterminado;
    private Button btnSumarDescansoPredeterminado;
    private SesionManager sesionManager;
    private FotoPerfilLocal fotoPerfilLocal;
    private Long usuarioId;
    private EjercicioRepository ejercicioRepository;
    private PerfilRepository perfilRepository;
    private PreferenciasEntrenamientoDataStore preferenciasEntrenamiento;
    private Call<List<EjercicioResponse>> currentCallEjercicios;
    private Call<Void> currentCallCambiarNombre;
    private Call<UsuarioResponse> currentCallEliminarUsuario;
    private boolean eliminandoCuenta;
    private Call<FotoPerfilResponse> currentCallGuardarFoto;
    private Call<Void> currentCallQuitarFoto;
    private boolean guardandoNombre;
    private boolean actualizandoFoto;

    /** Descanso que se ve en pantalla mientras llega la preferencia guardada. */
    private int segundosDescansoSeleccionados =
            PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_PREDETERMINADO;

    /** Último descanso que DataStore confirmó, para volver atrás si una escritura falla. */
    private int segundosDescansoGuardados = segundosDescansoSeleccionados;

    /** Lecturas y escrituras de la preferencia, que se cancelan al salir de la pantalla. */
    private final CompositeDisposable suscripcionesDescanso = new CompositeDisposable();

    /**
     * DataStore entrega el resultado en un hilo de trabajo, así que todo lo que llega de
     * allí se pasa primero por el hilo principal antes de tocar las vistas.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Los lanzadores se registran en onCreate, antes de que exista la vista, para que
        // el resultado pueda llegar aunque la pantalla se haya recreado.
        permisoGaleriaLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::procesarResultadoPermisoGaleria);
        registrarResultadoImagenSeleccionada();
        registrarResultadoEjercicioCreado();
    }

    /**
     * Escucha la fotografía elegida en la galería propia de FitTrack. La galería devuelve
     * la Uri y el destino que se le indicó al abrirla; este fragmento solo responde cuando
     * el destino es el perfil, y después sube la imagen con el flujo de siempre.
     */
    private void registrarResultadoImagenSeleccionada() {
        getParentFragmentManager().setFragmentResultListener(
                GaleriaImagenesFragment.REQUEST_IMAGEN_SELECCIONADA,
                this,
                (clave, resultado) -> {
                    String destino = resultado.getString(GaleriaImagenesFragment.EXTRA_DESTINO);
                    if (!GaleriaImagenesFragment.DESTINO_PERFIL.equals(destino)) {
                        return;
                    }

                    Uri uriImagen = resultado.getParcelable(
                            GaleriaImagenesFragment.EXTRA_URI_IMAGEN);
                    subirFotoPerfil(uriImagen);
                });
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
        preferenciasEntrenamiento =
                PreferenciasEntrenamientoDataStore.obtenerInstancia(requireContext());

        // La lista de ejercicios se consulta en onResume, no aquí: cargarDatosPerfil()
        // sigue siendo el responsable de los datos de la sesión y de la foto local.
        cargarDatosPerfil();
        cargarDescansoPredeterminado();
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
        tvTiempoDescansoPredeterminado = view.findViewById(R.id.tvTiempoDescansoPredeterminado);
        btnRestarDescansoPredeterminado =
                view.findViewById(R.id.btnRestarDescansoPredeterminado);
        btnSumarDescansoPredeterminado =
                view.findViewById(R.id.btnSumarDescansoPredeterminado);

        // La lista vive dentro del desplazamiento general del perfil, por eso no debe
        // intentar desplazarse por separado.
        rvEjerciciosPerfil.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosPerfil.setNestedScrollingEnabled(false);
        rvEjerciciosPerfil.setHasFixedSize(false);

        // Hasta que llegue la preferencia guardada se muestra el valor seguro y los botones
        // quedan apagados, para no sobrescribir algo que todavía no se ha leído.
        mostrarTiempoDescansoPredeterminado();
        mostrarBotonDescanso(btnRestarDescansoPredeterminado, false);
        mostrarBotonDescanso(btnSumarDescansoPredeterminado, false);
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
        btnRestarDescansoPredeterminado.setOnClickListener(
                v -> cambiarDescansoPredeterminado(
                        -PreferenciasEntrenamientoDataStore.PASO_AJUSTE_DESCANSO));
        btnSumarDescansoPredeterminado.setOnClickListener(
                v -> cambiarDescansoPredeterminado(
                        PreferenciasEntrenamientoDataStore.PASO_AJUSTE_DESCANSO));

        configurarLimpiarErrorNombre();
    }

    // ------------------------------------------------------------------ Descanso predeterminado

    /**
     * Lee de DataStore la duración del descanso que el usuario dejó configurada y la
     * muestra. Si la lectura falla, la pantalla sigue operativa con los tres minutos de
     * siempre: es mejor mostrar un valor conocido que dejar los botones apagados.
     */
    private void cargarDescansoPredeterminado() {
        int segundosDeRespaldo =
                PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_PREDETERMINADO;

        suscripcionesDescanso.add(
                preferenciasEntrenamiento.obtenerSegundosDescanso().subscribe(
                        segundosDescanso -> enPantalla(
                                () -> mostrarDescansoLeido(segundosDescanso)),
                        error -> enPantalla(() -> mostrarDescansoLeido(segundosDeRespaldo))
                ));
    }

    /**
     * Pone en pantalla el descanso que DataStore devolvió y habilita los botones según
     * los límites permitidos.
     *
     * @param segundosDescanso duración guardada, en segundos.
     */
    private void mostrarDescansoLeido(int segundosDescanso) {
        segundosDescansoSeleccionados = segundosDescanso;
        segundosDescansoGuardados = segundosDescanso;
        mostrarTiempoDescansoPredeterminado();
        actualizarBotonesDescansoPredeterminado();
    }

    /**
     * Suma o resta el paso indicado, respeta el rango permitido, actualiza la pantalla y
     * guarda el resultado de inmediato. No hay botón de guardar: cada cambio queda
     * escrito en DataStore.
     *
     * @param cambioSegundos cuántos segundos suma o resta la pulsación.
     */
    private void cambiarDescansoPredeterminado(int cambioSegundos) {
        segundosDescansoSeleccionados = limitarDescansoSeleccionado(
                segundosDescansoSeleccionados + cambioSegundos);

        mostrarTiempoDescansoPredeterminado();
        actualizarBotonesDescansoPredeterminado();

        guardarDescansoPredeterminado(segundosDescansoSeleccionados);
    }

    /**
     * @param segundosDescanso valor elegido por el usuario, que puede pasarse del límite.
     * @return el mismo valor si es válido, o el límite más cercano si no lo es.
     */
    private int limitarDescansoSeleccionado(int segundosDescanso) {
        if (segundosDescanso < PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_MINIMO) {
            return PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_MINIMO;
        }

        if (segundosDescanso > PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_MAXIMO) {
            return PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_MAXIMO;
        }

        return segundosDescanso;
    }

    /** Escribe el descanso en DataStore y recuerda el valor cuando queda confirmado. */
    private void guardarDescansoPredeterminado(int segundosDescanso) {
        suscripcionesDescanso.add(
                preferenciasEntrenamiento.guardarSegundosDescanso(segundosDescanso).subscribe(
                        () -> enPantalla(() -> segundosDescansoGuardados = segundosDescanso),
                        error -> enPantalla(this::restaurarUltimoDescansoGuardado)
                ));
    }

    /**
     * Vuelve al último descanso que DataStore confirmó. Así un fallo de escritura no
     * deja en pantalla un valor que el dispositivo nunca guardó.
     */
    private void restaurarUltimoDescansoGuardado() {
        segundosDescansoSeleccionados = segundosDescansoGuardados;
        mostrarTiempoDescansoPredeterminado();
        actualizarBotonesDescansoPredeterminado();

        Toast.makeText(
                requireContext(),
                "No se pudo guardar el descanso, se restauró el anterior",
                Toast.LENGTH_SHORT
        ).show();
    }

    /** Muestra el descanso seleccionado con el formato de minutos y segundos: 03:00. */
    private void mostrarTiempoDescansoPredeterminado() {
        tvTiempoDescansoPredeterminado.setText(
                getString(R.string.tvTiempoDescansoPredeterminado_formato,
                        formatearTiempoDescanso(segundosDescansoSeleccionados)));
    }

    /**
     * Apaga el botón de restar cuando ya se llegó al mínimo y el de sumar cuando ya se
     * llegó al máximo.
     */
    private void actualizarBotonesDescansoPredeterminado() {
        mostrarBotonDescanso(btnRestarDescansoPredeterminado,
                segundosDescansoSeleccionados
                        > PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_MINIMO);
        mostrarBotonDescanso(btnSumarDescansoPredeterminado,
                segundosDescansoSeleccionados
                        < PreferenciasEntrenamientoDataStore.SEGUNDOS_DESCANSO_MAXIMO);
    }

    /**
     * Habilita o deshabilita un botón de descanso. Un botón deshabilitado además se ve
     * más claro, porque el tema no cambia la apariencia de los botones al apagarlos.
     *
     * @param botonDescanso botón de restar o de sumar.
     * @param habilitado true si el usuario todavía puede pulsarlo.
     */
    private void mostrarBotonDescanso(Button botonDescanso, boolean habilitado) {
        botonDescanso.setEnabled(habilitado);
        botonDescanso.setAlpha(habilitado ? 1f : ALFA_BOTON_DESHABILITADO);
    }

    /**
     * @param segundosDescanso duración a mostrar, en segundos.
     * @return el tiempo con el formato mm:ss, por ejemplo 03:00 o 00:30.
     */
    private String formatearTiempoDescanso(int segundosDescanso) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                segundosDescanso / 60, segundosDescanso % 60);
    }

    /**
     * Ejecuta la acción en el hilo principal y solo si la vista del perfil sigue viva.
     * Las lecturas y escrituras de DataStore terminan en un hilo de trabajo, así que
     * nunca se tocan las vistas directamente desde allí.
     *
     * @param accion cambio que se debe aplicar a la pantalla.
     */
    private void enPantalla(Runnable accion) {
        handler.post(() -> {
            if (!isAdded() || getView() == null) {
                return;
            }
            accion.run();
        });
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

    /**
     * Punto de entrada de las tres acciones de fotografía: comprueba el permiso de
     * almacenamiento que corresponde a la versión de Android y, si está concedido, abre
     * la galería propia de FitTrack. Si falta, lo solicita.
     */
    private void abrirSelectorImagen() {
        if (actualizandoFoto) {
            return;
        }

        if (PermisosImagenes.tienePermisoLectura(requireContext())) {
            abrirGaleria();
            return;
        }

        permisoGaleriaLauncher.launch(PermisosImagenes.permisosSolicitados());
    }

    /** Abre la galería de imágenes de FitTrack para elegir la foto de perfil. */
    private void abrirGaleria() {
        ((MainActivity) requireActivity())
                .mostrarGaleriaImagenes(GaleriaImagenesFragment.DESTINO_PERFIL);
    }

    /**
     * Revisa el resultado de pedir el permiso de almacenamiento. Con cualquier acceso
     * (completo o parcial) se abre la galería; sin acceso se explica por qué FitTrack
     * necesita leer las imágenes y, si ya no se puede volver a preguntar, se ofrecen
     * los ajustes de la aplicación.
     *
     * @param resultados permiso o permisos solicitados con su estado final
     */
    private void procesarResultadoPermisoGaleria(Map<String, Boolean> resultados) {
        if (!isAdded()) {
            return;
        }

        if (PermisosImagenes.tienePermisoLectura(requireContext())) {
            abrirGaleria();
            return;
        }

        mostrarPermisoGaleriaRechazado();
    }

    /**
     * Explica el rechazo del permiso. Si Android todavía permite volver a preguntar se
     * ofrece el botón de conceder; si el usuario marcó "No volver a preguntar" o el
     * permiso quedó desactivado desde los ajustes, solo queda abrir la pantalla de
     * configuración de la aplicación.
     */
    private void mostrarPermisoGaleriaRechazado() {
        boolean sePuedeVolverAPreguntar = shouldShowRequestPermissionRationale(
                PermisosImagenes.permisosSolicitados()[0]);

        if (sePuedeVolverAPreguntar) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.tvTituloPermisoGaleria)
                    .setMessage(R.string.tvMensajePermisoGaleria)
                    .setPositiveButton(
                            R.string.btnConcederPermisoGaleria,
                            (dialogo, cual) -> permisoGaleriaLauncher.launch(
                                    PermisosImagenes.permisosSolicitados())
                    )
                    .setNegativeButton(R.string.btnCancelarPermisoGaleria, null)
                    .show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloPermisoGaleriaDesactivado)
                .setMessage(R.string.tvMensajePermisoGaleriaDesactivado)
                .setPositiveButton(
                        R.string.btnAbrirAjustesGaleria,
                        (dialogo, cual) -> PermisosImagenes.abrirAjustes(requireContext())
                )
                .setNegativeButton(R.string.btnCancelarPermisoGaleria, null)
                .show();
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

        suscripcionesDescanso.clear();

        super.onDestroyView();
    }
}
