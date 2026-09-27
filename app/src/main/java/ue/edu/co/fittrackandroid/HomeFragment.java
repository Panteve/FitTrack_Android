package ue.edu.co.fittrackandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Fragment para la pantalla principal (Hoy).
 * Muestra la fecha, el registro semanal, la rutina asignada y los últimos entrenamientos.
 */
public class HomeFragment extends Fragment {

    private Button btnIniciarEntrenamiento;
    private RecyclerView rvUltimosEntrenamientos;
    private TextView tvCantidadUltimosEntrenamientos;
    private TextView tvSinEntrenamientos;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvFecha = view.findViewById(R.id.tvFecha);
        btnIniciarEntrenamiento = view.findViewById(R.id.btnIniciarEntrenamiento);
        rvUltimosEntrenamientos = view.findViewById(R.id.rvUltimosEntrenamientos);
        tvCantidadUltimosEntrenamientos = view.findViewById(R.id.tvCantidadUltimosEntrenamientos);
        tvSinEntrenamientos = view.findViewById(R.id.tvSinEntrenamientos);

        tvFecha.setText(getString(R.string.tvFecha, formatearFechaHoy()));

        btnIniciarEntrenamiento.setOnClickListener(v -> iniciarEntrenamiento());

        configurarUltimosEntrenamientos();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver desde Crear ejercicio, la toolbar debe quedar como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();
    }

    private void iniciarEntrenamiento() {
        btnIniciarEntrenamiento.setEnabled(false);
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento_loading);
        btnIniciarEntrenamiento.postDelayed(() -> {
            btnIniciarEntrenamiento.setEnabled(true);
            btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);

            // TODO: Reemplazar esta lista por los ejercicios de la rutina asignada real.
            String[] nombresEjercicios = {
                    getString(R.string.tvNombreEjercicio1),
                    getString(R.string.tvNombreEjercicio2),
                    getString(R.string.tvNombreEjercicio3),
                    getString(R.string.tvNombreEjercicio4)
            };

            ((MainActivity) requireActivity()).mostrarEntrenamientoActivo(
                    getString(R.string.tvNombreRutina), nombresEjercicios);
        }, 700);
    }

    /** Arma la lista con los tres entrenamientos más recientes y ajusta el estado vacío. */
    private void configurarUltimosEntrenamientos() {
        List<UltimoEntrenamiento> entrenamientos = crearEntrenamientosDeEjemplo();

        rvUltimosEntrenamientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUltimosEntrenamientos.setAdapter(new UltimoEntrenamientoAdapter(entrenamientos));

        boolean hayEntrenamientos = !entrenamientos.isEmpty();
        rvUltimosEntrenamientos.setVisibility(hayEntrenamientos ? View.VISIBLE : View.GONE);
        tvSinEntrenamientos.setVisibility(hayEntrenamientos ? View.GONE : View.VISIBLE);
        tvCantidadUltimosEntrenamientos.setText(
                getString(R.string.tvCantidadUltimosEntrenamientos, entrenamientos.size()));
    }

    /**
     * Crea los entrenamientos de demostración, del más reciente al más antiguo.
     * TODO: Reemplazar por los entrenamientos realmente registrados.
     */
    private List<UltimoEntrenamiento> crearEntrenamientosDeEjemplo() {
        List<UltimoEntrenamiento> listaEntrenamientos = new ArrayList<>();

        listaEntrenamientos.add(new UltimoEntrenamiento(
                getString(R.string.tvNombreUltimoEntrenamiento1),
                getString(R.string.tvFechaUltimoEntrenamiento1),
                Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento1))));

        listaEntrenamientos.add(new UltimoEntrenamiento(
                getString(R.string.tvNombreUltimoEntrenamiento2),
                getString(R.string.tvFechaUltimoEntrenamiento2),
                Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento2))));

        listaEntrenamientos.add(new UltimoEntrenamiento(
                getString(R.string.tvNombreUltimoEntrenamiento3),
                getString(R.string.tvFechaUltimoEntrenamiento3),
                Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento3))));

        return listaEntrenamientos;
    }

    private String formatearFechaHoy() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        String fecha = formato.format(new Date());
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }
}
