package ue.edu.co.fittrackandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter de la lista de entrainamientos recientes de la pantalla de inicio.
 * Solo muestra nombre y detalle de cada fila, sin navegación ni lógica de negocio.
 */
public class UltimoEntrenamientoAdapter extends RecyclerView.Adapter<UltimoEntrenamientoAdapter.EntrenamientoViewHolder> {

    private final List<UltimoEntrenamiento> entrenamientos;

    /**
     * Crea el adapter con la lista de entrenamientos a mostrar.
     *
     * @param entrenamientos lista con máximo tres entrenamientos, ordenados del más reciente al más antiguo.
     */
    public UltimoEntrenamientoAdapter(List<UltimoEntrenamiento> entrenamientos) {
        this.entrenamientos = entrenamientos;
    }

    @NonNull
    @Override
    public EntrenamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ultimo_entrenamiento, parent, false);
        return new EntrenamientoViewHolder(view);
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

        EntrenamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreUltimoEntrenamiento);
            tvDetalle = itemView.findViewById(R.id.tvDetalleUltimoEntrenamiento);
            separador = itemView.findViewById(R.id.viewSeparadorUltimoEntrenamiento);
        }

        /**
         * Rellena la fila con los datos de un entrenamiento.
         *
         * @param entrenamiento        datos a mostrar.
         * @param esUltimoItem         true si es la última fila, para ocultar el separador.
         */
        void asignar(UltimoEntrenamiento entrenamiento, boolean esUltimoItem) {
            tvNombre.setText(entrenamiento.getNombre());
            tvDetalle.setText(itemView.getContext().getString(
                    R.string.tvDetalleUltimoEntrenamiento,
                    entrenamiento.getFecha(),
                    entrenamiento.getDuracionMinutos()));
            separador.setVisibility(esUltimoItem ? View.GONE : View.VISIBLE);
        }
    }
}
