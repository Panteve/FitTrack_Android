package ue.edu.co.fittrackandroid.hoy;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ue.edu.co.fittrackandroid.HomeFragment;
import ue.edu.co.fittrackandroid.login.LoginFragment;
import ue.edu.co.fittrackandroid.R;

/**
 * Activity principal (única). Decide qué fragment mostrar según el estado de sesión:
 * - Si hay sesión activa → HomeFragment
 * - Si no hay sesión → LoginFragment
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TextView toolbar;

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
            cargarFragment(fragment);
            return true;
        });
    }

    private void initObjects() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        toolbar = findViewById(R.id.tvToolbarTituloMain);
    }

    private Fragment obtenerFragment (int itemId){
        if (itemId == R.id.navigation_inicio) {
            return new HomeFragment();
        } else if (itemId == R.id.navigation_perfil) {
            // TODO: Navegar a Perfil
            return null;
        } else if (itemId == R.id.navigation_rutinas) {
            // TODO: Navegar a Rutinas
            return null;
        }
        return null; // Retorna null si no hay correspondencia
    }

    private void cargarFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        bottomNavigation.setSelectedItemId(fragment.getId());
    }

    private boolean verificarSesionActiva() {
        // Por ahora siempre false para que muestre login al iniciar
        return false;
    }

    /** Muestra el fragment de Home (pantalla principal "Hoy"). */
    public void mostrarHome() {
        toolbar.setVisibility(View.VISIBLE);
        bottomNavigation.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
        bottomNavigation.setSelectedItemId(R.id.navigation_inicio);
    }

    /** Muestra el fragment de Login SIN toolbar ni bottom nav. */
    public void mostrarLogin() {
        toolbar.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();
    }
}