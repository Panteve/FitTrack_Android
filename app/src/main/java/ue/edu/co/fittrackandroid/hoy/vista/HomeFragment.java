package ue.edu.co.fittrackandroid.hoy.vista;

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
import ue.edu.co.fittrackandroid.hoy.datos.HomeRepository;
import ue.edu.co.fittrackandroid.hoy.modelo.HomeResponse;
import ue.edu.co.fittrackandroid.hoy.modelo.ProximaRutina;
import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.hoy.modelo.UltimoEntrenamiento;
import ue.edu.co.fittrackandroid.MainActivity;
import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.resumen.vista.ResumenEntrenamientoFragment;
import ue.edu.co.fittrackandroid.rutinas.datos.RutinaRepository;
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
    private RutinaRepository rutinaRepository;
    private Long idProximaRutina;
    private Call<HomeResponse> currentCallHome;
    private Call<RutinaResponse> currentCallRutina;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El aviso de entrenamiento borrado se registra en onCreate, antes de que exista
        // la vista, porque el resultado se envía justo cuando la pantalla del resumen
        // sale de la pila.
        registrarResultadoEntrenamientoEliminado();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeRepository = new HomeRepository(requireContext());
        rutinaRepository = new RutinaRepository(requireContext());
        sesionManager = new SesionManager(requireContext());
        initObjects(view);

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

        tvFecha.setText(getString(R.string.tvFecha, formatearFechaHoy()));
        tvBienvenida.setText(sesionManager.obtenerNombre());
        rvUltimosEntrenamientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        mostrarEstadoCarga();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).mostrarToolbarPrincipal();
    }

    private void iniciarEntrenamiento() {
        if (idProximaRutina == null) {
            return;
        }

        btnIniciarEntrenamiento.setEnabled(false);
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento_loading);

        currentCallRutina = rutinaRepository.getRutinaById(idProximaRutina);
        currentCallRutina.enqueue(new Callback<RutinaResponse>() {
            @Override
            public void onResponse(@NonNull Call<RutinaResponse> call,
                                   @NonNull Response<RutinaResponse> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    RutinaResponse rutinaResponse = response.body();
                    btnIniciarEntrenamiento.setEnabled(true);
                    btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);
                    ((MainActivity) requireActivity()).mostrarEntrenamientoActivo(rutinaResponse);
                } else {
                    btnIniciarEntrenamiento.setEnabled(true);
                    btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);
                    ManejadorErroresApi.obtenerToast(requireContext(), response.code()).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RutinaResponse> call,
                                  @NonNull Throwable throwable) {
                if (call.isCanceled() || !isAdded()) {
                    return;
                }
                btnIniciarEntrenamiento.setEnabled(true);
                btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);

                ManejadorErroresApi
                        .obtenerToast(requireContext(), throwable)
                        .show();
            }
        });
    }

    /**
     * Escucha el resultado de ResumenEntrenamientoFragment y vuelve a consultar la
     * información del inicio, para que el entrenamiento borrado desaparezca de
     * "Últimos entrenamientos" y el contador se actualice. Si era el último, la
     * pantalla muestra el estado vacío.
     * La pantalla no se recarga en onResume: para eso está este resultado.
     */
    private void registrarResultadoEntrenamientoEliminado() {
        getParentFragmentManager().setFragmentResultListener(
                ResumenEntrenamientoFragment.REQUEST_ENTRENAMIENTO_ELIMINADO,
                this,
                (clave, resultado) -> {
                    if (!isAdded() || homeRepository == null) {
                        return;
                    }
                    cargarInfoHome();
                });
    }

    /** Consulta y presenta la información principal del usuario. */
    private void cargarInfoHome() {
        // Una consulta anterior puede seguir en vuelo, por ejemplo si el usuario borra
        // dos entrenamientos seguidos: se cancela para que no se crucen respuestas.
        if (currentCallHome != null) {
            currentCallHome.cancel();
        }

        currentCallHome = homeRepository.getInfoHome();
        currentCallHome.enqueue(new Callback<HomeResponse>() {

            @Override
            public void onResponse(@NonNull Call<HomeResponse> call,
                                   @NonNull Response<HomeResponse> response) {
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
            public void onFailure(@NonNull Call<HomeResponse> call,
                                  @NonNull Throwable throwable) {
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
     * El adapter entrega el elemento seleccionado y la pantalla del resumen consulta el
     * detalle guardado usando su identificador.
     *
     * @param entrenamiento entrenamiento seleccionado en la lista.
     */
    private void abrirResumenEntrenamiento(UltimoEntrenamiento entrenamiento) {
        ((MainActivity) requireActivity())
                .mostrarResumenEntrenamientoHistorial(entrenamiento.getId());
    }

    private String formatearFechaHoy() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        String fecha = formato.format(new Date());
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }

    @Override
    public void onDestroyView() {
        if (currentCallHome != null) {
            currentCallHome.cancel();
        }
        if (currentCallRutina != null) {
            currentCallRutina.cancel();
        }
        super.onDestroyView();
    }
}
