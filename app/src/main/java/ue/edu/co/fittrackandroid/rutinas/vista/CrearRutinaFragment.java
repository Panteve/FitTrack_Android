package ue.edu.co.fittrackandroid.rutinas.vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ue.edu.co.fittrackandroid.ejercicios.vista.EjerciciosFragment;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.rutinas.modelo.EjercicioRutinaEditable;
import ue.edu.co.fittrackandroid.rutinas.modelo.SerieRutina;

/**
 * Fragment para crear una rutina: nombre, ejercicios elegidos y series de cada ejercicio.
 * Los datos se conservan en memoria mientras el usuario navega dentro de esta pantalla.
 */
public class CrearRutinaFragment extends Fragment
        implements CrearRutinaEjercicioAdapter.OnAgregarSerieListener {

    private EditText etNombreRutinaNueva;
    private TextView tvErrorNombreRutinaNueva;
    private Button btnAgregarPrimerEjercicio;
    private Button btnAgregarEjercicio;
    private View layoutRutinaSinEjercicios;
    private View layoutRutinaConEjercicios;
    private RecyclerView rvEjerciciosRutina;
    private CrearRutinaEjercicioAdapter adapter;

    // Estado actual de la rutina que se está creando.
    private final List<EjercicioRutinaEditable> listaEjercicios = new ArrayList<>();
    private String nombreRutina = "";

    public CrearRutinaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrarResultadoEjercicio();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crear_rutina, container, false);

        inicializarVistas(view);
        configurarRecyclerView();
        configurarAcciones();
        restaurarNombreEnVista();
        actualizarEstadoPantalla();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // La toolbar se reconfigura aquí porque al volver del selector de ejercicios
        // sigue mostrando la pantalla anterior.
        MainActivity activity = (MainActivity) requireActivity();
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloCrearRutina),
                true,
                getString(R.string.btnGuardarRutinaNueva)
        );
        activity.setAccionToolbar(this::guardarRutina);
    }

    private void inicializarVistas(View view) {
        etNombreRutinaNueva = view.findViewById(R.id.etNombreRutinaNueva);
        tvErrorNombreRutinaNueva = view.findViewById(R.id.tvErrorNombreRutinaNueva);
        btnAgregarPrimerEjercicio = view.findViewById(R.id.btnAgregarPrimerEjercicio);
        btnAgregarEjercicio = view.findViewById(R.id.btnAgregarEjercicio);
        layoutRutinaSinEjercicios = view.findViewById(R.id.layoutRutinaSinEjercicios);
        layoutRutinaConEjercicios = view.findViewById(R.id.layoutRutinaConEjercicios);
        rvEjerciciosRutina = view.findViewById(R.id.rvEjerciciosRutina);
    }

    private void configurarRecyclerView() {
        adapter = new CrearRutinaEjercicioAdapter(listaEjercicios, this);
        rvEjerciciosRutina.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosRutina.setAdapter(adapter);
    }

    private void configurarAcciones() {
        btnAgregarPrimerEjercicio.setOnClickListener(v -> abrirSelectorEjercicios());
        btnAgregarEjercicio.setOnClickListener(v -> abrirSelectorEjercicios());
        limpiarErrorNombreAlEscribir();
    }

    /** Escucha el ejercicio que envía EjerciciosFragment y lo agrega a la rutina. */
    private void registrarResultadoEjercicio() {
        getParentFragmentManager().setFragmentResultListener(
                EjerciciosFragment.REQUEST_SELECCION_EJERCICIO,
                this,
                (clave, resultado) -> {
                    Long idEjercicio = resultado.getLong(
                            EjerciciosFragment.RESULT_ID_EJERCICIO);
                    String nombreEjercicio = resultado.getString(
                            EjerciciosFragment.RESULT_NOMBRE_EJERCICIO);
                    String grupoMuscular = resultado.getString(
                            EjerciciosFragment.RESULT_GRUPO_MUSCULAR);

                    agregarEjercicioSeleccionado(idEjercicio, nombreEjercicio, grupoMuscular);
                });
    }

    private void abrirSelectorEjercicios() {
        ((MainActivity) requireActivity()).mostrarSelectorEjercicios();
    }

    /**
     * Agrega el ejercicio elegido en el selector. Cada ejercicio entra con su identificador
     * y con su primera serie. El mismo ejercicio se puede agregar varias veces, por ejemplo
     * con series diferentes.
     */
    private void agregarEjercicioSeleccionado(Long idEjercicio, String nombreEjercicio,
                                              String grupoMuscular) {
        // TODO: Mostrar un error si el ejercicio fue eliminado antes de agregarlo a la rutina.
        if (nombreEjercicio == null || adapter == null) {
            return;
        }

        adapter.agregarEjercicio(new EjercicioRutinaEditable(idEjercicio, nombreEjercicio, grupoMuscular));
        actualizarEstadoPantalla();
    }

    @Override
    public void onAgregarSerie(int posicionEjercicio) {
        listaEjercicios.get(posicionEjercicio).agregarSerie();
        adapter.actualizarEjercicio(posicionEjercicio);
    }

    /** Alterna entre el estado vacío y el estado con ejercicios. */
    private void actualizarEstadoPantalla() {
        boolean hayEjercicios = !listaEjercicios.isEmpty();
        layoutRutinaSinEjercicios.setVisibility(hayEjercicios ? View.GONE : View.VISIBLE);
        layoutRutinaConEjercicios.setVisibility(hayEjercicios ? View.VISIBLE : View.GONE);
    }

    private void restaurarNombreEnVista() {
        if (etNombreRutinaNueva.getText().length() == 0 && !nombreRutina.isEmpty()) {
            etNombreRutinaNueva.setText(nombreRutina);
        }
    }

    private void limpiarErrorNombreAlEscribir() {
        etNombreRutinaNueva.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int cantidad, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int cantidad) {
            }

            @Override
            public void afterTextChanged(Editable texto) {
                nombreRutina = texto.toString();
                tvErrorNombreRutinaNueva.setVisibility(View.GONE);
                etNombreRutinaNueva.setBackgroundResource(R.drawable.bg_input);
            }
        });
    }

    private void guardarRutina() {
        if (!validarRutina()) {
            return;
        }

        // TODO: Comprobar en la base de datos si el usuario ya tiene una rutina con el mismo
        // nombre y mostrar un error claro si no se permiten nombres duplicados.

        // TODO: Convertir los datos editables al modelo persistente y guardar la rutina,
        // asociada al usuario autenticado, con los identificadores de sus ejercicios y todas
        // sus series en la base de datos o backend.

        // TODO: Deshabilitar temporalmente la acción de guardar para evitar duplicados y
        // manejar por separado el resultado exitoso y los errores de almacenamiento.

        // TODO: Mostrar el mensaje y regresar solamente después de confirmar que el guardado
        // terminó correctamente.
        Toast.makeText(requireContext(), "Rutina guardada", Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).regresar();
    }

    /**
     * Revisa el nombre, los ejercicios y las series antes de guardar.
     * El nombre y la existencia de al menos un ejercicio son obligatorios; el peso y las
     * repeticiones de cada serie son opcionales, solo se rechaza lo que esté mal escrito.
     *
     * @return true si la rutina se puede guardar.
     */
    private boolean validarRutina() {
        if (nombreRutina.trim().isEmpty()) {
            mostrarErrorNombre();
            return false;
        }

        if (listaEjercicios.isEmpty()) {
            Toast.makeText(requireContext(), "Agrega al menos un ejercicio a la rutina",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        for (EjercicioRutinaEditable ejercicio : listaEjercicios) {
            List<SerieRutina> series = ejercicio.getSeries();

            if (series.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Agrega al menos una serie en " + ejercicio.getNombre(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            for (int posicion = 0; posicion < series.size(); posicion++) {
                SerieRutina serie = series.get(posicion);
                String numeroSerie = String.valueOf(posicion + 1);

                // El peso y las repeticiones son opcionales: solo se revisan si el usuario
                // escribió algo, así una serie puede guardarse con los campos vacíos.
                if (!esPesoValido(serie.getPesoObjetivo())) {
                    Toast.makeText(requireContext(),
                            "Revisa el peso de la serie " + numeroSerie + " de " + ejercicio.getNombre(),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!sonRepeticionesValidas(serie.getRepeticiones())) {
                    Toast.makeText(requireContext(),
                            "Revisa las repeticiones de la serie " + numeroSerie + " de " + ejercicio.getNombre(),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    private void mostrarErrorNombre() {
        tvErrorNombreRutinaNueva.setVisibility(View.VISIBLE);
        etNombreRutinaNueva.setBackgroundResource(R.drawable.bg_input_error);
        Toast.makeText(requireContext(), "Ingresa el nombre de la rutina", Toast.LENGTH_SHORT).show();
    }

    /**
     * Revisa el peso de una serie. El campo puede quedar vacío, pero si el usuario escribió
     * algo tiene que ser un número igual o mayor que cero (el cero sirve para el peso corporal).
     */
    private boolean esPesoValido(String pesoObjetivo) {
        String peso = pesoObjetivo.trim();
        if (peso.isEmpty()) {
            return true;
        }

        try {
            // En algunos teclados el separador decimal es la coma.
            return Double.parseDouble(peso.replace(',', '.')) >= 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    /**
     * Revisa las repeticiones de una serie. El campo puede quedar vacío, pero si el usuario
     * escribió algo tiene que ser un número entero mayor que cero.
     */
    private boolean sonRepeticionesValidas(String repeticiones) {
        String numeroRepeticiones = repeticiones.trim();
        if (numeroRepeticiones.isEmpty()) {
            return true;
        }

        try {
            return Integer.parseInt(numeroRepeticiones) > 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }
}
