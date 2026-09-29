package ue.edu.co.fittrackandroid.hoy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para la pantalla principal (Hoy).
 * Muestra la fecha, el registro semanal, la rutina asignada y los últimos entrenamientos.
 */
public class HomeFragment extends Fragment {

    private Button btnIniciarEntrenamiento;
    private RecyclerView rvUltimosEntrenamientos;
    private TextView tvCantidadUltimosEntrenamientos;
    private TextView tvSinEntrenamientos;
    private TextView tvBienvenida;
    private TextView tvFecha;
    private TextView tvNombreRutina;
    private TextView tvEjerciciosRutina;
    private SesionManager sesionManager;
    private HomeRepository homeRepository;

    private Integer idProximaRutina;
    private Call<HomeResponse> currentCall;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initObjects(view);
        homeRepository = new HomeRepository(requireContext());

        cargarInfoHome();
        btnIniciarEntrenamiento.setOnClickListener(v -> iniciarEntrenamiento());

        return view;
    }

    private void initObjects(View view) {
        btnIniciarEntrenamiento = view.findViewById(R.id.btnIniciarEntrenamiento);
        rvUltimosEntrenamientos = view.findViewById(R.id.rvUltimosEntrenamientos);
        tvCantidadUltimosEntrenamientos = view.findViewById(R.id.tvCantidadUltimosEntrenamientos);
        tvSinEntrenamientos = view.findViewById(R.id.tvSinEntrenamientos);
        tvNombreRutina = view.findViewById(R.id.tvNombreRutina);
        tvEjerciciosRutina = view.findViewById(R.id.tvEjerciciosRutina);
        tvFecha = view.findViewById(R.id.tvFecha);
        tvBienvenida = view.findViewById(R.id.tvBienvenida);
        sesionManager = new SesionManager(requireContext());

        tvFecha.setText(getString(R.string.tvFecha, formatearFechaHoy()));
        tvBienvenida.setText(sesionManager.obtenerNombre());
        rvUltimosEntrenamientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        mostrarEstadoCarga();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver desde Crear ejercicio, la toolbar debe quedar como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();

        // TODO: Volver a consultar la rutina, el registro semanal y el historial cuando
        // estos datos sean persistentes, para reflejar los cambios hechos en otras pantallas.
    }

    private void iniciarEntrenamiento() {
        if (idProximaRutina == null) {
            return;
        }

        btnIniciarEntrenamiento.setEnabled(false);
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento_loading);

        // TODO: Eliminar esta espera simulada. El botón debe recuperar la rutina asignada
        // desde el almacenamiento y continuar solamente cuando sus datos estén disponibles.
        btnIniciarEntrenamiento.postDelayed(() -> {
            btnIniciarEntrenamiento.setEnabled(true);
            btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);

            // TODO: Reemplazar el nombre y esta lista por los datos de la rutina asignada real.
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

    /** Consulta y presenta la información principal del usuario. */
    private void cargarInfoHome() {
        currentCall = homeRepository.getInfoHome();
        currentCall.enqueue(new Callback<HomeResponse>() {

            @Override
            public void onResponse(@NonNull Call<HomeResponse> call, @NonNull Response<HomeResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    HomeResponse homeResponse = response.body();
                    mostrarRutinaRecomendada(homeResponse.getProximaRutina());
                    mostrarUltimosEntrenamientos(homeResponse.getUltimosEntrenamientos());
                } else {
                    mostrarErrorCarga();
                    ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<HomeResponse> call, @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }

                mostrarErrorCarga();
                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /** Deja la pantalla en espera mientras se consulta la API. */
    private void mostrarEstadoCarga() {
        idProximaRutina = null;
        tvNombreRutina.setText(R.string.tvNombreRutina_loading);
        tvEjerciciosRutina.setText(R.string.tvEjerciciosRutina_loading);
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento_loading);
        btnIniciarEntrenamiento.setEnabled(false);

        rvUltimosEntrenamientos.setVisibility(View.GONE);
        tvCantidadUltimosEntrenamientos.setVisibility(View.GONE);
        tvSinEntrenamientos.setText(R.string.tvSinEntrenamientos_loading);
        tvSinEntrenamientos.setVisibility(View.VISIBLE);
    }

    /** Muestra la rutina recomendada o un estado vacío cuando el usuario no tiene una. */
    private void mostrarRutinaRecomendada(ProximaRutina proximaRutina) {
        if (proximaRutina == null || proximaRutina.getId() == null) {
            idProximaRutina = null;
            tvNombreRutina.setText(R.string.tvNombreRutina_vacia);
            tvEjerciciosRutina.setText(R.string.tvEjerciciosRutina_vacia);
            btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);
            btnIniciarEntrenamiento.setEnabled(false);
            return;
        }

        int cantidadEjercicios = proximaRutina.getNumeroDeEjercicios() == null
                ? 0
                : proximaRutina.getNumeroDeEjercicios();

        idProximaRutina = proximaRutina.getId();
        tvNombreRutina.setText(proximaRutina.getNombre());
        tvEjerciciosRutina.setText(
                getString(R.string.tvEjerciciosRutina, cantidadEjercicios));
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);
        btnIniciarEntrenamiento.setEnabled(true);
    }

    /** Muestra el historial reciente o indica que todavía no existen registros. */
    private void mostrarUltimosEntrenamientos(List<UltimoEntrenamiento> entrenamientos) {
        int cantidadEntrenamientos = entrenamientos == null ? 0 : entrenamientos.size();
        tvCantidadUltimosEntrenamientos.setText(
                getString(R.string.tvCantidadUltimosEntrenamientos, cantidadEntrenamientos));
        tvCantidadUltimosEntrenamientos.setVisibility(View.VISIBLE);

        if (cantidadEntrenamientos == 0) {
            rvUltimosEntrenamientos.setAdapter(null);
            rvUltimosEntrenamientos.setVisibility(View.GONE);
            tvSinEntrenamientos.setText(R.string.tvSinEntrenamientos);
            tvSinEntrenamientos.setVisibility(View.VISIBLE);
            return;
        }

        rvUltimosEntrenamientos.setAdapter(
                new UltimoEntrenamientoAdapter(entrenamientos, this::abrirResumenEntrenamiento));
        rvUltimosEntrenamientos.setVisibility(View.VISIBLE);
        tvSinEntrenamientos.setVisibility(View.GONE);
    }

    /** Presenta un estado estable cuando no fue posible cargar la información del Home. */
    private void mostrarErrorCarga() {
        idProximaRutina = null;
        tvNombreRutina.setText(R.string.tvNombreRutina_error);
        tvEjerciciosRutina.setText(R.string.tvEjerciciosRutina_error);
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);
        btnIniciarEntrenamiento.setEnabled(false);

        rvUltimosEntrenamientos.setAdapter(null);
        rvUltimosEntrenamientos.setVisibility(View.GONE);
        tvCantidadUltimosEntrenamientos.setVisibility(View.GONE);
        tvSinEntrenamientos.setText(R.string.tvSinEntrenamientos_error);
        tvSinEntrenamientos.setVisibility(View.VISIBLE);
    }

    /**
     * Abre la pantalla con el resultado del entrenamiento reciente que el usuario pulsó.
     * El adapter solo entrega el elemento; la navegación la decide esta pantalla.
     *
     * @param entrenamiento entrenamiento seleccionado en la lista.
     */
    private void abrirResumenEntrenamiento(UltimoEntrenamiento entrenamiento) {
        // Un registro sin resumen asociado no se puede abrir: no se inventa una pantalla vacía.
        if (entrenamiento.getResumen() == null) {
            // TODO: Cuando el historial use identificadores, cargar el resumen correspondiente
            // y mostrar un mensaje si el entrenamiento ya no existe o no se puede recuperar.
            return;
        }

        ((MainActivity) requireActivity()).mostrarResumenEntrenamiento(entrenamiento.getResumen());
    }

    private String formatearFechaHoy() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        String fecha = formato.format(new Date());
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        super.onDestroyView();
    }
}
