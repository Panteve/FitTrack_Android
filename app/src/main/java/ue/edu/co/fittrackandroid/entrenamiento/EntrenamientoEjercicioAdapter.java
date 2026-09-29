package ue.edu.co.fittrackandroid.entrenamiento;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;

/**
 * Adapter del RecyclerView de ejercicios del entrenamiento en curso.
 * Cada tarjeta muestra el nombre del ejercicio, sus series y el botón para agregar otra.
 * Las series no usan un RecyclerView propio: se inflan como filas dentro de un LinearLayout,
 * igual que en la creación de rutinas.
 */
public class EntrenamientoEjercicioAdapter
        extends RecyclerView.Adapter<EntrenamientoEjercicioAdapter.EjercicioViewHolder> {

    // TODO: Agregar una acción para eliminar una serie individual agregada por error,
    // manteniendo por lo menos una serie por ejercicio y actualizando el resumen.

    /**
     * Callbacks que el fragment necesita para mantener el resumen y la lista al día.
     */
    public interface EscuchaEntrenamiento {

        /** El usuario pidió quitar un ejercicio de la pantalla. */
        void onQuitarEjercicio(int posicionEjercicio);

        /** El usuario pidió agregar una serie al ejercicio indicado. */
        void onAgregarSerie(int posicionEjercicio);

        /** Una serie cambió su estado de completada o de no completada. */
        void onEstadoSerieCambiado(int posicionEjercicio, int posicionSerie, boolean completada);

        /** El usuario cambió el peso o las repeticiones de alguna serie. */
        void onDatosSerieCambiados();
    }

    private final List<EjercicioEntrenamiento> listaEjercicios;
    private final EscuchaEntrenamiento escucha;

    public EntrenamientoEjercicioAdapter(List<EjercicioEntrenamiento> listaEjercicios,
                                        EscuchaEntrenamiento escucha) {
        this.listaEjercicios = listaEjercicios;
        this.escucha = escucha;
    }

    /** Agrega un ejercicio al final de la lista. */
    public void agregarEjercicio(EjercicioEntrenamiento ejercicioNuevo) {
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
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_ejercicio_entrenamiento, parent, false);
        return new EjercicioViewHolder(view, inflater, escucha);
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
        private final ImageButton btnQuitar;
        private final LinearLayout layoutSeries;
        private final Button btnAgregarSerie;
        private final LayoutInflater inflater;
        private final EscuchaEntrenamiento escucha;

        private EjercicioEntrenamiento ejercicio;

        /**
         * Se pone en true mientras el código revierte un check inválido, para que el
         *_listener del CheckBox no entienda ese cambio como una acción del usuario.
         */
        private boolean revirtiendoCheck;

        EjercicioViewHolder(@NonNull View itemView, LayoutInflater inflater,
                            EscuchaEntrenamiento escucha) {
            super(itemView);
            this.inflater = inflater;
            this.escucha = escucha;

            tvNombre = itemView.findViewById(R.id.tvNombreEjercicioEntrenamiento);
            tvGrupoMuscular = itemView.findViewById(R.id.tvGrupoMuscularEjercicioEntrenamiento);
            btnQuitar = itemView.findViewById(R.id.btnQuitarEjercicioEntrenamiento);
            layoutSeries = itemView.findViewById(R.id.layoutSeriesEntrenamiento);
            btnAgregarSerie = itemView.findViewById(R.id.btnAgregarSerieEntrenamiento);
        }

        void asignar(EjercicioEntrenamiento ejercicio) {
            this.ejercicio = ejercicio;

            tvNombre.setText(ejercicio.getNombre());
            tvGrupoMuscular.setVisibility(View.GONE);

            mostrarSeries();

            btnQuitar.setOnClickListener(v -> {
                int posicionEjercicio = getBindingAdapterPosition();
                if (posicionEjercicio != RecyclerView.NO_POSITION) {
                    escucha.onQuitarEjercicio(posicionEjercicio);
                }
            });

            btnAgregarSerie.setOnClickListener(v -> {
                int posicionEjercicio = getBindingAdapterPosition();
                if (posicionEjercicio != RecyclerView.NO_POSITION) {
                    escucha.onAgregarSerie(posicionEjercicio);
                }
            });
        }

        /** Limpia el contenedor y vuelve a inflar una fila por cada serie del ejercicio. */
        private void mostrarSeries() {
            layoutSeries.removeAllViews();
            List<SerieEntrenamiento> series = ejercicio.getSeries();

            for (int posicionSerie = 0; posicionSerie < series.size(); posicionSerie++) {
                layoutSeries.addView(crearFilaSerie(series.get(posicionSerie), posicionSerie));
            }
        }

        private View crearFilaSerie(SerieEntrenamiento serie, int posicionSerie) {
            View filaSerie = inflater.inflate(R.layout.item_serie_entrenamiento, layoutSeries, false);

            TextView tvNumeroSerie = filaSerie.findViewById(R.id.tvNumeroSerieEntrenamiento);
            EditText etPeso = filaSerie.findViewById(R.id.etPesoSerieEntrenamiento);
            EditText etRepeticiones = filaSerie.findViewById(R.id.etRepeticionesSerieEntrenamiento);
            CheckBox cbSerieCompletada = filaSerie.findViewById(R.id.cbSerieCompletada);

            int numeroSerie = serie.getNumeroSerie() > 0
                    ? serie.getNumeroSerie()
                    : posicionSerie + 1;
            tvNumeroSerie.setText(String.valueOf(numeroSerie));

            etPeso.setText(formatearPeso(serie.getPeso()));
            if (serie.getRepeticiones() > 0) {
                etRepeticiones.setText(String.valueOf(serie.getRepeticiones()));
            }

            conectarCampoPeso(etPeso, serie);
            conectarCampoRepeticiones(etRepeticiones, serie);

            // El check se pinta antes de conectar su listener, así el RecyclerView no
            // interpreta el estado guardado como una acción del usuario.
            cbSerieCompletada.setChecked(serie.isCompletada());
            aplicarEstadoCampos(etPeso, etRepeticiones, serie.isCompletada());
            conectarCheckSerie(serie, etPeso, etRepeticiones, cbSerieCompletada);

            return filaSerie;
        }

        /** Guarda en el modelo lo que el usuario escribe en el peso de la serie. */
        private void conectarCampoPeso(EditText etPeso, SerieEntrenamiento serie) {
            etPeso.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence texto, int inicio, int cantidad, int despues) {
                }

                @Override
                public void onTextChanged(CharSequence texto, int inicio, int antes, int cantidad) {
                }

                @Override
                public void afterTextChanged(Editable texto) {
                    serie.setPeso(convertirPeso(texto.toString()));
                    etPeso.setError(null);
                    escucha.onDatosSerieCambiados();
                }
            });
        }

        /** Guarda en el modelo lo que el usuario escribe en las repeticiones de la serie. */
        private void conectarCampoRepeticiones(EditText etRepeticiones, SerieEntrenamiento serie) {
            etRepeticiones.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence texto, int inicio, int cantidad, int despues) {
                }

                @Override
                public void onTextChanged(CharSequence texto, int inicio, int antes, int cantidad) {
                }

                @Override
                public void afterTextChanged(Editable texto) {
                    serie.setRepeticiones(convertirRepeticiones(texto.toString()));
                    etRepeticiones.setError(null);
                    escucha.onDatosSerieCambiados();
                }
            });
        }

        /** Valida la serie al marcarla y avisa al fragment para recalcular el resumen. */
        private void conectarCheckSerie(SerieEntrenamiento serie, EditText etPeso,
                                        EditText etRepeticiones, CheckBox cbSerieCompletada) {
            cbSerieCompletada.setOnCheckedChangeListener((boton, estaMarcada) -> {
                // Se está revirtiendo un check inválido, no es una acción del usuario.
                if (revirtiendoCheck) {
                    return;
                }

                int posicionEjercicio = getBindingAdapterPosition();
                if (posicionEjercicio == RecyclerView.NO_POSITION) {
                    return;
                }

                // Se leen los campos por si el usuario los editó sin pasar por el watcher.
                serie.setPeso(convertirPeso(etPeso.getText().toString()));
                serie.setRepeticiones(
                        convertirRepeticiones(etRepeticiones.getText().toString()));

                if (estaMarcada && !serie.tieneDatosValidos()) {
                    desmarcarSerie(serie, etPeso, etRepeticiones, cbSerieCompletada);
                    mostrarErrorSerie(serie, etPeso, etRepeticiones);
                    return;
                }

                serie.setCompletada(estaMarcada);
                aplicarEstadoCampos(etPeso, etRepeticiones, estaMarcada);
                escucha.onEstadoSerieCambiado(posicionEjercicio, posicionSerieDe(serie), estaMarcada);
            });
        }

        /** Desmarca el check de una serie que no tiene datos válidos. */
        private void desmarcarSerie(SerieEntrenamiento serie, EditText etPeso,
                                    EditText etRepeticiones, CheckBox cbSerieCompletada) {
            revirtiendoCheck = true;
            cbSerieCompletada.setChecked(false);
            revirtiendoCheck = false;

            serie.setCompletada(false);
            aplicarEstadoCampos(etPeso, etRepeticiones, false);
        }

        /**
         * Bloquea o habilita los campos de la serie según su estado: una serie completada
         * queda congelada para que el registro no cambie solo.
         */
        private void aplicarEstadoCampos(EditText etPeso, EditText etRepeticiones, boolean completada) {
            etPeso.setEnabled(!completada);
            etRepeticiones.setEnabled(!completada);
        }

        /** Muestra en el campo que corresponda el motivo por el que la serie no es válida. */
        private void mostrarErrorSerie(SerieEntrenamiento serie, EditText etPeso,
                                       EditText etRepeticiones) {
            if (serie.esPesoValido()) {
                etPeso.setError(null);
            } else {
                etPeso.setError(etPeso.getContext().getString(R.string.etPesoSerieEntrenamiento_error));
            }

            if (serie.sonRepeticionesValidas()) {
                etRepeticiones.setError(null);
            } else {
                etRepeticiones.setError(etRepeticiones.getContext()
                        .getString(R.string.etRepeticionesSerieEntrenamiento_error));
            }
        }

        /** @return la posición de la serie dentro de su ejercicio, para avisar al fragment. */
        private int posicionSerieDe(SerieEntrenamiento serie) {
            return ejercicio.getSeries().indexOf(serie);
        }

        /** Convierte el peso numérico en un texto corto para el campo editable. */
        private String formatearPeso(double peso) {
            if (peso < 0) {
                return "";
            }

            DecimalFormat formato = new DecimalFormat("#0.##",
                    DecimalFormatSymbols.getInstance(Locale.getDefault()));
            return formato.format(peso);
        }

        /**
         * Convierte el peso escrito al tipo numérico del modelo.
         *
         * @param texto peso escrito por el usuario
         * @return el peso convertido, o -1 si el campo está vacío o no es válido
         */
        private double convertirPeso(String texto) {
            String pesoEscrito = texto.trim();
            if (pesoEscrito.isEmpty()) {
                return -1;
            }

            try {
                return Double.parseDouble(pesoEscrito.replace(',', '.'));
            } catch (NumberFormatException excepcion) {
                return -1;
            }
        }

        /**
         * Convierte las repeticiones escritas al tipo numérico del modelo.
         *
         * @param texto repeticiones escritas por el usuario
         * @return las repeticiones convertidas o cero si el valor no es válido
         */
        private int convertirRepeticiones(String texto) {
            String repeticionesEscritas = texto.trim();
            if (repeticionesEscritas.isEmpty()) {
                return 0;
            }

            try {
                return Integer.parseInt(repeticionesEscritas);
            } catch (NumberFormatException excepcion) {
                return 0;
            }
        }
    }
}
