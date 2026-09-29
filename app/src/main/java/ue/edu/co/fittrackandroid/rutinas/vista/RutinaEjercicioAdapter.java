package ue.edu.co.fittrackandroid.rutinas.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ue.edu.co.fittrackandroid.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter para la lista de ejercicios que va dentro de la tarjeta de un plan.
 */
public class RutinaEjercicioAdapter extends RecyclerView.Adapter<RutinaEjercicioAdapter.EjercicioRutinaViewHolder> {

    private final List<String> ejercicios;

    public RutinaEjercicioAdapter(List<String> ejercicios) {
        this.ejercicios = ejercicios;
    }

    @NonNull
    @Override
    public EjercicioRutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina_ejercicio, parent, false);
        return new EjercicioRutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioRutinaViewHolder holder, int position) {
        holder.asignar(ejercicios.get(position));
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class EjercicioRutinaViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvSeries;

        EjercicioRutinaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEjercicioRutina);
            tvSeries = itemView.findViewById(R.id.tvSeriesEjercicioRutina);
        }

        void asignar(String ejercicio) {
            tvNombre.setText(ejercicio);
            tvSeries.setText("");
        }
    }
}
