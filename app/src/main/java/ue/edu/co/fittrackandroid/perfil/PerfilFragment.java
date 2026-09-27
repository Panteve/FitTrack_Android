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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Pantalla de perfil: foto, datos personales, ejercicios creados por el usuario, cambio de
 * contraseña, cambio de cuenta y cierre de sesión. El nombre y la foto se guardan localmente
 * con SharedPreferences porque todavía no existe una fuente de datos real.
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
    private Button btnNuevoEjercicio;
    private Button btnCambiarContrasena;
    private Button btnCambiarCuenta;
    private Button btnCerrarSesion;

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

        // TODO: Reemplazar los cuatro ejercicios definidos de forma fija en el layout por
        // los ejercicios creados por el usuario. Debe contemplar carga, estado vacío, error
        // y el guardado del nuevo orden cuando se habilite la acción de reordenar.
        cargarDatosPerfil();
        configurarAcciones();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // El perfil es una pestaña raíz: la toolbar debe verse siempre como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();

        // TODO: Volver a consultar el perfil y los ejercicios creados por el usuario cuando
        // exista una fuente de datos real, para reflejar cambios hechos desde otras pantallas
        // o dispositivos.
    }

    /** Busca las vistas de la pantalla y las guarda en los campos. */
    private void inicializarVistas(View view) {
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        btnIconoCambiarFoto = view.findViewById(R.id.btnIconoCambiarFoto);
        btnCambiarFoto = view.findViewById(R.id.btnCambiarFoto);
        etNombrePerfil = view.findViewById(R.id.etNombrePerfil);
        btnGuardarNombre = view.findViewById(R.id.btnGuardarNombre);
        btnNuevoEjercicio = view.findViewById(R.id.btnNuevoEjercicio);
        btnCambiarContrasena = view.findViewById(R.id.btnCambiarContrasena);
        btnCambiarCuenta = view.findViewById(R.id.btnCambiarCuenta);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
    }

    /** Muestra el nombre guardado e intenta restaurar la foto elegida anteriormente. */
    private void cargarDatosPerfil() {
        // TODO: Consultar el nombre, correo y foto del usuario autenticado. El correo que se
        // muestra actualmente está definido de forma fija en fragment_perfil.xml.

        // TODO: Dejar de compartir estas preferencias entre todas las cuentas del dispositivo.
        // Los datos locales deben asociarse al identificador del usuario o venir del backend.
        String nombreUsuario = obtenerPreferencias()
                .getString(CLAVE_NOMBRE_USUARIO, getString(R.string.tvNombreInicialPerfil));
        etNombrePerfil.setText(nombreUsuario);

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
        btnCambiarCuenta.setOnClickListener(v -> confirmarCambioCuenta());
        btnCerrarSesion.setOnClickListener(v -> confirmarCierreSesion());
    }

    /** Abre la pantalla de crear un ejercicio nuevo. */
    private void abrirCrearEjercicio() {
        ((MainActivity) requireActivity()).mostrarCrearEjercicio();
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

        // TODO: Actualizar el nombre en la fuente de datos del usuario y guardar localmente
        // solo después de confirmar el resultado. Mientras se guarda, deshabilitar el botón
        // para evitar solicitudes duplicadas y conservar el texto si ocurre un error.
        obtenerPreferencias().edit().putString(CLAVE_NOMBRE_USUARIO, nombreUsuario).apply();

        // TODO: Actualizar también el saludo de Home con el nuevo nombre.
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

    /** Pide confirmación antes de volver al acceso para entrar con otra cuenta. */
    private void confirmarCambioCuenta() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tvTituloCambiarCuenta)
                .setMessage(R.string.tvMensajeCambiarCuenta)
                .setPositiveButton(R.string.btnConfirmarCambiarCuenta,
                        (dialogo, cual) -> ((MainActivity) requireActivity()).mostrarLogin())
                .setNegativeButton(R.string.btnCancelarCambiarCuenta, null)
                .show();
    }

    private SharedPreferences obtenerPreferencias() {
        return requireContext().getSharedPreferences(PREFERENCIAS_PERFIL, Context.MODE_PRIVATE);
    }
}
