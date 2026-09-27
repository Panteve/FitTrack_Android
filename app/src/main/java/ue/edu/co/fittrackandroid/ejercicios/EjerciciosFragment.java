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
 * Fragment selector de ejercicios: buscador, filtros por grupo muscular y lista.
 * Al tocar un ejercicio lo devuelve a la pantalla que lo abrió y regresa.
 */
public class EjerciciosFragment extends Fragment {

    /**
     * Claves usadas para devolver el ejercicio elegido a la pantalla que abrió el selector.
     * Quien las escucha puede ser CrearRutinaFragment o EntrenamientoActivoFragment.
     */
    public static final String REQUEST_SELECCION_EJERCICIO = "seleccionEjercicio";
    public static final String RESULT_NOMBRE_EJERCICIO = "nombreEjercicio";
    public static final String RESULT_GRUPO_MUSCULAR = "grupoMuscular";

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

        adapter = new EjercicioAdapter(crearListaEjercicios(), this::seleccionarEjercicio);
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

        restaurarFiltrosEnVista();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Solo es un selector: la flecha de la toolbar regresa a la pantalla que lo abrió.
        // La derecha queda libre, así que ahí va CREAR para registrar un ejercicio nuevo.
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloSeleccionarEjercicio),
                true,
                null
        );
        activity.mostrarAccionSecundariaToolbar(getString(R.string.btnCrearEjercicio));
        activity.setAccionSecundariaToolbar(this::abrirCrearEjercicio);
    }

    /**
     * Abre la pantalla de crear ejercicio. Se navega con retroceso, así que al guardar el
     * ejercicio el usuario vuelve aquí, al selector, con el buscador y el filtro como estaban.
     */
    private void abrirCrearEjercicio() {
        ((MainActivity) requireActivity()).mostrarCrearEjercicio();
    }

    /**
     * Vuelve a escribir la búsqueda y a remarcar el chip que estaban activos. Al ir a crear
     * un ejercicio y volver, las vistas se recrean pero el filtro sigue guardado en el
     * fragment, así que hay que volver a reflejarlo o la lista saldría filtrada sin que
     * se vea por qué.
     */
    private void restaurarFiltrosEnVista() {
        if (!textoBusqueda.isEmpty()) {
            etBuscarEjercicio.setText(textoBusqueda);
        }

        if (GRUPO_PECHO.equals(grupoMuscularSeleccionado)) {
            seleccionarChip(tvChipPecho, GRUPO_PECHO);
        } else if (GRUPO_ESPALDA.equals(grupoMuscularSeleccionado)) {
            seleccionarChip(tvChipEspalda, GRUPO_ESPALDA);
        } else if (GRUPO_PIERNA.equals(grupoMuscularSeleccionado)) {
            seleccionarChip(tvChipPierna, GRUPO_PIERNA);
        } else {
            seleccionarChip(tvChipTodos, null);
        }
    }

    private List<Ejercicio> crearListaEjercicios() {
        List<Ejercicio> lista = new ArrayList<>();
        // TODO: Reemplazar esta lista de ejemplo por los ejercicios guardados (base de datos / backend)
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista1),
                GRUPO_PECHO));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista2),
                GRUPO_PECHO));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista3),
                GRUPO_ESPALDA));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista4),
                GRUPO_ESPALDA));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista5),
                GRUPO_PIERNA));
        lista.add(new Ejercicio(
                getString(R.string.tvNombreEjercicioLista6),
                GRUPO_PIERNA));
        return lista;
    }

    /**
     * Devuelve el ejercicio elegido a quien abrió el selector y regresa a esa pantalla.
     * Se envían solo textos porque los modelos no implementan Parcelable ni Serializable.
     */
    private void seleccionarEjercicio(Ejercicio ejercicio) {
        Bundle datosEjercicio = new Bundle();
        datosEjercicio.putString(RESULT_NOMBRE_EJERCICIO, ejercicio.getNombre());
        datosEjercicio.putString(RESULT_GRUPO_MUSCULAR, ejercicio.getGrupoMuscular());

        getParentFragmentManager().setFragmentResult(REQUEST_SELECCION_EJERCICIO, datosEjercicio);
        ((MainActivity) requireActivity()).regresar();
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
