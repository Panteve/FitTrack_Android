package ue.edu.co.fittrackandroid.hoy.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ue.edu.co.fittrackandroid.hoy.modelo.UltimoEntrenamiento;
import ue.edu.co.fittrackandroid.R;

/**
 * Adapter de la lista de entrainamientos recientes de la pantalla de inicio.
 * Solo muestra nombre y detalle de cada fila, sin navegación ni lógica de negocio:
 * cuando el usuario pulsa una fila, entrega el entrenamiento mediante un callback.
 */
public class UltimoEntrenamientoAdapter extends RecyclerView.Adapter<UltimoEntrenamientoAdapter.EntrenamientoViewHolder> {

    /**
     * Callback que recibe el entrenamiento que el usuario seleccionó.
     * La pantalla que lo recibe decide si lo abre y cómo.
     */
    public interface OnEntrenamientoClickListener {

        /**
         * El usuario pulsó un entrenamiento de la lista.
         */
        void onEntrenamientoClick(UltimoEntrenamiento entrenamiento);
    }

    private final List<UltimoEntrenamiento> entrenamientos;
    private final OnEntrenamientoClickListener escucha;

    /**
     * Crea el adapter con la lista de entrenamientos a mostrar, ordenados del más
     * reciente al más antiguo, y el callback que se ejecuta al pulsar una fila.
     */
    public UltimoEntrenamientoAdapter(List<UltimoEntrenamiento> entrenamientos,
                                      OnEntrenamientoClickListener escucha) {
        this.entrenamientos = entrenamientos;
        this.escucha = escucha;
    }

    @NonNull
    @Override
    public EntrenamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ultimo_entrenamiento, parent, false);
        return new EntrenamientoViewHolder(view, escucha);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrenamientoViewHolder holder, int position) {
        holder.asignar(entrenamientos.get(position), position == getItemCount() - 1);
    }

    @Override
    public int getItemCount() {
        return entrenamientos.size();
    }

    static class EntrenamientoViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvDetalle;
        private final View separador;
        private final OnEntrenamientoClickListener escucha;

        /** Crea el contenedor de una fila y busca las vistas donde se pintarán el nombre y el detalle. */
        EntrenamientoViewHolder(@NonNull View itemView, OnEntrenamientoClickListener escucha) {
            super(itemView);
            this.escucha = escucha;

            tvNombre = itemView.findViewById(R.id.tvNombreUltimoEntrenamiento);
            tvDetalle = itemView.findViewById(R.id.tvDetalleUltimoEntrenamiento);
            separador = itemView.findViewById(R.id.viewSeparadorUltimoEntrenamiento);
        }

        /**
         * Rellena la fila con los datos de un entrenamiento, oculta el separador cuando
         * es la última y conecta el toque con el callback de la pantalla.
         */
        void asignar(UltimoEntrenamiento entrenamiento, boolean esUltimoItem) {
            tvNombre.setText(entrenamiento.getNombre());
            tvDetalle.setText(itemView.getContext().getString(
                    R.string.tvDetalleUltimoEntrenamiento,
                    entrenamiento.getFecha(),
                    entrenamiento.getDuracionMinutos()));
            separador.setVisibility(esUltimoItem ? View.GONE : View.VISIBLE);

            itemView.setOnClickListener(v -> {
                if (escucha != null) {
                    escucha.onEntrenamientoClick(entrenamiento);
                }
            });
        }
    }
}
