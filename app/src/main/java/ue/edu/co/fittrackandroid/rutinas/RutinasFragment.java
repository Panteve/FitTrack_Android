package ue.edu.co.fittrackandroid.rutinas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Fragment para la pantalla de rutinas (lista de planes de entrenamiento).
 */
public class RutinasFragment extends Fragment {

    private Button btnNuevaRutina;
    private Button btnEmpezarRutinaVacia;

    // Botones de tarjeta 1
    private Button btnVerDetalles1;
    private Button btnIniciarRutina1;

    // Botones de tarjeta 2
    private Button btnVerDetalles2;
    private Button btnIniciarRutina2;

    // Botones de tarjeta 3
    private Button btnVerDetalles3;
    private Button btnIniciarRutina3;

    public RutinasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutinas, container, false);

        btnNuevaRutina = view.findViewById(R.id.btnNuevaRutina);
        btnEmpezarRutinaVacia = view.findViewById(R.id.btnEmpezarRutinaVacia);

        btnVerDetalles1 = view.findViewById(R.id.btnVerDetalles1);
        btnIniciarRutina1 = view.findViewById(R.id.btnIniciarRutina1);

        btnVerDetalles2 = view.findViewById(R.id.btnVerDetalles2);
        btnIniciarRutina2 = view.findViewById(R.id.btnIniciarRutina2);

        btnVerDetalles3 = view.findViewById(R.id.btnVerDetalles3);
        btnIniciarRutina3 = view.findViewById(R.id.btnIniciarRutina3);

        btnNuevaRutina.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Crear nueva rutina próximamente", Toast.LENGTH_SHORT).show());

        btnEmpezarRutinaVacia.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Empezar rutina vacía próximamente", Toast.LENGTH_SHORT).show());

        btnVerDetalles1.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Ver detalles: Día A", Toast.LENGTH_SHORT).show());
        btnIniciarRutina1.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Iniciar: Día A", Toast.LENGTH_SHORT).show());

        btnVerDetalles2.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Ver detalles: Día B", Toast.LENGTH_SHORT).show());
        btnIniciarRutina2.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Iniciar: Día B", Toast.LENGTH_SHORT).show());

        btnVerDetalles3.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Ver detalles: Día C", Toast.LENGTH_SHORT).show());
        btnIniciarRutina3.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Iniciar: Día C", Toast.LENGTH_SHORT).show());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver desde Ejercicios o Crear ejercicio, la toolbar debe quedar como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();
    }
}