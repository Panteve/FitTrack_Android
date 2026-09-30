package ue.edu.co.fittrackandroid.login.vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ue.edu.co.fittrackandroid.login.datos.LoginRepository;
import ue.edu.co.fittrackandroid.login.modelo.LoginRequest;
import ue.edu.co.fittrackandroid.login.modelo.LoginResponse;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.perfil.datos.DescargadorFotoPerfil;
import ue.edu.co.fittrackandroid.perfil.datos.FotoPerfilLocal;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Fragment para el login del usuario.
 */
public class LoginFragment extends Fragment {

    private EditText etCorreo;
    private EditText etContrasena;
    private ImageButton btnOjoContrasena;
    private Button btnIniciarSesion;
    private TextView tvErrorCorreo;
    private TextView tvErrorContrasena;
    private TextView tvErrorCredenciales;
    private Button btnCrearCuenta;
    private SesionManager sesionManager;
    private LoginRepository loginRepository;
    private Call<LoginResponse> currentCall;

    private boolean contrasenaVisible = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initObjects(view);
        limpiarErrorAlEscribir(etCorreo, tvErrorCorreo);
        limpiarErrorAlEscribir(etContrasena, tvErrorContrasena);
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());
        btnOjoContrasena.setOnClickListener(v -> alternarVisibilidadContrasena());
        btnCrearCuenta.setOnClickListener(v -> abrirCrearCuenta());
        loginRepository = new LoginRepository(requireContext());
        return view;
    }

    /** Busca las vistas del formulario y las guarda en sus campos. */
    private void initObjects(View view) {
        etCorreo = view.findViewById(R.id.etCorreo);
        etContrasena = view.findViewById(R.id.etContrasena);
        btnOjoContrasena = view.findViewById(R.id.btnOjoContrasena);
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion);
        tvErrorCorreo = view.findViewById(R.id.tvErrorCorreo);
        tvErrorContrasena = view.findViewById(R.id.tvErrorContrasena);
        tvErrorCredenciales = view.findViewById(R.id.tvErrorCredenciales);
        btnCrearCuenta = view.findViewById(R.id.btnCrearCuenta);
    }

    @Override
    public void onResume() {
        super.onResume();
        // El login se muestra sin barra de herramientas y debe ocultarla también cuando se
        // vuelve desde la pantalla de registro.
        ((MainActivity) requireActivity()).ocultarToolbar();
    }

    /** Abre la pantalla de registro de una cuenta nueva. */
    private void abrirCrearCuenta() {
        ((MainActivity) requireActivity()).mostrarCrearCuenta();
    }

    /**
     * Valida el correo y la contraseña, envía el inicio de sesión al servidor y, si la
     * respuesta trae un token válido, guarda la sesión, prepara la foto de perfil y
     * abre la pantalla de inicio.
     */
    private void iniciarSesion() {
        mostrarCargando(true);
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (correo.isEmpty()) {
            tvErrorCorreo.setText(R.string.error_correo_vacio);
            tvErrorCorreo.setVisibility(View.VISIBLE);
            mostrarCargando(false);
            return;
        } else {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                tvErrorCorreo.setText(R.string.error_correo_invalido);
                tvErrorCorreo.setVisibility(View.VISIBLE);
                mostrarCargando(false);
                return;
            }
        }

        if (contrasena.isEmpty()) {
            tvErrorContrasena.setVisibility(View.VISIBLE);
            mostrarCargando(false);
            return;
        }

        LoginRequest loginRequest =  new LoginRequest(correo, contrasena);
        currentCall = loginRepository.loginUser(loginRequest);
        currentCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<LoginResponse> call,
                    @NonNull Response<LoginResponse> response
            ) {
                mostrarCargando(false);

                LoginResponse loginResponse = response.body();
                if (response.isSuccessful()
                        && loginResponse != null
                        && loginResponse.getToken() != null
                        && !loginResponse.getToken().isEmpty()
                        && loginResponse.getUsuarioId() != null
                        && loginResponse.getUsuarioId() > 0) {
                    sesionManager = new SesionManager(requireContext());
                    sesionManager.guardarTokens(loginResponse.getToken());
                    sesionManager.guardarInfoPersonal(
                            loginResponse.getUsuarioId(),
                            loginResponse.getNombre(),
                            correo
                    );

                    FotoPerfilLocal fotoPerfilLocal = new FotoPerfilLocal(requireContext());
                    String fotoPerfilUrl = loginResponse.getFotoPerfilUrl();
                    if (fotoPerfilUrl == null || fotoPerfilUrl.trim().isEmpty()) {
                        fotoPerfilLocal.eliminar(loginResponse.getUsuarioId());
                    } else {
                        DescargadorFotoPerfil.descargar(
                                fotoPerfilUrl,
                                loginResponse.getUsuarioId(),
                                fotoPerfilLocal
                        );
                    }

                    ((MainActivity) requireActivity()).mostrarHome();
                    return;
                }

                int codigoRespuesta = response.code();
                if (codigoRespuesta == 400 || codigoRespuesta == 401 ) {
                    tvErrorCredenciales.setVisibility(View.VISIBLE);
                }else if (codigoRespuesta == 404) {
                    Toast.makeText(
                            requireContext(),
                            R.string.error_usuario_no_encontrado,
                            Toast.LENGTH_SHORT
                    ).show();
                }
                else {
                    ManejadorErroresApi
                            .obtenerToast(requireContext(), codigoRespuesta)
                            .show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<LoginResponse> call,
                    @NonNull Throwable throwable
            ) {
                mostrarCargando(false);

                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });

    }

    /** Activa o desactiva el botón de iniciar sesión y le cambia el texto según esté esperando la respuesta. */
    private void mostrarCargando(boolean cargando) {
        if (cargando) {
            btnIniciarSesion.setEnabled(false);
            btnIniciarSesion.setText(R.string.btnIniciarSesion_loading);
        } else {
            btnIniciarSesion.setEnabled(true);
            btnIniciarSesion.setText(R.string.btnIniciarSesion);
        }
    }

    /** Muestra u oculta los caracteres escritos en el campo de contraseña y cambia el ícono del ojo. */
    private void alternarVisibilidadContrasena() {
        int posicionCursor = etContrasena.getSelectionEnd();
        if (contrasenaVisible) {
            etContrasena.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnOjoContrasena.setImageResource(R.drawable.ic_visibility);
        } else {
            etContrasena.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnOjoContrasena.setImageResource(R.drawable.ic_visibility_off);
        }
        contrasenaVisible = !contrasenaVisible;
        etContrasena.setSelection(posicionCursor);
    }

    /** Oculta los mensajes de error del campo y de las credenciales en cuanto el usuario vuelve a escribir. */
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
                tvErrorCredenciales.setVisibility(View.GONE);
                textoError.setVisibility(View.GONE);
                campo.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }
}
