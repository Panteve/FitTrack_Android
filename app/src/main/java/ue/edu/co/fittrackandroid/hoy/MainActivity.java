package ue.edu.co.fittrackandroid.hoy;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutPrincipal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initObjects();
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
    }

    private Fragment obtenerFragment (int itemId){

        if (itemId == R.id.navigation_hoy) {
            return new HomeFragment();
        } else if (itemId == R.id.navigation_entrenar) {
            // TODO: Navegar a Entrenar
            return null;
        } else if (itemId == R.id.navigation_rutinas) {
            // TODO: Navegar a Rutinas
            return null;
        } else if (itemId == R.id.navigation_progreso) {
            // TODO: Navegar a Progreso
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
        bottomNavigation.setSelectedItemId(R.id.navigation_hoy);
    }

    /** Muestra el fragment de Login. */
    public void mostrarLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();
    }
}