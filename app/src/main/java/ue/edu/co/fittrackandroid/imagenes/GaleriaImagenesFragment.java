package ue.edu.co.fittrackandroid.imagenes;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;

/**
 * Galería propia de FitTrack. Consulta las imágenes del dispositivo con MediaStore,
 * las muestra en una cuadrícula y devuelve la elegida mediante Fragment Results.
 *
 * <p>El destino se usa para que la pantalla que abrió la galería solo responda a sus
 * propias imágenes: {@link #DESTINO_PERFIL} para la foto de perfil y
 * {@link #DESTINO_ENTRENAMIENTO} para la fotografía del entrenamiento.
 *
 * <p>La consulta y las miniaturas se ejecutan fuera del hilo principal. Si el permiso
 * se retira mientras la pantalla está abierta, la galería avisa y ofrece abrir los
 * ajustes de la aplicación.
 */
public class GaleriaImagenesFragment extends Fragment {

    /** Argumento con el destino de la imagen elegida. */
    public static final String ARG_DESTINO = "destino";

    /** Clave del resultado que devuelve la imagen elegida. */
    public static final String REQUEST_IMAGEN_SELECCIONADA = "imagenSeleccionada";

    /** Extra con la Uri {@code content://} de la fotografía elegida. */
    public static final String EXTRA_URI_IMAGEN = "uriImagen";

    /** Extra con el destino que la pantalla indicó al abrir la galería. */
    public static final String EXTRA_DESTINO = "destinoImagen";

    /** Destino para cambiar la fotografía de perfil. */
    public static final String DESTINO_PERFIL = "perfil";

    /** Destino para agregar la fotografía al entrenamiento. */
    public static final String DESTINO_ENTRENAMIENTO = "entrenamiento";

    private static final String TIPO_JPEG = "image/jpeg";
    private static final String TIPO_PNG = "image/png";

    /** Consultas y miniaturas se leen aquí, nunca en el hilo principal. */
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    /** Lleva el resultado de la consulta de vuelta al hilo principal. */
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String destino;
    private boolean consultando;

    private RecyclerView rvGaleriaImagenes;
    private ProgressBar pbCargaGaleriaImagenes;
    private TextView tvGaleriaImagenesVacia;
    private TextView tvGaleriaImagenesSinPermiso;
    private Button btnGaleriaImagenesAbrirAjustes;
    private ImagenDispositivoAdapter adaptador;

    /**
     * Crea la galería indicando si la imagen elegida será la foto de perfil
     * (la constante DESTINO_PERFIL) o la fotografía del entrenamiento
     * (la constante DESTINO_ENTRENAMIENTO).
     */
    public static GaleriaImagenesFragment newInstance(String destino) {
        GaleriaImagenesFragment fragmento = new GaleriaImagenesFragment();
        Bundle argumentos = new Bundle();
        argumentos.putString(ARG_DESTINO, destino);
        fragmento.setArguments(argumentos);
        return fragmento;
    }

    /** Crea el fragmento vacío, tal como lo exige el sistema al reconstruir la pantalla. */
    public GaleriaImagenesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        destino = requireArguments().getString(ARG_DESTINO, DESTINO_PERFIL);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_galeria_imagenes, container, false);
        inicializarVistas(vista);
        return vista;
    }

    @Override
    public void onResume() {
        super.onResume();
        configurarToolbar();

        // El permiso pudo retirarse en los ajustes mientras la pantalla estaba abierta.
        if (!PermisosImagenes.tienePermisoLectura(requireContext())) {
            mostrarSinPermiso();
            return;
        }

        consultarImagenes();
    }

    /** Busca las vistas de la pantalla y prepara la cuadrícula. */
    private void inicializarVistas(View vista) {
        rvGaleriaImagenes = vista.findViewById(R.id.rvGaleriaImagenes);
        pbCargaGaleriaImagenes = vista.findViewById(R.id.pbCargaGaleriaImagenes);
        tvGaleriaImagenesVacia = vista.findViewById(R.id.tvGaleriaImagenesVacia);
        tvGaleriaImagenesSinPermiso = vista.findViewById(R.id.tvGaleriaImagenesSinPermiso);
        btnGaleriaImagenesAbrirAjustes = vista.findViewById(R.id.btnGaleriaImagenesAbrirAjustes);

        rvGaleriaImagenes.setLayoutManager(
                new GridLayoutManager(requireContext(), ImagenDispositivoAdapter.NUMERO_COLUMNAS));
        rvGaleriaImagenes.setHasFixedSize(true);

        adaptador = new ImagenDispositivoAdapter(
                requireContext(),
                new ArrayList<>(),
                this::seleccionarImagen
        );
        rvGaleriaImagenes.setAdapter(adaptador);

        btnGaleriaImagenesAbrirAjustes.setOnClickListener(
                v -> PermisosImagenes.abrirAjustes(requireContext()));
    }

    /**
     * Toolbar de la galería: título y flecha para regresar. Es una pantalla de elección,
     * así que se oculta la navegación inferior y la isla del entrenamiento.
     */
    private void configurarToolbar() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloSeleccionarFoto),
                true,
                null
        );
        activity.ocultarNavegacionInferior();
        activity.ocultarIslaEntrenamiento();
    }

    /** Deja visible únicamente el indicador mientras se consulta MediaStore. */
    private void mostrarCargando() {
        pbCargaGaleriaImagenes.setVisibility(View.VISIBLE);
        rvGaleriaImagenes.setVisibility(View.GONE);
        tvGaleriaImagenesVacia.setVisibility(View.GONE);
        tvGaleriaImagenesSinPermiso.setVisibility(View.GONE);
        btnGaleriaImagenesAbrirAjustes.setVisibility(View.GONE);
    }

    /** Presenta la cuadrícula con las imágenes encontradas. */
    private void mostrarGaleria() {
        pbCargaGaleriaImagenes.setVisibility(View.GONE);
        tvGaleriaImagenesVacia.setVisibility(View.GONE);
        tvGaleriaImagenesSinPermiso.setVisibility(View.GONE);
        btnGaleriaImagenesAbrirAjustes.setVisibility(View.GONE);
        rvGaleriaImagenes.setVisibility(View.VISIBLE);
    }

    /** Avisa que el dispositivo no tiene imágenes JPEG o PNG que mostrar. */
    private void mostrarVacia() {
        pbCargaGaleriaImagenes.setVisibility(View.GONE);
        rvGaleriaImagenes.setVisibility(View.GONE);
        tvGaleriaImagenesSinPermiso.setVisibility(View.GONE);
        btnGaleriaImagenesAbrirAjustes.setVisibility(View.GONE);
        tvGaleriaImagenesVacia.setVisibility(View.VISIBLE);
    }

    /** Avisa que el permiso fue retirado y ofrece abrir los ajustes. */
    private void mostrarSinPermiso() {
        pbCargaGaleriaImagenes.setVisibility(View.GONE);
        rvGaleriaImagenes.setVisibility(View.GONE);
        tvGaleriaImagenesVacia.setVisibility(View.GONE);
        tvGaleriaImagenesSinPermiso.setVisibility(View.VISIBLE);
        btnGaleriaImagenesAbrirAjustes.setVisibility(View.VISIBLE);
    }

    /**
     * Consulta MediaStore en un hilo de trabajo. La consulta se limita a JPEG y PNG y
     * ordena desde la imagen más reciente.
     */
    private void consultarImagenes() {
        if (consultando) {
            return;
        }

        consultando = true;
        mostrarCargando();

        // El contexto se captura antes de lanzar el hilo: dentro de él la pantalla
        // podría haberse cerrado y requireContext() ya no sería seguro.
        Context contexto = requireContext();

        ejecutor.execute(() -> {
            List<ImagenDispositivo> imagenes = leerImagenesDelDispositivo(contexto);
            boolean sinPermiso = imagenes == null
                    || !PermisosImagenes.tienePermisoLectura(contexto);
            List<ImagenDispositivo> resultado = imagenes == null
                    ? new ArrayList<>()
                    : imagenes;

            handler.post(() -> {
                consultando = false;
                if (!isAdded() || getView() == null) {
                    return;
                }

                if (sinPermiso) {
                    mostrarSinPermiso();
                    return;
                }

                if (resultado.isEmpty()) {
                    mostrarVacia();
                    return;
                }

                adaptador.actualizarImagenes(resultado);
                mostrarGaleria();
            });
        });
    }

    /**
     * Recupera los campos que la galería necesita y construye la dirección de tipo
     * content:// de cada fotografía, o devuelve nulo si el permiso impidió la lectura.
     */
    @Nullable
    private List<ImagenDispositivo> leerImagenesDelDispositivo(Context contexto) {
        List<ImagenDispositivo> imagenes = new ArrayList<>();
        ContentResolver resolver = contexto.getContentResolver();
        Uri coleccion = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] proyeccion = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
        };
        String seleccion = MediaStore.Images.Media.MIME_TYPE + " = ? OR "
                + MediaStore.Images.Media.MIME_TYPE + " = ?";
        String[] argumentos = {TIPO_JPEG, TIPO_PNG};
        String orden = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = resolver.query(coleccion, proyeccion, seleccion, argumentos, orden)) {
            if (cursor == null) {
                return imagenes;
            }

            int columnaId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int columnaNombre = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int columnaMime = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
            int columnaTamano = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            int columnaFecha = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(columnaId);
                String nombre = cursor.getString(columnaNombre);
                long tamanoBytes = cursor.isNull(columnaTamano) ? 0 : cursor.getLong(columnaTamano);

                Uri uriImagen = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                );

                imagenes.add(new ImagenDispositivo(
                        id,
                        nombre == null ? "" : nombre,
                        cursor.getString(columnaMime),
                        tamanoBytes,
                        cursor.getLong(columnaFecha),
                        uriImagen
                ));
            }

            return imagenes;
        } catch (SecurityException error) {
            // El permiso se retiró mientras se consultaba: la pantalla lo avisa.
            return null;
        }
    }

    /**
     * Devuelve la imagen elegida mediante Fragment Results y regresa a la pantalla
     * anterior automáticamente.
     */
    private void seleccionarImagen(ImagenDispositivo imagen) {
        if (!isAdded()) {
            return;
        }

        Bundle resultado = new Bundle();
        resultado.putParcelable(EXTRA_URI_IMAGEN, imagen.getUri());
        resultado.putString(EXTRA_DESTINO, destino);

        getParentFragmentManager().setFragmentResult(
                REQUEST_IMAGEN_SELECCIONADA,
                resultado
        );

        ((MainActivity) requireActivity()).regresar();
    }

    @Override
    public void onDestroyView() {
        if (adaptador != null) {
            adaptador.liberarRecursos();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        ejecutor.shutdownNow();
        super.onDestroy();
    }
}