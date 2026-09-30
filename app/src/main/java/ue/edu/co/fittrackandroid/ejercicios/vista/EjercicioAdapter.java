package ue.edu.co.fittrackandroid.ejercicios.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.ejercicios.modelo.Ejercicio;
import ue.edu.co.fittrackandroid.R;

/**
 * Adapter para la lista de ejercicios. Permite filtrar por nombre desde el buscador
 * y avisa cuándo el usuario toca un ejercicio.
 */
public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {

    /** Callback que recibe el ejercicio que el usuario tocó en la lista. */
    public interface OnEjercicioClickListener {
        /** Avisa que el usuario tocó el ejercicio recibido en la lista. */
        void onEjercicioClick(Ejercicio ejercicio);
    }

    private final List<Ejercicio> listaCompleta;
    private final List<Ejercicio> listaFiltrada = new ArrayList<>();
    private final OnEjercicioClickListener listener;

    /** Crea el adapter con la lista completa de ejercicios y el aviso para cuando se toca uno. */
    public EjercicioAdapter(List<Ejercicio> lista, OnEjercicioClickListener listener) {
        this.listaCompleta = lista;
        this.listener = listener;
        listaFiltrada.addAll(lista);
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ejercicio, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        holder.asignar(listaFiltrada.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    /** Filtra la lista por grupo muscular y por nombre (coincidencia parcial, sin distinguir mayúsculas). */
    public void filtrar(String texto, String grupoMuscular) {
        listaFiltrada.clear();
        String busqueda = texto.trim().toLowerCase(Locale.getDefault());
        boolean filtrarPorGrupo = grupoMuscular != null;

        for (Ejercicio ejercicio : listaCompleta) {
            boolean coincideGrupo = !filtrarPorGrupo
                    || ejercicio.getGrupoMuscular().equalsIgnoreCase(grupoMuscular);

            boolean coincideTexto = busqueda.isEmpty()
                    || ejercicio.getNombre().toLowerCase(Locale.getDefault()).contains(busqueda)
                    || ejercicio.getGrupoMuscular().toLowerCase(Locale.getDefault()).contains(busqueda);

            if (coincideGrupo && coincideTexto) {
                listaFiltrada.add(ejercicio);
            }
        }

        notifyDataSetChanged();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvGrupoMuscular;

        /** Crea el contenedor de una fila y busca las vistas del nombre y del grupo muscular. */
        EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEjercicio);
            tvGrupoMuscular = itemView.findViewById(R.id.tvGrupoMuscularEjercicio);
        }

        /** Muestra el nombre y el grupo del ejercicio y conecta el toque con el callback. */
        void asignar(Ejercicio ejercicio, OnEjercicioClickListener listener) {
            tvNombre.setText(ejercicio.getNombre());
            tvGrupoMuscular.setText(ejercicio.getGrupoMuscular());
            itemView.setOnClickListener(v -> listener.onEjercicioClick(ejercicio));
        }
    }
}
