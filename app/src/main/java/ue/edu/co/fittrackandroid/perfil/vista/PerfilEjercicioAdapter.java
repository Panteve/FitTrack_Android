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
 */
public class PerfilEjercicioAdapter
        extends RecyclerView.Adapter<PerfilEjercicioAdapter.EjercicioPerfilViewHolder> {

    private final List<EjercicioResponse> ejercicios;

    public PerfilEjercicioAdapter(List<EjercicioResponse> ejercicios) {
        this.ejercicios = ejercicios;
    }

    @NonNull
    @Override
    public EjercicioPerfilViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio_perfil, parent, false);
        return new EjercicioPerfilViewHolder(view);
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

        EjercicioPerfilViewHolder(@NonNull View itemView) {
            super(itemView);
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
        }
    }
}
