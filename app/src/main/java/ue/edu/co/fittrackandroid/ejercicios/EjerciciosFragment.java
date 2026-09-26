package ue.edu.co.fittrackandroid.ejercicios;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Fragment con la lista de ejercicios: buscador, filtros y acceso a la creación de ejercicios.
 */
public class EjerciciosFragment extends Fragment {

    private static final String GRUPO_PECHO = "Pecho";
    private static final String GRUPO_ESPALDA = "Espalda";
    private static final String GRUPO_PIERNA = "Pierna";

    private EditText etBuscarEjercicio;
    private RecyclerView rvEjercicios;
    private EjercicioAdapter adapter;

    private TextView tvChipTodos;
    private TextView tvChipPecho;
    private TextView tvChipEspalda;
    private TextView tvChipPierna;

    // Estado actual de los filtros: el buscador y el chip seleccionado se combinan.
    private String textoBusqueda = "";
    private String grupoMuscularSeleccionado = null;

    public EjerciciosFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ejercicios, container, false);

        etBuscarEjercicio = view.findViewById(R.id.etBuscarEjercicio);
        rvEjercicios = view.findViewById(R.id.rvEjercicios);

        tvChipTodos = view.findViewById(R.id.tvChipTodos);
        tvChipPecho = view.findViewById(R.id.tvChipPecho);
        tvChipEspalda = view.findViewById(R.id.tvChipEspalda);
        tvChipPierna = view.findViewById(R.id.tvChipPierna);

        adapter = new EjercicioAdapter(crearListaEjercicios());
        rvEjercicios.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjercicios.setAdapter(adapter);

        etBuscarEjercicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textoBusqueda = s.toString();
                aplicarFiltros();
            }
        });

        tvChipTodos.setOnClickListener(v -> seleccionarChip(tvChipTodos, null));
        tvChipPecho.setOnClickListener(v -> seleccionarChip(tvChipPecho, GRUPO_PECHO));
        tvChipEspalda.setOnClickListener(v -> seleccionarChip(tvChipEspalda, GRUPO_ESPALDA));
        tvChipPierna.setOnClickListener(v -> seleccionarChip(tvChipPierna, GRUPO_PIERNA));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloEjercicios),
                true,
                getString(R.string.btnCrearEjercicio)
        );
        activity.setAccionToolbar(this::abrirCrearEjercicio);
    }

    private List<Ejercicio> crearListaEjercicios() {
        List<Ejercicio> lista = new ArrayList<>();
        // TODO: Reemplazar esta lista de ejemplo por los ejercicios guardados (base de datos / backend)
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista1),
                getString(R.string.tvSubtituloEjercicioLista1),
                GRUPO_PECHO));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista2),
                getString(R.string.tvSubtituloEjercicioLista2),
                GRUPO_PECHO));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista3),
                getString(R.string.tvSubtituloEjercicioLista3),
                GRUPO_ESPALDA));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista4),
                getString(R.string.tvSubtituloEjercicioLista4),
                GRUPO_ESPALDA));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista5),
                getString(R.string.tvSubtituloEjercicioLista5),
                GRUPO_PIERNA));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista6),
                getString(R.string.tvSubtituloEjercicioLista6),
                GRUPO_PIERNA));
        return lista;
    }

    private void abrirCrearEjercicio() {
        ((MainActivity) requireActivity()).mostrarCrearEjercicio();
    }

    private void seleccionarChip(TextView seleccionado, String grupoMuscular) {
        List<TextView> listaChips = Arrays.asList(tvChipTodos, tvChipPecho, tvChipEspalda, tvChipPierna);

        for (TextView chip : listaChips) {
            boolean estaSeleccionado = chip == seleccionado;
            chip.setBackgroundResource(estaSeleccionado
                    ? R.drawable.bg_filter_chip_selected
                    : R.drawable.bg_filter_chip);
            chip.setTextColor(requireContext().getColor(estaSeleccionado
                    ? R.color.colorOnPrimary
                    : R.color.colorTextStrong));
        }

        grupoMuscularSeleccionado = grupoMuscular;
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        adapter.filtrar(textoBusqueda, grupoMuscularSeleccionado);
    }
}
