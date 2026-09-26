package ue.edu.co.fittrackandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;

/**
 * Fragment para la pantalla principal (Hoy).
 */
public class HomeFragment extends Fragment {

    private Button btnIniciarEntrenamiento;
    private ImageButton btnFabAgregar;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvFecha = view.findViewById(R.id.tvFecha);
        Button btnNuevoEjercicio = view.findViewById(R.id.btnNuevoEjercicio);
        btnIniciarEntrenamiento = view.findViewById(R.id.btnIniciarEntrenamiento);
        btnFabAgregar = view.findViewById(R.id.btnFabAgregar);

        tvFecha.setText(getString(R.string.tvFecha, formatearFechaHoy()));

        btnIniciarEntrenamiento.setOnClickListener(v -> iniciarEntrenamiento());
        btnNuevoEjercicio.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Nuevo ejercicio próximamente", Toast.LENGTH_SHORT).show());
        btnFabAgregar.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Agregar ejercicio próximamente", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void iniciarEntrenamiento() {
        btnIniciarEntrenamiento.setEnabled(false);
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento_loading);
        btnIniciarEntrenamiento.postDelayed(() -> {
            btnIniciarEntrenamiento.setEnabled(true);
            btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);
            Toast.makeText(requireContext(), "Iniciando sesión de entrenamiento", Toast.LENGTH_SHORT).show();
        }, 700);
    }

    private String formatearFechaHoy() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        String fecha = formato.format(new Date());
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }
}