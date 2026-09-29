package ue.edu.co.fittrackandroid.rutinas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.HomeResponse;
import ue.edu.co.fittrackandroid.hoy.MainActivity;
import ue.edu.co.fittrackandroid.hoy.RutinaResponse;
import ue.edu.co.fittrackandroid.utils.ManejadorErroresApi;

/**
 * Fragment para la pantalla de rutinas (lista de planes de entrenamiento).
 */
public class RutinasFragment extends Fragment {

    private Button btnNuevaRutina;
    private TextView tvCantidadPlanes;
    private RecyclerView rvPlanes;
    private List<RutinasResponse> rutinas;
    private RutinaRepository rutinaRepository;
    private Call<List<RutinasResponse>> currentCallRutinas;

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
        rvPlanes = view.findViewById(R.id.rvPlanes);
        rvPlanes.setLayoutManager(new LinearLayoutManager(requireContext()));
        //rvPlanes.setAdapter(new RutinaAdapter(planes, this::iniciarEntrenamientoConRutina));
    }
    private void obtenerRutinas() {
        currentCallRutinas = rutinaRepository.getRutinas();
        currentCallRutinas.enqueue(new Callback<List<RutinasResponse>>() {
            @Override
            public void onResponse(Call<List<RutinasResponse>> call, Response<List<RutinasResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rutinas = response.body();
                    tvCantidadPlanes.setText(getString(R.string.tvCantidadPlanes, rutinas.size()));
                    rvPlanes.setAdapter(new RutinaAdapter(rutinas, rutina -> {
                    }));
                } else {
                    // Manejar el caso en que la respuesta no sea exitosa
                }
            }

            @Override
            public void onFailure(Call<List<RutinasResponse>> call, Throwable t) {
                ManejadorErroresApi.obtenerToast(requireContext(), t).show();
            }
        });





    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver desde Ejercicios o Crear ejercicio, la toolbar debe quedar como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();

        // TODO: Volver a consultar las rutinas cuando exista persistencia, para que una
        // rutina recién creada, editada o eliminada aparezca sin reiniciar la aplicación.
    }

    /**
     * Abre el entrenamiento en curso con los ejercicios del plan elegido.
     * Solo se envían nombres porque el modelo de rutinas guarda las series como un texto
     * de presentación, no como datos que se puedan recuperar.
    private void iniciarEntrenamientoConRutina(Rutina rutina) {
        List<String> nombresEjercicios = new ArrayList<>();
        for (EjercicioRutina ejercicio : rutina.getEjercicios()) {
            nombresEjercicios.add(ejercicio.getNombre());
        }

        // TODO: Enviar también las series, pesos y repeticiones de la rutina. Actualmente
        // solo se conservan los nombres porque EjercicioRutina guarda las series como texto.
        ((MainActivity) requireActivity()).mostrarEntrenamientoActivo(
                rutina.getNombre(),
                nombresEjercicios.toArray(new String[0]));
    }*/
}
