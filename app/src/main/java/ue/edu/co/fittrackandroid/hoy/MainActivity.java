package ue.edu.co.fittrackandroid.hoy;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ue.edu.co.fittrackandroid.R;

public class MainActivity extends AppCompatActivity {

    private Button btnIniciarEntrenamiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvFecha = findViewById(R.id.tvFecha);
        Button btnEditarRutina = findViewById(R.id.btnEditarRutina);
        Button btnNuevoEjercicio = findViewById(R.id.btnNuevoEjercicio);
        btnIniciarEntrenamiento = findViewById(R.id.btnIniciarEntrenamiento);
        ImageButton btnFabAgregar = findViewById(R.id.btnFabAgregar);
        ImageButton btnBuscar = findViewById(R.id.btnBuscar);
        ImageButton btnOpciones = findViewById(R.id.btnOpciones);

        tvFecha.setText(getString(R.string.tvFecha, formatearFechaHoy()));

        btnIniciarEntrenamiento.setOnClickListener(v -> iniciarEntrenamiento());
        btnEditarRutina.setOnClickListener(v ->
                Toast.makeText(this, "Edición de rutina próximamente", Toast.LENGTH_SHORT).show());
        btnNuevoEjercicio.setOnClickListener(v ->
                Toast.makeText(this, "Nuevo ejercicio próximamente", Toast.LENGTH_SHORT).show());
        btnFabAgregar.setOnClickListener(v ->
                Toast.makeText(this, "Agregar ejercicio próximamente", Toast.LENGTH_SHORT).show());
        btnBuscar.setOnClickListener(v ->
                Toast.makeText(this, "Buscar próximamente", Toast.LENGTH_SHORT).show());
        btnOpciones.setOnClickListener(v ->
                Toast.makeText(this, "Opciones próximamente", Toast.LENGTH_SHORT).show());

        configurarBarraInferior();
    }

    private void iniciarEntrenamiento() {
        btnIniciarEntrenamiento.setEnabled(false);
        btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento_loading);
        btnIniciarEntrenamiento.postDelayed(() -> {
            btnIniciarEntrenamiento.setEnabled(true);
            btnIniciarEntrenamiento.setText(R.string.btnIniciarEntrenamiento);
            Toast.makeText(this, "Iniciando sesión de entrenamiento", Toast.LENGTH_SHORT).show();
        }, 700);
    }

    private void configurarBarraInferior() {
        findViewById(R.id.layoutNavEntrenar).setOnClickListener(v ->
                Toast.makeText(this, "Entrenar próximamente", Toast.LENGTH_SHORT).show());
        findViewById(R.id.layoutNavRutinas).setOnClickListener(v ->
                Toast.makeText(this, "Rutinas próximamente", Toast.LENGTH_SHORT).show());
        findViewById(R.id.layoutNavProgreso).setOnClickListener(v ->
                Toast.makeText(this, "Progreso próximamente", Toast.LENGTH_SHORT).show());
    }

    private String formatearFechaHoy() {
        SimpleDateFormat formato = new SimpleDateFormat(
                "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
        String fecha = formato.format(new Date());
        return fecha.substring(0, 1).toUpperCase(Locale.getDefault()) + fecha.substring(1);
    }
}