package ue.edu.co.fittrackandroid.entrenamiento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.hoy.MainActivity;

/**
 * Pantalla de solo lectura con el resultado de un entrenamiento terminado.
 *
 * <p>Muestra el nombre del entrenamiento, el día y la hora en que se realizó, las tres
 * métricas finales (duración, volumen y series), la distribución porcentual de los
 * grupos musculares trabajados y los ejercicios con sus series realizadas.
 *
 * <p>La pantalla se abre al confirmar "Terminar" y también al pulsar un registro de
 * "Últimos entrenamientos" en Inicio. En los dos casos MainActivity guarda el
 * {@link ResumenEntrenamiento} que se está mostrando.
 *
 * <p>No hay ningún temporizador: todos los datos ya son finales.
 */
public class ResumenEntrenamientoFragment extends Fragment {

    private MainActivity activity;
    private ResumenEntrenamiento resumen;

    private TextView tvNombreResumenEntrenamiento;
    private TextView tvFechaResumenEntrenamiento;
    private TextView tvDuracionResumenEntrenamiento;
    private TextView tvVolumenResumenEntrenamiento;
    private TextView tvSeriesResumenEntrenamiento;
    private TextView tvResumenSinEjercicios;
    private LinearLayout layoutGruposMuscularesResumen;
    private RecyclerView rvEjerciciosResumen;

    public ResumenEntrenamientoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) requireActivity();
        // El resumen todavía no se guarda en disco: solo existe mientras la app siga viva.
        // TODO: Leer el resumen del historial guardado cuando exista la persistencia.
        resumen = activity.obtenerResumenEntrenamientoActual();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen_entrenamiento, container, false);

        // Si no hay resumen no hay nada que pintar: onResume devuelve a la pantalla anterior.
        if (resumen != null) {
            inicializarVistas(view);
            mostrarCabecera();
            mostrarMetricas();
            mostrarGruposMusculares();
            mostrarEjercicios();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (resumen == null) {
            activity.regresar();
            return;
        }

        configurarToolbar();
    }

    private void inicializarVistas(View view) {
        tvNombreResumenEntrenamiento = view.findViewById(R.id.tvNombreResumenEntrenamiento);
        tvFechaResumenEntrenamiento = view.findViewById(R.id.tvFechaResumenEntrenamiento);
        tvDuracionResumenEntrenamiento = view.findViewById(R.id.tvDuracionResumenEntrenamiento);
        tvVolumenResumenEntrenamiento = view.findViewById(R.id.tvVolumenResumenEntrenamiento);
        tvSeriesResumenEntrenamiento = view.findViewById(R.id.tvSeriesResumenEntrenamiento);
        tvResumenSinEjercicios = view.findViewById(R.id.tvResumenSinEjercicios);
        layoutGruposMuscularesResumen = view.findViewById(R.id.layoutGruposMuscularesResumen);
        rvEjerciciosResumen = view.findViewById(R.id.rvEjerciciosResumen);
    }

    /**
     * Toolbar de la pantalla: la flecha vuelve a la pantalla anterior y no hay ninguna
     * acción a la derecha. Como el entrenamiento ya terminó, se muestran la navegación
     * inferior y se oculta la isla.
     */
    private void configurarToolbar() {
        activity.mostrarToolbarSecundaria(
                getString(R.string.tvToolbarTituloResumenEntrenamiento),
                true,
                null
        );
        activity.mostrarNavegacionInferior();
        activity.ocultarIslaEntrenamiento();
    }

    // ------------------------------------------------------------------ Cabecera

    /** Muestra el nombre del entrenamiento y el día y la hora reales en que se hizo. */
    private void mostrarCabecera() {
        String nombre = resumen.getNombre();

        // Si la sesión se inició sin nombre de rutina se usa un nombre de respaldo.
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = getString(R.string.tvNombreResumenEntrenamiento_fallback);
        }

        tvNombreResumenEntrenamiento.setText(nombre);
        tvFechaResumenEntrenamiento.setText(getString(R.string.tvFechaResumenEntrenamiento,
                formatearFechaResumen(), formatearHoraResumen()));
    }

    /** @return el día del entrenamiento, por ejemplo "Sábado, 26 de septiembre". */
    private String formatearFechaResumen() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM", new Locale("es", "CO"));
        String fecha = formato.format(new Date(resumen.getFechaHoraInicio()));
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }

    /** @return la hora del entrenamiento, por ejemplo "6:30 p. m.". */
    private String formatearHoraResumen() {
        SimpleDateFormat formato = new SimpleDateFormat("h:mm a", new Locale("es", "CO"));
        return formato.format(new Date(resumen.getFechaHoraInicio()));
    }

    // ------------------------------------------------------------------ Métricas

    /** Muestra la duración, el volumen y las series completadas sobre las totales. */
    private void mostrarMetricas() {
        tvDuracionResumenEntrenamiento.setText(
                EntrenamientoEnCurso.formatearTiempo(resumen.getDuracionSegundos()));

        tvVolumenResumenEntrenamiento.setText(getString(R.string.tvVolumenResumenEntrenamiento,
                formatearVolumen(resumen.calcularVolumenTotal())));

        tvSeriesResumenEntrenamiento.setText(getString(R.string.tvSeriesResumenEntrenamiento,
                resumen.contarSeriesCompletadas(), resumen.getSeriesTotales()));
    }

    /** Muestra el volumen sin decimales cuando no hacen falta: 600 kg, 22.5 kg. */
    private String formatearVolumen(double volumenTotal) {
        DecimalFormat formato = new DecimalFormat("#0.##",
                DecimalFormatSymbols.getInstance(Locale.getDefault()));
        return formato.format(volumenTotal);
    }

    // ------------------------------------------------------------------ Grupos musculares

    /**
     * Muestra una fila por cada grupo muscular trabajado, de mayor a menor porcentaje.
     * Las filas se inflan aquí porque siempre son pocas.
     */
    private void mostrarGruposMusculares() {
        layoutGruposMuscularesResumen.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        List<GrupoMuscularResumen> grupos = resumen.calcularGruposMusculares();

        for (GrupoMuscularResumen grupo : grupos) {
            View filaGrupo = inflater.inflate(R.layout.item_grupo_muscular_resumen,
                    layoutGruposMuscularesResumen, false);

            TextView tvNombreGrupo = filaGrupo.findViewById(R.id.tvNombreGrupoMuscularResumen);
            TextView tvPorcentajeGrupo = filaGrupo.findViewById(R.id.tvPorcentajeGrupoMuscularResumen);
            ProgressBar pbGrupo = filaGrupo.findViewById(R.id.pbGrupoMuscularResumen);

            tvNombreGrupo.setText(grupo.getNombre());
            tvPorcentajeGrupo.setText(getString(R.string.tvPorcentajeGrupoMuscularResumen,
                    grupo.getPorcentaje()));
            pbGrupo.setProgress(grupo.getPorcentaje());

            layoutGruposMuscularesResumen.addView(filaGrupo);
        }
    }

    // ------------------------------------------------------------------ Ejercicios

    /** Muestra los ejercicios realizados y ajusta el mensaje cuando no hay ninguno. */
    private void mostrarEjercicios() {
        List<EjercicioResumen> ejercicios = resumen.getEjercicios();
        boolean hayEjercicios = !ejercicios.isEmpty();

        rvEjerciciosResumen.setVisibility(hayEjercicios ? View.VISIBLE : View.GONE);
        tvResumenSinEjercicios.setVisibility(hayEjercicios ? View.GONE : View.VISIBLE);

        if (!hayEjercicios) {
            return;
        }

        rvEjerciciosResumen.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvEjerciciosResumen.setAdapter(new ResumenEjercicioAdapter(ejercicios));
    }
}
