package ue.edu.co.fittrackandroid.resumen.vista;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.entrenamiento.datos.EntrenamientoRepository;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoActualizarNotasRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoDetalleResponse;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoEnCurso;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoFotoResponse;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.RegistroSerieResponse;
import ue.edu.co.fittrackandroid.imagenes.GaleriaImagenesFragment;
import ue.edu.co.fittrackandroid.imagenes.PermisosImagenes;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.resumen.datos.DescargadorFotoEntrenamiento;
import ue.edu.co.fittrackandroid.resumen.datos.FotoEntrenamientoLocal;
import ue.edu.co.fittrackandroid.resumen.modelo.EjercicioResumen;
import ue.edu.co.fittrackandroid.resumen.modelo.GrupoMuscularResumen;
import ue.edu.co.fittrackandroid.resumen.modelo.ResumenEntrenamiento;
import ue.edu.co.fittrackandroid.resumen.modelo.SerieResumen;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Pantalla con el resultado de un entrenamiento terminado.
 *
 * <p>Muestra el nombre del entrenamiento, el día y la hora en que se realizó, las tres
 * métricas finales (duración, volumen y series), la distribución porcentual de los
 * grupos musculares trabajados y los ejercicios con sus series realizadas. También permite
 * guardar una nota o descripción del entrenamiento. La fotografía del resumen se puede
 * tomar con la cámara o agregar eligiendo una imagen del almacenamiento externo; en los
 * dos casos se valida, se sube al backend y recién entonces se guarda la copia local.
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

    private static final String ARG_ID_ENTRENAMIENTO = "idEntrenamiento";
    private static final int LONGITUD_MAXIMA_NOTA = 1000;
    private static final String TIPO_JPEG = "image/jpeg";
    private static final String TIPO_PNG = "image/png";
    private static final String NOMBRE_ARCHIVO_FOTO = "foto_entrenamiento.jpg";

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
    private EntrenamientoDetalleResponse detalleEntrenamiento;
    private Call<EntrenamientoDetalleResponse> currentCallDetalle;
    private Call<EntrenamientoDetalleResponse> currentCallActualizar;
    private Call<Void> currentCallEliminar;
    private boolean eliminandoEntrenamiento;
    private boolean guardandoNota;
    private boolean subiendoFoto;
    /** true cuando el cuadro de la tarjeta está mostrando una foto y no el icono. */
    private boolean hayFotoVisible;
    /** Foto preparada que no se pudo subir todavía, para poder reenviarla sin recapturar. */
    private byte[] contenidoFotoPendiente;
    private FotoEntrenamientoLocal fotoEntrenamientoLocal;
    private File archivoCapturaFoto;
    private Call<EntrenamientoFotoResponse> currentCallSubirFoto;
    private ActivityResultLauncher<String> permisoCamaraLauncher;
    private ActivityResultLauncher<Uri> camaraLauncher;
    private ActivityResultLauncher<String[]> permisoGaleriaLauncher;

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
    private EditText etNotaResumenEntrenamiento;
    private Button btnGuardarNotaResumenEntrenamiento;
    private Button btnTomarFotoResumenEntrenamiento;
    private Button btnAgregarFotoResumenEntrenamiento;
    private ImageView imgFotoResumenEntrenamiento;
    private TextView tvResumenSinEjercicios;
    private LinearLayout layoutGruposMuscularesResumen;
    private RecyclerView rvEjerciciosResumen;

    public ResumenEntrenamientoFragment() {
        // Required empty public constructor
    }

    /**
     * Crea la pantalla preparada para consultar un entrenamiento guardado.
     *
     * @param idEntrenamiento identificador asignado por el backend
     * @return Fragment con el identificador dentro de sus argumentos
     */
    public static ResumenEntrenamientoFragment newInstance(Long idEntrenamiento) {
        ResumenEntrenamientoFragment fragment = new ResumenEntrenamientoFragment();
        Bundle argumentos = new Bundle();
        if (idEntrenamiento != null) {
            argumentos.putLong(ARG_ID_ENTRENAMIENTO, idEntrenamiento);
        }
        fragment.setArguments(argumentos);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) requireActivity();
        Bundle argumentos = getArguments();
        if (argumentos != null && argumentos.containsKey(ARG_ID_ENTRENAMIENTO)) {
            idEntrenamiento = argumentos.getLong(ARG_ID_ENTRENAMIENTO);
        }
        entrenamientoRepository = new EntrenamientoRepository(requireContext());
        fotoEntrenamientoLocal = new FotoEntrenamientoLocal(requireContext());

        // Los lanzadores se registran aquí, antes de que exista la vista, porque así
        // Android puede devolver el resultado aunque la pantalla se haya recreado.
        permisoCamaraLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                concedido -> {
                    if (Boolean.TRUE.equals(concedido)) {
                        abrirCamara();
                        return;
                    }
                    mostrarPermisoCamaraRechazado();
                }
        );
        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                exitoso -> {
                    if (Boolean.TRUE.equals(exitoso)) {
                        procesarFotoCapturada();
                        return;
                    }
                    // El usuario canceló la cámara. No es un error: solo se limpia el
                    // archivo temporal que quedó sin usar.
                    fotoEntrenamientoLocal.eliminarCaptura(archivoCapturaFoto);
                    archivoCapturaFoto = null;
                }
        );
        // La galería propia usa permisos reales según la versión de Android. El lanzador
        // de permisos y el escucha del resultado de la galería se registran aquí, antes
        // de que exista la vista, para que el resultado pueda llegar aunque la pantalla
        // se haya recreado.
        permisoGaleriaLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::procesarResultadoPermisoGaleria
        );
        registrarResultadoImagenSeleccionada();
    }

    /**
     * Escucha el resultado que devuelve {@link GaleriaImagenesFragment} cuando el usuario
     * elige una imagen. Solo se procesa cuando el destino es el entrenamiento.
     */
    private void registrarResultadoImagenSeleccionada() {
        getParentFragmentManager().setFragmentResultListener(
                GaleriaImagenesFragment.REQUEST_IMAGEN_SELECCIONADA,
                this,
                (clave, resultado) -> {
                    String destino = resultado.getString(GaleriaImagenesFragment.EXTRA_DESTINO);
                    if (!GaleriaImagenesFragment.DESTINO_ENTRENAMIENTO.equals(destino)) {
                        return;
                    }

                    Uri uri = resultado.getParcelable(GaleriaImagenesFragment.EXTRA_URI_IMAGEN);
                    procesarFotoSeleccionada(uri);
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen_entrenamiento, container, false);

        inicializarVistas(view);
        btnReintentarResumenEntrenamiento.setOnClickListener(v -> cargarResumenDesdeApi());
        btnBorrarEntrenamiento.setOnClickListener(v -> confirmarBorradoEntrenamiento());
        btnGuardarNotaResumenEntrenamiento.setOnClickListener(v -> guardarNota());
        btnTomarFotoResumenEntrenamiento.setOnClickListener(v -> tomarFoto());
        btnAgregarFotoResumenEntrenamiento.setOnClickListener(v -> agregarFoto());
        imgFotoResumenEntrenamiento.setOnClickListener(v -> tocarFoto());

        if (idEntrenamiento != null && idEntrenamiento > 0) {
            cargarResumenDesdeApi();
        } else {
            // Sin un identificador válido no hay ningún entrenamiento que consultar.
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
        etNotaResumenEntrenamiento = view.findViewById(R.id.etNotaResumenEntrenamiento);
        btnGuardarNotaResumenEntrenamiento = view.findViewById(
                R.id.btnGuardarNotaResumenEntrenamiento);
        btnTomarFotoResumenEntrenamiento = view.findViewById(
                R.id.btnTomarFotoResumenEntrenamiento);
        btnAgregarFotoResumenEntrenamiento = view.findViewById(
                R.id.btnAgregarFotoResumenEntrenamiento);
        imgFotoResumenEntrenamiento = view.findViewById(R.id.imgFotoResumenEntrenamiento);
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
        mostrarNota();
        mostrarFoto();
        mostrarGruposMusculares();
        mostrarEjercicios();
        actualizarBotonBorrar();
    }

    // ------------------------------------------------------------------ Nota y fotografía

    /** Muestra la nota recibida y habilita su edición cuando existe el detalle completo. */
    private void mostrarNota() {
        boolean detalleDisponible = detalleEntrenamiento != null;
        etNotaResumenEntrenamiento.setEnabled(detalleDisponible && !guardandoNota);
        btnGuardarNotaResumenEntrenamiento.setEnabled(detalleDisponible && !guardandoNota);

        if (!detalleDisponible) {
            return;
        }

        String nota = detalleEntrenamiento.getNotas();
        etNotaResumenEntrenamiento.setText(nota == null ? "" : nota);
    }

    /** Valida la nota y actualiza el entrenamiento ya guardado. */
    private void guardarNota() {
        if (guardandoNota || detalleEntrenamiento == null
                || idEntrenamiento == null || idEntrenamiento <= 0) {
            return;
        }

        String nota = etNotaResumenEntrenamiento.getText().toString().trim();
        if (nota.length() > LONGITUD_MAXIMA_NOTA) {
            Toast.makeText(
                    requireContext(),
                    R.string.etNotaResumenEntrenamiento_error_largo,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        EntrenamientoActualizarNotasRequest request =
                new EntrenamientoActualizarNotasRequest(nota);

        mostrarGuardandoNota(true);
        currentCallActualizar = entrenamientoRepository.actualizarNotas(
                idEntrenamiento,
                request
        );
        currentCallActualizar.enqueue(new Callback<EntrenamientoDetalleResponse>() {
            @Override
            public void onResponse(@NonNull Call<EntrenamientoDetalleResponse> call,
                                   @NonNull Response<EntrenamientoDetalleResponse> response) {
                if (!isAdded() || getView() == null) {
                    return;
                }

                mostrarGuardandoNota(false);
                if (response.isSuccessful() && response.body() != null) {
                    detalleEntrenamiento = response.body();
                    mostrarNota();
                    Toast.makeText(
                            requireContext(),
                            R.string.btnGuardarNotaResumenEntrenamiento_confirmacion,
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
            }

            @Override
            public void onFailure(@NonNull Call<EntrenamientoDetalleResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded() || getView() == null) {
                    return;
                }

                mostrarGuardandoNota(false);
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /** Cambia el formulario entre su estado editable y el guardado en curso. */
    private void mostrarGuardandoNota(boolean guardando) {
        guardandoNota = guardando;
        etNotaResumenEntrenamiento.setEnabled(!guardando);
        btnGuardarNotaResumenEntrenamiento.setEnabled(!guardando);
        btnGuardarNotaResumenEntrenamiento.setText(guardando
                ? R.string.btnGuardarNotaResumenEntrenamiento_loading
                : R.string.btnGuardarNotaResumenEntrenamiento);
        btnBorrarEntrenamiento.setEnabled(!guardando);
    }

    // ------------------------------------------------------------------ Fotografía

    /**
     * Muestra la fotografía del entrenamiento.
     *
     * <p>Primero se busca la copia que quedó en el caché, que es inmediata. Solo si no
     * existe, y el backend tiene una foto guardada, se descarga. Así el espacio
     * siempre tiene su imagen y la descarga es un refuerzo, no un requisito.
     */
    private void mostrarFoto() {
        if (idEntrenamiento == null || idEntrenamiento <= 0) {
            mostrarFotoPorDefecto();
            return;
        }

        byte[] copiaLocal = fotoEntrenamientoLocal.leerCopiaLocal(idEntrenamiento);
        if (copiaLocal != null) {
            mostrarFotoEnPantalla(copiaLocal);
            return;
        }

        mostrarFotoPorDefecto();

        if (detalleEntrenamiento == null) {
            return;
        }

        String fotoUrl = detalleEntrenamiento.getFotoUrl();
        if (fotoUrl == null || fotoUrl.trim().isEmpty()) {
            return;
        }

        DescargadorFotoEntrenamiento.descargar(
                requireContext(),
                idEntrenamiento,
                fotoUrl,
                contenido -> {
                    // La pantalla pudo cerrarse mientras se descargaba.
                    if (isAdded() && getView() != null) {
                        mostrarFotoEnPantalla(contenido);
                    }
                }
        );
    }

    /** Vuelve al icono de cámara cuando no hay ninguna foto que mostrar. */
    private void mostrarFotoPorDefecto() {
        hayFotoVisible = false;

        int relleno = getResources().getDimensionPixelSize(R.dimen.spacing_xl);
        imgFotoResumenEntrenamiento.setPadding(
                relleno,
                relleno,
                relleno,
                relleno
        );
        // El color del icono viene de app:tint en el layout. Hay que ponerlo aquí
        // también porque al volver de la vista ampliada la foto sigue teñida.
        ImageViewCompat.setImageTintList(
                imgFotoResumenEntrenamiento,
                ColorStateList.valueOf(
                        requireContext().getColor(R.color.colorTextTertiary)
                )
        );
        // El icono va centrado y con aire alrededor; la foto real usa recorte central.
        imgFotoResumenEntrenamiento.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgFotoResumenEntrenamiento.setImageResource(R.drawable.ic_camera);
    }

    /**
     * Punto de entrada del botón de agregar foto: abre la galería propia para
     * que el usuario elija una imagen ya guardada en el teléfono.
     *
     * <p>Se repiten las mismas comprobaciones que al tomar foto porque el backend solo
     * admite imágenes de un entrenamiento que ya tenga identificador, y así un botón
     * no puede iniciar una subida mientras la otra sigue en vuelo.
     */
    private void agregarFoto() {
        if (subiendoFoto) {
            return;
        }

        if (idEntrenamiento == null || idEntrenamiento <= 0) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnAgregarFotoResumenEntrenamiento_error_entrenamiento,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Si la subida anterior falló se reintenta con la foto que ya está en memoria,
        // sin obligar al usuario a elegirla otra vez.
        if (reintentarSubidaFoto()) {
            return;
        }

        if (PermisosImagenes.tienePermisoLectura(requireContext())) {
            abrirGaleria();
            return;
        }

        permisoGaleriaLauncher.launch(PermisosImagenes.permisosSolicitados());
    }

    /** Abre la galería propia para elegir una fotografía. */
    private void abrirGaleria() {
        ((MainActivity) requireActivity())
                .mostrarGaleriaImagenes(GaleriaImagenesFragment.DESTINO_ENTRENAMIENTO);
    }

    /**
     * Revisa el resultado de pedir el permiso de lectura de imágenes. Con cualquier acceso
     * (completo o parcial) se abre la galería; sin acceso se explica por qué FitTrack
     * necesita leer las imágenes y, si ya no se puede volver a preguntar, se ofrecen los
     * ajustes de la aplicación.
     *
     * @param resultados permiso o permisos solicitados con su estado final
     */
    private void procesarResultadoPermisoGaleria(Map<String, Boolean> resultados) {
        if (PermisosImagenes.tienePermisoLectura(requireContext())) {
            abrirGaleria();
            return;
        }

        mostrarPermisoGaleriaRechazado();
    }

    /**
     * Explica por qué se necesita el permiso de galería. Si Android permite volver a
     * preguntarlo, se ofrece el reintento; si no, se abre la pantalla de ajustes.
     */
    private void mostrarPermisoGaleriaRechazado() {
        final boolean sePuedeVolverAPreguntar = puedeVolverAPreguntarPermisoGaleria();

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloPermisoGaleriaEntrenamiento)
                .setMessage(sePuedeVolverAPreguntar
                        ? R.string.tvMensajePermisoGaleriaEntrenamiento
                        : R.string.tvMensajePermisoGaleriaEntrenamiento_desactivado)
                .setPositiveButton(
                        sePuedeVolverAPreguntar
                                ? R.string.btnConcederPermisoGaleria
                                : R.string.btnAbrirAjustesGaleria,
                        (dialogoVisible, cual) -> {
                            if (sePuedeVolverAPreguntar) {
                                permisoGaleriaLauncher.launch(
                                        PermisosImagenes.permisosSolicitados());
                                return;
                            }
                            PermisosImagenes.abrirAjustes(requireContext());
                        }
                )
                .setNegativeButton(R.string.btnCancelarPermisoGaleria, null)
                .create();

        dialogo.show();
    }

    /**
     * @return true si Android todavía permite volver a preguntar por el permiso, porque
     *         el usuario no lo rechazó de forma definitiva. En Android 14 se revisan los
     *         dos permisos: basta con que alguno de los dos pueda volver a preguntarse.
     */
    private boolean puedeVolverAPreguntarPermisoGaleria() {
        for (String permiso : PermisosImagenes.permisosSolicitados()) {
            if (shouldShowRequestPermissionRationale(permiso)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Punto de entrada del botón de tomar foto.
     *
     * <p>El backend solo admite fotos de un entrenamiento que ya tenga identificador,
     * por eso sin él no se llega ni a abrir la cámara. Después se revisa el permiso y,
     * si falta, se solicita.
     */
    private void tomarFoto() {
        if (subiendoFoto) {
            return;
        }

        if (idEntrenamiento == null || idEntrenamiento <= 0) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_entrenamiento,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Si la subida anterior falló se reintenta con la foto que ya está en memoria,
        // sin obligar a tomarla de nuevo.
        if (reintentarSubidaFoto()) {
            return;
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
            return;
        }

        permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
    }

    /**
     * Prepara el archivo temporal y abre la cámara.
     *
     * <p>Se usa TakePicture y no TakePicturePreview porque el segundo solo entrega una
     * miniatura de baja calidad, y esta fotografía es el recuerdo del entrenamiento.
     */
    private void abrirCamara() {
        if (!hayAplicacionDeCamara()) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_camara,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        File archivoNuevo = fotoEntrenamientoLocal.crearArchivoCaptura();
        if (archivoNuevo == null) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_captura,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Uri uriSeguro;
        try {
            uriSeguro = fotoEntrenamientoLocal.obtenerUriParaCamara(archivoNuevo);
        } catch (IllegalArgumentException | SecurityException error) {
            // El archivo quedó fuera de la ruta compartida: no se puede continuar.
            fotoEntrenamientoLocal.eliminarCaptura(archivoNuevo);
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_captura,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        archivoCapturaFoto = archivoNuevo;
        camaraLauncher.launch(uriSeguro);
    }

    /**
     * @return true si el dispositivo tiene alguna aplicación capaz de tomar la foto.
     *         No todos los dispositivos con cámara la traen instalada.
     */
    private boolean hayAplicacionDeCamara() {
        Intent intencion = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        return requireContext()
                .getPackageManager()
                .queryIntentActivities(intencion, PackageManager.MATCH_DEFAULT_ONLY)
                .size() > 0;
    }

    /**
     * Explica que el permiso fue rechazado. Android no permite volver a mostrar el
     * diálogo después de un rechazo definitivo, así que en ese caso hay que ir a los
     * ajustes de la aplicación.
     */
    private void mostrarPermisoCamaraRechazado() {
        boolean sePuedeVolverAPreguntar = shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
        );

        Toast.makeText(
                requireContext(),
                sePuedeVolverAPreguntar
                        ? R.string.btnTomarFotoResumenEntrenamiento_error_permiso
                        : R.string.btnTomarFotoResumenEntrenamiento_error_permiso_ajustes,
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Valida la fotografía recién capturada, la muestra de inmediato y comienza la
     * subida. La captura original se elimina: a partir de aquí solo importa la copia
     * comprimida, que es la que se envía.
     */
    private void procesarFotoCapturada() {
        File archivo = archivoCapturaFoto;
        archivoCapturaFoto = null;

        if (archivo == null || !fotoEntrenamientoLocal.esCapturaValida(archivo)) {
            fotoEntrenamientoLocal.eliminarCaptura(archivo);
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_captura,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        byte[] contenidoFoto = fotoEntrenamientoLocal.prepararParaSubir(archivo);
        fotoEntrenamientoLocal.eliminarCaptura(archivo);

        if (contenidoFoto == null) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_tamano,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Solo se envía. La copia local y el cuadro de la vista previa se actualizan
        // más adelante, cuando el backend confirme que la guardó.
        subirFoto(contenidoFoto);
    }

    /**
     * Valida la imagen elegida en la galería propia y la sube igual que una
     * fotografía tomada con la cámara.
     *
     * <p>Se copia primero a un archivo del caché porque es la única forma de pasar por
     * la misma preparación que usa la cámara: enderezar, reducir y comprimir. Así una
     * foto de galería que venga girada o muy grande llega al backend en el mismo formato
     * que una captura.
     *
     * @param uri dirección {@code content://} del archivo elegido, o null si el usuario canceló
     */
    private void procesarFotoSeleccionada(@Nullable Uri uri) {
        if (uri == null || subiendoFoto) {
            return;
        }

        if (idEntrenamiento == null || idEntrenamiento <= 0) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_entrenamiento,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // El tipo lo declara el proveedor del archivo, no el nombre: por eso se pregunta
        // y se compara en lugar de mirar la extensión.
        String tipoContenido = requireContext().getContentResolver().getType(uri);
        if (!TIPO_JPEG.equals(tipoContenido) && !TIPO_PNG.equals(tipoContenido)) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnAgregarFotoResumenEntrenamiento_error_formato,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        File archivoSeleccionado;
        try {
            archivoSeleccionado = fotoEntrenamientoLocal.copiarDesdeUri(uri);
        } catch (IOException | SecurityException error) {
            // El proveedor no dejó leer el archivo, o llegó vacío.
            Toast.makeText(
                    requireContext(),
                    R.string.btnAgregarFotoResumenEntrenamiento_error_lectura,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (archivoSeleccionado == null) {
            // La copia interna ya lo borró: solo faltaba avisar que pesa demasiado.
            Toast.makeText(
                    requireContext(),
                    R.string.btnAgregarFotoResumenEntrenamiento_error_tamano,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        byte[] contenidoFoto = fotoEntrenamientoLocal.prepararParaSubir(archivoSeleccionado);
        fotoEntrenamientoLocal.eliminarCaptura(archivoSeleccionado);

        if (contenidoFoto == null) {
            // El archivo pasó la validación de formato pero no se pudo decodificar
            // como imagen.
            Toast.makeText(
                    requireContext(),
                    R.string.btnAgregarFotoResumenEntrenamiento_error_imagen,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Solo se envía. La copia local y el cuadro de la vista previa se actualizan
        // más adelante, cuando el backend confirme que la guardó.
        subirFoto(contenidoFoto);
    }

    /**
     * Envía la fotografía al backend con la petición
     * {@code POST /entrenamientos/{id}/foto}. Mientras está en vuelo los dos botones
     * de la tarjeta quedan apagados para que no se pueda tomar otra foto encima.
     *
     * @param contenidoFoto JPEG ya comprimido y preparado
     */
    private void subirFoto(byte[] contenidoFoto) {
        if (subiendoFoto || idEntrenamiento == null || idEntrenamiento <= 0) {
            return;
        }

        RequestBody cuerpoFoto = RequestBody.create(
                contenidoFoto,
                MediaType.get(TIPO_JPEG)
        );
        MultipartBody.Part parteFoto = MultipartBody.Part.createFormData(
                "foto",
                NOMBRE_ARCHIVO_FOTO,
                cuerpoFoto
        );

        mostrarSubiendoFoto(true);

        currentCallSubirFoto = entrenamientoRepository.subirFoto(
                idEntrenamiento,
                parteFoto
        );
        currentCallSubirFoto.enqueue(new Callback<EntrenamientoFotoResponse>() {
            @Override
            public void onResponse(@NonNull Call<EntrenamientoFotoResponse> call,
                                   @NonNull Response<EntrenamientoFotoResponse> response) {
                if (!isAdded() || getView() == null) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    conservarFotoParaReintento(contenidoFoto);
                    mostrarErrorSubidaFoto(response.code());
                    return;
                }

                procesarFotoGuardada(contenidoFoto);
            }

            @Override
            public void onFailure(@NonNull Call<EntrenamientoFotoResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded() || getView() == null) {
                    return;
                }

                // Sin respuesta del servidor, por ejemplo sin internet.
                conservarFotoParaReintento(contenidoFoto);
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /**
     * Muestra un aviso propio para los rechazos de tamaño y de formato. La imagen ya
     * se validó en el teléfono, pero el backend puede rechazarla igual: por eso no
     * alcanza con el error genérico de cada código.
     *
     * @param codigoRespuesta código HTTP devuelto por el servidor
     */
    private void mostrarErrorSubidaFoto(int codigoRespuesta) {
        if (codigoRespuesta == 413) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_tamano,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (codigoRespuesta == 415) {
            Toast.makeText(
                    requireContext(),
                    R.string.btnAgregarFotoResumenEntrenamiento_error_formato,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ManejadorErroresApi.obtenerToast(requireContext(), codigoRespuesta).show();
    }

    /**
     * Escribe la copia local y muestra la foto, ya con el backend confirmado que la
     * guardó. Es el único punto donde se actualiza lo que el usuario ve.
     *
     * <p>El orden importa: mientras la subida está en vuelo la foto anterior sigue
     * intacta, tanto en pantalla como en el caché. Si el servidor rechaza la imagen no
     * se reemplaza nada, así que nunca se muestra una foto que el usuario no llegó a
     * guardar.
     *
     * @param contenidoFoto JPEG que el backend confirmó
     */
    private void procesarFotoGuardada(byte[] contenidoFoto) {
        contenidoFotoPendiente = null;
        mostrarSubiendoFoto(false);

        try {
            fotoEntrenamientoLocal.guardarCopiaLocal(idEntrenamiento, contenidoFoto);
        } catch (IOException error) {
            // La foto ya está en el backend, así que no se pierde nada: solo habrá que
            // volver a descargarla la próxima vez que se abra este entrenamiento.
            Toast.makeText(
                    requireContext(),
                    R.string.btnTomarFotoResumenEntrenamiento_error_guardado_local,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        mostrarFotoEnPantalla(contenidoFoto);

        Toast.makeText(
                requireContext(),
                R.string.btnTomarFotoResumenEntrenamiento_confirmacion,
                Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * Vuelve a enviar la fotografía que ya se tenía en pantalla. Se usa cuando la
     * subida anterior falló, para no obligar al usuario a tomarla de nuevo.
     *
     * @return true si había una foto pendiente y se empezó a reenviar
     */
    private boolean reintentarSubidaFoto() {
        if (contenidoFotoPendiente == null) {
            return false;
        }

        byte[] contenidoFoto = contenidoFotoPendiente;
        contenidoFotoPendiente = null;
        subirFoto(contenidoFoto);

        return true;
    }

    /**
     * Deja la fotografía a la vista y ofrece reenviarla. El error concreto ya lo
     * muestra la pantalla, así que aquí solo se restaura el estado de los botones.
     *
     * @param contenidoFoto JPEG que no se pudo enviar
     */
    private void conservarFotoParaReintento(byte[] contenidoFoto) {
        contenidoFotoPendiente = contenidoFoto;
        mostrarSubiendoFoto(false);
        btnTomarFotoResumenEntrenamiento.setText(
                R.string.btnTomarFotoResumenEntrenamiento_reintentar);
    }

    /**
     * Cambia el estado de la tarjeta de fotografía mientras la imagen viaja al
     * backend, o cuando se deja de subir.
     *
     * @param subiendo true mientras la petición está en vuelo
     */
    private void mostrarSubiendoFoto(boolean subiendo) {
        subiendoFoto = subiendo;
        btnTomarFotoResumenEntrenamiento.setEnabled(!subiendo);
        btnAgregarFotoResumenEntrenamiento.setEnabled(!subiendo);

        btnTomarFotoResumenEntrenamiento.setText(subiendo
                ? R.string.btnTomarFotoResumenEntrenamiento_subiendo
                : R.string.btnTomarFotoResumenEntrenamiento);
    }

    /**
     * Tocar la fotografía hace una cosa u otra según lo que haya en pantalla: si ya
     * hay una foto, se abre a pantalla completa; si no hay ninguna, se abre la cámara.
     * Recapturar es una acción que no debería pasar por un toque accidental sobre la
     * imagen, así que eso se queda en el botón de abajo.
     */
    private void tocarFoto() {
        if (hayFotoVisible && idEntrenamiento != null) {
            FotoEntrenamientoDialogFragment
                    .newInstance(idEntrenamiento)
                    .show(getParentFragmentManager(), FotoEntrenamientoDialogFragment.ETIQUETA);
            return;
        }

        tomarFoto();
    }

    /**
     * Muestra la fotografía en el cuadro de la tarjeta. El recorte central hace que la
     * imagen llene el cuadro sin deformarse; la foto entera, sin recortar, se ve al
     * tocarla.
     */
    private void mostrarFotoEnPantalla(@Nullable byte[] contenidoFoto) {
        if (contenidoFoto == null || getView() == null) {
            return;
        }

        int ladoPrevia = getResources()
                .getDimensionPixelSize(R.dimen.workout_photo_preview_height);
        Bitmap foto = fotoEntrenamientoLocal
                .cargarBitmapParaVistaPrevia(contenidoFoto, ladoPrevia);

        if (foto == null) {
            return;
        }

        hayFotoVisible = true;
        // Sin relleno para que la fotografía ocupe todo el cuadro, sin tinte y con
        // recorte central para que no queden bandas vacías.
        imgFotoResumenEntrenamiento.setPadding(0, 0, 0, 0);
        ImageViewCompat.setImageTintList(imgFotoResumenEntrenamiento, null);
        imgFotoResumenEntrenamiento.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgFotoResumenEntrenamiento.setImageBitmap(foto);
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
        if (eliminandoEntrenamiento || guardandoNota
                || idEntrenamiento == null || idEntrenamiento <= 0) {
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
        if (eliminandoEntrenamiento || guardandoNota
                || idEntrenamiento == null || idEntrenamiento <= 0) {
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
     * pantalla anterior.
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
     * Consulta el entrenamiento guardado y reconstruye su resumen usando el identificador.
     */
    private void cargarResumenDesdeApi() {
        if (idEntrenamiento == null || idEntrenamiento <= 0) {
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
                    detalleEntrenamiento = response.body();
                    resumen = convertirAResumen(detalleEntrenamiento);
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

        if (currentCallSubirFoto != null) {
            currentCallSubirFoto.cancel();
        }

        if (currentCallEliminar != null) {
            currentCallEliminar.cancel();
        }

        if (currentCallActualizar != null) {
            currentCallActualizar.cancel();
        }

        // Al cancelar las llamadas ya no queda ninguna petición en vuelo, así que el
        // bloqueo puede desaparecer si las vistas se recrean. El identificador sigue
        // disponible en los argumentos del Fragment para volver a consultar el detalle.
        eliminandoEntrenamiento = false;
        guardandoNota = false;
        super.onDestroyView();
    }
}
