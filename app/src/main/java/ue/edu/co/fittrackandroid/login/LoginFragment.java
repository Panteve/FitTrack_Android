package ue.edu.co.fittrackandroid.login;

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

import ue.edu.co.fittrackandroid.hoy.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.remote.TokenManager;
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
    private TokenManager tokenManager;
    private LoginRepository repository;
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
        repository = new LoginRepository(requireContext());
        return view;
    }

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
        currentCall = repository.loginUser(loginRequest);
        currentCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<LoginResponse> call,
                    @NonNull Response<LoginResponse> response
            ) {
                mostrarCargando(false);

                if (response.isSuccessful() && response.body() != null) {
                    tokenManager =
                            new TokenManager(requireContext());
                    tokenManager.guardarTokens(
                            response.body().getToken()
                    );
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

    private void mostrarCargando(boolean cargando) {
        if (cargando) {
            btnIniciarSesion.setEnabled(false);
            btnIniciarSesion.setText(R.string.btnIniciarSesion_loading);
        } else {
            btnIniciarSesion.setEnabled(true);
            btnIniciarSesion.setText(R.string.btnIniciarSesion);
        }
    }

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
