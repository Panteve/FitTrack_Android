package ue.edu.co.fittrackandroid.perfil;

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
import java.util.ArrayList;
import java.util.List;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Pantalla de perfil: foto, datos personales, últimos entrenamientos y cierre de sesión.
 * El nombre y la foto se guardan localmente con SharedPreferences porque todavía
 * no existe una fuente de datos real.
 */
public class PerfilFragment extends Fragment {

    private static final String PREFERENCIAS_PERFIL = "preferencias_perfil";
    private static final String CLAVE_NOMBRE_USUARIO = "nombre_usuario";
    private static final String CLAVE_URI_FOTO_PERFIL = "uri_foto_perfil";

    private ActivityResultLauncher<String[]> selectorImagen;

    private ImageView imgFotoPerfil;
    private ImageButton btnIconoCambiarFoto;
    private Button btnCambiarFoto;
    private EditText etNombrePerfil;
    private Button btnGuardarNombre;
    private Button btnCerrarSesion;
    private RecyclerView rvUltimosEntrenamientos;
    private TextView tvCantidadUltimosEntrenamientos;
    private TextView tvSinEntrenamientos;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        inicializarVistas(view);
        cargarDatosPerfil();
        configurarUltimosEntrenamientos();
        configurarAcciones();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // El perfil es una pestaña raíz: la toolbar debe verse siempre como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();
    }

    /** Busca las vistas de la pantalla y las guarda en los campos. */
    private void inicializarVistas(View view) {
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        btnIconoCambiarFoto = view.findViewById(R.id.btnIconoCambiarFoto);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        etNombrePerfil = view.findViewById(R.id.etNombrePerfil);
        btnGuardarNombre = view.findViewById(R.id.btnGuardarNombre);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        rvUltimosEntrenamientos = view.findViewById(R.id.rvUltimosEntrenamientos);
        tvCantidadUltimosEntrenamientos = view.findViewById(R.id.tvCantidadUltimosEntrenamientos);
        tvSinEntrenamientos = view.findViewById(R.id.tvSinEntrenamientos);
    }

    /** Muestra el nombre guardado e intenta restaurar la foto elegida anteriormente. */
    private void cargarDatosPerfil() {
        // TODO: Usar el nombre del usuario autenticado cuando exista una sesión real.
        String nombreUsuario = obtenerPreferencias()
                .getString(CLAVE_NOMBRE_USUARIO, getString(R.string.tvNombreInicialPerfil));
        etNombrePerfil.setText(nombreUsuario);

        restaurarFotoPerfil();
    }

    /** Arma la lista con los tres entrenamientos más recientes y ajusta el estado vacío. */
    private void configurarUltimosEntrenamientos() {
        List<UltimoEntrenamiento> entrenamientos = crearEntrenamientosDeEjemplo();

        rvUltimosEntrenamientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUltimosEntrenamientos.setAdapter(new UltimoEntrenamientoAdapter(entrenamientos));

        boolean hayEntrenamientos = !entrenamientos.isEmpty();
        rvUltimosEntrenamientos.setVisibility(hayEntrenamientos ? View.VISIBLE : View.GONE);
        tvSinEntrenamientos.setVisibility(hayEntrenamientos ? View.GONE : View.VISIBLE);
        tvCantidadUltimosEntrenamientos.setText(
                getString(R.string.tvCantidadUltimosEntrenamientos, entrenamientos.size()));
    }

    /**
     * Crea los entrenamientos de demostración, del más reciente al más antiguo.
     * TODO: Reemplazar por los entrenamientos realmente registrados.
     */
    private List<UltimoEntrenamiento> crearEntrenamientosDeEjemplo() {
        List<UltimoEntrenamiento> listaEntrenamientos = new ArrayList<>();

        listaEntrenamientos.add(new UltimoEntrenamiento(
                getString(R.string.tvNombreUltimoEntrenamiento1),
                getString(R.string.tvFechaUltimoEntrenamiento1),
                Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento1))));

        listaEntrenamientos.add(new UltimoEntrenamiento(
                getString(R.string.tvNombreUltimoEntrenamiento2),
                getString(R.string.tvFechaUltimoEntrenamiento2),
                Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento2))));

        listaEntrenamientos.add(new UltimoEntrenamiento(
                getString(R.string.tvNombreUltimoEntrenamiento3),
                getString(R.string.tvFechaUltimoEntrenamiento3),
                Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento3))));

        return listaEntrenamientos;
    }

    /** Conecta los botones y la foto con las acciones de la pantalla. */
    private void configurarAcciones() {
        imgFotoPerfil.setOnClickListener(v -> abrirSelectorImagen());
        btnIconoCambiarFoto.setOnClickListener(v -> abrirSelectorImagen());
        btnCambiarFoto.setOnClickListener(v -> abrirSelectorImagen());
        btnGuardarNombre.setOnClickListener(v -> guardarNombre());
        btnCerrarSesion.setOnClickListener(v -> confirmarCierreSesion());
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

        // Se pide permiso persistente para poder volver a leer la imagen al abrir el perfil.
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            // Algunos proveedores no lo entregan; la foto se podrá ver mientras la app siga abierta.
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

        obtenerPreferencias().edit().putString(CLAVE_NOMBRE_USUARIO, nombreUsuario).apply();
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
}
