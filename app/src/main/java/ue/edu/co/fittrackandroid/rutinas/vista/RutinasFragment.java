package ue.edu.co.fittrackandroid.rutinas.vista;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.rutinas.datos.RutinaRepository;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinasResponse;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para la pantalla de rutinas (lista de planes de entrenamiento).
 */
public class RutinasFragment extends Fragment {

    private Button btnNuevaRutina;
    private TextView tvCantidadPlanes;
    private ProgressBar pbCargaRutinas;
    private View layoutRutinasVacias;
    private RecyclerView rvPlanes;
    private List<RutinasResponse> rutinas;
    private RutinaRepository rutinaRepository;
    private Call<List<RutinasResponse>> currentCallRutinas;
    private Call<RutinaResponse> currentCallDetalleRutina;

    public RutinasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutinas, container, false);

        initObjects(view);
        rutinaRepository = new RutinaRepository(requireContext());
        obtenerRutinas();

        btnNuevaRutina.setOnClickListener(v ->
                ((MainActivity) requireActivity()).mostrarCrearRutina());

        return view;
    }

    private void initObjects(View view){
        btnNuevaRutina = view.findViewById(R.id.btnNuevaRutina);
        tvCantidadPlanes = view.findViewById(R.id.tvCantidadPlanes);
        pbCargaRutinas = view.findViewById(R.id.pbCargaRutinas);
        layoutRutinasVacias = view.findViewById(R.id.layoutRutinasVacias);
        rvPlanes = view.findViewById(R.id.rvPlanes);
        rvPlanes.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void obtenerRutinas() {
        mostrarEstadoCarga();
        currentCallRutinas = rutinaRepository.getRutinas();
        currentCallRutinas.enqueue(new Callback<List<RutinasResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<RutinasResponse>> call,
                                   @NonNull Response<List<RutinasResponse>> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    rutinas = response.body();
                    mostrarRutinas(rutinas);
                } else {
                    mostrarErrorCarga();
                    ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RutinasResponse>> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }
                mostrarErrorCarga();
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    /** Deja visible únicamente el indicador mientras se consulta la API. */
    private void mostrarEstadoCarga() {
        pbCargaRutinas.setVisibility(View.VISIBLE);
        tvCantidadPlanes.setVisibility(View.INVISIBLE);
        layoutRutinasVacias.setVisibility(View.GONE);
        rvPlanes.setVisibility(View.GONE);
    }

    /** Muestra las rutinas recibidas o el mensaje para crear la primera. */
    private void mostrarRutinas(List<RutinasResponse> rutinasRecibidas) {
        pbCargaRutinas.setVisibility(View.GONE);
        tvCantidadPlanes.setText(
                getString(R.string.tvCantidadPlanes, rutinasRecibidas.size()));
        tvCantidadPlanes.setVisibility(View.VISIBLE);

        if (rutinasRecibidas.isEmpty()) {
            rvPlanes.setAdapter(null);
            rvPlanes.setVisibility(View.GONE);
            layoutRutinasVacias.setVisibility(View.VISIBLE);
            return;
        }

        layoutRutinasVacias.setVisibility(View.GONE);
        rvPlanes.setAdapter(new RutinaAdapter(
                rutinasRecibidas,
                RutinasFragment.this::iniciarEntrenamientoConRutina));
        rvPlanes.setVisibility(View.VISIBLE);
    }

    /** Oculta los estados de datos cuando la consulta no pudo completarse. */
    private void mostrarErrorCarga() {
        pbCargaRutinas.setVisibility(View.GONE);
        tvCantidadPlanes.setVisibility(View.INVISIBLE);
        layoutRutinasVacias.setVisibility(View.GONE);
        rvPlanes.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver desde Ejercicios o Crear ejercicio, la toolbar debe quedar como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();

    }

    /** Consulta el detalle del plan elegido antes de iniciar el entrenamiento. */
    private void iniciarEntrenamientoConRutina(RutinasResponse rutina) {
        currentCallDetalleRutina = rutinaRepository.getRutinaById(rutina.getId());
        currentCallDetalleRutina.enqueue(new Callback<RutinaResponse>() {
            @Override
            public void onResponse(@NonNull Call<RutinaResponse> call,
                                   @NonNull Response<RutinaResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ((MainActivity) requireActivity())
                            .mostrarEntrenamientoActivo(response.body());
                } else {
                    ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RutinaResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }
                ManejadorErroresApi.obtenerToast(requireContext(), throwable).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (currentCallRutinas != null) {
            currentCallRutinas.cancel();
        }
        if (currentCallDetalleRutina != null) {
            currentCallDetalleRutina.cancel();
        }
        super.onDestroyView();
    }
}
