package ue.edu.co.fittrackandroid.rutinas.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinasResponse;

/**
 * Adapter para la lista de planes de entrenamiento.
 * Cada tarjeta muestra sus ejercicios con un RecyclerView anidado.
 * Ninguno de los dos botones navega: avisan a la pantalla para que sea ella la que abra
 * el entrenamiento en curso o la pantalla de modificar la rutina.
 */
public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    /**
     * Callback que avisa qué plan se quiere iniciar.
     */
    public interface OnIniciarRutinaListener {
        void onIniciarRutina(RutinasResponse rutina);
    }

    /**
     * Callback que avisa qué plan se quiere consultar y modificar.
     */
    public interface OnVerDetallesRutinaListener {
        void onVerDetallesRutina(RutinasResponse rutina);
    }

    private final List<RutinasResponse> rutinas;
    private final OnIniciarRutinaListener listenerIniciarRutina;
    private final OnVerDetallesRutinaListener listenerVerDetallesRutina;

    public RutinaAdapter(List<RutinasResponse> rutinas,
                         OnIniciarRutinaListener listenerIniciarRutina,
                         OnVerDetallesRutinaListener listenerVerDetallesRutina) {
        this.rutinas = rutinas;
        this.listenerIniciarRutina = listenerIniciarRutina;
        this.listenerVerDetallesRutina = listenerVerDetallesRutina;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(view, listenerIniciarRutina, listenerVerDetallesRutina);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        holder.asignar(rutinas.get(position));
    }

    @Override
    public int getItemCount() {
        return rutinas.size();
    }

    static class RutinaViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvResumen;
        private final RecyclerView rvEjercicios;
        private final Button btnVerDetalles;
        private final Button btnIniciar;
        private final OnIniciarRutinaListener listenerIniciarRutina;
        private final OnVerDetallesRutinaListener listenerVerDetallesRutina;

        RutinaViewHolder(@NonNull View itemView, OnIniciarRutinaListener listenerIniciarRutina,
                         OnVerDetallesRutinaListener listenerVerDetallesRutina) {
            super(itemView);
            this.listenerIniciarRutina = listenerIniciarRutina;
            this.listenerVerDetallesRutina = listenerVerDetallesRutina;

            tvNombre = itemView.findViewById(R.id.tvNombrePlan);
            tvResumen = itemView.findViewById(R.id.tvResumenPlan);
            rvEjercicios = itemView.findViewById(R.id.rvEjerciciosPlan);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnIniciar = itemView.findViewById(R.id.btnIniciarRutina);
        }

        void asignar(RutinasResponse rutina) {
            tvNombre.setText(rutina.getNombre());
            tvResumen.setText(itemView.getContext().getString(
                    R.string.tvResumenPlan_dinamico,
                    rutina.getCantidadEjercicios()));

            rvEjercicios.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            if (rutina.getEjercicios() != null) {
                rvEjercicios.setAdapter(new RutinaEjercicioAdapter(rutina.getEjercicios()));
            } else {
                rvEjercicios.setAdapter(null);
            }

            // Ver detalles abre la pantalla de modificar la rutina; no se navega desde aquí,
            // solo se avisa a RutinasFragment con la rutina seleccionada.
            btnVerDetalles.setOnClickListener(
                    v -> listenerVerDetallesRutina.onVerDetallesRutina(rutina));
            btnIniciar.setOnClickListener(v -> listenerIniciarRutina.onIniciarRutina(rutina));
        }
    }
}
