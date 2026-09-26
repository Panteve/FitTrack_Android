# Plan de implementación — Fragment para crear una rutina

## Objetivo

Implementar una pantalla para crear una rutina dentro de la arquitectura actual de FitTrack.

La pantalla debe permitir:

1. Escribir el nombre de la rutina.
2. Abrir `EjerciciosFragment` para seleccionar un ejercicio.
3. Regresar automáticamente a la creación de la rutina después de seleccionar el ejercicio.
4. Mostrar los ejercicios agregados dentro de un `RecyclerView`.
5. Crear una primera serie automáticamente para cada ejercicio agregado.
6. Agregar más series de forma independiente a cada ejercicio.
7. Escribir el peso objetivo y las repeticiones de cada serie.
8. Mostrar el número de cada serie dentro de un recuadro antes de sus campos.
9. Mantener el botón “Agregar ejercicio” inmediatamente debajo del último ejercicio de la lista.
10. Guardar la rutina desde la acción ubicada en la toolbar.

Antes de modificar código, leer completamente:

- `AGENTS.md`.
- `guia_uso_recursos_visuales.md`.
- `MainActivity.java`.
- `RutinasFragment.java`.
- `EjerciciosFragment.java`.
- `EjercicioAdapter.java`.
- Los layouts de rutinas y ejercicios existentes.

No usar Jetpack Compose, Navigation Component ni dependencias externas.

## Decisiones ya confirmadas

- La pantalla nueva será `CrearRutinaFragment`, no una Activity.
- El contenido del fragment utilizará un `ScrollView` normal, igual que otros fragments del proyecto.
- La toolbar pertenece a `MainActivity` y no debe repetirse dentro del layout.
- La toolbar mostrará una flecha para regresar, el título “Crear rutina” y la acción “Guardar”.
- `EjerciciosFragment` tendrá una única función: seleccionar un ejercicio para añadirlo a una rutina.
- `EjerciciosFragment` no necesita modo normal y modo selección.
- El botón para agregar ejercicios abrirá `EjerciciosFragment`.
- Cuando no existan ejercicios, se mostrará un estado vacío con un botón centrado.
- Cuando exista al menos un ejercicio, el estado vacío desaparecerá.
- Con ejercicios agregados, el botón “Agregar ejercicio” estará inmediatamente después del `RecyclerView`, pegado visualmente al último ejercicio.
- Cada ejercicio tendrá su propio botón “Agregar otra serie”.
- Cada serie mostrará primero su número dentro de un recuadro, seguido por el peso objetivo y las repeticiones.

## Mockup aprobado

### Estado vacío

```text
┌──────────────────────────────────────┐
│ ←          CREAR RUTINA      GUARDAR │  ← Toolbar de MainActivity
├──────────────────────────────────────┤
│                                      │
│ NOMBRE DE LA RUTINA                  │
│ ┌──────────────────────────────────┐ │
│ │ Ej. Rutina de pierna             │ │
│ └──────────────────────────────────┘ │
│                                      │
│                                      │
│      Todavía no hay ejercicios       │
│                                      │
│     ┌──────────────────────────┐     │
│     │  + AGREGAR EJERCICIO     │     │
│     └──────────────────────────┘     │
│                                      │
└──────────────────────────────────────┘
```

### Estado con ejercicios

```text
┌──────────────────────────────────────┐
│ ←          CREAR RUTINA      GUARDAR │
├──────────────────────────────────────┤
│ NOMBRE DE LA RUTINA                  │
│ ┌──────────────────────────────────┐ │
│ │ Día A: Pecho y tríceps           │ │
│ └──────────────────────────────────┘ │
│                                      │
│ EJERCICIOS                           │
│                                      │
│ ┌──────────────────────────────────┐ │
│ │ Press de banca                   │ │
│ │                                  │ │
│ │       PESO OBJ.          REPS    │ │
│ │ ┌───┐ ┌────────────┐ ┌────────┐ │ │
│ │ │ 1 │ │     60     │ │   10   │ │ │
│ │ └───┘ └────────────┘ └────────┘ │ │
│ │ ┌───┐ ┌────────────┐ ┌────────┐ │ │
│ │ │ 2 │ │     65     │ │    8   │ │ │
│ │ └───┘ └────────────┘ └────────┘ │ │
│ │                                  │ │
│ │       + AGREGAR OTRA SERIE        │ │
│ └──────────────────────────────────┘ │
│ ┌──────────────────────────────────┐ │
│ │ Sentadilla con barra             │ │
│ │                                  │ │
│ │       PESO OBJ.          REPS    │ │
│ │ ┌───┐ ┌────────────┐ ┌────────┐ │ │
│ │ │ 1 │ │     80     │ │   12   │ │ │
│ │ └───┘ └────────────┘ └────────┘ │ │
│ │                                  │ │
│ │       + AGREGAR OTRA SERIE        │ │
│ └──────────────────────────────────┘ │
│ ┌──────────────────────────────────┐ │
│ │       + AGREGAR EJERCICIO         │ │  ← siempre debajo del último
│ └──────────────────────────────────┘ │
└──────────────────────────────────────┘
```

## Flujo entre fragments

El flujo esperado es:

```text
RutinasFragment
      │
      │ pulsa “Nueva rutina”
      ▼
CrearRutinaFragment
      │
      │ pulsa “Agregar ejercicio”
      ▼
EjerciciosFragment
      │
      │ selecciona un ejercicio
      ▼
regresa a CrearRutinaFragment
      │
      └── agrega el ejercicio al RecyclerView
```

La comunicación debe implementarse con la API nativa `FragmentResult` de AndroidX:

1. `CrearRutinaFragment` registra un `FragmentResultListener`.
2. Al pulsar “Agregar ejercicio”, solicita a `MainActivity` abrir `EjerciciosFragment` con back stack.
3. `EjerciciosFragment` muestra la lista existente con buscador y filtros.
4. Al pulsar un ejercicio, `EjerciciosFragment` envía el nombre y los demás datos necesarios mediante `setFragmentResult(...)`.
5. Después llama a `MainActivity.regresar()`.
6. `CrearRutinaFragment` recibe el resultado y agrega el ejercicio a su lista.
7. La toolbar vuelve a configurarse como “Crear rutina” desde `onResume()`.

No crear una Activity para seleccionar ejercicios. No comunicar directamente los fragments mediante referencias entre ellos.

## Organización de archivos

Todo el código nuevo debe quedar dentro de la feature `rutinas`.

```text
app/src/main/
├── java/ue/edu/co/fittrackandroid/
│   ├── hoy/
│   │   └── MainActivity.java
│   ├── ejercicios/
│   │   ├── EjerciciosFragment.java
│   │   └── EjercicioAdapter.java
│   └── rutinas/
│       ├── RutinasFragment.java
│       ├── CrearRutinaFragment.java
│       ├── CrearRutinaEjercicioAdapter.java
│       ├── EjercicioRutinaEditable.java
│       └── SerieRutina.java
│
└── res/
    ├── layout/
    │   ├── fragment_crear_rutina.xml
    │   ├── item_ejercicio_crear_rutina.xml
    │   └── item_serie_rutina.xml
    ├── drawable/
    │   └── agregar recursos solamente si los existentes no sirven
    └── values/
        ├── strings.xml
        └── dimens.xml, solo si hace falta una medida reutilizable
```

No crear una nueva Activity ni registrar componentes nuevos en `AndroidManifest.xml`.

## Responsabilidades de las clases

### `CrearRutinaFragment.java`

Será responsable de:

- Inflar `fragment_crear_rutina.xml`.
- Mantener el nombre temporal de la rutina.
- Mantener la lista de ejercicios agregados.
- Registrar y recibir el resultado enviado por `EjerciciosFragment`.
- Configurar el `RecyclerView` de ejercicios.
- Mostrar el estado vacío o el estado con ejercicios.
- Abrir el selector de ejercicios.
- Configurar la toolbar secundaria.
- Validar los datos cuando el usuario pulse “Guardar”.
- Dejar un `TODO` para guardar la rutina realmente cuando exista persistencia.

Organizar la lógica con métodos sencillos y descriptivos. Una estructura posible es:

```text
onCreate(...)
    registrarResultadoEjercicio()

onCreateView(...)
    inflar layout
    inicializar vistas
    configurarRecyclerView
    configurarAcciones
    restaurar datos actuales en las vistas
    actualizarEstadoPantalla

onResume()
    configurar toolbar

abrirSelectorEjercicios()
agregarEjercicioSeleccionado(...)
actualizarEstadoPantalla()
guardarRutina()
validarRutina()
```

No concentrar toda la lógica dentro de `onCreateView()`.

El nombre escrito y los valores de las series deben permanecer al abrir el selector y regresar. Para eso:

- Mantener el nombre actual en un campo del Fragment y actualizarlo mientras el usuario escribe.
- Mantener los ejercicios y las series en sus modelos, no únicamente dentro de los `EditText`.
- Al recrear la vista después de regresar, volver a mostrar el estado conservado en esos campos.

No es necesario implementar persistencia ante cierre del proceso para esta primera versión.

### `EjercicioRutinaEditable.java`

Modelo que representa un ejercicio agregado a la rutina que se está creando.

Debe contener solamente lo necesario:

- Nombre del ejercicio.
- Subtítulo o grupo muscular si se desea mostrar como información secundaria.
- Lista de objetos `SerieRutina`.

Al crear un ejercicio, agregar automáticamente una primera serie vacía.

Debe ofrecer un método sencillo para agregar una serie nueva. No agregar lógica de interfaz al modelo.

### `SerieRutina.java`

Modelo de una serie editable.

Debe contener:

- Peso objetivo como texto temporal o número compatible con decimales.
- Cantidad de repeticiones.

El número visible de la serie no necesita almacenarse. Debe calcularse con su posición en la lista más uno:

```text
posición 0 → serie 1
posición 1 → serie 2
posición 2 → serie 3
```

Así, la numeración siempre queda consecutiva.

### `CrearRutinaEjercicioAdapter.java`

Será el adapter del `RecyclerView` principal y representará un ejercicio por tarjeta.

Cada `ViewHolder` debe:

- Mostrar el nombre del ejercicio.
- Mostrar opcionalmente su subtítulo.
- Mostrar la cabecera “Peso obj.” y “Reps”.
- Mostrar las filas de series dentro de un `LinearLayout` vertical.
- Inflar una instancia de `item_serie_rutina.xml` por cada serie.
- Actualizar `SerieRutina` cuando cambie el peso o las repeticiones.
- Agregar una serie vacía al pulsar `btnAgregarSerie`.
- Volver a dibujar solamente el ejercicio modificado.

No es necesario crear un segundo `RecyclerView` ni otro adapter para las series. Como cada ejercicio tendrá pocas series, inflar las filas dentro de un `LinearLayout` es más sencillo y evita anidar otro RecyclerView.

Al enlazar una tarjeta, limpiar primero el contenedor de series con `removeAllViews()` y volver a inflar las filas correspondientes. Esto evita duplicar filas cuando el RecyclerView reutiliza sus vistas.

### `EjerciciosFragment.java`

Su única función será seleccionar un ejercicio.

Debe conservar:

- Buscador.
- Filtros por grupo muscular.
- Lista de ejercicios.

Debe cambiar:

- La descripción Javadoc para indicar que es un selector.
- La toolbar para mostrar el título “Seleccionar ejercicio”.
- La toolbar no debe mostrar el botón “Crear”.
- Al tocar un ejercicio debe enviar el resultado a `CrearRutinaFragment` y regresar.
- Eliminar el método `abrirCrearEjercicio()` si deja de utilizarse.

No eliminar `CrearEjercicioFragment` ni sus archivos; simplemente quedará fuera de este flujo.

### `EjercicioAdapter.java`

Reemplazar el Toast actual de cada fila por un callback de selección.

Implementar una interfaz sencilla, por ejemplo:

```java
public interface OnEjercicioClickListener {
    void onEjercicioClick(Ejercicio ejercicio);
}
```

El adapter debe recibir este callback en su constructor y llamarlo cuando el usuario pulse una fila.

No debe conocer `MainActivity`, `CrearRutinaFragment` ni `FragmentResult`.

### `RutinasFragment.java`

Cambiar la acción actual de `btnNuevaRutina`:

- Eliminar el Toast provisional de ese botón.
- Llamar a un método de `MainActivity` para abrir `CrearRutinaFragment`.

No cambiar el comportamiento de `btnEmpezarRutinaVacia`, porque no forma parte de esta tarea.

### `MainActivity.java`

Agregar o adaptar métodos públicos sencillos:

```text
mostrarCrearRutina()
mostrarSelectorEjercicios()
```

Ambos deben usar el método existente `cargarFragmentConBackStack(...)`.

- `mostrarCrearRutina()` abre `CrearRutinaFragment`.
- `mostrarSelectorEjercicios()` abre `EjerciciosFragment`.

El método existente `mostrarEjercicios()` puede renombrarse a `mostrarSelectorEjercicios()` si no tiene otros usos. Antes de renombrarlo, buscar todas sus referencias y actualizar únicamente las necesarias.

No duplicar el código de las transacciones y no agregar Navigation Component.

## Layout `fragment_crear_rutina.xml`

Usar la misma estructura general de los fragments desplazables existentes:

```text
FrameLayout
└── ScrollView
    └── LinearLayout vertical
        ├── label del nombre
        ├── EditText del nombre
        ├── contenedor del estado vacío
        │   ├── mensaje
        │   └── botón para agregar el primer ejercicio
        └── contenedor del estado con ejercicios
            ├── cabecera de ejercicios
            ├── RecyclerView
            └── botón para agregar otro ejercicio
```

Ids sugeridos:

```text
layoutCrearRutina
svCrearRutina
layoutContenidoCrearRutina
tvLabelNombreRutinaNueva
etNombreRutinaNueva
tvErrorNombreRutinaNueva
layoutRutinaSinEjercicios
tvRutinaSinEjercicios
btnAgregarPrimerEjercicio
layoutRutinaConEjercicios
tvTituloEjerciciosRutina
rvEjerciciosRutina
btnAgregarEjercicio
```

Usar el prefijo `sv` para el `ScrollView`, igual que `svCrearEjercicio` en el proyecto actual.

### Estado vacío

`layoutRutinaSinEjercicios` debe ocupar suficiente altura para que el mensaje y `btnAgregarPrimerEjercicio` aparezcan aproximadamente en el centro del espacio libre.

Cuando la lista esté vacía:

- Mostrar `layoutRutinaSinEjercicios`.
- Ocultar `layoutRutinaConEjercicios`.

### Estado con ejercicios

Cuando la lista tenga elementos:

- Ocultar `layoutRutinaSinEjercicios`.
- Mostrar `layoutRutinaConEjercicios`.
- Configurar `rvEjerciciosRutina` con `wrap_content` y `nestedScrollingEnabled="false"`, porque el desplazamiento pertenece al `ScrollView` principal.
- Colocar `btnAgregarEjercicio` inmediatamente después del RecyclerView.
- Usar un margen superior pequeño para que el botón se vea unido visualmente al último ejercicio, sin superponerse.

Los dos botones para agregar ejercicios ejecutan exactamente la misma acción, pero cada uno pertenece a un estado diferente:

- `btnAgregarPrimerEjercicio`: visible solamente cuando la lista está vacía.
- `btnAgregarEjercicio`: visible solamente cuando la lista tiene ejercicios.

Cada botón debe tener su propio recurso de texto, aunque ambos muestren “AGREGAR EJERCICIO”, porque `AGENTS.md` pide que el string corresponda al id del componente.

## Layout `item_ejercicio_crear_rutina.xml`

Este layout representa una tarjeta de ejercicio.

Estructura recomendada:

```text
LinearLayout vertical (tarjeta)
├── TextView con nombre del ejercicio
├── TextView con subtítulo opcional
├── cabecera horizontal de las columnas
│   ├── espacio reservado para el número
│   ├── “PESO OBJ.”
│   └── “REPS”
├── LinearLayout vertical para las series
└── Button “AGREGAR OTRA SERIE”
```

Ids sugeridos:

```text
cardEjercicioCrearRutina
tvNombreEjercicioRutinaEditable
tvSubtituloEjercicioRutinaEditable
layoutCabeceraSeries
tvCabeceraPesoObjetivo
tvCabeceraRepeticiones
layoutSeriesEjercicio
btnAgregarSerie
```

No agregar opciones para borrar o reordenar ejercicios en esta tarea, porque no fueron solicitadas.

## Layout `item_serie_rutina.xml`

Cada serie será una fila horizontal con tres elementos:

```text
┌───┐ ┌────────────────┐ ┌──────────────┐
│ 1 │ │ Peso objetivo  │ │ Repeticiones │
└───┘ └────────────────┘ └──────────────┘
```

Ids sugeridos:

```text
layoutSerieRutina
tvNumeroSerie
etPesoObjetivoSerie
etRepeticionesSerie
```

Requisitos:

- `tvNumeroSerie` debe tener tamaño cuadrado y usar `bg_number_badge.xml` si encaja con el diseño.
- `etPesoObjetivoSerie` debe aceptar números decimales.
- `etRepeticionesSerie` debe aceptar únicamente números enteros.
- Los campos deben estar en una sola fila.
- El campo de peso puede ocupar un poco más de espacio que el campo de repeticiones.
- Usar hints breves provenientes de `strings.xml`.
- El número debe establecerse desde el adapter según la posición de la serie.
- No agregar un botón para eliminar series en esta tarea.

## Comunicación mediante `FragmentResult`

Definir claves constantes claras. Pueden estar en `EjerciciosFragment` si se declaran como `public static final` para compartirlas sin crear una clase de utilidades innecesaria.

Ejemplo conceptual:

```text
REQUEST_SELECCION_EJERCICIO
RESULT_NOMBRE_EJERCICIO
RESULT_SUBTITULO_EJERCICIO
RESULT_GRUPO_MUSCULAR
```

En `CrearRutinaFragment`:

- Registrar el listener antes de abrir el selector.
- Leer el resultado.
- Crear un `EjercicioRutinaEditable`.
- Añadir una primera `SerieRutina` vacía.
- Añadir el ejercicio al adapter.
- Actualizar el estado vacío/con ejercicios.

En `EjerciciosFragment`:

- Crear un `Bundle` con los datos seleccionados.
- Enviar el resultado con el mismo request key.
- Regresar mediante `MainActivity.regresar()`.

No pasar objetos completos si no implementan `Parcelable` o `Serializable`. Enviar solamente strings sencillos evita complejidad innecesaria.

## Toolbar

### Crear rutina

En `CrearRutinaFragment.onResume()`:

```java
MainActivity activity = (MainActivity) requireActivity();
activity.mostrarToolbarSecundaria(
        getString(R.string.tvToolbarTituloCrearRutina),
        true,
        getString(R.string.btnGuardarRutinaNueva)
);
activity.setAccionToolbar(this::guardarRutina);
```

### Seleccionar ejercicio

En `EjerciciosFragment.onResume()`:

```java
((MainActivity) requireActivity()).mostrarToolbarSecundaria(
        getString(R.string.tvToolbarTituloSeleccionarEjercicio),
        true,
        null
);
```

Así la flecha existente regresa a la creación de la rutina y no se añade otra toolbar al fragment.

## Comportamiento de las series

Al seleccionar un ejercicio:

- Crear el ejercicio editable.
- Crear automáticamente la serie número 1 con campos vacíos.

Al pulsar `btnAgregarSerie` dentro de una tarjeta:

1. Añadir una nueva `SerieRutina` al ejercicio correspondiente.
2. Notificar el cambio de ese ejercicio al adapter.
3. Volver a mostrar sus filas.
4. Numerar la nueva fila con el tamaño actual de la lista.

Ejemplo:

```text
Primera serie agregada automáticamente → 1
Primera pulsación en “Agregar otra serie” → 2
Segunda pulsación → 3
```

Cada serie debe pertenecer únicamente al ejercicio que la contiene. Agregar una serie a “Press de banca” no debe modificar las series de “Sentadilla”.

Los cambios escritos en los `EditText` deben actualizar inmediatamente el modelo correspondiente mediante `TextWatcher`, para que los valores no se pierdan cuando una tarjeta se recicle o la vista se reconstruya al regresar del selector.

## Selección de ejercicios repetidos

Evitar agregar dos veces el mismo ejercicio a una rutina. Mientras los datos de demostración no tengan un identificador único, comparar por nombre.

Si el ejercicio ya fue agregado:

- No añadirlo nuevamente.
- Mostrar un Toast corto indicando que ya está en la rutina.
- Mantener al usuario en `CrearRutinaFragment` después de regresar del selector.

Dejar un `TODO` para reemplazar esta comparación por un identificador real cuando exista una fuente de datos persistente.

## Validación al guardar

La acción “Guardar” de la toolbar debe validar en este orden:

1. El nombre de la rutina no puede estar vacío.
2. Debe existir al menos un ejercicio.
3. Cada ejercicio debe tener al menos una serie.
4. El peso objetivo de cada serie debe estar completo y ser un número válido igual o mayor que cero.
5. Las repeticiones deben estar completas y ser un número entero mayor que cero.

Permitir peso cero porque algunos ejercicios pueden realizarse con peso corporal.

Cuando exista un error:

- Mostrar un mensaje claro.
- No cerrar el fragment.
- Marcar el campo del nombre si ese es el error.
- Para errores de series, indicar mediante Toast qué ejercicio y serie deben revisarse, manteniendo el código simple.

Cuando todo sea válido:

- Mostrar un Toast provisional indicando que la rutina fue guardada.
- Dejar un `TODO` para reemplazarlo por persistencia real.
- Regresar a `RutinasFragment` mediante el back stack.

No implementar base de datos, backend o `SharedPreferences` para guardar rutinas en esta tarea.

## Textos en `strings.xml`

Agregar todos los textos visibles. Como mínimo:

```text
tvToolbarTituloCrearRutina
btnGuardarRutinaNueva
tvLabelNombreRutinaNueva
etNombreRutinaNueva_hint
tvErrorNombreRutinaNueva
tvRutinaSinEjercicios
btnAgregarPrimerEjercicio
tvTituloEjerciciosRutina
btnAgregarEjercicio
tvNombreEjercicioRutinaEditable
tvSubtituloEjercicioRutinaEditable
tvCabeceraPesoObjetivo
tvCabeceraRepeticiones
tvNumeroSerie
etPesoObjetivoSerie_hint
etRepeticionesSerie_hint
btnAgregarSerie
tvToolbarTituloSeleccionarEjercicio
```

Recordar:

- El nombre de cada string debe coincidir con el id del componente que lo utiliza.
- Si un componente necesita un hint, usar el sufijo `_hint`.
- No hardcodear textos visibles en los layouts ni en Java.
- Los Toasts pueden usar texto directo porque `AGENTS.md` lo permite.

## Diseño visual

Seguir `guia_uso_recursos_visuales.md`.

Usar:

- `@color/colorBackground` para el fondo.
- `@color/colorSurface` para tarjetas.
- `@color/colorPrimary` para números, iconos y detalles activos.
- `@color/colorActionPrimary` para botones principales.
- `@color/colorTextPrimary` para nombres de ejercicios.
- `@color/colorTextSecondary` y `colorTextTertiary` para etiquetas.
- `@color/colorError` para errores.
- `@dimen/spacing_*` para márgenes y rellenos.
- `@drawable/bg_card` para las tarjetas.
- `@drawable/bg_input` y `bg_input_error` para los campos.
- `@drawable/bg_number_badge` para el número de serie si su forma es adecuada.
- Los estilos de texto y botones existentes siempre que encajen.

La pantalla debe conservar la apariencia Android clásica de los mockups: fondo gris claro, tarjetas blancas compactas, teal para identidad y morado para acciones principales.

No crear un diseño excesivamente complejo ni agregar animaciones.

## Convenciones obligatorias

- Código Java y vistas XML tradicionales.
- Una sola Activity: `MainActivity`.
- Pantallas nuevas como fragments.
- Código organizado por feature.
- Variables con nombres descriptivos.
- Todos los ids con prefijo según su tipo.
- Todos los textos visibles en `strings.xml`.
- Sin dependencias nuevas.
- Sin patrones avanzados, ViewModel, repositorios o interfaces adicionales salvo el callback sencillo del adapter.
- Clases y métodos públicos documentados con Javadoc.
- No modificar archivos no relacionados.
- No eliminar funcionalidades existentes que no formen parte de este flujo.
- Mantener métodos pequeños y fáciles de leer.

## Orden recomendado de implementación

1. Crear `SerieRutina.java`.
2. Crear `EjercicioRutinaEditable.java`.
3. Crear `item_serie_rutina.xml`.
4. Crear `item_ejercicio_crear_rutina.xml`.
5. Crear `CrearRutinaEjercicioAdapter.java`.
6. Crear `fragment_crear_rutina.xml`.
7. Agregar los textos necesarios a `strings.xml`.
8. Crear `CrearRutinaFragment.java`.
9. Configurar el estado vacío y el estado con ejercicios.
10. Configurar las series dinámicas y su numeración.
11. Cambiar `EjercicioAdapter` para entregar el ejercicio seleccionado mediante callback.
12. Cambiar `EjerciciosFragment` para devolver el resultado y actuar únicamente como selector.
13. Agregar los métodos de navegación necesarios a `MainActivity`.
14. Conectar `btnNuevaRutina` desde `RutinasFragment`.
15. Implementar las validaciones de “Guardar”.
16. Revisar estáticamente todas las referencias.

## Criterios de aceptación

La tarea se considera terminada cuando:

- `btnNuevaRutina` abre `CrearRutinaFragment`.
- La toolbar muestra “Crear rutina”, flecha para regresar y “Guardar”.
- El layout no incluye una segunda toolbar.
- El contenido usa un `ScrollView` normal siguiendo el patrón del proyecto.
- El nombre de la rutina se escribe en un `EditText` ubicado arriba.
- Con la lista vacía se muestra el botón centrado para agregar el primer ejercicio.
- Ambos botones de agregar ejercicio abren `EjerciciosFragment`.
- `EjerciciosFragment` funciona únicamente como selector.
- La toolbar del selector muestra una flecha y el título “Seleccionar ejercicio”.
- Pulsar una fila devuelve el ejercicio seleccionado.
- Al regresar, el ejercicio aparece en `rvEjerciciosRutina`.
- El ejercicio nuevo contiene automáticamente la serie número 1.
- El número de cada serie aparece primero y dentro de un recuadro.
- Al lado del número aparecen el peso objetivo y las repeticiones.
- “Agregar otra serie” afecta solamente al ejercicio correspondiente.
- Las series quedan numeradas consecutivamente.
- Los valores escritos no se pierden al añadir series o seleccionar otro ejercicio.
- Con ejercicios agregados, el botón “Agregar ejercicio” queda inmediatamente debajo del último elemento del RecyclerView.
- No se permite agregar el mismo ejercicio dos veces.
- Guardar valida el nombre, la existencia de ejercicios y los campos de todas las series.
- No se creó una Activity adicional.
- No se agregó ninguna dependencia.
- No existen textos visibles hardcodeados fuera de las excepciones permitidas.
- Todos los recursos referenciados existen.

## Verificación final

No ejecutar Gradle ni compilar el proyecto, porque `AGENTS.md` indica que solamente debe compilarse cuando el usuario lo solicite expresamente.

Realizar una revisión estática:

1. Revisar imports y paquetes.
2. Revisar referencias a layouts, strings, drawables, colores y dimensiones.
3. Buscar textos visibles hardcodeados.
4. Confirmar los prefijos de todos los ids.
5. Confirmar que el adapter guarda los cambios de los `EditText` en los modelos.
6. Confirmar que el contenedor de series se limpia antes de volver a inflar filas.
7. Confirmar que el resultado del selector usa las mismas claves en ambos fragments.
8. Confirmar que la flecha de la toolbar utiliza el back stack existente.
9. Confirmar que el botón para agregar ejercicios está después del RecyclerView.
10. Confirmar que no se agregaron dependencias ni Activities.
11. Informar qué archivos fueron creados y modificados y qué `TODO` quedan pendientes.
