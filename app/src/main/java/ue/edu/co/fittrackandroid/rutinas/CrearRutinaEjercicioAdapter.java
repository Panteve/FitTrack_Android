package ue.edu.co.fittrackandroid.rutinas;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ue.edu.co.fittrackandroid.R;

/**
 * Adapter de los ejercicios de la rutina que se está creando.
 * Cada tarjeta muestra el nombre del ejercicio y una fila por cada serie, con su número,
 * peso objetivo y repeticiones.
 */
public class CrearRutinaEjercicioAdapter
        extends RecyclerView.Adapter<CrearRutinaEjercicioAdapter.EjercicioEditableViewHolder> {

    // TODO: Agregar acciones para quitar un ejercicio seleccionado y eliminar una serie
    // agregada por error, actualizando tanto el modelo como las posiciones visibles.

    /**
     * Callback que avisa cuando el usuario pide una serie nueva para un ejercicio.
     */
    public interface OnAgregarSerieListener {
        void onAgregarSerie(int posicionEjercicio);
    }

    private final List<EjercicioRutinaEditable> listaEjercicios;
    private final OnAgregarSerieListener listenerAgregarSerie;

    public CrearRutinaEjercicioAdapter(List<EjercicioRutinaEditable> listaEjercicios,
                                         OnAgregarSerieListener listenerAgregarSerie) {
        this.listaEjercicios = listaEjercicios;
        this.listenerAgregarSerie = listenerAgregarSerie;
    }

    /** Agrega un ejercicio al final de la lista. */
    public void agregarEjercicio(EjercicioRutinaEditable ejercicioNuevo) {
        listaEjercicios.add(ejercicioNuevo);
        notifyItemInserted(listaEjercicios.size() - 1);
    }

    /** Vuelve a dibujar solamente la tarjeta del ejercicio indicado. */
    public void actualizarEjercicio(int posicionEjercicio) {
        notifyItemChanged(posicionEjercicio);
    }

    @NonNull
    @Override
    public EjercicioEditableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_ejercicio_crear_rutina, parent, false);
        return new EjercicioEditableViewHolder(view, inflater, listenerAgregarSerie);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioEditableViewHolder holder, int position) {
        holder.asignar(listaEjercicios.get(position));
    }

    @Override
    public int getItemCount() {
        return listaEjercicios.size();
    }

    static class EjercicioEditableViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNombre;
        private final TextView tvGrupoMuscular;
        private final LinearLayout layoutSeries;
        private final Button btnAgregarSerie;
        private final LayoutInflater inflater;
        private final OnAgregarSerieListener listenerAgregarSerie;

        private EjercicioRutinaEditable ejercicio;

        EjercicioEditableViewHolder(@NonNull View itemView, LayoutInflater inflater,
                                    OnAgregarSerieListener listenerAgregarSerie) {
            super(itemView);
            this.inflater = inflater;
            this.listenerAgregarSerie = listenerAgregarSerie;

            tvNombre = itemView.findViewById(R.id.tvNombreEjercicioRutinaEditable);
            tvGrupoMuscular = itemView.findViewById(R.id.tvGrupoMuscularEjercicioRutina);
            layoutSeries = itemView.findViewById(R.id.layoutSeriesEjercicio);
            btnAgregarSerie = itemView.findViewById(R.id.btnAgregarSerie);
        }

        void asignar(EjercicioRutinaEditable ejercicio) {
            this.ejercicio = ejercicio;

            tvNombre.setText(ejercicio.getNombre());
            tvGrupoMuscular.setText(ejercicio.getGrupoMuscular());

            mostrarSeries();

            btnAgregarSerie.setOnClickListener(v -> {
                int posicionEjercicio = getBindingAdapterPosition();
                if (posicionEjercicio != RecyclerView.NO_POSITION) {
                    listenerAgregarSerie.onAgregarSerie(posicionEjercicio);
                }
            });
        }

        /** Limpia el contenedor y vuelve a inflar una fila por cada serie del ejercicio. */
        private void mostrarSeries() {
            layoutSeries.removeAllViews();
            List<SerieRutina> series = ejercicio.getSeries();

            for (int posicion = 0; posicion < series.size(); posicion++) {
                // El número visible sale de la posición de la serie, por eso siempre es consecutivo.
                layoutSeries.addView(crearFilaSerie(series.get(posicion), posicion + 1));
            }
        }

        private View crearFilaSerie(SerieRutina serie, int numeroSerie) {
            View filaSerie = inflater.inflate(R.layout.item_serie_rutina, layoutSeries, false);

            TextView tvNumeroSerie = filaSerie.findViewById(R.id.tvNumeroSerie);
            EditText etPesoObjetivo = filaSerie.findViewById(R.id.etPesoObjetivoSerie);
            EditText etRepeticiones = filaSerie.findViewById(R.id.etRepeticionesSerie);

            tvNumeroSerie.setText(String.valueOf(numeroSerie));

            // Los TextWatcher guardan lo que el usuario escribe en el modelo para que no se pierda
            // cuando la tarjeta se recicle o la pantalla se vuelva a crear.
            etPesoObjetivo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int inicio, int cantidad, int despues) {
                }

                @Override
                public void onTextChanged(CharSequence s, int inicio, int antes, int cantidad) {
                }

                @Override
                public void afterTextChanged(Editable texto) {
                    serie.setPesoObjetivo(texto.toString());
                }
            });

            etRepeticiones.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int inicio, int cantidad, int despues) {
                }

                @Override
                public void onTextChanged(CharSequence s, int inicio, int antes, int cantidad) {
                }

                @Override
                public void afterTextChanged(Editable texto) {
                    serie.setRepeticiones(texto.toString());
                }
            });

            etPesoObjetivo.setText(serie.getPesoObjetivo());
            etRepeticiones.setText(serie.getRepeticiones());

            return filaSerie;
        }
    }
}
