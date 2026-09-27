package ue.edu.co.fittrackandroid.hoy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

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

import ue.edu.co.fittrackandroid.R;
import ue.edu.co.fittrackandroid.ejercicios.CrearEjercicioFragment;
import ue.edu.co.fittrackandroid.ejercicios.EjerciciosFragment;
import ue.edu.co.fittrackandroid.entrenamiento.EjercicioEntrenamiento;
import ue.edu.co.fittrackandroid.entrenamiento.EntrenamientoActivoFragment;
import ue.edu.co.fittrackandroid.entrenamiento.EntrenamientoEnCurso;
import ue.edu.co.fittrackandroid.login.LoginFragment;
import ue.edu.co.fittrackandroid.perfil.CambiarContrasenaFragment;
import ue.edu.co.fittrackandroid.perfil.PerfilFragment;
import ue.edu.co.fittrackandroid.registro.CrearCuentaFragment;
import ue.edu.co.fittrackandroid.resumen.ResumenEntrenamiento;
import ue.edu.co.fittrackandroid.resumen.ResumenEntrenamientoFragment;
import ue.edu.co.fittrackandroid.rutinas.CrearRutinaFragment;
import ue.edu.co.fittrackandroid.rutinas.RutinasFragment;

/**
 * Activity principal (única). Decide qué fragment mostrar según el estado de sesión:
 * - Si hay sesión activa → HomeFragment
 * - Si no hay sesión → LoginFragment
 *
 * También es la dueña del estado del entrenamiento en curso y de la isla compacta que
 * permite recuperarlo cuando el usuario lo minimiza.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private View layoutToolbar;
    private TextView tvToolbarTituloMain;
    private ImageButton btnVolverToolbar;
    private Button btnAccionToolbar;
    private Button btnAccionToolbarSecundario;
    private View layoutIslaEntrenamiento;
    private View layoutInfoEntrenamientoMinimizado;
    private TextView tvTiempoEntrenamientoMinimizado;
    private ImageButton btnAbrirEntrenamiento;
    private ImageButton btnDescartarEntrenamientoMinimizado;

    /**
     * Estado del entrenamiento en curso. Se guarda aquí y no en el fragment para que la
     * sesión sobreviva a que sus vistas se destruyan al abrir el selector de ejercicios
     * o al minimizar el entrenamiento.
     */
    private EntrenamientoEnCurso entrenamientoEnCurso;

    /**
     * Resumen del último entrenamiento terminado que se está mostrando.
     * Vive en la Activity porque todavía no existe el historial guardado.
     * TODO: Reemplazar por el entrenamiento leído del almacenamiento cuando exista.
     */
    private ResumenEntrenamiento resumenEntrenamientoActual;

    /** Reloj de la isla del entrenamiento minimizado. */
    private final Handler handlerIsla = new Handler(Looper.getMainLooper());

    /** Refresca el tiempo de la isla una vez por segundo. */
    private final Runnable runnableTiempoIsla = new Runnable() {
        @Override
        public void run() {
            actualizarTiempoIsla();
        }
    };

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
            boolean haySesionActiva = verificarSesionActiva();

            if (haySesionActiva) {
                mostrarHome();
            } else {
                mostrarLogin();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerIsla.removeCallbacksAndMessages(null);
    }

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
        btnAccionToolbarSecundario = findViewById(R.id.btnAccionToolbarSecundario);
        inicializarIslaEntrenamiento();

        mostrarToolbarPrincipal();
    }

    /** Conecta la isla: la flecha y la zona central reabren, la papelera pide confirmación. */
    private void inicializarIslaEntrenamiento() {
        layoutIslaEntrenamiento = findViewById(R.id.layoutIslaEntrenamiento);
        layoutInfoEntrenamientoMinimizado = findViewById(R.id.layoutInfoEntrenamientoMinimizado);
        tvTiempoEntrenamientoMinimizado = findViewById(R.id.tvTiempoEntrenamientoMinimizado);
        btnAbrirEntrenamiento = findViewById(R.id.btnAbrirEntrenamiento);
        btnDescartarEntrenamientoMinimizado = findViewById(R.id.btnDescartarEntrenamientoMinimizado);

        btnAbrirEntrenamiento.setOnClickListener(v -> abrirEntrenamientoEnCurso());
        layoutInfoEntrenamientoMinimizado.setOnClickListener(v -> abrirEntrenamientoEnCurso());
        btnDescartarEntrenamientoMinimizado.setOnClickListener(v -> confirmarDescarteEntrenamientoMinimizado());
    }

    private Fragment obtenerFragment (int itemId){
        if (itemId == R.id.navigation_inicio) {
            return new HomeFragment();
        } else if (itemId == R.id.navigation_perfil) {
            return new PerfilFragment();
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

    /**
     * TODO: Consultar la sesión guardada y comprobar que el token exista y siga vigente.
     * Si el token expiró o no es válido, se debe limpiar la sesión local y mostrar el login.
     *
     * @return {@code true} cuando el usuario tenga una sesión válida.
     */
    private boolean verificarSesionActiva() {
        // TODO: Reemplazar este valor fijo por el resultado de la verificación del token.
        return false;
    }

    /** Muestra el fragment de Home (pantalla principal "Hoy"). */
    public void mostrarHome() {
        // Inicio es una raíz: se descarta cualquier pantalla secundaria abierta.
        limpiarBackStack();
        layoutToolbar.setVisibility(View.VISIBLE);
        mostrarNavegacionInferior();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
        bottomNavigation.setSelectedItemId(R.id.navigation_inicio);
    }

    /** Muestra el fragment de Login SIN toolbar ni bottom nav. */
    public void mostrarLogin() {
        limpiarBackStack();
        layoutToolbar.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);
        ocultarIslaEntrenamiento();
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
        restaurarControlVolver();
        btnVolverToolbar.setVisibility(View.GONE);
        ocultarAccionSecundariaToolbar();
        btnAccionToolbar.setVisibility(View.GONE);
        btnAccionToolbar.setOnClickListener(null);
    }

    /** Configura la toolbar para una pantalla secundaria (título, volver opcional y acción opcional).
     *  La acción debe configurarse con {@link #setAccionToolbar(Runnable)}.
     *  Si la pantalla necesita además una segunda acción, se llama después a
     *  {@link #mostrarAccionSecundariaToolbar(String)}. */
    public void mostrarToolbarSecundaria(String titulo, boolean conVolver, String textoAccion) {
        layoutToolbar.setVisibility(View.VISIBLE);
        tvToolbarTituloMain.setText(titulo);
        restaurarControlVolver();
        btnVolverToolbar.setVisibility(conVolver ? View.VISIBLE : View.GONE);
        ocultarAccionSecundariaToolbar();
        btnAccionToolbar.setOnClickListener(null);
        if (textoAccion != null) {
            btnAccionToolbar.setVisibility(View.VISIBLE);
            btnAccionToolbar.setText(textoAccion);
        } else {
            btnAccionToolbar.setVisibility(View.GONE);
        }
    }

    /**
     * Muestra el segundo botón de acción, a la izquierda del principal, para las pantallas
     * que necesitan dos acciones (por ejemplo, Crear rutina: crear ejercicio y guardar).
     * También ensancha el espacio del título para que no quede debajo de los dos botones.
     *
     * @param textoAccion texto del botón; no puede ser null porque el botón siempre se muestra.
     */
    public void mostrarAccionSecundariaToolbar(String textoAccion) {
        int espacioLado = getResources().getDimensionPixelSize(R.dimen.toolbar_side_reserved);
        int espacioDerecha = getResources()
                .getDimensionPixelSize(R.dimen.toolbar_side_reserved_doble_accion);

        tvToolbarTituloMain.setPadding(espacioLado, tvToolbarTituloMain.getPaddingTop(),
                espacioDerecha, tvToolbarTituloMain.getPaddingBottom());

        btnAccionToolbarSecundario.setText(textoAccion);
        btnAccionToolbarSecundario.setVisibility(View.VISIBLE);
    }

    /** Oculta el segundo botón de acción y devuelve al título su espacio habitual. */
    public void ocultarAccionSecundariaToolbar() {
        int espacioLado = getResources().getDimensionPixelSize(R.dimen.toolbar_side_reserved);

        tvToolbarTituloMain.setPadding(espacioLado, tvToolbarTituloMain.getPaddingTop(),
                espacioLado, tvToolbarTituloMain.getPaddingBottom());

        btnAccionToolbarSecundario.setVisibility(View.GONE);
        btnAccionToolbarSecundario.setOnClickListener(null);
    }

    /** Asigna la acción del segundo botón de la toolbar. */
    public void setAccionSecundariaToolbar(Runnable accion) {
        btnAccionToolbarSecundario.setOnClickListener(v -> accion.run());
    }

    /**
     * Sustituye temporalmente la flecha de la toolbar por el chevron hacia abajo, que
     * minimiza el entrenamiento en lugar de volver a la pantalla anterior.
     * Cualquier otra configuración de toolbar vuelve a poner la flecha normal.
     *
     * @param accion acción a ejecutar cuando el usuario pulse el chevron.
     */
    public void mostrarControlMinimizarEntrenamiento(Runnable accion) {
        btnVolverToolbar.setVisibility(View.VISIBLE);
        btnVolverToolbar.setImageResource(R.drawable.ic_expand_more);
        btnVolverToolbar.setContentDescription(getString(R.string.cdMinimizarEntrenamiento));
        btnVolverToolbar.setOnClickListener(v -> accion.run());
    }

    /** Deja el control izquierdo de la toolbar como la flecha de volver habitual. */
    private void restaurarControlVolver() {
        btnVolverToolbar.setImageResource(R.drawable.ic_arrow_back);
        btnVolverToolbar.setContentDescription(getString(R.string.cdVolver));
        btnVolverToolbar.setOnClickListener(v -> onBackPressed());
    }

    /** Asigna la acción del botón derecho de la toolbar. */
    public void setAccionToolbar(Runnable accion) {
        btnAccionToolbar.setOnClickListener(v -> accion.run());
    }

    /** Oculta la navegación inferior, por ejemplo durante un entrenamiento en curso. */
    public void ocultarNavegacionInferior() {
        bottomNavigation.setVisibility(View.GONE);
    }

    /**
     * Oculta la barra de herramientas, por ejemplo en el login, que se muestra sin barra
     * y la necesita ocultar de nuevo cuando se vuelve desde una pantalla secundaria.
     */
    public void ocultarToolbar() {
        layoutToolbar.setVisibility(View.GONE);
    }

    /** Muestra la navegación inferior. */
    public void mostrarNavegacionInferior() {
        bottomNavigation.setVisibility(View.VISIBLE);
    }

    /**
     * Navega al selector de ejercicios (con retroceso).
     * Se usa desde la creación de rutinas para elegir un ejercicio y también desde el
     * entrenamiento en curso para agregar ejercicios a la sesión.
     */
    public void mostrarSelectorEjercicios() {
        cargarFragmentConBackStack(new EjerciciosFragment());
    }

    /**
     * Abre la pantalla de entrenamiento activo.
     *
     * <p>Si todavía no hay sesión en curso se crea una con los datos recibidos. Si ya existe
     * una sesión (por ejemplo, si el usuario la minimizó y volvió a home) primero se le
     * pregunta si quiere descartarla, reanudarla o cancelar, porque empezar otro
     * entrenamiento reemplazaría lo que ya registró.
     *
     * <p>Solo se entregan datos simples: el nombre de la rutina y los nombres de sus
     * ejercicios, porque los modelos de rutinas no implementan Parcelable ni Serializable.
     *
     * @param nombreRutina nombre del plan que se está entrenando.
     * @param nombresEjercicios nombres de los ejercicios, uno por cada ejercicio de la sesión.
     */
    public void mostrarEntrenamientoActivo(String nombreRutina, String[] nombresEjercicios) {
        pedirConfirmacionSiHayEntrenamientoEnCurso(() -> {
            if (entrenamientoEnCurso == null) {
                iniciarEntrenamientoEnCurso(nombreRutina, nombresEjercicios);
            }

            abrirEntrenamientoEnCurso();
        });
    }

    /**
     * Crea la sesión de entrenamiento con los ejercicios recibidos. Solo se usa cuando todavía
     * no hay ninguna: si el usuario ya minimizó una sesión, esa misma se reabre.
     *
     * @param nombreRutina      nombre de la rutina que se va a entrenar.
     * @param nombresEjercicios nombres de los ejercicios, uno por cada ejercicio de la sesión.
     */
    private void iniciarEntrenamientoEnCurso(String nombreRutina, String[] nombresEjercicios) {
        // Son dos referencias de tiempo distintas: elElapsedRealtime sirve para medir la
        // duración aunque cambie la hora del dispositivo, y el currentTimeMillis guarda el
        // día y la hora reales en que empezó la sesión, que luego muestra el resumen.
        entrenamientoEnCurso = new EntrenamientoEnCurso(nombreRutina,
                SystemClock.elapsedRealtime(), System.currentTimeMillis());

        for (String nombreEjercicio : nombresEjercicios) {
            // TODO: Crear cada ejercicio con su identificador, grupo muscular y series reales,
            // incluyendo los pesos y repeticiones objetivo definidos en la rutina.
            entrenamientoEnCurso.agregarEjercicio(new EjercicioEntrenamiento(nombreEjercicio, ""));
        }

        // TODO: Persistir inmediatamente la nueva sesión para poder recuperarla si Android
        // cierra el proceso antes de que el usuario complete la primera serie.
    }

    /** @return la sesión de entrenamiento en curso, o null si no hay ninguna. */
    public EntrenamientoEnCurso obtenerEntrenamientoEnCurso() {
        return entrenamientoEnCurso;
    }

    /** @return true si hay un entrenamiento en curso que se puede recuperar. */
    public boolean hayEntrenamientoEnCurso() {
        return entrenamientoEnCurso != null && entrenamientoEnCurso.isActiva();
    }

    /**
     * Abre el entrenamiento en curso reutilizando el estado guardado: mismos ejercicios,
     * mismas series y mismo cronómetro. No crea una sesión nueva.
     */
    private void abrirEntrenamientoEnCurso() {
        if (!hayEntrenamientoEnCurso()) {
            return;
        }

        cargarFragmentConBackStack(new EntrenamientoActivoFragment());
        ocultarIslaEntrenamiento();
        ocultarNavegacionInferior();
    }

    /**
     * Minimiza el entrenamiento sin terminarlo ni descartarlo: la sesión se conserva y la
     * isla permite recuperarla. Es la acción del chevron hacia abajo de la toolbar
     * y del botón físico de retroceso.
     */
    public void minimizarEntrenamiento() {
        if (!hayEntrenamientoEnCurso()) {
            return;
        }

        // Se sale de la pantalla del entrenamiento; si no hay nada en la pila se vuelve a Inicio.
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            mostrarHome();
        }

        mostrarNavegacionInferior();
        mostrarIslaEntrenamiento();
    }

    /** Cierra la sesión: la usan tanto "Terminar" como "Descartar". */
    public void cerrarEntrenamientoEnCurso() {
        descartarEntrenamientoEnCurso();

        if (!getSupportFragmentManager().popBackStackImmediate()) {
            mostrarHome();
        }
        mostrarNavegacionInferior();
    }

    /**
     * Abre el resumen de un entrenamiento terminado. Lo usan tanto el entrenamiento en
     * curso al confirmar "Terminar" como Inicio al pulsar un entrenamiento reciente.
     *
     * <p>La sesión en curso se borra antes de mostrar el resumen, y la pantalla del
     * entrenamiento sale de la pila, así la flecha hacia atrás nunca devuelve a una sesión
     * finalizada: se recupera la pantalla desde la que se empezó a entrenar.
     *
     * @param resumen copia de solo lectura del entrenamiento terminado.
     */
    public void mostrarResumenEntrenamiento(ResumenEntrenamiento resumen) {
        // TODO: Reemplazar esta referencia en memoria por el historial guardado cuando exista.
        resumenEntrenamientoActual = resumen;

        descartarEntrenamientoEnCurso();
        mostrarNavegacionInferior();

        getSupportFragmentManager().popBackStackImmediate();
        cargarFragmentConBackStack(new ResumenEntrenamientoFragment());
    }

    /**
     * @return el resumen del entrenamiento que se está mostrando, o null si no hay ninguno.
     */
    public ResumenEntrenamiento obtenerResumenEntrenamientoActual() {
        return resumenEntrenamientoActual;
    }

    /**
     * Olvida el resumen que se está mostrando.
     * TODO: Se usará para dejar de depender de la referencia en memoria cuando exista
     *       el historial guardado; por ahora nadie la llama.
     */
    public void limpiarResumenEntrenamientoActual() {
        resumenEntrenamientoActual = null;
    }

    /**
     * Elimina la sesión en curso: detiene su descanso, borra el estado y oculta la isla.
     * La usan el descarte de la isla y el cierre completo de la sesión.
     */
    private void descartarEntrenamientoEnCurso() {
        ocultarIslaEntrenamiento();

        if (entrenamientoEnCurso != null) {
            entrenamientoEnCurso.detenerDescanso();
            entrenamientoEnCurso.finalizar();
        }
        entrenamientoEnCurso = null;
    }

    /** Muestra la isla con el tiempo del entrenamiento y arranca su reloj. */
    private void mostrarIslaEntrenamiento() {
        if (!hayEntrenamientoEnCurso()) {
            ocultarIslaEntrenamiento();
            return;
        }

        layoutIslaEntrenamiento.setVisibility(View.VISIBLE);
        detenerRelojIsla();
        handlerIsla.post(runnableTiempoIsla);
    }

    /**
     * Oculta la isla y detiene su reloj. La usan el cierre de la sesión y las pantallas
     * que se abren con un entrenamiento ya terminado, como el resumen.
     */
    public void ocultarIslaEntrenamiento() {
        detenerRelojIsla();
        layoutIslaEntrenamiento.setVisibility(View.GONE);
    }

    /**
     * Refresca el tiempo de la isla. Usa el instante de inicio guardado en la sesión:
     * no hay un segundo cronómetro corriendo al lado del del fragment.
     */
    private void actualizarTiempoIsla() {
        if (entrenamientoEnCurso == null || layoutIslaEntrenamiento.getVisibility() != View.VISIBLE) {
            return;
        }

        tvTiempoEntrenamientoMinimizado.setText(entrenamientoEnCurso.getTiempoTranscurrido());
        handlerIsla.postDelayed(runnableTiempoIsla, 1000);
    }

    private void detenerRelojIsla() {
        handlerIsla.removeCallbacks(runnableTiempoIsla);
    }

    /** Pide confirmación antes de descartar la sesión desde la isla. */
    private void confirmarDescarteEntrenamientoMinimizado() {
        if (!hayEntrenamientoEnCurso()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.tvTituloDescartarEntrenamientoMinimizado)
                .setMessage(R.string.tvMensajeDescartarEntrenamientoMinimizado)
                .setPositiveButton(R.string.btnConfirmarDescartarEntrenamientoMinimizado,
                        (dialogo, cual) -> cerrarEntrenamientoEnCurso())
                .setNegativeButton(R.string.btnCancelarDescartarEntrenamientoMinimizado, null)
                .show();
    }

    /**
     * Ejecuta una acción que reemplazaría al entrenamiento en curso, pero antes le pregunta
     * al usuario qué quiere hacer si todavía tiene una sesión abierta:
     * - Descartar: borra la sesión actual y sigue con la acción.
     * - Reanudar: vuelve a la pantalla del entrenamiento actual y no ejecuta la acción.
     * - Cancelar: no hace nada.
     *
     * @param accionContinuar acción a ejecutar cuando no hay entrenamiento en curso, o cuando
     *                        el usuario acepta descartar el que tenía.
     */
    private void pedirConfirmacionSiHayEntrenamientoEnCurso(Runnable accionContinuar) {
        if (!hayEntrenamientoEnCurso()) {
            accionContinuar.run();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.tvTituloReemplazarEntrenamiento)
                .setMessage(getString(R.string.tvMensajeReemplazarEntrenamiento,
                        entrenamientoEnCurso.getTiempoTranscurrido()))
                .setPositiveButton(R.string.btnDescartarYContinuar, (dialogo, cual) -> {
                    // Se borra la sesión anterior para que la acción parta desde cero.
                    descartarEntrenamientoEnCurso();
                    accionContinuar.run();
                })
                .setNeutralButton(R.string.btnReanudarEntrenamientoActual,
                        (dialogo, cual) -> abrirEntrenamientoEnCurso())
                .setNegativeButton(R.string.btnCancelarReemplazarEntrenamiento, null)
                .show();
    }

    /** Navega a la pantalla de crear una rutina (con retroceso). */
    public void mostrarCrearRutina() {
        pedirConfirmacionSiHayEntrenamientoEnCurso(() ->
                cargarFragmentConBackStack(new CrearRutinaFragment()));
    }

    /** Navega a la pantalla de crear ejercicio (con retroceso). */
    public void mostrarCrearEjercicio() {
        cargarFragmentConBackStack(new CrearEjercicioFragment());
    }

    /** Navega a la pantalla de crear una cuenta nueva (con retroceso). */
    public void mostrarCrearCuenta() {
        cargarFragmentConBackStack(new CrearCuentaFragment());
    }

    /** Navega a la pantalla de cambiar la contraseña (con retroceso). */
    public void mostrarCambiarContrasena() {
        cargarFragmentConBackStack(new CambiarContrasenaFragment());
    }

    /** Retrocede a la pantalla anterior si hay una en la pila. */
    public void regresar() {
        onBackPressed();
    }

    /**
     * Cierra la sesión activa y vuelve al login.
     * Solo se borran los datos que representan la sesión; el nombre y la foto del perfil
     * se conservan en sus propias preferencias.
     */
    public void cerrarSesion() {
        // TODO: Eliminar también el token / credenciales cuando exista autenticación real.
        getSharedPreferences("sesion", MODE_PRIVATE).edit().clear().apply();
        // Un entrenamiento en curso no puede sobrevivir al cierre de sesión.
        descartarEntrenamientoEnCurso();
        limpiarResumenEntrenamientoActual();
        limpiarBackStack();
        mostrarLogin();
    }


}
