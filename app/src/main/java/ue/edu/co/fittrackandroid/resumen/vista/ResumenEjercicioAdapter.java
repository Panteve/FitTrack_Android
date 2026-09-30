package ue.edu.co.fittrackandroid.resumen.vista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.resumen.modelo.EjercicioResumen;
import ue.edu.co.fittrackandroid.resumen.modelo.SerieResumen;

/**
 * Adapter del RecyclerView de ejercicios del resumen de un entrenamiento terminado.
 *
 * <p>Cada tarjeta muestra el nombre del ejercicio, su grupo muscular y una fila por cada
 * serie realizada. Es de solo lectura, así que no recibe ningún callback.
 */
public class ResumenEjercicioAdapter
        extends RecyclerView.Adapter<ResumenEjercicioAdapter.EjercicioViewHolder> {

    private final List<EjercicioResumen> listaEjercicios;

    /** Crea el adapter con los ejercicios que se realizaron. */
    public ResumenEjercicioAdapter(List<EjercicioResumen> listaEjercicios) {
        this.listaEjercicios = listaEjercicios;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_ejercicio_resumen, parent, false);
        return new EjercicioViewHolder(view, inflater);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        holder.asignar(listaEjercicios.get(position));
    }

    @Override
    public int getItemCount() {
        return listaEjercicios.size();
    }

    static class EjercicioViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvGrupoMuscular;
        private final LinearLayout layoutSeries;
        private final LayoutInflater inflater;

        /** Crea la tarjeta del ejercicio, guardando las vistas y el inflater que usará. */
        EjercicioViewHolder(@NonNull View itemView, LayoutInflater inflater) {
            super(itemView);
            this.inflater = inflater;

            tvNombre = itemView.findViewById(R.id.tvNombreEjercicioResumen);
            tvGrupoMuscular = itemView.findViewById(R.id.tvGrupoMuscularEjercicioResumen);
            layoutSeries = itemView.findViewById(R.id.layoutSeriesEjercicioResumen);
        }

        /** Rellena la tarjeta con el nombre y el grupo muscular de un ejercicio del resumen. */
        void asignar(EjercicioResumen ejercicio) {
            tvNombre.setText(ejercicio.getNombre());
            tvGrupoMuscular.setText(ejercicio.getGrupoMuscular());

            mostrarSeries(ejercicio);
        }

        /** Limpia el contenedor y vuelve a inflar una fila por cada serie realizada. */
        private void mostrarSeries(EjercicioResumen ejercicio) {
            layoutSeries.removeAllViews();
            List<SerieResumen> series = ejercicio.getSeries();

            for (int posicionSerie = 0; posicionSerie < series.size(); posicionSerie++) {
                layoutSeries.addView(crearFilaSerie(series.get(posicionSerie), posicionSerie));
            }
        }

        /** Crea la fila de solo lectura que muestra el número de la serie, su peso y sus repeticiones. */
        private View crearFilaSerie(SerieResumen serie, int posicionSerie) {
            View filaSerie = inflater.inflate(R.layout.item_serie_resumen, layoutSeries, false);

            TextView tvNumeroSerie = filaSerie.findViewById(R.id.tvNumeroSerieResumen);
            TextView tvDetalleSerie = filaSerie.findViewById(R.id.tvDetalleSerieResumen);

            // El número sale de la posición de la serie, por eso siempre es consecutivo.
            tvNumeroSerie.setText(String.valueOf(posicionSerie + 1));

            // El peso llega como texto para no mostrar ".0" cuando el número es entero.
            tvDetalleSerie.setText(filaSerie.getContext().getString(R.string.tvDetalleSerieResumen,
                    formatearPeso(serie.getPeso()), serie.getRepeticiones()));

            return filaSerie;
        }

        /** Muestra el peso sin decimales cuando no hacen falta: 85 kg, 22.5 kg. */
        private String formatearPeso(double peso) {
            DecimalFormat formato = new DecimalFormat("#0.##",
                    DecimalFormatSymbols.getInstance(Locale.getDefault()));
            return formato.format(peso);
        }
    }
}
