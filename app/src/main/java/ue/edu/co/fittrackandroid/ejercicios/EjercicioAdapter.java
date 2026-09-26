package ue.edu.co.fittrackandroid.ejercicios;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;

/**
 * Adapter para la lista de ejercicios. Permite filtrar por nombre desde el buscador.
 */
public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder> {

    private final List<Ejercicio> listaCompleta;
    private final List<Ejercicio> listaFiltrada = new ArrayList<>();

    public EjercicioAdapter(List<Ejercicio> lista) {
        this.listaCompleta = lista;
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
        holder.asignar(listaFiltrada.get(position));
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
                    || ejercicio.getSubtitulo().toLowerCase(Locale.getDefault()).contains(busqueda);

            if (coincideGrupo && coincideTexto) {
                listaFiltrada.add(ejercicio);
            }
        }

        notifyDataSetChanged();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvSubtitulo;

        EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEjercicio);
            tvSubtitulo = itemView.findViewById(R.id.tvSubtituloEjercicio);
        }

        void asignar(Ejercicio ejercicio) {
            tvNombre.setText(ejercicio.getNombre());
            tvSubtitulo.setText(ejercicio.getSubtitulo());
            itemView.setOnClickListener(v -> Toast.makeText(v.getContext(),
                    "Ver detalles: " + ejercicio.getNombre(), Toast.LENGTH_SHORT).show());
        }
    }
}