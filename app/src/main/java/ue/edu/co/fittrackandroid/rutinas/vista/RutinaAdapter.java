package ue.edu.co.fittrackandroid.rutinas.vista;

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
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinasResponse;

/**
 * Adapter para la lista de planes de entrenamiento.
 * Cada tarjeta muestra sus ejercicios con un RecyclerView anidado.
 * El botón de iniciar no navega: avisa a la pantalla para que sea ella la que abra
 * el entrenamiento en curso.
 */
public class RutinaAdapter extends RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder> {

    /**
     * Callback que avisa qué plan se quiere iniciar.
     */
    public interface OnIniciarRutinaListener {
        void onIniciarRutina(RutinasResponse rutina);
    }

    private final List<RutinasResponse> rutinas;
    private final OnIniciarRutinaListener listenerIniciarRutina;

    public RutinaAdapter(List<RutinasResponse> rutinas, OnIniciarRutinaListener listenerIniciarRutina) {
        this.rutinas = rutinas;
        this.listenerIniciarRutina = listenerIniciarRutina;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rutina, parent, false);
        return new RutinaViewHolder(view, listenerIniciarRutina);
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
        private final ImageView imgMenu;
        private final RecyclerView rvEjercicios;
        private final Button btnVerDetalles;
        private final Button btnIniciar;
        private final OnIniciarRutinaListener listenerIniciarRutina;

        RutinaViewHolder(@NonNull View itemView, OnIniciarRutinaListener listenerIniciarRutina) {
            super(itemView);
            this.listenerIniciarRutina = listenerIniciarRutina;

            tvNombre = itemView.findViewById(R.id.tvNombrePlan);
            tvResumen = itemView.findViewById(R.id.tvResumenPlan);
            imgMenu = itemView.findViewById(R.id.imgMenuPlan);
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

            // TODO: Reemplazar este mensaje por un menú con las acciones disponibles para
            // la rutina, como editar y eliminar. La eliminación debe pedir confirmación,
            // actualizar el almacenamiento y notificar el cambio a RutinasFragment.
            imgMenu.setOnClickListener(v -> Toast.makeText(v.getContext(),
                    "Opciones de: " + rutina.getNombre(), Toast.LENGTH_SHORT).show());

            // TODO: Crear RutinaDetalleFragment y solicitar la navegación mediante un
            // callback, entregando el identificador de la rutina seleccionada.
            btnVerDetalles.setOnClickListener(v -> Toast.makeText(v.getContext(),
                    "Ver detalles: " + rutina.getNombre(), Toast.LENGTH_SHORT).show());
            btnIniciar.setOnClickListener(v -> listenerIniciarRutina.onIniciarRutina(rutina));
        }
    }
}
