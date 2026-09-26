# Guía de Código — App de Gimnasio (Android)

Este archivo define cómo debe escribirse el código en este proyecto. Cualquier agente de IA (Claude, ChatGPT, OpenCode, etc.) que colabore en este repositorio debe seguir estas reglas antes de generar o modificar código.

## Contexto del proyecto

App de Android para gestionar rutinas de gimnasio (ejercicios, rutinas, progreso, etc.), escrita en Java con Vistas XML tradicionales (no Jetpack Compose). La aplicación usa una sola Activity principal y las pantallas se implementan con Fragments.

## 1. Nunca hardcodear texto

Todo texto visible para el usuario (labels, hints, mensajes, textos de botones, toasts, etc.) debe ir en `res/values/strings.xml`, nunca escrito directamente en el XML de layout ni en el código Java.

**Mal:**
```xml
<TextView android:text="Rutina de hoy" />
```

**Bien:**
```xml
<TextView
    android:id="@+id/tvRutinaDeHoy"
    android:text="@string/tvRutinaDeHoy" />
```

```xml
<!-- strings.xml -->
<string name="tvRutinaDeHoy">Rutina de hoy</string>
```

Esto aplica también a hints de EditText y a otros textos generados desde Java (usar `getString(R.string.xxx)` en vez de literales), **excepto Toasts y Snackbars**, que sí pueden llevar el texto directo (hardcodeado) en el código.

**El `name` del string debe ser el mismo nombre (id) del componente que lo usa**, no una descripción aparte. Así queda claro a qué elemento pertenece cada string con solo mirar el nombre.

```xml
<Button
    android:id="@+id/btnCheckPermission"
    android:text="@string/btnCheckPermission" />
```

```xml
<!-- strings.xml -->
<string name="btnCheckPermission">Check Permission</string>
```

Si un mismo componente necesita más de un string (por ejemplo, un hint distinto al texto), agregar un sufijo corto que lo distinga, ej. `etPeso_hint`.

## 2. Prefijos según el tipo de elemento

Todo `id` de un elemento en un layout XML debe llevar un prefijo que indique su tipo. Nombre en camelCase después del prefijo.

| Elemento | Prefijo | Ejemplo |
|---|---|---|
| TextView | `tv` | `tvTitulo`, `tvNombreEjercicio` |
| Button | `btn` | `btnContinuar`, `btnGuardarRutina` |
| EditText | `et` | `etNombreUsuario`, `etPeso` |
| ImageView | `img` | `imgPerfil`, `imgEjercicio` |
| RecyclerView | `rv` | `rvListaRutinas` |
| CardView | `card` | `cardEjercicio` |
| LinearLayout / ConstraintLayout | `layout` | `layoutContenedor` |
| CheckBox | `cb` | `cbCompletado` |
| Spinner | `sp` | `spCategoria` |
| ProgressBar | `pb` | `pbCarga` |

Si aparece un elemento que no está en esta tabla, usar un prefijo corto y consistente con el mismo criterio (ej. `sw` para Switch) y avisar para agregarlo a esta lista.

## 3. Nombres de variables descriptivos

Las variables deben describir claramente qué contienen, sin abreviaturas confusas ni nombres genéricos.

**Mal:**
```java
int x = 0;
String s = etNombre.getText().toString();
List<String> lst = new ArrayList<>();
```

**Bien:**
```java
int cantidadRepeticiones = 0;
String nombreUsuario = etNombre.getText().toString();
List<String> listaEjercicios = new ArrayList<>();
```

Regla simple: si alguien lee el nombre de la variable sin ver el resto del código, debería poder adivinar qué guarda.

## 4. No usar librerías externas salvo que se pida explícitamente

Usar solo lo que ofrece el SDK de Android por defecto (Java estándar, `AppCompat`, `RecyclerView`, `CardView`, etc., que ya vienen incluidos en un proyecto Android normal). No agregar dependencias nuevas en `build.gradle` (Retrofit, Glide, Room, Dagger/Hilt, etc.) a menos que el usuario lo pida de forma explícita.

Los componentes y estilos de Material ya incluidos en el proyecto sí se pueden usar cuando sean adecuados para la interfaz. Esto no autoriza agregar nuevas dependencias sin solicitarlo primero.

Si en algún momento una librería externa realmente simplifica mucho una tarea, se debe **preguntar primero** en vez de agregarla directamente al proyecto.

## 5. Código simple, no sobre-ingenierizado

El código debe verse como el de un estudiante universitario que está aprendiendo desarrollo móvil, no como el de alguien con años de experiencia. Esto **no** significa usar malas prácticas, significa evitar complejidad innecesaria.

Evitar:
- Patrones de diseño avanzados (Factory, Observer, arquitecturas tipo MVVM/MVP complejas) salvo que se pida explícitamente.
- Librerías externas sofisticadas cuando algo simple del SDK de Android resuelve el problema igual.
- Abstracciones prematuras (interfaces, clases base genéricas) para casos de uso que solo se usan una vez.
- Trucos de una sola línea difíciles de leer (streams anidados, expresiones muy condensadas) cuando un `for` normal es igual de claro.

Sí mantener:
- Buenas prácticas básicas: nombres claros, código ordenado, comentarios cuando algo no es obvio.
- Separación razonable (por ejemplo, no meter toda la lógica en el `onCreate`).
- Manejo de errores básico (validar campos vacíos, nulls, etc.).

En resumen: preferir siempre la solución más directa y fácil de entender, aunque exista una forma "más elegante" o más avanzada de resolverlo.

## 6. Una sola Activity principal y pantallas con Fragments

El proyecto debe tener una única Activity principal, `MainActivity`, que funciona como contenedor de la toolbar, el contenedor de fragments y la navegación general de la aplicación.

Cada pantalla o sección nueva se debe implementar como un `Fragment` con su propio layout XML. No crear una Activity por pantalla ni registrar Activities adicionales en `AndroidManifest.xml`. La navegación entre Inicio, Rutinas, Progreso, Perfil y cualquier otra sección debe reemplazar el Fragment mostrado dentro de `MainActivity`.

La toolbar y la navegación inferior pertenecen a `MainActivity`. Por eso, los layouts de los Fragments no deben volver a dibujar estos elementos. Si una pantalla necesita ocultarlos, debe solicitarlo a `MainActivity` mediante un método simple y explícito, como ya ocurre con el login.

Convenciones:

- Clase de pantalla: `NombrePantallaFragment.java`.
- Layout de pantalla: `fragment_nombre_pantalla.xml`.
- El código del Fragment debe permanecer dentro de la carpeta de su feature.
- Usar `getSupportFragmentManager()` desde `MainActivity` para cambiar la pantalla visible.
- Mantener la navegación simple, sin agregar Navigation Component ni otras dependencias salvo petición explícita del usuario.

## 7. Estructura de carpetas — organizada por feature

El código Java se organiza por *feature* (funcionalidad/pantalla), no por tipo de archivo. Todo lo relacionado a una misma funcionalidad va en la misma carpeta.

```
app/src/main/
├── java/com/tuempresa/gymapp/
│   ├── login/
│   │   ├── LoginFragment.java
│   │   └── Usuario.java
│   │
│   ├── rutinas/
│   │   ├── RutinasListaFragment.java
│   │   ├── RutinaDetalleFragment.java
│   │   ├── RutinaAdapter.java
│   │   └── Rutina.java
│   │
│   ├── ejercicios/
│   │   ├── EjercicioAdapter.java
│   │   └── Ejercicio.java
│   │
│   ├── perfil/
│   │   ├── PerfilFragment.java
│   │   └── EstadisticaUsuario.java
│   │
│   └── utils/                → compartido entre features (validaciones, formateo, constantes)
│       └── ValidadorFormularios.java
│
└── res/
    └── layout/               → Android no permite subcarpetas por feature aquí, queda plano
        ├── activity_main.xml
        ├── fragment_login.xml
        ├── fragment_rutinas_lista.xml
        ├── fragment_rutina_detalle.xml
        ├── item_rutina.xml
        ├── item_ejercicio.xml
        └── fragment_perfil.xml
```

**Nota sobre `res/layout/`:** aunque el código Java se organice por feature, los layouts XML siempre quedan juntos en una sola carpeta (limitación de Android sin configurar `sourceSets`, que no vamos a usar). Por eso el nombre del archivo debe dejar claro a qué pantalla pertenece (`fragment_rutina_detalle.xml`, `item_ejercicio.xml`).

### Convención de nombres de archivos

| Tipo | Convención | Ejemplo |
|---|---|---|
| Activity contenedora | `MainActivity.java` | `MainActivity.java` |
| Fragment de pantalla | `NombrePantallaFragment.java` | `RutinaDetalleFragment.java` |
| Layout de Activity principal | `activity_main.xml` | `activity_main.xml` |
| Layout de Fragment | `fragment_nombre_pantalla.xml` | `fragment_rutina_detalle.xml` |
| Adapter | `NombreAdapter.java` | `EjercicioAdapter.java` |
| Layout de item (RecyclerView) | `item_nombre.xml` | `item_ejercicio.xml` |
| Modelo | `Nombre.java` (singular) | `Rutina.java`, `Ejercicio.java` |

Cuando una clase nueva no encaje claramente en ningún feature existente, preguntar antes de crear un feature nuevo o de meterla en `utils/` por defecto.

## 8. Compilación y verificación

No compilar el proyecto para verificar cambios, a menos que el usuario lo solicite explícitamente. Para validaciones normales, revisar de forma estática los archivos modificados y sus referencias.

## 9. Diseño de pantallas y Fragments

Antes de crear o modificar el diseño de una pantalla, Fragment o layout XML, leer `guia_uso_recursos_visuales.md` para aplicar los colores, dimensiones y estilos visuales definidos en el proyecto.

Tomar los mockups de `stitch_neobrutalist_gym_tracker/` como referencia visual. No es necesario copiarlos exactamente, pero la pantalla debe conservar una apariencia coherente con ellos: interfaz Android clásica, paleta teal/morado/turquesa, tarjetas blancas compactas y jerarquía visual sencilla.

Si existe una duda de diseño que no se pueda resolver con la guía o los mockups, preguntar al usuario antes de tomar una decisión visual importante.
