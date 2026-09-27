package ue.edu.co.fittrackandroid.rutinas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Fragment para la pantalla de rutinas (lista de planes de entrenamiento).
 */
public class RutinasFragment extends Fragment {

    private Button btnNuevaRutina;
    private Button btnEmpezarRutinaVacia;
    private TextView tvCantidadPlanes;
    private List<Rutina> planes;

    public RutinasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rutinas, container, false);

        btnNuevaRutina = view.findViewById(R.id.btnNuevaRutina);
        btnEmpezarRutinaVacia = view.findViewById(R.id.btnEmpezarRutinaVacia);
        tvCantidadPlanes = view.findViewById(R.id.tvCantidadPlanes);
        RecyclerView rvPlanes = view.findViewById(R.id.rvPlanes);

        planes = crearListaPlanes();

        rvPlanes.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPlanes.setAdapter(new RutinaAdapter(planes, this::iniciarEntrenamientoConRutina));

        tvCantidadPlanes.setText(getString(R.string.tvCantidadPlanes, planes.size()));

        btnNuevaRutina.setOnClickListener(v ->
                ((MainActivity) requireActivity()).mostrarCrearRutina());

        btnEmpezarRutinaVacia.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Empezar rutina vacía próximamente", Toast.LENGTH_SHORT).show());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Al volver desde Ejercicios o Crear ejercicio, la toolbar debe quedar como la principal.
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();
    }

    /**
     * Abre el entrenamiento en curso con los ejercicios del plan elegido.
     * Solo se envían nombres porque el modelo de rutinas guarda las series como un texto
     * de presentación, no como datos que se puedan recuperar.
     */
    private void iniciarEntrenamientoConRutina(Rutina rutina) {
        List<String> nombresEjercicios = new ArrayList<>();
        for (EjercicioRutina ejercicio : rutina.getEjercicios()) {
            nombresEjercicios.add(ejercicio.getNombre());
        }

        // TODO: Cargar las cantidades y objetivos de cada serie cuando el modelo de rutinas
        //       deje de guardarlos como texto de presentación.
        ((MainActivity) requireActivity()).mostrarEntrenamientoActivo(
                rutina.getNombre(),
                nombresEjercicios.toArray(new String[0]));
    }

    /**
     * Arma los planes de ejemplo. TODO: reemplazar por los planes guardados
     * (base de datos o backend) cuando exista esa capa.
     */
    private List<Rutina> crearListaPlanes() {
        List<Rutina> listaPlanes = new ArrayList<>();
        listaPlanes.add(new Rutina(
                getString(R.string.tvNombreRutina1),
                getString(R.string.tvResumenRutina1),
                crearEjerciciosPlan1()));
        listaPlanes.add(new Rutina(
                getString(R.string.tvNombreRutina2),
                getString(R.string.tvResumenRutina2),
                crearEjerciciosPlan2()));
        listaPlanes.add(new Rutina(
                getString(R.string.tvNombreRutina3),
                getString(R.string.tvResumenRutina3),
                crearEjerciciosPlan3()));
        return listaPlanes;
    }

    private List<EjercicioRutina> crearEjerciciosPlan1() {
        List<EjercicioRutina> lista = new ArrayList<>();
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio1_1), getString(R.string.tvSeriesEjercicio1_1)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio1_2), getString(R.string.tvSeriesEjercicio1_2)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio1_3), getString(R.string.tvSeriesEjercicio1_3)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio1_4), getString(R.string.tvSeriesEjercicio1_4)));
        return lista;
    }

    private List<EjercicioRutina> crearEjerciciosPlan2() {
        List<EjercicioRutina> lista = new ArrayList<>();
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio2_1), getString(R.string.tvSeriesEjercicio2_1)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio2_2), getString(R.string.tvSeriesEjercicio2_2)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio2_3), getString(R.string.tvSeriesEjercicio2_3)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio2_4), getString(R.string.tvSeriesEjercicio2_4)));
        return lista;
    }

    private List<EjercicioRutina> crearEjerciciosPlan3() {
        List<EjercicioRutina> lista = new ArrayList<>();
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio3_1), getString(R.string.tvSeriesEjercicio3_1)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio3_2), getString(R.string.tvSeriesEjercicio3_2)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio3_3), getString(R.string.tvSeriesEjercicio3_3)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio3_4), getString(R.string.tvSeriesEjercicio3_4)));
        lista.add(new EjercicioRutina(getString(R.string.tvNombreEjercicio3_5), getString(R.string.tvSeriesEjercicio3_5)));
        return lista;
    }
}
