package ue.edu.co.fittrackandroid.hoy;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.resumen.EjercicioResumen;
import ue.edu.co.fittrackandroid.resumen.ResumenEntrenamiento;
import ue.edu.co.fittrackandroid.resumen.SerieResumen;

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

        // TODO: Mostrar en tvBienvenida el nombre del usuario de la sesión activa.

        // TODO: Cargar el registro de la semana actual y actualizar cada día del calendario
        // según los entrenamientos realmente completados. Actualmente los números, el día
        // actual y los estados marcados están definidos de forma fija en el layout.

        // TODO: Consultar la rutina asignada al usuario y actualizar su nombre, duración y
        // cantidad de ejercicios. Si no hay una rutina asignada, mostrar un estado vacío
        // y no permitir iniciar un entrenamiento con datos de ejemplo.

        btnIniciarEntrenamiento.setOnClickListener(v -> iniciarEntrenamiento());

        configurarUltimosEntrenamientos();

        return view;
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

    /** Arma la lista con los tres entrenamientos más recientes y ajusta el estado vacío. */
    private void configurarUltimosEntrenamientos() {
        // TODO: Consultar el historial del usuario, ordenarlo del más reciente al más
        // antiguo y mostrar como máximo tres resultados. También se deben contemplar
        // los estados de carga y error, además del estado vacío que ya existe.
        List<UltimoEntrenamiento> entrenamientos = crearEntrenamientosDeEjemplo();

        rvUltimosEntrenamientos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUltimosEntrenamientos.setAdapter(
                new UltimoEntrenamientoAdapter(entrenamientos, this::abrirResumenEntrenamiento));

        boolean hayEntrenamientos = !entrenamientos.isEmpty();
        rvUltimosEntrenamientos.setVisibility(hayEntrenamientos ? View.VISIBLE : View.GONE);
        tvSinEntrenamientos.setVisibility(hayEntrenamientos ? View.GONE : View.VISIBLE);
        tvCantidadUltimosEntrenamientos.setText(
                getString(R.string.tvCantidadUltimosEntrenamientos, entrenamientos.size()));
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

    /**
     * Crea los entrenamientos de demostración, del más reciente al más antiguo.
     * Cada fila lleva su propio resumen completo para que al pulsarla se abra una pantalla
     * de resultado y no un estado vacío.
     * TODO: Reemplazar por los entrenamientos realmente registrados, leídos del historial.
     */
    private List<UltimoEntrenamiento> crearEntrenamientosDeEjemplo() {
        List<UltimoEntrenamiento> listaEntrenamientos = new ArrayList<>();

        int duracionPierna = Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento1));
        listaEntrenamientos.add(crearFilaDeEjemplo(
                getString(R.string.tvNombreUltimoEntrenamiento1),
                duracionPierna,
                crearResumenPiernaYAbdomen(duracionPierna)));

        int duracionPecho = Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento2));
        listaEntrenamientos.add(crearFilaDeEjemplo(
                getString(R.string.tvNombreUltimoEntrenamiento2),
                duracionPecho,
                crearResumenPechoYTriceps(duracionPecho)));

        int duracionEspalda = Integer.parseInt(getString(R.string.duracionUltimoEntrenamiento3));
        listaEntrenamientos.add(crearFilaDeEjemplo(
                getString(R.string.tvNombreUltimoEntrenamiento3),
                duracionEspalda,
                crearResumenEspaldaYBiceps(duracionEspalda)));

        return listaEntrenamientos;
    }

    /**
     * Arma una fila de la lista a partir de su resumen.
     *
     * <p>La fecha de la fila se saca del mismo resumen que se abre al pulsarla, para que
     * el día que se ve en la lista sea el mismo que muestra la pantalla de resumen.
     *
     * @param nombre           nombre de la rutina entrenada.
     * @param duracionMinutos  duración del entrenamiento, en minutos.
     * @param resumen          resumen completo que se abrirá al pulsar la fila.
     * @return la fila lista para la lista de últimos entrenamientos.
     */
    private UltimoEntrenamiento crearFilaDeEjemplo(String nombre, int duracionMinutos,
                                                   ResumenEntrenamiento resumen) {
        return new UltimoEntrenamiento(nombre,
                formatearFechaCorta(resumen.getFechaHoraInicio()),
                duracionMinutos,
                resumen);
    }

    /**
     * Resumen de ejemplo: sentadilla, prensa, cuádriceps y plancha. De las 14 series que
     * existían se completaron 12.
     *
     * @param duracionMinutos duración del entrenamiento, en minutos.
     */
    private ResumenEntrenamiento crearResumenPiernaYAbdomen(int duracionMinutos) {
        List<EjercicioResumen> ejercicios = new ArrayList<>();

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio1),
                getString(R.string.tvGrupoMuscularEjercicio1),
                new double[]{85, 85, 80}, new int[]{9, 8, 10}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio2),
                getString(R.string.tvGrupoMuscularEjercicio2),
                new double[]{70, 72.5, 72.5, 75}, new int[]{10, 9, 8, 8}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio3),
                getString(R.string.tvGrupoMuscularEjercicio3),
                new double[]{40, 40, 42.5}, new int[]{12, 12, 10}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio4),
                getString(R.string.tvGrupoMuscularEjercicio4),
                new double[]{0, 0}, new int[]{45, 45}));

        return new ResumenEntrenamiento(getString(R.string.tvNombreUltimoEntrenamiento1),
                crearFechaDeEjemplo(2, 18, 30), duracionMinutos * 60L, 14, ejercicios);
    }

    /**
     * Resumen de ejemplo: press de banca, press inclinado, fondos y extensión de tríceps.
     * De las 13 series que existían se completaron 12.
     *
     * @param duracionMinutos duración del entrenamiento, en minutos.
     */
    private ResumenEntrenamiento crearResumenPechoYTriceps(int duracionMinutos) {
        List<EjercicioResumen> ejercicios = new ArrayList<>();

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio1_1),
                getString(R.string.tvGrupoMuscularEjemploPecho),
                new double[]{60, 65, 65, 70}, new int[]{10, 9, 8, 8}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio1_2),
                getString(R.string.tvGrupoMuscularEjemploPecho),
                new double[]{22.5, 25, 25}, new int[]{12, 11, 10}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio1_3),
                getString(R.string.tvGrupoMuscularEjemploTriceps),
                new double[]{0, 0, 5}, new int[]{12, 10, 12}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio1_4),
                getString(R.string.tvGrupoMuscularEjemploTriceps),
                new double[]{35, 37.5}, new int[]{15, 12}));

        return new ResumenEntrenamiento(getString(R.string.tvNombreUltimoEntrenamiento2),
                crearFechaDeEjemplo(4, 19, 0), duracionMinutos * 60L, 13, ejercicios);
    }

    /**
     * Resumen de ejemplo: dominadas, remo, jalón al pecho y curl de bíceps.
     * De las 13 series que existían se completaron las 13.
     *
     * @param duracionMinutos duración del entrenamiento, en minutos.
     */
    private ResumenEntrenamiento crearResumenEspaldaYBiceps(int duracionMinutos) {
        List<EjercicioResumen> ejercicios = new ArrayList<>();

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio2_1),
                getString(R.string.tvGrupoMuscularEjemploEspalda),
                new double[]{0, 0, 0, 5}, new int[]{8, 7, 6, 6}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio2_2),
                getString(R.string.tvGrupoMuscularEjemploEspalda),
                new double[]{65, 67.5, 67.5, 70}, new int[]{8, 8, 7, 7}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio2_3),
                getString(R.string.tvGrupoMuscularEjemploEspalda),
                new double[]{55, 57.5}, new int[]{12, 11}));

        ejercicios.add(crearEjercicioDeEjemplo(getString(R.string.tvNombreEjercicio2_4),
                getString(R.string.tvGrupoMuscularEjemploBiceps),
                new double[]{30, 30, 32.5}, new int[]{12, 11, 10}));

        return new ResumenEntrenamiento(getString(R.string.tvNombreUltimoEntrenamiento3),
                crearFechaDeEjemplo(6, 17, 45), duracionMinutos * 60L, 13, ejercicios);
    }

    /**
     * Crea un ejercicio de demostración con sus series completadas.
     *
     * @param nombre        nombre del ejercicio.
     * @param grupoMuscular grupo muscular del ejercicio.
     * @param pesos         peso de cada serie realizada.
     * @param repeticiones  repeticiones de cada serie realizada.
     * @return el ejercicio del resumen.
     */
    private EjercicioResumen crearEjercicioDeEjemplo(String nombre, String grupoMuscular,
                                                    double[] pesos, int[] repeticiones) {
        List<SerieResumen> series = new ArrayList<>();

        for (int posicion = 0; posicion < pesos.length; posicion++) {
            series.add(new SerieResumen(pesos[posicion], repeticiones[posicion]));
        }

        return new EjercicioResumen(nombre, grupoMuscular, series);
    }

    /**
     * Crea el momento real de un entrenamiento de demostración.
     *
     * @param diasAtras cuántos días antes de hoy se entrenó.
     * @param hora       hora del día en que empezó.
     * @param minuto     minuto en que empezó.
     * @return la fecha en milisegundos, como la que guarda el entrenamiento en curso.
     */
    private long crearFechaDeEjemplo(int diasAtras, int hora, int minuto) {
        Calendar calendario = Calendar.getInstance();
        calendario.add(Calendar.DAY_OF_YEAR, -diasAtras);
        calendario.set(Calendar.HOUR_OF_DAY, hora);
        calendario.set(Calendar.MINUTE, minuto);
        calendario.set(Calendar.SECOND, 0);
        return calendario.getTimeInMillis();
    }

    private String formatearFechaHoy() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        String fecha = formato.format(new Date());
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }

    /** @return el día corto de un entrenamiento, por ejemplo "24 de mayo". */
    private String formatearFechaCorta(long fechaEnMilisegundos) {
        SimpleDateFormat formato = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "CO"));
        return formato.format(new Date(fechaEnMilisegundos));
    }
}
