package ue.edu.co.fittrackandroid;

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

import java.util.ArrayList;
import java.util.List;

import ue.edu.co.fittrackandroid.ejercicios.vista.CrearEjercicioFragment;
import ue.edu.co.fittrackandroid.ejercicios.vista.EjerciciosFragment;
import ue.edu.co.fittrackandroid.ejercicios.vista.ModificarEjercicioFragment;
import ue.edu.co.fittrackandroid.entrenamiento.datos.local.EntrenamientoBorradorRepository;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EjercicioEntrenamiento;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoEnCurso;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.SerieEntrenamiento;
import ue.edu.co.fittrackandroid.entrenamiento.vista.EntrenamientoActivoFragment;
import ue.edu.co.fittrackandroid.hoy.modelo.EjercisioEnRutina;
import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.hoy.vista.HomeFragment;
import ue.edu.co.fittrackandroid.login.vista.LoginFragment;
import ue.edu.co.fittrackandroid.perfil.datos.FotoPerfilLocal;
import ue.edu.co.fittrackandroid.perfil.vista.CambiarContrasenaFragment;
import ue.edu.co.fittrackandroid.perfil.vista.PerfilFragment;

import ue.edu.co.fittrackandroid.registro.vista.CrearCuentaFragment;
import ue.edu.co.fittrackandroid.remote.SesionManager;
import ue.edu.co.fittrackandroid.resumen.modelo.ResumenEntrenamiento;
import ue.edu.co.fittrackandroid.resumen.vista.ResumenEntrenamientoFragment;
import ue.edu.co.fittrackandroid.rutinas.vista.CrearRutinaFragment;
import ue.edu.co.fittrackandroid.rutinas.vista.ModificarRutinaFragment;
import ue.edu.co.fittrackandroid.rutinas.vista.RutinasFragment;
import ue.edu.co.fittrackandroid.utils.SerieRutina;

/**
 * Activity principal (única). Aloja la toolbar, el contenedor de fragments y la navegación
 * inferior, y decide qué pantalla se muestra al abrir la aplicación.
 * - Al iniciar por primera vez, o si no hay sesión → LoginFragment
 * - Al iniciar con sesión guardada → HomeFragment
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

    /**
     * Identificador del entrenamiento guardado que se está mostrando en el resumen.
     * Sirve para consultar el detalle cuando el resumen no viene ya construido,
     * por ejemplo al abrir un registro de "Últimos entrenamientos", y para borrarlo
     * con {@code DELETE /entrenamientos/{id}} desde la pantalla del resumen.
     */
    private Long idEntrenamientoResumenActual;

    /** Reloj de la isla del entrenamiento minimizado. */
    private final Handler handlerIsla = new Handler(Looper.getMainLooper());

    /**
     * Datos de la sesión del usuario (token, nombre y correo). Se usa sobre todo para
     * saber a quién pertenece el borrador local del entrenamiento en curso.
     */
    private SesionManager sesionManager;

    /** Guarda y recupera el borrador del entrenamiento en curso en la base de datos local. */
    private EntrenamientoBorradorRepository borradorRepository;

    /** Evita pedir dos veces la misma recuperación del borrador, por ejemplo al iniciar sesión. */
    private boolean recuperandoBorrador;

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
            mostrarLogin();
        } else {
            // Al girar la pantalla se reconstruye la Activity con la pila de fragments
            // guardada, así que también puede haber un entrenamiento en curso que recuperar.
            recuperarEntrenamientoEnCursoGuardado();
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
            // Ninguna pestaña es el entrenamiento activo, así que la isla puede volver a verse.
            mostrarIslaEntrenamiento();
            return true;
        });
    }

    private void initObjects() {
        sesionManager = new SesionManager(this);
        borradorRepository = new EntrenamientoBorradorRepository(this);
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

    /** Muestra el fragment de Home (pantalla principal "Hoy"). */
    public void mostrarHome() {
        limpiarBackStack();
        layoutToolbar.setVisibility(View.VISIBLE);
        mostrarNavegacionInferior();
        if (bottomNavigation.getSelectedItemId() == R.id.navigation_inicio) {
            cargarFragment(new HomeFragment());
        } else {
            bottomNavigation.setSelectedItemId(R.id.navigation_inicio);
        }

        // Al entrar de nuevo a la aplicación puede haber un entrenamiento que quedó abierto
        // antes de que Android cerrara el proceso: se recupera del borrador local.
        recuperarEntrenamientoEnCursoGuardado();
    }

    /**
     * Recupera de la base de datos local el entrenamiento que la cuenta tenía abierto.
     *
     * <p>La lectura se hace en segundo plano: cuando llega, si el usuario no alcanzó a
     * empezar otra sesión, la sesión recuperada vuelve a ser la que MainActivity conserva y
     * la isla del entrenamiento minimizado vuelve a aparecer. Si no había borrador, no se
     * hace nada y la aplicación sigue igual.
     */
    private void recuperarEntrenamientoEnCursoGuardado() {
        if (hayEntrenamientoEnCurso() || recuperandoBorrador) {
            return;
        }

        recuperandoBorrador = true;
        borradorRepository.cargarBorradorActivo(
                sesionManager.obtenerCorreo(),
                borrador -> {
                    recuperandoBorrador = false;

                    if (isDestroyed() || borrador == null) {
                        return;
                    }

                    // Si el usuario alcanzó a empezar otra sesión mientras se leía el
                    // borrador, la nueva sesión es la que manda.
                    if (entrenamientoEnCurso != null) {
                        return;
                    }

                    entrenamientoEnCurso = borrador;
                    refrescarIslaSegunPantallaVisible();
                }
        );
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
            fm.popBackStackImmediate();
            refrescarIslaSegunPantallaVisible();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Vuelve a mostrar la isla del entrenamiento minimizado cuando la pantalla que quedó
     * visible no es el entrenamiento activo.
     *
     * <p>Hace falta porque las pantallas de solo lectura, como el resumen, ocultan la isla.
     * Si el usuario todavía tiene una sesión abierta, al regresar a Inicio la isla debe
     * reaparecer con su cronómetro intacto. La decisión vive aquí para no repetirla en
     * cada pantalla. Tampoco se muestra en el login, que se dibuja sin isla.
     */
    private void refrescarIslaSegunPantallaVisible() {
        Fragment fragmentVisible = getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);

        if (fragmentVisible instanceof EntrenamientoActivoFragment
                || fragmentVisible instanceof LoginFragment) {
            ocultarIslaEntrenamiento();
            return;
        }

        mostrarIslaEntrenamiento();
    }

    /** Restaura la toolbar principal: solo título "FitTrack", sin volver ni acción. */
    public void mostrarToolbarPrincipal() {
        layoutToolbar.setVisibility(View.VISIBLE);
        tvToolbarTituloMain.setText(R.string.tvToolbarTituloMain);
        restaurarControlVolver();
        btnVolverToolbar.setVisibility(View.GONE);
        ocultarAccionSecundariaToolbar();
        btnAccionToolbar.setEnabled(true);
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
        btnAccionToolbar.setEnabled(true);
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

    /** Habilita o bloquea temporalmente la acción principal de la toolbar. */
    public void habilitarAccionToolbar(boolean habilitada) {
        btnAccionToolbar.setEnabled(habilitada);
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

    /** Navega al selector de ejercicios utilizado durante la creación de rutinas. */
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
     * <p>La respuesta completa se convierte al modelo editable antes de abrir el Fragment,
     * por lo que la pantalla recibe los identificadores, ejercicios y series ya preparados.
     *
     * @param rutinaResponse detalle completo de la rutina seleccionada
     */
    public void mostrarEntrenamientoActivo(RutinaResponse rutinaResponse) {
        pedirConfirmacionSiHayEntrenamientoEnCurso(() -> {
            if (entrenamientoEnCurso == null) {
                iniciarEntrenamientoEnCurso(rutinaResponse);
            }

            abrirEntrenamientoEnCurso();
        });
    }

    /**
     * Inicia una nueva sesión de entrenamiento a partir de la rutina seleccionada.
     *
     * <p>La rutina llega con la estructura de la API: cada ejercicio guarda su lista de series como
     * {@link SerieRutina}. La pantalla de entrenamiento activo, en cambio, trabaja con
     * {@link EjercicioEntrenamiento} y {@link SerieEntrenamiento}. Por eso esta conversión toma la
     * lista de ejercicios y la transforma a la estructura interna del entrenamiento, manteniendo el
     * nombre, el identificador y los valores reales de peso y repeticiones cuando existen.</p>
     *
     * <p>Si una rutina no trae series definidas, se crea una serie vacía automáticamente para que el
     * ejercicio pueda abrirse y el usuario pueda completar la primera serie sin errores de UI ni
     * valores nulos.</p>
     */
    private void iniciarEntrenamientoEnCurso(RutinaResponse rutinaResponse) {
        entrenamientoEnCurso = new EntrenamientoEnCurso(
                rutinaResponse.getId(),
                rutinaResponse.getNombre(),
                SystemClock.elapsedRealtime(),
                System.currentTimeMillis()
        );

        if (rutinaResponse.getEjercicios() == null) {
            return;
        }

        for (EjercisioEnRutina ejercicio : rutinaResponse.getEjercicios()) {
            List<SerieEntrenamiento> seriesEntrenamiento = new ArrayList<>();

            if (ejercicio.getSeries() != null) {
                for (SerieRutina serieRutina : ejercicio.getSeries()) {
                    int repeticiones = serieRutina.getRepeticionesObjetivo();
                    double peso = serieRutina.getPesoObjetivo();

                    int numeroSerie = serieRutina.getNumeroSerie();
                    if (numeroSerie <= 0) {
                        numeroSerie = seriesEntrenamiento.size() + 1;
                    }

                    seriesEntrenamiento.add(new SerieEntrenamiento(
                            numeroSerie,
                            peso,
                            repeticiones
                    ));
                }
            }

            EjercicioEntrenamiento ejercicioEntrenamiento = new EjercicioEntrenamiento(
                    ejercicio.getId(),
                    ejercicio.getNombre(),
                    ejercicio.getGrupoMuscular(),
                    seriesEntrenamiento
            );

            if (ejercicioEntrenamiento.getSeries().isEmpty()) {
                ejercicioEntrenamiento.agregarSerie();
            }

            entrenamientoEnCurso.agregarEjercicio(ejercicioEntrenamiento);
        }

        // La sesión se guarda de una vez, con sus ejercicios y series, para poder recuperarla
        // si Android cierra el proceso antes de que el usuario complete la primera serie.
        borradorRepository.guardarBorradorInicial(
                sesionManager.obtenerCorreo(),
                entrenamientoEnCurso
        );
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
     * Abre el resumen de la sesión activa después de guardarla correctamente.
     *
     * <p>La sesión en curso se borra antes de mostrar el resumen, y la pantalla del
     * entrenamiento sale de la pila, así la flecha hacia atrás nunca devuelve a una sesión
     * finalizada: se recupera la pantalla desde la que se empezó a entrenar.
     *
     * <p>El identificador que devuelve el backend también se guarda aquí: es el único dato
     * que permite borrar el entrenamiento recién creado, porque todavía no existe historial
     * en disco del que recuperarlo.
     *
     * @param idEntrenamiento identificador del entrenamiento guardado en el backend.
     * @param resumen         copia de solo lectura del entrenamiento terminado.
     */
    public void mostrarResumenEntrenamiento(Long idEntrenamiento,
                                            ResumenEntrenamiento resumen) {
        idEntrenamientoResumenActual = idEntrenamiento;
        resumenEntrenamientoActual = resumen;

        descartarEntrenamientoEnCurso();
        mostrarNavegacionInferior();

        getSupportFragmentManager().popBackStackImmediate();
        cargarFragmentConBackStack(new ResumenEntrenamientoFragment());
    }

    /**
     * Abre el resumen de un entrenamiento que ya está guardado en el historial.
     *
     * <p>A diferencia de {@link #mostrarResumenEntrenamiento(Long, ResumenEntrenamiento)},
     * esta pantalla no borra nada: consultar el pasado nunca puede terminar, descartar ni
     * pausear el entrenamiento que el usuario puede tener abierto. Si el resumen todavía
     * no está construido se guarda el identificador para que la propia pantalla del
     * resumen consulte el detalle del entrenamiento.
     *
     * @param idEntrenamiento identificador del entrenamiento guardado.
     * @param resumen        resumen ya construido, o null si hay que consultarlo.
     */
    public void mostrarResumenEntrenamientoHistorial(Long idEntrenamiento,
                                                     ResumenEntrenamiento resumen) {
        idEntrenamientoResumenActual = idEntrenamiento;
        resumenEntrenamientoActual = resumen;

        mostrarNavegacionInferior();
        cargarFragmentConBackStack(new ResumenEntrenamientoFragment());
    }

    /**
     * @return el resumen del entrenamiento que se está mostrando, o null si no hay ninguno.
     */
    public ResumenEntrenamiento obtenerResumenEntrenamientoActual() {
        return resumenEntrenamientoActual;
    }

    /**
     * @return el identificador del entrenamiento guardado que se está mostrando,
     *         o null si el resumen actual no viene del historial.
     */
    public Long obtenerIdEntrenamientoResumenActual() {
        return idEntrenamientoResumenActual;
    }

    /**
     * Olvida el resumen que se está mostrando.
     * La usa el cierre de sesión y la pantalla del resumen cuando el entrenamiento
     * ya fue borrado en el backend, para que no quede en memoria un registro que
     * dejó de existir.
     * TODO: Reemplazar por el entrenamiento leído del almacenamiento cuando exista
     *       el historial guardado.
     */
    public void limpiarResumenEntrenamientoActual() {
        resumenEntrenamientoActual = null;
        idEntrenamientoResumenActual = null;
    }

    /**
     * Elimina la sesión en curso: detiene su descanso, borra el estado y oculta la isla.
     * La usan el descarte de la isla y el cierre completo de la sesión.
     */
    private void descartarEntrenamientoEnCurso() {
        descartarEntrenamientoEnCurso(sesionManager.obtenerCorreo());
    }

    /**
     * Elimina la sesión en curso y con ella el borrador local de la cuenta.
     *
     * <p>El borrador se borra por correo, y no por identificador de fila, para que también
     * desaparezca cuando la sesión ya no está en memoria, como pasa al cerrar sesión. Así el
     * borrador de una cuenta nunca se confunde con el de otra.
     *
     * <p>Lo usan las tres salidas posibles: terminar y guardar en el backend, descartar el
     * entrenamiento y cerrar sesión.
     *
     * @param correoUsuario cuenta cuyo borrador local se borra.
     */
    private void descartarEntrenamientoEnCurso(String correoUsuario) {
        ocultarIslaEntrenamiento();

        if (entrenamientoEnCurso != null) {
            entrenamientoEnCurso.detenerDescanso();
            entrenamientoEnCurso.finalizar();
        }
        entrenamientoEnCurso = null;

        borradorRepository.eliminarBorrador(correoUsuario);
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

    /**
     * Navega a la pantalla que permite consultar, modificar o borrar una rutina existente.
     * A diferencia de crear rutina, no interrumpe un entrenamiento en curso: editar la
     * rutina no reemplaza la sesión que el usuario ya tiene abierta.
     *
     * @param rutinaId identificador de la rutina elegida en la lista.
     */
    public void mostrarModificarRutina(Long rutinaId) {
        cargarFragmentConBackStack(ModificarRutinaFragment.newInstance(rutinaId));
    }

    /** Navega a la pantalla de crear ejercicio (con retroceso). */
    public void mostrarCrearEjercicio() {
        cargarFragmentConBackStack(new CrearEjercicioFragment());
    }

    /**
     * Navega a la pantalla que permite consultar, modificar o borrar un ejercicio propio.
     * Solo se pasa el identificador: la pantalla consulta el detalle al backend para no
     * trabajar con datos que ya pudieron quedar viejos en la lista del perfil.
     *
     * @param ejercicioId identificador del ejercicio elegido en la lista del perfil.
     */
    public void mostrarModificarEjercicio(Long ejercicioId) {
        cargarFragmentConBackStack(ModificarEjercicioFragment.newInstance(ejercicioId));
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
     * Se borran el token y los demás datos asociados a la sesión autenticada.
     */
    public void cerrarSesion() {
        // El correo se guarda antes de cerrar la sesión porque es lo que identifica el
        // borrador local que hay que borrar.
        String correoUsuario = sesionManager.obtenerCorreo();
        Long usuarioId = sesionManager.obtenerUsuarioId();
        new FotoPerfilLocal(this).eliminar(usuarioId);
        sesionManager.cerrarSesion();
        // Un entrenamiento en curso no puede sobrevivir al cierre de sesión.
        descartarEntrenamientoEnCurso(correoUsuario);
        limpiarResumenEntrenamientoActual();
        limpiarBackStack();
        mostrarLogin();
    }


}
