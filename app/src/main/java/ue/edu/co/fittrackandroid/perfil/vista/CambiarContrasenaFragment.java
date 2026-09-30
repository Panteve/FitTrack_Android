package ue.edu.co.fittrackandroid.perfil.vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import ue.edu.co.fittrackandroid.perfil.datos.PerfilRepository;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarContrasenaRequest;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para cambiar la contraseña de la cuenta. Valida los tres campos y envía el
 * cambio al backend: solo se confirma cuando el servidor responde 204. Ni la contraseña
 * actual ni la nueva se guardan en el dispositivo, solo viajan en la petición.
 */
public class CambiarContrasenaFragment extends Fragment {

    /** El backend exige una contraseña de 8 caracteres como mínimo y hasta 72 como máximo. */
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private static final int LONGITUD_MAXIMA_CONTRASENA = 72;

    private EditText etContrasenaActual;
    private EditText etNuevaContrasena;
    private EditText etRepetirNuevaContrasena;
    private TextView tvErrorContrasenaActual;
    private TextView tvErrorNuevaContrasena;
    private TextView tvErrorRepetirNuevaContrasena;
    private ProgressBar pbGuardarContrasena;
    private PerfilRepository perfilRepository;
    private Call<Void> currentCallCambiarContrasena;
    private boolean cambiandoContrasena;

    /** Crea el fragmento vacío, tal como lo exige el sistema al reconstruir la pantalla. */
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
        pbGuardarContrasena = view.findViewById(R.id.pbGuardarContrasena);

        perfilRepository = new PerfilRepository(requireContext());

        limpiarErrorAlEscribir(etContrasenaActual, tvErrorContrasenaActual);
        limpiarErrorAlEscribir(etNuevaContrasena, tvErrorNuevaContrasena);
        limpiarErrorAlEscribir(etRepetirNuevaContrasena, tvErrorRepetirNuevaContrasena);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        configurarToolbar(false);
    }

    /**
     * Deja la toolbar lista para esta pantalla. Mientras se espera la respuesta la acción
     * GUARDAR muestra otro texto, para que el usuario vea que el toque sí fue recibido.
     * Si la petición está en vuelo la acción se dibuja como ocupada y se deshabilita, y
     * si la pantalla está libre se vuelve a habilitar para permitir nuevos intentos.
     */
    private void configurarToolbar(boolean guardando) {
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCambiarContrasena),
                true,
                getString(guardando ? R.string.btnGuardar_loading : R.string.btnGuardar)
        );
        // mostrarToolbarSecundaria borra el listener, hay que volver a asignarlo.
        activity.setAccionToolbar(this::cambiarContrasena);
        activity.habilitarAccionToolbar(!guardando);
    }

    /**
     * Valida los tres campos y pide al backend que actualice la contraseña.
     * El texto no se recorta con trim(): los espacios pueden formar parte de la contraseña.
     */
    private void cambiarContrasena() {
        // Bloqueo de reintentos: evita que una pulsación repetida envíe dos peticiones.
        if (cambiandoContrasena) {
            return;
        }

        String contrasenaActual = etContrasenaActual.getText().toString();
        String nuevaContrasena = etNuevaContrasena.getText().toString();
        String repetirNuevaContrasena = etRepetirNuevaContrasena.getText().toString();

        if (!validarCampos(contrasenaActual, nuevaContrasena, repetirNuevaContrasena)) {
            return;
        }

        CambiarContrasenaRequest cambiarContrasenaRequest =
                new CambiarContrasenaRequest(contrasenaActual, nuevaContrasena);

        mostrarCambiandoContrasena(true);

        currentCallCambiarContrasena = perfilRepository.cambiarContrasena(cambiarContrasenaRequest);
        currentCallCambiarContrasena.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                // El backend responde 204 sin cuerpo, así que solo importa el código.
                if (response.isSuccessful()) {
                    procesarContrasenaActualizada();
                    return;
                }

                // Un 401 aquí no es una sesión vencida: es la contraseña actual equivocada,
                // así que se señala ese campo en vez de mostrar el aviso genérico.
                if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    mostrarCambiandoContrasena(false);
                    mostrarError(etContrasenaActual, tvErrorContrasenaActual,
                            R.string.tvErrorContrasenaActual_incorrecta);
                    return;
                }

                mostrarCambiandoContrasena(false);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), response.code())
                        .show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                // Una llamada cancelada es la pantalla cerrándose, no un fallo que avisar.
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                // Se restaura el formulario sin borrar lo escrito para que el usuario reintente.
                mostrarCambiandoContrasena(false);
                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /**
     * Revisa los tres campos en orden y muestra el primer error que encuentre, usando los
     * tres textos escritos tal cual, sin recortar espacios. Devuelve verdadero solo cuando
     * la contraseña nueva tiene la longitud correcta, coincide con su confirmación y
     * además es distinta de la actual, porque en ese caso el cambio se puede enviar.
     */
    private boolean validarCampos(String contrasenaActual, String nuevaContrasena,
                                  String repetirNuevaContrasena) {
        if (contrasenaActual.isEmpty()) {
            mostrarError(etContrasenaActual, tvErrorContrasenaActual,
                    R.string.tvErrorContrasenaActual);
            return false;
        }

        if (nuevaContrasena.length() < LONGITUD_MINIMA_CONTRASENA
                || nuevaContrasena.length() > LONGITUD_MAXIMA_CONTRASENA) {
            mostrarError(etNuevaContrasena, tvErrorNuevaContrasena,
                    R.string.tvErrorNuevaContrasena);
            return false;
        }

        if (!nuevaContrasena.equals(repetirNuevaContrasena)) {
            mostrarError(etRepetirNuevaContrasena, tvErrorRepetirNuevaContrasena,
                    R.string.tvErrorRepetirNuevaContrasena);
            return false;
        }

        // Dejar la contraseña igual no cambia nada y solo confunde al usuario.
        if (nuevaContrasena.equals(contrasenaActual)) {
            mostrarError(etNuevaContrasena, tvErrorNuevaContrasena,
                    R.string.tvErrorNuevaContrasena_igual);
            return false;
        }

        return true;
    }

    /**
     * Limpia los campos y vuelve al perfil después de que el backend confirmó el 204.
     * La sesión sigue abierta: el token sigue siendo válido y no se toca nada en el
     * dispositivo. El Toast puede llevar el texto directo, según la guía del proyecto.
     */
    private void procesarContrasenaActualizada() {
        etContrasenaActual.setText("");
        etNuevaContrasena.setText("");
        etRepetirNuevaContrasena.setText("");
        mostrarCambiandoContrasena(false);

        Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).regresar();
    }

    /**
     * Muestra el estado de espera: el spinner aparece y la acción de la toolbar pasa a
     * "GUARDANDO…" mientras la petición está en vuelo, y todo vuelve a su estado normal
     * cuando termina. El texto escrito se conserva para poder reintentar.
     * Si la llamada sigue en vuelo los campos se bloquean, y si ya terminó se liberan.
     */
    private void mostrarCambiandoContrasena(boolean cambiando) {
        cambiandoContrasena = cambiando;
        pbGuardarContrasena.setVisibility(cambiando ? View.VISIBLE : View.GONE);
        etContrasenaActual.setEnabled(!cambiando);
        etNuevaContrasena.setEnabled(!cambiando);
        etRepetirNuevaContrasena.setEnabled(!cambiando);
        configurarToolbar(cambiando);
    }

    /**
     * Muestra el mensaje de error del campo y pide el foco en él. El campo recibido es el
     * que falló la validación, su etiqueta de error es la que ya está en la pantalla, y el
     * identificador recibido es el texto que además se muestra en un Toast.
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
        // Si la pantalla se cierra mientras se guarda la contraseña, la llamada se cancela
        // para no intentar tocar vistas de un Fragment que ya no está visible.
        if (currentCallCambiarContrasena != null) {
            currentCallCambiarContrasena.cancel();
        }

        super.onDestroyView();
    }
}
