package ue.edu.co.fittrackandroid.perfil;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
 * Fragment para cambiar la contraseña de la cuenta. Valida los tres campos y, como
 * todavía no existe autenticación real, termina el cambio con un mensaje y vuelve al perfil.
 */
public class CambiarContrasenaFragment extends Fragment {

    /** Cantidad mínima de caracteres que debe tener la contraseña nueva. */
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private EditText etContrasenaActual;
    private EditText etNuevaContrasena;
    private EditText etRepetirNuevaContrasena;
    private TextView tvErrorContrasenaActual;
    private TextView tvErrorNuevaContrasena;
    private TextView tvErrorRepetirNuevaContrasena;

    public CambiarContrasenaFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cambiar_contrasena, container, false);

        etContrasenaActual = view.findViewById(R.id.etContrasenaActual);
        etNuevaContrasena = view.findViewById(R.id.etNuevaContrasena);
        etRepetirNuevaContrasena = view.findViewById(R.id.etRepetirNuevaContrasena);
        tvErrorContrasenaActual = view.findViewById(R.id.tvErrorContrasenaActual);
        tvErrorNuevaContrasena = view.findViewById(R.id.tvErrorNuevaContrasena);
        tvErrorRepetirNuevaContrasena = view.findViewById(R.id.tvErrorRepetirNuevaContrasena);

        limpiarErrorAlEscribir(etContrasenaActual, tvErrorContrasenaActual);
        limpiarErrorAlEscribir(etNuevaContrasena, tvErrorNuevaContrasena);
        limpiarErrorAlEscribir(etRepetirNuevaContrasena, tvErrorRepetirNuevaContrasena);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCambiarContrasena),
                true,
                getString(R.string.btnGuardar)
        );
        activity.setAccionToolbar(this::cambiarContrasena);
    }

    /** Valida los tres campos y termina el cambio de contraseña. */
    private void cambiarContrasena() {
        String contrasenaActual = etContrasenaActual.getText().toString();
        String nuevaContrasena = etNuevaContrasena.getText().toString();
        String repetirNuevaContrasena = etRepetirNuevaContrasena.getText().toString();

        if (contrasenaActual.isEmpty()) {
            mostrarError(etContrasenaActual, tvErrorContrasenaActual, "Ingresa tu contraseña actual");
            return;
        }

        // La contraseña vacía también se avisa aquí porque no alcanza la longitud mínima.
        if (nuevaContrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            mostrarError(etNuevaContrasena, tvErrorNuevaContrasena,
                    "La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!nuevaContrasena.equals(repetirNuevaContrasena)) {
            mostrarError(etRepetirNuevaContrasena, tvErrorRepetirNuevaContrasena,
                    "Verifica que las contraseñas coincidan");
            return;
        }

        // TODO: Verificar la contraseña actual contra el backend y enviar la nueva antes de
        // confirmarlo. La contraseña no debe quedar guardada en el dispositivo.
        Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).regresar();
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
