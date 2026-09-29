package ue.edu.co.fittrackandroid.perfil.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.R;

/**
 * Adapter para los ejercicios creados por el usuario en el perfil.
 * Solo muestra los datos recibidos: no consulta la API ni navega a otra pantalla.
 * Cuando se toca una fila avisa a PerfilFragment, que es quien abre la pantalla para
 * consultar, modificar o borrar ese ejercicio.
 */
public class PerfilEjercicioAdapter
        extends RecyclerView.Adapter<PerfilEjercicioAdapter.EjercicioPerfilViewHolder> {

    /**
     * Callback que avisa qué ejercicio quiere consultar el usuario.
     */
    public interface OnEjercicioClickListener {
        void onEjercicioClick(EjercicioResponse ejercicio);
    }

    private final List<EjercicioResponse> ejercicios;
    private final OnEjercicioClickListener listener;

    public PerfilEjercicioAdapter(List<EjercicioResponse> ejercicios,
                                  OnEjercicioClickListener listener) {
        this.ejercicios = ejercicios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EjercicioPerfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio_perfil, parent, false);
        return new EjercicioPerfilViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioPerfilViewHolder holder, int position) {
        holder.asignar(ejercicios.get(position), position, position == ejercicios.size() - 1);
    }

    @Override
    public int getItemCount() {
        return ejercicios.size();
    }

    static class EjercicioPerfilViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNumeroEjercicio;
        private final TextView tvNombreEjercicio;
        private final TextView tvGrupoMuscularEjercicio;
        private final View viewSeparadorEjercicio;
        private final OnEjercicioClickListener listener;

        EjercicioPerfilViewHolder(@NonNull View itemView,
                                  OnEjercicioClickListener listener) {
            super(itemView);
            this.listener = listener;
            tvNumeroEjercicio = itemView.findViewById(R.id.tvNumeroEjercicioPerfil);
            tvNombreEjercicio = itemView.findViewById(R.id.tvNombreEjercicioPerfil);
            tvGrupoMuscularEjercicio = itemView.findViewById(R.id.tvGrupoMuscularEjercicioPerfil);
            viewSeparadorEjercicio = itemView.findViewById(R.id.viewSeparadorEjercicioPerfil);
        }

        void asignar(EjercicioResponse ejercicio, int posicion, boolean esUltimo) {
            // El número corresponde al orden en que llegó la lista.
            tvNumeroEjercicio.setText(String.valueOf(posicion + 1));
            tvNombreEjercicio.setText(ejercicio.getNombre());
            tvGrupoMuscularEjercicio.setText(ejercicio.getGrupoMuscular());

            // El último ejercicio no deja una línea colgando al final de la tarjeta.
            viewSeparadorEjercicio.setVisibility(esUltimo ? View.GONE : View.VISIBLE);

            // Toda la fila abre la pantalla de modificar el ejercicio. El click se asigna
            // en cada bind porque el RecyclerView reutiliza la vista con otros ejercicios.
            itemView.setOnClickListener(view -> listener.onEjercicioClick(ejercicio));
        }
    }
}
