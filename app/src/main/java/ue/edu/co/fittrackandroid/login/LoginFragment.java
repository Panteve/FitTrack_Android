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

import androidx.fragment.app.Fragment;

import ue.edu.co.fittrackandroid.hoy.MainActivity;
import ue.edu.co.fittrackandroid.R;

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

    private boolean contrasenaVisible = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etCorreo = view.findViewById(R.id.etCorreo);
        etContrasena = view.findViewById(R.id.etContrasena);
        btnOjoContrasena = view.findViewById(R.id.btnOjoContrasena);
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion);
        tvErrorCorreo = view.findViewById(R.id.tvErrorCorreo);
        tvErrorContrasena = view.findViewById(R.id.tvErrorContrasena);
        TextView tvRecuperarClave = view.findViewById(R.id.tvRecuperarClave);
        Button btnCrearCuenta = view.findViewById(R.id.btnCrearCuenta);

        limpiarErrorAlEscribir(etCorreo, tvErrorCorreo);
        limpiarErrorAlEscribir(etContrasena, tvErrorContrasena);

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());
        btnOjoContrasena.setOnClickListener(v -> alternarVisibilidadContrasena());
        tvRecuperarClave.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Recuperación de contraseña próximamente", Toast.LENGTH_SHORT).show());
        btnCrearCuenta.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Registro próximamente", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void iniciarSesion() {
        mostrarCargando(true);
        btnIniciarSesion.postDelayed(() -> {
            // TODO: Validar credenciales reales contra backend / base de datos
            // Si login OK → guardar sesión (SharedPreferences, token, etc.)
            ((MainActivity) requireActivity()).mostrarHome();
        }, 900);
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
                textoError.setVisibility(View.GONE);
                campo.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }
}