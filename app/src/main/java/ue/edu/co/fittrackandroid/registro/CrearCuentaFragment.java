package ue.edu.co.fittrackandroid.registro;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Fragment para registrar una cuenta nueva. Valida los datos escritos por el usuario y,
 * como todavía no existe autenticación real, termina el registro con un mensaje y entra
 * a la pantalla principal.
 */
public class CrearCuentaFragment extends Fragment {

    /** Cantidad mínima de caracteres que debe tener la contraseña. */
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private EditText etNombreUsuario;
    private EditText etCorreo;
    private EditText etContrasenaRegistro;
    private EditText etRepetirContrasena;
    private TextView tvErrorNombreUsuario;
    private TextView tvErrorCorreo;
    private TextView tvErrorContrasenaRegistro;
    private TextView tvErrorRepetirContrasena;

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
        tvErrorNombreUsuario = view.findViewById(R.id.tvErrorNombreUsuario);
        tvErrorCorreo = view.findViewById(R.id.tvErrorCorreo);
        tvErrorContrasenaRegistro = view.findViewById(R.id.tvErrorContrasenaRegistro);
        tvErrorRepetirContrasena = view.findViewById(R.id.tvErrorRepetirContrasena);

        limpiarErrorAlEscribir(etNombreUsuario, tvErrorNombreUsuario);
        limpiarErrorAlEscribir(etCorreo, tvErrorCorreo);
        limpiarErrorAlEscribir(etContrasenaRegistro, tvErrorContrasenaRegistro);
        limpiarErrorAlEscribir(etRepetirContrasena, tvErrorRepetirContrasena);

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
    }

    /** Valida los datos del formulario y termina el registro. */
    private void crearCuenta() {
        String nombreUsuario = etNombreUsuario.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasenaRegistro.getText().toString();
        String repetirContrasena = etRepetirContrasena.getText().toString();

        if (nombreUsuario.isEmpty()) {
            mostrarError(etNombreUsuario, tvErrorNombreUsuario, "Ingresa tu nombre");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError(etCorreo, tvErrorCorreo, "Ingresa un correo válido");
            return;
        }

        // La contraseña vacía también se avisa aquí porque no alcanza la longitud mínima.
        if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            mostrarError(etContrasenaRegistro, tvErrorContrasenaRegistro,
                    "La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!contrasena.equals(repetirContrasena)) {
            mostrarError(etRepetirContrasena, tvErrorRepetirContrasena,
                    "Verifica que las contraseñas coincidan");
            return;
        }

        // TODO: Enviar los datos al backend para crear la cuenta y guardar la sesión del
        // usuario autenticado. La contraseña no debe quedar guardada en el dispositivo.
        Toast.makeText(requireContext(), "Cuenta creada", Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).mostrarHome();
    }

    /**
     * Muestra el mensaje de error del campo y pide el foco en él.
     *
     * @param campo       campo que falló la validación.
     * @param textoError  mensaje de error que ya está en la pantalla.
     * @param mensaje     texto del mensaje que se muestra en un Toast.
     */
    private void mostrarError(EditText campo, TextView textoError, String mensaje) {
        textoError.setVisibility(View.VISIBLE);
        campo.setBackgroundResource(R.drawable.bg_input_error);
        campo.requestFocus();
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
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
}
