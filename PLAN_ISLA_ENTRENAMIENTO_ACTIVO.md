# Plan de implementación — Isla de entrenamiento activo

## Objetivo

Implementar una isla compacta que permita conservar y recuperar un entrenamiento cuando el usuario minimice `EntrenamientoActivoFragment` para navegar por otras pantallas de FitTrack.

La isla debe mostrar toda su información y acciones en una sola fila:

1. Una flecha apuntando hacia arriba para volver al entrenamiento.
2. El texto “Entrenamiento activo”.
3. La duración actual del entrenamiento.
4. Un icono de papelera para descartarlo.

Este documento complementa `PLAN_ENTRENAMIENTO_ACTIVO.md`. Cuando exista una diferencia sobre el diseño de la barra compacta, este archivo tiene prioridad. En particular, reemplaza el mockup anterior que mostraba botones separados con texto “VOLVER” y “DESCARTAR”.

Antes de modificar código, leer completamente:

- `AGENTS.md`.
- `guia_uso_recursos_visuales.md`.
- `PLAN_ENTRENAMIENTO_ACTIVO.md`.
- `MainActivity.java`.
- `activity_main.xml`.

No usar Jetpack Compose, Navigation Component ni dependencias externas.

## Mockup aprobado

La isla debe aparecer inmediatamente encima de la navegación inferior y mantener todo en una sola línea:

```text
┌──────────────────────────────────────────┐
│  ⌃   ENTRENAMIENTO ACTIVO / 00:18:34  🗑 │
└──────────────────────────────────────────┘
│    Inicio       Rutinas        Perfil     │
└──────────────────────────────────────────┘
```

Distribución:

```text
flecha arriba | texto del estado / tiempo | papelera
```

- La flecha hacia arriba queda a la izquierda.
- “ENTRENAMIENTO ACTIVO” y el tiempo ocupan el centro.
- La papelera queda a la derecha.
- Todos los elementos están alineados verticalmente y pertenecen a la misma fila.
- No agregar una segunda fila.
- No usar botones con los textos “VOLVER” o “DESCARTAR”.

## Comportamiento general

### Cuando el entrenamiento está abierto

- Ocultar la isla.
- Ocultar la navegación inferior.
- Mostrar `EntrenamientoActivoFragment` completo.
- La toolbar del entrenamiento usa el chevron hacia abajo para minimizar.

### Cuando el usuario minimiza

Al pulsar el chevron hacia abajo de la toolbar:

1. No terminar ni descartar el entrenamiento.
2. Conservar el estado de la sesión.
3. Regresar al fragment anterior.
4. Mostrar nuevamente la navegación inferior.
5. Mostrar la isla encima de la navegación.
6. Continuar actualizando el tiempo visible.

### Cuando se pulsa la flecha hacia arriba

1. Volver a abrir `EntrenamientoActivoFragment`.
2. Restaurar ejercicios, series, pesos, repeticiones y checks.
3. Restaurar el descanso si todavía está activo.
4. Ocultar la isla.
5. Ocultar la navegación inferior.
6. Continuar el cronómetro desde el instante original, sin reiniciarlo.

La zona central con el texto y el tiempo también puede ejecutar la misma acción de volver al entrenamiento, siempre que se configure como una única zona pulsable clara. La flecha hacia arriba debe continuar siendo visible para comunicar la acción.

### Cuando se pulsa la papelera

1. Mostrar un `AlertDialog` de confirmación.
2. Explicar que se perderá el entrenamiento actual.
3. Si el usuario cancela, conservar la sesión y mantener la isla visible.
4. Si confirma:
   - Detener el cronómetro del entrenamiento.
   - Detener el descanso.
   - Eliminar el estado de `EntrenamientoEnCurso`.
   - Ocultar la isla.
   - Mantener visible la navegación inferior.

No descartar el entrenamiento directamente al tocar la papelera sin confirmación.

## Ubicación en la arquitectura

La isla debe pertenecer a `MainActivity`, no a un Fragment concreto.

Esto permite que permanezca visible mientras el usuario navega entre:

- Inicio.
- Rutinas.
- Perfil.
- Otras pantallas donde la navegación inferior esté disponible.

No copiar la isla dentro de cada layout de Fragment.

## Archivos involucrados

```text
app/src/main/
├── java/ue/edu/co/fittrackandroid/
│   ├── hoy/
│   │   └── MainActivity.java
│   └── entrenamiento/
│       ├── EntrenamientoActivoFragment.java
│       └── EntrenamientoEnCurso.java
│
└── res/
    ├── layout/
    │   └── activity_main.xml
    ├── drawable/
    │   ├── ic_expand_less.xml
    │   ├── ic_delete.xml
    │   └── fondo de la isla solo si los recursos actuales no son suficientes
    └── values/
        ├── strings.xml
        └── dimens.xml, solo si hace falta una medida reutilizable
```

No crear una Activity nueva ni registrar componentes adicionales en `AndroidManifest.xml`.

## Layout en `activity_main.xml`

Agregar la isla inmediatamente antes de `bottomNavigation`, para que quede encima de ella.

Estructura recomendada:

```text
LinearLayout raíz de MainActivity
├── toolbar
├── fragmentContainer
├── layoutIslaEntrenamiento
└── bottomNavigation
```

La isla debe ser un `LinearLayout` horizontal con id:

```text
layoutIslaEntrenamiento
```

Debe comenzar con:

```xml
android:visibility="gone"
```

Contenido recomendado:

```text
layoutIslaEntrenamiento
├── btnAbrirEntrenamiento
├── layoutInfoEntrenamientoMinimizado
│   ├── tvEntrenamientoActivoMinimizado
│   ├── tvSeparadorEntrenamientoMinimizado
│   └── tvTiempoEntrenamientoMinimizado
└── btnDescartarEntrenamientoMinimizado
```

`layoutInfoEntrenamientoMinimizado` también debe ser horizontal para que el texto, separador y tiempo estén en una única fila.

Ids sugeridos:

```text
layoutIslaEntrenamiento
btnAbrirEntrenamiento
layoutInfoEntrenamientoMinimizado
tvEntrenamientoActivoMinimizado
tvSeparadorEntrenamientoMinimizado
tvTiempoEntrenamientoMinimizado
btnDescartarEntrenamientoMinimizado
```

### Distribución del espacio

- `btnAbrirEntrenamiento`: ancho compacto fijo.
- `layoutInfoEntrenamientoMinimizado`: ancho `0dp` y `layout_weight="1"`.
- `btnDescartarEntrenamientoMinimizado`: ancho compacto fijo.
- El texto central debe usar una sola línea.
- El tiempo no debe cortarse.
- Si hace falta ahorrar espacio, reducir ligeramente el tamaño del texto antes de crear una segunda fila.

La isla debe tener:

- Margen horizontal para que parezca una tarjeta flotante.
- Margen inferior pequeño respecto a la navegación inferior.
- Fondo blanco o de superficie.
- Elevación ligera.
- Relleno compacto.
- Altura suficiente para que ambos botones tengan un área táctil cómoda.

Aunque visualmente parezca flotante, debe ocupar espacio dentro del layout para no cubrir contenido ni la navegación inferior.

## Flecha hacia arriba

Usar un `ImageButton` con id:

```text
btnAbrirEntrenamiento
```

Debe mostrar un chevron o flecha apuntando hacia arriba, por ejemplo mediante un vector `ic_expand_less.xml`.

Requisitos:

- No reutilizar `ic_arrow_back.xml` rotándolo mediante código.
- Fondo transparente o compatible con la tarjeta.
- Color teal mediante tint.
- Descripción de contenido definida en `strings.xml`.
- Al pulsarlo, reabrir el entrenamiento.

El icono hacia arriba es el opuesto visual del chevron hacia abajo usado para minimizar desde `EntrenamientoActivoFragment`.

## Información central

Mostrar exactamente esta estructura visual:

```text
ENTRENAMIENTO ACTIVO / 00:18:34
```

Puede implementarse mediante tres `TextView` para conservar nombres de recursos coherentes:

- `tvEntrenamientoActivoMinimizado`.
- `tvSeparadorEntrenamientoMinimizado`.
- `tvTiempoEntrenamientoMinimizado`.

El separador debe provenir de `strings.xml`, no estar hardcodeado en el layout.

Formato del tiempo:

```text
HH:MM:SS
```

Ejemplos:

```text
00:03:12
00:58:45
01:12:09
```

Usar el mismo instante de inicio guardado en `EntrenamientoEnCurso`. No crear un cronómetro distinto para la isla.

## Papelera

Usar un `ImageButton` con id:

```text
btnDescartarEntrenamientoMinimizado
```

Debe:

- Mostrar un icono vectorial de papelera.
- Usar color de error mediante tint.
- Tener fondo transparente o discreto.
- Incluir una descripción de contenido.
- Abrir el diálogo de confirmación al pulsarse.

Crear `ic_delete.xml` solamente si no existe un icono apropiado.

## Estado compartido

La isla no debe almacenar una segunda copia de los datos del entrenamiento.

Debe consultar el mismo `EntrenamientoEnCurso` utilizado por `EntrenamientoActivoFragment`.

El estado compartido debe incluir, como mínimo:

- Si hay una sesión activa.
- Instante de inicio.
- Ejercicios.
- Series.
- Peso y repeticiones.
- Estado completado.
- Instante final del descanso, cuando corresponda.

`MainActivity` mantiene la referencia mientras el proceso siga vivo.

Esta primera versión no garantiza recuperar el entrenamiento después de que Android cierre completamente el proceso. La persistencia local puede implementarse en otra tarea mediante almacenamiento nativo, sin mezclarla con el diseño de la isla.

## Actualización del tiempo

Mientras la isla esté visible:

1. Calcular la duración usando el instante inicial de `EntrenamientoEnCurso`.
2. Actualizar `tvTiempoEntrenamientoMinimizado` cada segundo.
3. No incrementar una variable de tiempo manualmente.
4. Usar `SystemClock.elapsedRealtime()` para mantener coherencia con `EntrenamientoActivoFragment`.

El `Handler` y el `Runnable` de la isla pertenecen a `MainActivity`.

Detener sus actualizaciones cuando:

- La isla se oculta.
- Se reabre el entrenamiento.
- Se termina el entrenamiento.
- Se descarta el entrenamiento.
- Se destruye `MainActivity`.

Cuando la isla vuelva a mostrarse, recalcular el tiempo desde el instante original.

## Métodos recomendados en `MainActivity`

Mantener métodos sencillos y explícitos:

```text
iniciarEntrenamientoEnCurso(...)
minimizarEntrenamiento()
mostrarIslaEntrenamiento()
ocultarIslaEntrenamiento()
abrirEntrenamientoEnCurso()
confirmarDescarteEntrenamientoMinimizado()
descartarEntrenamientoEnCurso()
actualizarTiempoIsla()
hayEntrenamientoEnCurso()
```

No es obligatorio usar exactamente estos nombres si existe una alternativa más clara.

Evitar colocar en `MainActivity` la lógica de cálculo de volumen o validación de series. La Activity solo coordina:

- Estado general de la sesión.
- Visibilidad de la isla.
- Navegación.
- Tiempo mostrado en la isla.
- Descarte global.

## Relación con la toolbar del entrenamiento

En `EntrenamientoActivoFragment`:

- El botón izquierdo de la toolbar muestra un chevron hacia abajo.
- Su descripción es “Minimizar entrenamiento”.
- Pulsarlo llama a `MainActivity.minimizarEntrenamiento()`.

En las demás pantallas:

- La toolbar recupera su flecha hacia atrás normal cuando corresponda.
- La isla muestra el chevron hacia arriba para reabrir.

Relación visual:

```text
⌄ en el entrenamiento completo → minimizar
⌃ en la isla                  → volver a abrir
```

## Navegación esperada

```text
EntrenamientoActivoFragment
        │
        │ pulsa ⌄
        ▼
Fragment anterior + navegación inferior + isla
        │
        ├── pulsa ⌃ o la información central
        │       └── vuelve a EntrenamientoActivoFragment
        │
        └── pulsa papelera
                └── confirmación
                        ├── cancelar → conserva la isla
                        └── descartar → elimina la sesión
```

Si el usuario cambia entre Inicio, Rutinas y Perfil mientras el entrenamiento está minimizado, la isla debe permanecer visible.

Si abre una pantalla secundaria desde esas secciones, puede mantenerse visible siempre que no interfiera con su contenido. No duplicarla ni moverla a cada Fragment.

## Textos en `strings.xml`

Agregar como mínimo:

```text
cdAbrirEntrenamiento
tvEntrenamientoActivoMinimizado
tvSeparadorEntrenamientoMinimizado
tvTiempoEntrenamientoMinimizado
cdDescartarEntrenamientoMinimizado
tvTituloDescartarEntrenamientoMinimizado
tvMensajeDescartarEntrenamientoMinimizado
btnConfirmarDescartarEntrenamientoMinimizado
btnCancelarDescartarEntrenamientoMinimizado
```

Valores visuales esperados:

```text
tvEntrenamientoActivoMinimizado = ENTRENAMIENTO ACTIVO
tvSeparadorEntrenamientoMinimizado = /
tvTiempoEntrenamientoMinimizado = 00:00:00
```

Todo texto visible debe provenir de `strings.xml`. Los nombres deben corresponder con los ids de los componentes, según `AGENTS.md`.

## Diseño visual

Seguir `guia_uso_recursos_visuales.md`.

Usar principalmente:

- `@color/colorSurface` para el fondo.
- `@color/colorPrimary` para la flecha hacia arriba y el estado activo.
- `@color/colorTextPrimary` para el tiempo.
- `@color/colorErrorText` para la papelera.
- `@color/colorBorder` o `colorDivider` para un borde discreto.
- `@dimen/spacing_*` para márgenes y relleno.
- `@dimen/elevation_card` para el efecto flotante.

La isla debe ser compacta y fácil de reconocer, sin competir visualmente con la navegación inferior.

No agregar animaciones complejas. Como máximo, usar el cambio normal entre `VISIBLE` y `GONE`.

## Orden recomendado de implementación

1. Confirmar que `EntrenamientoEnCurso` existe según `PLAN_ENTRENAMIENTO_ACTIVO.md`.
2. Crear `ic_expand_less.xml` si hace falta.
3. Crear `ic_delete.xml` si hace falta.
4. Agregar textos a `strings.xml`.
5. Agregar `layoutIslaEntrenamiento` en `activity_main.xml`.
6. Inicializar sus vistas en `MainActivity`.
7. Implementar mostrar y ocultar la isla.
8. Implementar la actualización del tiempo.
9. Conectar el chevron hacia abajo del entrenamiento con minimizar.
10. Conectar la flecha hacia arriba y el área central con reabrir.
11. Implementar el descarte con confirmación.
12. Revisar la restauración de toolbar y navegación inferior.
13. Realizar una revisión estática completa.

## Criterios de aceptación

La tarea se considera terminada cuando:

- La isla pertenece a `MainActivity`.
- La isla está oculta cuando no hay un entrenamiento activo.
- Minimizar el entrenamiento muestra la isla y la navegación inferior.
- La isla aparece inmediatamente encima de la navegación inferior.
- Flecha, información y papelera están en una única fila.
- La flecha apunta hacia arriba.
- El centro muestra “ENTRENAMIENTO ACTIVO / HH:MM:SS”.
- La papelera aparece al extremo derecho.
- No existen botones de texto separados para volver o descartar.
- La duración continúa desde el instante original.
- Pulsar la flecha hacia arriba reabre la misma sesión.
- Pulsar la información central también puede reabrirla si se implementa como zona pulsable.
- Reabrir oculta la isla y la navegación inferior.
- La sesión conserva ejercicios, series, valores y checks.
- La papelera siempre pide confirmación.
- Cancelar el descarte conserva la isla y la sesión.
- Confirmar el descarte limpia la sesión y oculta la isla.
- Cambiar entre Inicio, Rutinas y Perfil no oculta la isla.
- No se creó otra Activity.
- No se agregaron dependencias.
- Todos los textos visibles están en `strings.xml`.
- Todos los ids respetan los prefijos definidos en `AGENTS.md`.
- No quedan callbacks del reloj activos después de ocultar o destruir la isla.

## Verificación final

No ejecutar Gradle ni compilar el proyecto, porque `AGENTS.md` indica que solo debe hacerse cuando el usuario lo solicite expresamente.

Realizar una revisión estática:

1. Confirmar que la isla esté antes de `bottomNavigation` en `activity_main.xml`.
2. Confirmar que todos sus elementos estén en una única fila.
3. Confirmar que no cubra el contenedor de fragments.
4. Revisar ids, prefijos, strings, drawables, colores y dimensiones.
5. Confirmar que el tiempo use el estado original del entrenamiento.
6. Confirmar que solamente exista un `EntrenamientoEnCurso`.
7. Confirmar que reabrir no cree una sesión nueva.
8. Confirmar que descartar limpie los temporizadores y el estado.
9. Confirmar que la navegación inferior se muestre al minimizar y se oculte al reabrir.
10. Confirmar que no se agregaron Activities ni dependencias.
11. Informar qué archivos se crearon o modificaron y qué limitaciones quedan pendientes.
