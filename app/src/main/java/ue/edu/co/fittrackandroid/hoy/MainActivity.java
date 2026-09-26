package ue.edu.co.fittrackandroid.hoy;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ue.edu.co.fittrackandroid.HomeFragment;
import ue.edu.co.fittrackandroid.ejercicios.CrearEjercicioFragment;
import ue.edu.co.fittrackandroid.ejercicios.EjerciciosFragment;
import ue.edu.co.fittrackandroid.login.LoginFragment;
import ue.edu.co.fittrackandroid.rutinas.RutinasFragment;
import ue.edu.co.fittrackandroid.R;

/**
 * Activity principal (única). Decide qué fragment mostrar según el estado de sesión:
 * - Si hay sesión activa → HomeFragment
 * - Si no hay sesión → LoginFragment
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private View layoutToolbar;
    private TextView tvToolbarTituloMain;
    private ImageButton btnVolverToolbar;
    private Button btnAccionToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initObjects();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutPrincipal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Bottom lo maneja el BottomNavigationView
            // Aplicar bottom inset al BottomNavigationView para que quede pegado al borde superior de la barra de gestos
            bottomNavigation.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        configurarBottomNavigation();

        if (savedInstanceState == null) {
            // TODO: Verificar si hay sesión guardada (SharedPreferences, token, etc.)
            boolean haySesionActiva = verificarSesionActiva();

            if (haySesionActiva) {
                mostrarHome();
            } else {
                mostrarLogin();
            }
        }
    }

    /**
     * TODO: Implementar verificación real de sesión.
     * Ejemplos:
     * - SharedPreferences: getSharedPreferences("session", MODE_PRIVATE).getBoolean("logged_in", false)
     * - Token JWT: comprobar expiración
     * - DataStore / Room: leer usuario actual
     */

    private void configurarBottomNavigation(){
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = obtenerFragment(item.getItemId());
            if (fragment == null) {
                return false;
            }
            // Las pestañas del menú inferior son raíces: se descarta cualquier pantalla secundaria abierta.
            limpiarBackStack();
            cargarFragment(fragment);
            return true;
        });
    }

    private void initObjects() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        layoutToolbar = findViewById(R.id.layoutToolbar);
        tvToolbarTituloMain = findViewById(R.id.tvToolbarTituloMain);
        btnVolverToolbar = findViewById(R.id.btnVolverToolbar);
        btnAccionToolbar = findViewById(R.id.btnAccionToolbar);

        mostrarToolbarPrincipal();
        btnVolverToolbar.setOnClickListener(v -> onBackPressed());
    }

    private Fragment obtenerFragment (int itemId){
        if (itemId == R.id.navigation_inicio) {
            return new HomeFragment();
        } else if (itemId == R.id.navigation_perfil) {
            // TODO: Navegar a Perfil
            return null;
        } else if (itemId == R.id.navigation_rutinas) {
            return new RutinasFragment();
        }
        return null; // Retorna null si no hay correspondencia
    }

    private void cargarFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private boolean verificarSesionActiva() {
        // Por ahora siempre false para que muestre login al iniciar
        return false;
    }

    /** Muestra el fragment de Home (pantalla principal "Hoy"). */
    public void mostrarHome() {
        layoutToolbar.setVisibility(View.VISIBLE);
        bottomNavigation.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
        bottomNavigation.setSelectedItemId(R.id.navigation_inicio);
    }

    /** Muestra el fragment de Login SIN toolbar ni bottom nav. */
    public void mostrarLogin() {
        layoutToolbar.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();
    }

    private void cargarFragmentConBackStack(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void limpiarBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /** Restaura la toolbar principal: solo título "FitTrack", sin volver ni acción. */
    public void mostrarToolbarPrincipal() {
        layoutToolbar.setVisibility(View.VISIBLE);
        tvToolbarTituloMain.setText(R.string.tvToolbarTituloMain);
        btnVolverToolbar.setVisibility(View.GONE);
        btnAccionToolbar.setVisibility(View.GONE);
        btnAccionToolbar.setOnClickListener(null);
    }

    /** Configura la toolbar para una pantalla secundaria (título, volver opcional y acción opcional).
     *  La acción debe configurarse con {@link #setAccionToolbar(Runnable)}. */
    public void mostrarToolbarSecundaria(String titulo, boolean conVolver, String textoAccion) {
        layoutToolbar.setVisibility(View.VISIBLE);
        tvToolbarTituloMain.setText(titulo);
        btnVolverToolbar.setVisibility(conVolver ? View.VISIBLE : View.GONE);
        btnAccionToolbar.setOnClickListener(null);
        if (textoAccion != null) {
            btnAccionToolbar.setVisibility(View.VISIBLE);
            btnAccionToolbar.setText(textoAccion);
        } else {
            btnAccionToolbar.setVisibility(View.GONE);
        }
    }

    /** Asigna la acción del botón derecho de la toolbar. */
    public void setAccionToolbar(Runnable accion) {
        btnAccionToolbar.setOnClickListener(v -> accion.run());
    }

    /**
     * Navega a la lista de ejercicios (con retroceso).
     * Todavía no se llama desde ninguna pantalla: la lista se conectará más adelante.
     */
    public void mostrarEjercicios() {
        cargarFragmentConBackStack(new EjerciciosFragment());
    }

    /** Navega a la pantalla de crear ejercicio (con retroceso). */
    public void mostrarCrearEjercicio() {
        cargarFragmentConBackStack(new CrearEjercicioFragment());
    }

    /** Retrocede a la pantalla anterior si hay una en la pila. */
    public void regresar() {
        onBackPressed();
    }


}