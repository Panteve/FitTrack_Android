package ue.edu.co.fittrackandroid.ejercicios;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Fragment para crear un ejercicio (datos hardcodeados, sin lógica de negocio todavía).
 */
public class CrearEjercicioFragment extends Fragment {

    private ImageButton btnMultimediaEjercicio;
    private EditText etNombreEjercicio;
    private TextView tvErrorNombreEjercicio;

    public CrearEjercicioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_ejercicio, container, false);

        btnMultimediaEjercicio = view.findViewById(R.id.btnMultimediaEjercicio);
        etNombreEjercicio = view.findViewById(R.id.etNombreEjercicio);
        tvErrorNombreEjercicio = view.findViewById(R.id.tvErrorNombreEjercicio);

        View layoutCampoGrupoMuscular = view.findViewById(R.id.layoutCampoGrupoMuscular);
        View layoutCampoTipoEquipo = view.findViewById(R.id.layoutCampoTipoEquipo);
        View layoutCampoPeso = view.findViewById(R.id.layoutCampoPeso);
        View layoutCampoRepeticiones = view.findViewById(R.id.layoutCampoRepeticiones);

        btnMultimediaEjercicio.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Agregar multimedia próximamente", Toast.LENGTH_SHORT).show());

        layoutCampoGrupoMuscular.setOnClickListener(v -> seleccionarCampo("Grupo muscular"));
        layoutCampoTipoEquipo.setOnClickListener(v -> seleccionarCampo("Tipo de equipo"));
        layoutCampoPeso.setOnClickListener(v -> seleccionarCampo("Peso"));
        layoutCampoRepeticiones.setOnClickListener(v -> seleccionarCampo("Repeticiones"));

        limpiarErrorAlEscribir();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCrearEjercicio),
                true,
                getString(R.string.btnGuardar)
        );
        activity.setAccionToolbar(this::guardarEjercicio);
    }

    private void guardarEjercicio() {
        String nombre = etNombreEjercicio.getText().toString().trim();

        if (nombre.isEmpty()) {
            // TODO: Guardar realmente contra la base de datos
            tvErrorNombreEjercicio.setVisibility(View.VISIBLE);
            etNombreEjercicio.setBackgroundResource(R.drawable.bg_input_error);
            Toast.makeText(requireContext(), "Ingresa el nombre del ejercicio", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Ejercicio guardado", Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).regresar();
    }

    private void seleccionarCampo(String campo) {
        Toast.makeText(requireContext(), "Seleccionar " + campo + " próximamente", Toast.LENGTH_SHORT).show();
    }

    private void limpiarErrorAlEscribir() {
        etNombreEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvErrorNombreEjercicio.setVisibility(View.GONE);
                etNombreEjercicio.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }
}