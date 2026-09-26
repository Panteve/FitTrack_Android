# Plan para OpenCode — Fragment de Rutinas

## Objetivo

Crear la pantalla de rutinas como un `Fragment` en Java y XML tradicional, tomando como referencia visual `stitch_neobrutalist_gym_tracker/rutinas_android_primitivo_universidad/screen.png`.

OpenCode debe construir solamente el contenido de la sección Rutinas. La toolbar superior, la barra de estado y la navegación inferior ya pertenecen a `MainActivity` y no deben aparecer dentro del layout del Fragment.

## Cambio solicitado respecto al mockup

El mockup original muestra, en este orden:

1. Botón morado `EMPEZAR RUTINA VACÍA`.
2. Buscador blanco.
3. Cabecera `MIS PLANES DE ENTRENAMIENTO`.
4. Tres tarjetas de rutinas.
5. Botón morado `NUEVA RUTINA` al final de la lista.

La implementación debe cambiarlo así:

1. Colocar arriba el botón `NUEVA RUTINA` como acción principal de la pantalla.
2. Colocar inmediatamente debajo el botón `EMPEZAR RUTINA VACÍA` como acción secundaria.
3. Eliminar por completo el buscador, incluido su icono de lupa, texto y botón de filtros. El bloque de los dos botones ocupa la zona superior donde estaban el botón principal y el buscador.
4. No repetir `NUEVA RUTINA` al final de la lista.
5. Debajo de los dos botones comienza la cabecera y la lista de rutinas.

## Alcance técnico

Crear dentro del feature `rutinas`:

- `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/RutinasFragment.java`
- `app/src/main/res/layout/fragment_rutinas.xml`

Modificar únicamente lo necesario en:

- `app/src/main/res/values/strings.xml`
- Recursos visuales (`drawable`, `colors.xml` o `dimens.xml`) solo si un recurso equivalente no existe.
- `MainActivity.java`, únicamente para devolver `new RutinasFragment()` cuando se pulse `navigation_rutinas` y para actualizar el título de la toolbar si corresponde.

No crear otra Activity, no registrar otra Activity en el manifest, no agregar librerías y no usar Jetpack Compose. No modificar la toolbar ni la navegación inferior como parte del diseño del Fragment.

## Estructura visual del Fragment

Usar como raíz un contenedor que ocupe todo el espacio disponible del `fragmentContainer`, con fondo `@color/colorBackground`. Dentro, usar un `ScrollView` vertical sin barra visible y un `LinearLayout` vertical con aproximadamente 14–16 dp de padding lateral y superior.

El contenido debe quedar en este orden. No colocar los botones uno al lado del otro porque sus textos son largos y quedarían apretados en teléfonos pequeños.

### 1. Botón Nueva rutina

- Ancho completo.
- Altura aproximada de 48 dp.
- Fondo morado `@color/colorActionPrimary` o el recurso existente `@drawable/bg_button_primary`.
- Texto blanco, centrado, en mayúsculas: `NUEVA RUTINA`.
- Elevación leve y esquinas pequeñas, coherentes con los botones actuales.
- Puede usar `@drawable/ic_add` como `drawableStart`, con tinte blanco, si se conserva una separación equilibrada.
- Id sugerido: `btnNuevaRutina`.

### 2. Botón Empezar rutina vacía

- Ubicarlo justo debajo del botón anterior, con margen superior de 12–16 dp.
- Ancho completo y altura aproximada de 48 dp.
- Aplicar una jerarquía secundaria: fondo gris claro o blanco, texto oscuro y borde gris sutil. Reutilizar `@drawable/bg_button_neutral` o `FitTrack.Button.Secondary` si el resultado coincide con la guía visual.
- Texto centrado, en mayúsculas: `EMPEZAR RUTINA VACÍA`.
- No usar el mismo fondo morado del botón principal, porque las dos acciones no deben competir visualmente.
- Id sugerido: `btnEmpezarRutinaVacia`.
- Este botón completa el bloque que reemplaza al buscador; no debe quedar ningún campo de búsqueda.

### 3. Cabecera de planes

- Margen superior aproximado de 18–24 dp.
- Fila horizontal compacta.
- A la izquierda, texto negro y en negrita: `MIS PLANES DE ENTRENAMIENTO`.
- A continuación, badge teal con texto blanco: `3 ACTIVOS`.
- Al extremo derecho, un icono de ordenamiento vertical o un símbolo sencillo equivalente usando un drawable local; no usar texto hardcodeado para simularlo.
- Ids sugeridos: `tvTituloPlanes`, `tvCantidadPlanes`, `imgOrdenarPlanes`.

### 4. Lista de tarjetas

Debajo de la cabecera mostrar tres tarjetas blancas, una por rutina, separadas por 14–16 dp. Las tarjetas deben tener borde gris fino, elevación ligera, esquinas de 2–3 dp y padding interno de 12 dp. Reutilizar `@drawable/bg_card` o `FitTrack.Container.Card` si produce este resultado.

Para esta entrega, implementar las tres tarjetas directamente dentro del layout y el contenido fijo en `strings.xml`. No agregar RecyclerView si eso exige incorporar una dependencia que el proyecto todavía no tiene. Mantener la solución sencilla y fácil de leer.

Cada tarjeta contiene:

1. Una cabecera horizontal.
   - Icono cuadrado de aproximadamente 48–52 dp a la izquierda, con fondo de color suave y un icono deportivo local.
   - Bloque central con nombre de rutina en negrita y una segunda línea gris con cantidad de ejercicios, un separador `•` y duración.
   - Menú de tres puntos verticales a la derecha.
2. Lista de ejercicios dentro de un rectángulo con borde gris claro.
   - Cada ejercicio es una fila horizontal de aproximadamente 40–44 dp.
   - Punto circular teal pequeño a la izquierda.
   - Nombre del ejercicio ocupando el espacio central.
   - Series y repeticiones alineadas a la derecha en gris.
   - Separador fino entre filas.
3. Separador horizontal y fila de acciones.
   - `VER DETALLES`: botón secundario gris.
   - `INICIAR`: botón morado con icono de reproducción blanco.
   - Ambos alineados a la derecha; `INICIAR` debe tener mayor contraste visual.

## Contenido exacto de las tarjetas

### Tarjeta 1

- Título: `Día A: Pecho y Tríceps`
- Resumen: `4 ejercicios  •  45 min`
- Ejercicios:
  - `Press de Banca Plano con Barra` — `4 × 8-10`
  - `Press Inclinado con Mancuernas` — `3 × 10`
  - `Fondos en Paralelas (Dips)` — `3 × 12`
  - `Extensión de Tríceps en Polea` — `4 × 12-15`

### Tarjeta 2

- Título: `Día B: Espalda y Bíceps`
- Resumen: `4 ejercicios  •  50 min`
- Ejercicios:
  - `Dominadas Pronas (Pull-ups)` — `4 × 6-8`
  - `Remo con Barra Pendlay` — `4 × 8`
  - `Jalón al Pecho Agarre Neutro` — `3 × 12`
  - `Curl Bíceps Barra Z` — `3 × 12`

### Tarjeta 3

- Título: `Día C: Pierna y Hombro`
- Resumen: `5 ejercicios  •  55 min`
- Ejercicios:
  - `Sentadilla Trasera Profunda` — `4 × 6`
  - `Prensa Inclinada 45°` — `3 × 12`
  - `Curl Femoral Tumbado` — `4 × 12`
  - `Press Militar con Mancuernas` — `4 × 8-10`
  - `Elevaciones Laterales` — `3 × 15`

## Recursos y nombres

- Todo texto visible debe declararse en `strings.xml`; no hardcodear texto en XML ni Java.
- El nombre de cada string debe coincidir con el id de la vista que lo usa. Para vistas repetidas, agregar un número o sufijo claro, por ejemplo `tvNombreRutina1`, `tvNombreEjercicio1_1` y `btnIniciarRutina1`.
- Usar ids con los prefijos definidos en `AGENTS.md`: `tv`, `btn`, `img`, `layout`, `card`, `sv`.
- Los separadores decorativos sin interacción pueden ser `View` sin id.
- Reutilizar primero los recursos existentes: colores de marca, escala de espaciado, `bg_card`, `bg_button_primary`, `bg_button_neutral`, `ic_play_arrow`, `ic_add`, `ic_fitness_center` e `ic_schedule`.
- Si hace falta un icono de tres puntos o de ordenamiento, crear un VectorDrawable XML sencillo; no usar emojis ni caracteres de texto como sustituto visual.

## Comportamiento mínimo

En `RutinasFragment.java`:

- Inflar `fragment_rutinas.xml` en `onCreateView`.
- Obtener referencias de los dos botones superiores y de los botones de cada tarjeta con nombres descriptivos.
- Para acciones todavía no implementadas, mostrar un `Toast` corto que indique la acción elegida. Los Toasts pueden llevar texto directo según `AGENTS.md`.
- Evitar lógica de negocio, modelos, adapters o arquitecturas adicionales mientras los datos sean estáticos.

En `MainActivity.java`:

- En `obtenerFragment`, hacer que `R.id.navigation_rutinas` devuelva `new RutinasFragment()`.
- Al seleccionar Rutinas, mantener visibles la toolbar y la navegación inferior porque pertenecen a la Activity.
- No llamar `bottomNavigation.setSelectedItemId(fragment.getId())`, porque el id interno del Fragment no equivale al id del menú. Mantener la selección a partir del item pulsado o asignar explícitamente `R.id.navigation_rutinas` cuando sea necesario.

## Apariencia esperada

La pantalla debe parecer una interfaz Android clásica, compacta y universitaria:

- Fondo gris muy claro.
- `NUEVA RUTINA` como único botón morado en el bloque superior.
- `EMPEZAR RUTINA VACÍA` con apariencia secundaria gris o blanca.
- Acentos teal en el badge, puntos de ejercicios e iconos secundarios.
- Tarjetas blancas con borde gris y poca elevación.
- Texto principal casi negro y texto auxiliar gris.
- Jerarquía clara, sin espacios excesivos ni componentes modernos complejos.

En una pantalla angosta, los nombres largos de ejercicios pueden usar una sola línea con elipsis para conservar visible la columna de series y repeticiones. Las tarjetas deben poder crecer verticalmente y todo el contenido debe ser accesible mediante scroll.

## Criterios de aceptación

- Existe `RutinasFragment` y su layout `fragment_rutinas.xml`.
- La navegación `Rutinas` de `MainActivity` abre el Fragment.
- No se creó ninguna Activity nueva.
- El Fragment no contiene toolbar, barra de estado ni navegación inferior.
- No existe buscador ni controles de búsqueda/filtro.
- `NUEVA RUTINA` está arriba como acción principal morada y no se repite al final.
- `EMPEZAR RUTINA VACÍA` está inmediatamente debajo con estilo secundario.
- Los dos botones ocupan el ancho disponible y no están colocados lado a lado.
- Se ven la cabecera de planes y las tres tarjetas con el contenido indicado.
- Todos los textos visibles están en `strings.xml` y sus nombres siguen la regla del id.
- Se reutilizan colores, dimensiones, estilos y drawables existentes antes de crear recursos nuevos.
- No se agregaron dependencias ni se usó Compose.
- No compilar el proyecto salvo que el usuario lo solicite; hacer una revisión estática de ids, imports, recursos y referencias.
