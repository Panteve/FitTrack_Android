package ue.edu.co.fittrackandroid.rutinas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ue.edu.co.fittrackandroid.R;

/**
 * Adapter para la lista de planes de entrenamiento.
 * Cada tarjeta muestra sus ejercicios con un RecyclerView anidado.
 */
public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    private final List<Rutina> planes;

    public RutinaAdapter(List<Rutina> planes) {
        this.planes = planes;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutinaViewHolder holder, int position) {
        holder.asignar(planes.get(position));
    }

    @Override
    public int getItemCount() {
        return planes.size();
    }

    static class RutinaViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvResumen;
        private final ImageView imgMenu;
        private final RecyclerView rvEjercicios;
        private final Button btnVerDetalles;
        private final Button btnIniciar;

        RutinaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombrePlan);
            tvResumen = itemView.findViewById(R.id.tvResumenPlan);
            imgMenu = itemView.findViewById(R.id.imgMenuPlan);
            rvEjercicios = itemView.findViewById(R.id.rvEjerciciosPlan);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnIniciar = itemView.findViewById(R.id.btnIniciarRutina);
        }

        void asignar(Rutina rutina) {
            tvNombre.setText(rutina.getNombre());
            tvResumen.setText(rutina.getResumen());

            rvEjercicios.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvEjercicios.setAdapter(new RutinaEjercicioAdapter(rutina.getEjercicios()));

            imgMenu.setOnClickListener(v -> Toast.makeText(v.getContext(),
                    "Opciones de: " + rutina.getNombre(), Toast.LENGTH_SHORT).show());
            btnVerDetalles.setOnClickListener(v -> Toast.makeText(v.getContext(),
                    "Ver detalles: " + rutina.getNombre(), Toast.LENGTH_SHORT).show());
            btnIniciar.setOnClickListener(v -> Toast.makeText(v.getContext(),
                    "Iniciar: " + rutina.getNombre(), Toast.LENGTH_SHORT).show());
        }
    }
}
