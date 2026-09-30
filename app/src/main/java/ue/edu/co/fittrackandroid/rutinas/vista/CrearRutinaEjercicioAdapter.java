package ue.edu.co.fittrackandroid.rutinas.vista;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.rutinas.modelo.EjercicioRutinaEditable;
import ue.edu.co.fittrackandroid.rutinas.modelo.SerieRutina;

/**
 * Adapter de los ejercicios de la rutina que se está creando o modificando.
 * Cada tarjeta muestra el nombre del ejercicio, la opción de quitarlo y una fila por cada
 * serie, con su número, peso objetivo, repeticiones y la opción de quitarla.
 * El adapter solo avisa qué botón se pulsó: quitar ejercicios o series lo decide el fragment.
 */
public class CrearRutinaEjercicioAdapter
        extends RecyclerView.Adapter<CrearRutinaEjercicioAdapter.EjercicioEditableViewHolder> {

    /**
     * Callbacks que el fragment necesita para mantener la lista y la pantalla al día.
     * El adapter solo avisa qué botón se pulsó: las listas las modifica el fragment.
     */
    public interface EscuchaCrearRutina {

        /** El usuario pidió una serie nueva para el ejercicio indicado. */
        void onAgregarSerie(int posicionEjercicio);

        /** El usuario pidió quitar una serie del ejercicio indicado. */
        void onQuitarSerie(int posicionEjercicio, int posicionSerie);

        /** El usuario pidió quitar un ejercicio de la rutina. */
        void onQuitarEjercicio(int posicionEjercicio);
    }

    private final List<EjercicioRutinaEditable> listaEjercicios;
    private final EscuchaCrearRutina escucha;

    public CrearRutinaEjercicioAdapter(List<EjercicioRutinaEditable> listaEjercicios,
                                        EscuchaCrearRutina escucha) {
        this.listaEjercicios = listaEjercicios;
        this.escucha = escucha;
    }

    /** Agrega un ejercicio al final de la lista. */
    public void agregarEjercicio(EjercicioRutinaEditable ejercicioNuevo) {
        listaEjercicios.add(ejercicioNuevo);
        notifyItemInserted(listaEjercicios.size() - 1);
    }

    /** Elimina un ejercicio junto con todas sus series. */
    public void quitarEjercicio(int posicionEjercicio) {
        listaEjercicios.remove(posicionEjercicio);
        notifyItemRemoved(posicionEjercicio);
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
        return new EjercicioEditableViewHolder(view, inflater, escucha);
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
        private final ImageButton btnQuitarEjercicio;
        private final LinearLayout layoutSeries;
        private final Button btnAgregarSerie;
        private final LayoutInflater inflater;
        private final EscuchaCrearRutina escucha;

        private EjercicioRutinaEditable ejercicio;

        EjercicioEditableViewHolder(@NonNull View itemView, LayoutInflater inflater,
                                    EscuchaCrearRutina escucha) {
            super(itemView);
            this.inflater = inflater;
            this.escucha = escucha;

            tvNombre = itemView.findViewById(R.id.tvNombreEjercicioRutinaEditable);
            tvGrupoMuscular = itemView.findViewById(R.id.tvGrupoMuscularEjercicioRutina);
            btnQuitarEjercicio = itemView.findViewById(R.id.btnQuitarEjercicioRutina);
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
                    escucha.onAgregarSerie(posicionEjercicio);
                }
            });

            btnQuitarEjercicio.setOnClickListener(v -> {
                int posicionEjercicio = getBindingAdapterPosition();
                if (posicionEjercicio != RecyclerView.NO_POSITION) {
                    escucha.onQuitarEjercicio(posicionEjercicio);
                }
            });
        }

        /** Limpia el contenedor y vuelve a inflar una fila por cada serie del ejercicio. */
        private void mostrarSeries() {
            layoutSeries.removeAllViews();
            List<SerieRutina> series = ejercicio.getSeries();

            for (int posicion = 0; posicion < series.size(); posicion++) {
                // El número visible sale de la posición de la serie, por eso siempre es consecutivo.
                layoutSeries.addView(crearFilaSerie(series.get(posicion), posicion));
            }
        }

        private View crearFilaSerie(SerieRutina serie, int posicionSerie) {
            View filaSerie = inflater.inflate(R.layout.item_serie_rutina, layoutSeries, false);

            TextView tvNumeroSerie = filaSerie.findViewById(R.id.tvNumeroSerie);
            EditText etPesoObjetivo = filaSerie.findViewById(R.id.etPesoObjetivoSerie);
            EditText etRepeticiones = filaSerie.findViewById(R.id.etRepeticionesSerie);
            ImageButton btnQuitarSerie = filaSerie.findViewById(R.id.btnQuitarSerieRutina);

            tvNumeroSerie.setText(String.valueOf(posicionSerie + 1));

            // La única serie del ejercicio no se puede quitar, porque el backend exige al
            // menos una. El botón queda visible pero apagado para no mover los campos.
            boolean puedeQuitarSerie = ejercicio.puedeEliminarSerie();
            btnQuitarSerie.setEnabled(puedeQuitarSerie);
            btnQuitarSerie.setAlpha(puedeQuitarSerie ? 1f : 0.25f);
            btnQuitarSerie.setOnClickListener(v -> {
                int posicionEjercicio = getBindingAdapterPosition();
                if (puedeQuitarSerie && posicionEjercicio != RecyclerView.NO_POSITION) {
                    escucha.onQuitarSerie(posicionEjercicio, posicionSerie);
                }
            });

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
