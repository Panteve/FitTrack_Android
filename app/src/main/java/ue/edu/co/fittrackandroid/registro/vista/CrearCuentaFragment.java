package ue.edu.co.fittrackandroid.registro.vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.perfil.datos.DescargadorFotoPerfil;
import ue.edu.co.fittrackandroid.perfil.datos.FotoPerfilLocal;
import ue.edu.co.fittrackandroid.registro.datos.RegistroRepository;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroRequest;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroResponse;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para registrar una cuenta nueva.
 * Valida los datos escritos con las mismas reglas que aplica el backend y los envía
 * a auth/register. La pantalla solo avanza a Inicio cuando el servidor confirma el
 * registro y devuelve un token: la contraseña nunca se guarda en el dispositivo.
 */
public class CrearCuentaFragment extends Fragment {

    /** El backend acepta nombre y correo de hasta 255 caracteres. */
    private static final int LONGITUD_MAXIMA_NOMBRE = 255;
    private static final int LONGITUD_MAXIMA_CORREO = 255;

    /** El backend exige una contraseña de 8 caracteres como mínimo y hasta 72 como máximo. */
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private static final int LONGITUD_MAXIMA_CONTRASENA = 72;

    private EditText etNombreUsuario;
    private EditText etCorreo;
    private EditText etContrasenaRegistro;
    private EditText etRepetirContrasena;
    private ImageButton btnOjoContrasenaRegistro;
    private TextView tvErrorNombreUsuario;
    private TextView tvErrorCorreo;
    private TextView tvErrorContrasenaRegistro;
    private TextView tvErrorRepetirContrasena;
    private ProgressBar pbCrearCuenta;
    private SesionManager sesionManager;
    private RegistroRepository registroRepository;
    private Call<RegistroResponse> currentCallRegistro;
    private boolean registrandoCuenta;
    private boolean contrasenaVisible;

    public CrearCuentaFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_cuenta, container, false);

        etNombreUsuario = view.findViewById(R.id.etNombreUsuario);
        etCorreo = view.findViewById(R.id.etCorreo);
        etContrasenaRegistro = view.findViewById(R.id.etContrasenaRegistro);
        etRepetirContrasena = view.findViewById(R.id.etRepetirContrasena);
        btnOjoContrasenaRegistro = view.findViewById(R.id.btnOjoContrasenaRegistro);
        tvErrorNombreUsuario = view.findViewById(R.id.tvErrorNombreUsuario);
        tvErrorCorreo = view.findViewById(R.id.tvErrorCorreo);
        tvErrorContrasenaRegistro = view.findViewById(R.id.tvErrorContrasenaRegistro);
        tvErrorRepetirContrasena = view.findViewById(R.id.tvErrorRepetirContrasena);
        pbCrearCuenta = view.findViewById(R.id.pbCrearCuenta);

        sesionManager = new SesionManager(requireContext());
        registroRepository = new RegistroRepository(requireContext());

        limpiarErrorAlEscribir(etNombreUsuario, tvErrorNombreUsuario);
        limpiarErrorAlEscribir(etCorreo, tvErrorCorreo);
        limpiarErrorAlEscribir(etContrasenaRegistro, tvErrorContrasenaRegistro);
        limpiarErrorAlEscribir(etRepetirContrasena, tvErrorRepetirContrasena);

        btnOjoContrasenaRegistro.setOnClickListener(v -> alternarVisibilidadContrasena());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCrearCuenta),
                true,
                getString(R.string.btnGuardar)
        );
        activity.setAccionToolbar(this::crearCuenta);
        // Volver a habilitar la acción evita que quede bloqueada por una pantalla anterior.
        activity.habilitarAccionToolbar(true);
    }

    /**
     * Valida los datos del formulario y, si todo está correcto, los envía al backend.
     * Nombre y correo se recortan, pero la contraseña no: los espacios pueden
     * formar parte de ella y recortarla cambiaría lo que el usuario escribió.
     */
    private void crearCuenta() {
        // Bloqueo de reintentos: evita que una pulsación repetida envíe dos peticiones.
        if (registrandoCuenta) {
            return;
        }

        String nombreUsuario = etNombreUsuario.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasenaRegistro.getText().toString();
        String repetirContrasena = etRepetirContrasena.getText().toString();

        if (!validarCampos(nombreUsuario, correo, contrasena, repetirContrasena)) {
            return;
        }

        registrarCuenta(nombreUsuario, correo, contrasena);
    }

    /**
     * Revisa los cuatro campos en el mismo orden que valida el backend y muestra
     * el primer error que encuentre. Devuelve true solo si el registro se puede
     * enviar al servidor.
     */
    private boolean validarCampos(String nombreUsuario, String correo, String contrasena,
                                  String repetirContrasena) {
        if (nombreUsuario.isEmpty()) {
            mostrarError(etNombreUsuario, tvErrorNombreUsuario, R.string.tvErrorNombreUsuario);
            return false;
        }

        if (nombreUsuario.length() > LONGITUD_MAXIMA_NOMBRE) {
            mostrarError(etNombreUsuario, tvErrorNombreUsuario,
                    R.string.tvErrorNombreUsuario_largo);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError(etCorreo, tvErrorCorreo, R.string.tvErrorCorreo);
            return false;
        }

        if (correo.length() > LONGITUD_MAXIMA_CORREO) {
            mostrarError(etCorreo, tvErrorCorreo, R.string.tvErrorCorreo_largo);
            return false;
        }

        // La contraseña vacía también se avisa aquí porque no alcanza la longitud mínima.
        if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            mostrarError(etContrasenaRegistro, tvErrorContrasenaRegistro,
                    R.string.tvErrorContrasenaRegistro);
            return false;
        }

        if (contrasena.length() > LONGITUD_MAXIMA_CONTRASENA) {
            mostrarError(etContrasenaRegistro, tvErrorContrasenaRegistro,
                    R.string.tvErrorContrasenaRegistro_larga);
            return false;
        }

        if (!contrasena.equals(repetirContrasena)) {
            mostrarError(etRepetirContrasena, tvErrorRepetirContrasena,
                    R.string.tvErrorRepetirContrasena);
            return false;
        }

        return true;
    }

    /**
     * Envía el nombre, el correo y la contraseña ya validados al backend y espera su
     * respuesta. Mientras la llamada está en vuelo la pantalla queda bloqueada, de modo
     * que no se puedan crear dos cuentas con el mismo toque.
     */
    private void registrarCuenta(String nombreUsuario, String correo, String contrasena) {
        RegistroRequest registroRequest = new RegistroRequest(nombreUsuario, correo, contrasena);

        mostrarRegistrando(true);

        currentCallRegistro = registroRepository.registrarUsuario(registroRequest);
        currentCallRegistro.enqueue(new Callback<RegistroResponse>() {

            @Override
            public void onResponse(@NonNull Call<RegistroResponse> call,
                                   @NonNull Response<RegistroResponse> response) {
                // Sin vista visible no hay nada que mostrar ni a dónde navegar.
                if (!isAdded() || getView() == null) {
                    return;
                }

                RegistroResponse registroResponse = response.body();
                if (response.isSuccessful()
                        && registroResponse != null
                        && registroResponse.getToken() != null
                        && !registroResponse.getToken().isEmpty()
                        && registroResponse.getUsuarioId() != null
                        && registroResponse.getUsuarioId() > 0) {
                    procesarCuentaCreada(registroResponse, correo);
                    return;
                }

                // Un 2xx sin token sirve de poco: no se puede abrir sesión con esa respuesta.
                if (response.isSuccessful()) {
                    mostrarRegistrando(false);
                    Toast.makeText(requireContext(), R.string.error_respuesta_invalida,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // El 409 significa que el correo ya existe, así que se señala ese campo.
                if (response.code() == HttpURLConnection.HTTP_CONFLICT) {
                    mostrarRegistrando(false);
                    mostrarError(etCorreo, tvErrorCorreo, R.string.tvErrorCorreo_registrado);
                    return;
                }

                // El resto, incluido el 400, los traduce ManejadorErroresApi.
                mostrarRegistrando(false);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), response.code())
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<RegistroResponse> call,
                                  @NonNull Throwable throwable) {
                // Una llamada cancelada es la pantalla cerrándose, no un fallo que avisar.
                if (call.isCanceled() || !isAdded() || getView() == null) {
                    return;
                }

                // Se restaura el formulario sin borrar lo escrito para que el usuario reintente.
                mostrarRegistrando(false);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /**
     * Guarda la sesión devuelta por el backend con el correo con el que se registró el
     * usuario, prepara su foto de perfil y abre Inicio. Solo se llega aquí con un 2xx y
     * un token utilizable, así que la cuenta ya existe en el servidor. La contraseña no
     * se guarda en ninguna parte.
     */
    private void procesarCuentaCreada(RegistroResponse registroResponse, String correo) {
        sesionManager.guardarTokens(registroResponse.getToken());
        sesionManager.guardarInfoPersonal(
                registroResponse.getUsuarioId(),
                registroResponse.getNombre(),
                correo
        );

        FotoPerfilLocal fotoPerfilLocal = new FotoPerfilLocal(requireContext());
        String fotoPerfilUrl = registroResponse.getFotoPerfilUrl();
        if (fotoPerfilUrl == null || fotoPerfilUrl.trim().isEmpty()) {
            fotoPerfilLocal.eliminar(registroResponse.getUsuarioId());
        } else {
            DescargadorFotoPerfil.descargar(
                    fotoPerfilUrl,
                    registroResponse.getUsuarioId(),
                    fotoPerfilLocal
            );
        }

        // El Toast puede llevar el texto directo, según la guía del proyecto.
        Toast.makeText(requireContext(), "Cuenta creada", Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).mostrarHome();
    }

    /**
     * Bloquea el formulario y la acción de GUARDAR mientras la petición está en
     * vuelo, y los vuelve a habilitar cuando termina. El texto escrito se conserva
     * para poder reintentar sin volver a escribirlo todo.
     */
    private void mostrarRegistrando(boolean registrando) {
        registrandoCuenta = registrando;
        etNombreUsuario.setEnabled(!registrando);
        etCorreo.setEnabled(!registrando);
        etContrasenaRegistro.setEnabled(!registrando);
        etRepetirContrasena.setEnabled(!registrando);
        btnOjoContrasenaRegistro.setEnabled(!registrando);
        pbCrearCuenta.setVisibility(registrando ? View.VISIBLE : View.GONE);
        ((MainActivity) requireActivity()).habilitarAccionToolbar(!registrando);
    }

    /**
     * Muestra u oculta la contraseña del primer campo, como hace el login.
     * Solo cambia cómo se pinta el texto: la contraseña escrita no se modifica.
     */
    private void alternarVisibilidadContrasena() {
        int posicionCursor = etContrasenaRegistro.getSelectionEnd();

        if (contrasenaVisible) {
            etContrasenaRegistro.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnOjoContrasenaRegistro.setImageResource(R.drawable.ic_visibility);
        } else {
            etContrasenaRegistro.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnOjoContrasenaRegistro.setImageResource(R.drawable.ic_visibility_off);
        }

        contrasenaVisible = !contrasenaVisible;
        etContrasenaRegistro.setSelection(posicionCursor);
    }

    /**
     * Muestra el mensaje de error del campo que falló la validación, le cambia el
     * fondo, le pide el foco y repite el mismo texto en un Toast.
     */
    private void mostrarError(EditText campo, TextView textoError, @StringRes int mensajeId) {
        textoError.setText(mensajeId);
        textoError.setVisibility(View.VISIBLE);
        campo.setBackgroundResource(R.drawable.bg_input_error);
        campo.requestFocus();
        Toast.makeText(requireContext(), getString(mensajeId), Toast.LENGTH_SHORT).show();
    }

    /** Oculta el error del campo mientras el usuario escribe. */
    private void limpiarErrorAlEscribir(EditText campo, TextView textoError) {
        campo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textoError.setVisibility(View.GONE);
                campo.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }

    @Override
    public void onDestroyView() {
        // Si la pantalla se cierra mientras se registra, la llamada se cancela para no
        // intentar tocar vistas de un Fragment que ya no está visible.
        if (currentCallRegistro != null) {
            currentCallRegistro.cancel();
            currentCallRegistro = null;
        }

        super.onDestroyView();
    }
}
