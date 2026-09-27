# Plan de implementación — Entrenamiento activo

## Objetivo

Implementar el fragment que se muestra cuando el usuario comienza un entrenamiento.

La pantalla debe permitir:

1. Ver la duración total del entrenamiento en tiempo real.
2. Ver el volumen acumulado de las series completadas.
3. Ver la cantidad de series completadas frente al total de series disponibles.
4. Consultar y editar los ejercicios del entrenamiento en un `RecyclerView`.
5. Quitar un ejercicio del entrenamiento.
6. Registrar peso y repeticiones en cada serie.
7. Marcar cada serie como completada mediante un check ubicado después de las repeticiones.
8. Agregar series a un ejercicio.
9. Agregar ejercicios usando el `EjerciciosFragment` existente.
10. Mostrar un temporizador de descanso después de completar una serie.
11. Terminar el entrenamiento desde la toolbar.
12. Descartar el entrenamiento mediante un botón ubicado debajo de “Agregar ejercicio”.

Antes de modificar código, leer completamente:

- `AGENTS.md`.
- `guia_uso_recursos_visuales.md`.
- `MainActivity.java`.
- `HomeFragment.java`.
- `RutinasFragment.java`.
- `RutinaAdapter.java`.
- `EjerciciosFragment.java`.
- `CrearRutinaFragment.java`.
- `CrearRutinaEjercicioAdapter.java`.
- Los modelos y layouts existentes de rutinas y series.

No utilizar Jetpack Compose, Navigation Component ni dependencias externas.

## Decisiones confirmadas

- La pantalla será un Fragment llamado `EntrenamientoActivoFragment`.
- Todo su código debe quedar dentro de la feature `entrenamiento`.
- La toolbar sigue perteneciendo a `MainActivity`.
- El control izquierdo de la toolbar será un chevron apuntando hacia abajo, no una flecha hacia atrás.
- El chevron hacia abajo representa la acción de minimizar el entrenamiento sin terminarlo ni descartarlo.
- La toolbar mostrará el título “Entrenamiento activo” y la acción “Terminar”.
- “Terminar” solamente debe aparecer mientras este fragment esté activo.
- La navegación inferior de FitTrack debe desaparecer durante todo el entrenamiento.
- La navegación inferior debe continuar oculta cuando se abra el selector de ejercicios desde el entrenamiento.
- La navegación inferior debe reaparecer al terminar, descartar o abandonar definitivamente el entrenamiento.
- El contenido desplazable usará un `ScrollView`, siguiendo el patrón del proyecto.
- Los ejercicios se mostrarán en un `RecyclerView` dentro del contenido desplazable.
- El botón “Agregar ejercicio” estará inmediatamente después del último ejercicio.
- El botón “Descartar entreno” estará inmediatamente debajo de “Agregar ejercicio”.
- Cada ejercicio tendrá una opción para quitarlo.
- Cada ejercicio tendrá su propio botón “Agregar otra serie”.
- Cada serie mostrará, en este orden: número, peso, repeticiones y check.
- El volumen se calcula como `peso × repeticiones` y solo incluye series completadas.
- Al completar una serie comienza un descanso de tres minutos.
- El panel de descanso se muestra fijo en la parte inferior, fuera del `ScrollView`.
- Los controles de descanso aparecen en una única fila y en este orden: `-15 s`, tiempo, `+15 s`, `OMITIR`.

## Mockup aprobado

### Antes de completar una serie

```text
┌──────────────────────────────────────────┐
│ ⌄     ENTRENAMIENTO ACTIVO      TERMINAR │  — ⌄ minimiza el entrenamiento
├──────────────────────────────────────────┤
│ ┌───────────┬────────────┬─────────────┐ │
│ │ DURACIÓN  │  VOLUMEN   │   SERIES    │ │
│ │  00:02:15 │    0 kg    │    0/5      │ │
│ └───────────┴────────────┴─────────────┘ │
│                                          │
│ EJERCICIOS                               │
│                                          │
│ ┌──────────────────────────────────────┐ │
│ │ Press de banca               [QUITAR]│ │
│ │                                      │ │
│ │      PESO             REPS           │ │
│ │ ┌───┐ ┌───────────┐ ┌───────┐ ┌───┐│ │
│ │ │ 1 │ │    60     │ │  10   │ │   ││ │
│ │ └───┘ └───────────┘ └───────┘ └───┘│ │
│ │                                      │ │
│ │ ┌───┐ ┌───────────┐ ┌───────┐ ┌───┐│ │
│ │ │ 2 │ │           │ │       │ │   ││ │
│ │ └───┘ └───────────┘ └───────┘ └───┘│ │
│ │                                      │ │
│ │         + AGREGAR OTRA SERIE          │ │
│ └──────────────────────────────────────┘ │
│ ┌──────────────────────────────────────┐ │
│ │         + AGREGAR EJERCICIO           │ │
│ └──────────────────────────────────────┘ │
│ ┌──────────────────────────────────────┐ │
│ │         DESCARTAR ENTRENO             │ │
│ └──────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

El panel de descanso permanece oculto hasta completar una serie.

### Después de completar una serie

```text
┌──────────────────────────────────────────┐
│ ⌄     ENTRENAMIENTO ACTIVO      TERMINAR │
├──────────────────────────────────────────┤
│ ┌───────────┬────────────┬─────────────┐ │
│ │ DURACIÓN  │  VOLUMEN   │   SERIES    │ │
│ │  00:02:24 │   600 kg   │    1/5      │ │
│ └───────────┴────────────┴─────────────┘ │
│                                          │
│ ┌──────────────────────────────────────┐ │
│ │ Press de banca               [QUITAR]│ │
│ │                                      │ │
│ │      PESO             REPS           │ │
│ │ ┌───┐ ┌───────────┐ ┌───────┐ ┌───┐│ │
│ │ │ 1 │ │    60     │ │  10   │ │ ✓ ││ │
│ │ └───┘ └───────────┘ └───────┘ └───┘│ │
│ │                                      │ │
│ │         + AGREGAR OTRA SERIE          │ │
│ └──────────────────────────────────────┘ │
│ ┌──────────────────────────────────────┐ │
│ │         + AGREGAR EJERCICIO           │ │
│ └──────────────────────────────────────┘ │
│ ┌──────────────────────────────────────┐ │
│ │         DESCARTAR ENTRENO             │ │
│ └──────────────────────────────────────┘ │
├──────────────────────────────────────────┤
│          DESCANSO ENTRE SERIES           │
│ ┌──────┐   ┌────────┐   ┌──────┐ ┌─────┐│
│ │ -15s │   │  03:00 │   │ +15s │ │OMITIR││
│ └──────┘   └────────┘   └──────┘ └─────┘│
└──────────────────────────────────────────┘
```

El orden definitivo del panel inferior es:

```text
-15 s  |  tiempo restante  |  +15 s  |  OMITIR
```

## Organización de archivos

La feature `entrenamiento` ya existe como carpeta reservada. Todo el código nuevo de esta pantalla debe quedar allí.

```text
app/src/main/
├── java/ue/edu/co/fittrackandroid/
│   ├── hoy/
│   │   └── MainActivity.java
│   ├── ejercicios/
│   │   └── EjerciciosFragment.java
│   ├── rutinas/
│   │   ├── RutinaAdapter.java
│   │   └── RutinasFragment.java
│   └── entrenamiento/
│       ├── EntrenamientoActivoFragment.java
│       ├── EntrenamientoEjercicioAdapter.java
│       ├── EntrenamientoEnCurso.java
│       ├── EjercicioEntrenamiento.java
│       └── SerieEntrenamiento.java
│
└── res/
    ├── layout/
    │   ├── activity_main.xml                  → agregar barra de sesión minimizada
    │   ├── fragment_entrenamiento_activo.xml
    │   ├── item_ejercicio_entrenamiento.xml
    │   └── item_serie_entrenamiento.xml
    ├── drawable/
    │   └── agregar solamente los recursos que realmente falten
    └── values/
        ├── strings.xml
        └── dimens.xml, solo si hace falta una medida reutilizable
```

No crear una Activity nueva ni registrar componentes adicionales en `AndroidManifest.xml`.

### Estado de una sesión minimizada

Crear `EntrenamientoEnCurso.java` como un contenedor sencillo del estado que debe sobrevivir mientras el usuario navega por otros fragments:

- Instante de inicio.
- Lista de ejercicios y series.
- Estado completado de cada serie.
- Instante final del descanso, cuando exista.
- Indicador de sesión activa.

`MainActivity` debe mantener una única referencia a este objeto mientras la aplicación continúe viva. Así se puede retirar y reconstruir la vista del Fragment sin perder la sesión.

Esta primera versión conserva el entrenamiento al navegar dentro de FitTrack, pero no después de que Android cierre el proceso. No agregar persistencia local como parte de esta tarea.

## Por qué usar modelos separados

No modificar `SerieRutina` ni `EjercicioRutinaEditable` para representar una sesión activa. Esos modelos pertenecen al proceso de creación de rutinas.

Durante un entrenamiento, una serie necesita información adicional:

- Saber si está completada.
- Participar en el cálculo de volumen.
- Bloquear o desbloquear sus campos según su estado.

Crear modelos sencillos dentro de `entrenamiento` evita mezclar la edición de una plantilla con el registro de una sesión real.

## Responsabilidades de las clases

### `EntrenamientoActivoFragment.java`

Será responsable de:

- Inflar `fragment_entrenamiento_activo.xml`.
- Mantener la lista de ejercicios de la sesión.
- Configurar el adapter.
- Registrar el resultado enviado por `EjerciciosFragment`.
- Actualizar duración, volumen y series.
- Iniciar y detener el temporizador general.
- Iniciar, ajustar, omitir y detener el temporizador de descanso.
- Mostrar u ocultar el panel de descanso.
- Configurar la toolbar con la acción “Terminar”.
- Solicitar a `MainActivity` ocultar la navegación inferior.
- Restaurar la navegación inferior al finalizar o descartar.
- Validar y confirmar la finalización del entrenamiento.
- Confirmar el descarte del entrenamiento.
- Solicitar a `MainActivity` que minimice el entrenamiento cuando se pulse el chevron hacia abajo.

Organizar la lógica en métodos pequeños y descriptivos. Una estructura posible es:

```text
onCreate(...)
    registrarResultadoEjercicio()
    prepararDatosIniciales()

onCreateView(...)
    inflar layout
    inicializarVistas
    configurarRecyclerView
    configurarAcciones
    actualizarResumen
    actualizarEstadoDescanso

onResume()
    configurarToolbar
    ocultarNavegacionInferior
    reanudarActualizacionesVisuales

onPause()
    detener callbacks visuales del reloj

onDestroy()
    cancelar callbacks pendientes

iniciarRelojEntrenamiento()
actualizarDuracion()
iniciarDescanso()
restarTiempoDescanso()
sumarTiempoDescanso()
omitirDescanso()
actualizarTiempoDescanso()
recalcularResumen()
abrirSelectorEjercicios()
agregarEjercicioSeleccionado(...)
confirmarQuitarEjercicio(...)
confirmarTerminarEntrenamiento()
confirmarDescartarEntrenamiento()
minimizarEntrenamiento()
```

No concentrar toda la lógica en `onCreateView()`.

### `SerieEntrenamiento.java`

Modelo de una serie durante el entrenamiento.

Campos necesarios:

- `String peso`.
- `String repeticiones`.
- `boolean completada`.

Debe ofrecer getters y setters sencillos.

El número visible de la serie no necesita almacenarse. Debe obtenerse a partir de su posición más uno.

Puede incluir un método simple para calcular su volumen únicamente cuando esté completada y tenga valores válidos:

```text
volumen = peso × repeticiones
```

Si se usa `double` para el peso, mantener el resultado como `double`. No redondear internamente; el formateo pertenece a la interfaz.

### `EjercicioEntrenamiento.java`

Modelo de un ejercicio dentro de la sesión activa.

Campos necesarios:

- Nombre.
- Grupo muscular o subtítulo, si se desea mostrar.
- Lista de `SerieEntrenamiento`.

Debe permitir:

- Consultar sus series.
- Agregar una serie vacía al final.
- Conocer cuántas series tiene.

Cuando se agregue un ejercicio nuevo desde el selector, crear automáticamente una primera serie vacía.

### `EntrenamientoEjercicioAdapter.java`

Adapter del `RecyclerView` principal. Cada elemento representa un ejercicio completo.

Debe recibir callbacks sencillos para que el Fragment maneje acciones generales:

```text
onQuitarEjercicio(posicion)
onAgregarSerie(posicion)
onEstadoSerieCambiado(posicionEjercicio, posicionSerie, completada)
onDatosSerieCambiados()
```

No es obligatorio usar exactamente una interfaz por cada acción. Preferir una sola interfaz clara para mantener el código simple.

Cada `ViewHolder` debe:

- Mostrar el nombre del ejercicio.
- Conectar la opción para quitarlo.
- Mostrar la cabecera de peso y repeticiones.
- Inflar una fila `item_serie_entrenamiento.xml` por cada serie dentro de un `LinearLayout`.
- Limpiar el contenedor con `removeAllViews()` antes de volver a inflar las filas.
- Asignar el número de cada serie según su posición.
- Actualizar el modelo cuando cambien peso o repeticiones.
- Escuchar el check de completado.
- Bloquear peso y repeticiones después de completar correctamente una serie.
- Volver a habilitarlos si el usuario desmarca la serie.
- Notificar al Fragment para que recalcule el resumen y active el descanso.
- Agregar una nueva serie solamente al ejercicio correspondiente.

No crear otro RecyclerView para las series. Usar un `LinearLayout` vertical dentro de cada ejercicio, igual que en el flujo de creación de rutinas.

## Layout `fragment_entrenamiento_activo.xml`

La pantalla necesita contenido desplazable y un panel de descanso fijo. La estructura debe ser:

```text
LinearLayout vertical raíz
├── ScrollView con height 0dp y weight 1
│   └── LinearLayout vertical
│       ├── tarjeta de resumen
│       ├── título de ejercicios
│       ├── RecyclerView de ejercicios
│       ├── botón Agregar ejercicio
│       └── botón Descartar entreno
└── panel de descanso fijo, inicialmente GONE
    ├── título de descanso
    └── fila: -15 s | 03:00 | +15 s | OMITIR
```

Ids sugeridos:

```text
layoutEntrenamientoActivo
svEntrenamientoActivo
layoutContenidoEntrenamientoActivo
layoutResumenEntrenamiento
layoutMetricaDuracion
tvLabelDuracionEntrenamiento
tvDuracionEntrenamiento
layoutMetricaVolumen
tvLabelVolumenEntrenamiento
tvVolumenEntrenamiento
layoutMetricaSeries
tvLabelSeriesEntrenamiento
tvSeriesEntrenamiento
tvTituloEjerciciosEntrenamiento
rvEjerciciosEntrenamiento
btnAgregarEjercicioEntrenamiento
btnDescartarEntrenamiento
layoutDescansoEntrenamiento
tvTituloDescansoEntrenamiento
layoutControlesDescanso
btnRestarDescanso
tvTiempoDescanso
btnSumarDescanso
btnOmitirDescanso
```

Usar el prefijo `sv` para el `ScrollView`, tal como exige `AGENTS.md` y como ya hace el proyecto.

### Resumen superior

La tarjeta de resumen contiene tres columnas de igual ancho:

```text
DURACIÓN | VOLUMEN | SERIES
00:18:34 | 1120 kg | 2/6
```

- Duración: tiempo total desde que comenzó el entrenamiento.
- Volumen: suma de `peso × repeticiones` de las series completadas.
- Series: cantidad completada sobre cantidad total existente.

Usar separadores discretos entre columnas si ayudan a la lectura.

### Lista y botones finales

Configurar `rvEjerciciosEntrenamiento` con:

- `layout_height="wrap_content"`.
- `nestedScrollingEnabled="false"`.
- `LinearLayoutManager`.

El desplazamiento pertenece al `ScrollView` principal.

Después del RecyclerView deben aparecer, sin otros elementos intermedios:

1. `btnAgregarEjercicioEntrenamiento`.
2. `btnDescartarEntrenamiento`.

El primer botón debe quedar visualmente pegado al último ejercicio usando un margen pequeño. El botón de descarte debe quedar debajo con separación suficiente para distinguir la acción peligrosa.

### Panel fijo de descanso

`layoutDescansoEntrenamiento` debe estar fuera del `ScrollView`, en la parte inferior del layout raíz.

Debe estar `GONE` inicialmente y aparecer después de completar correctamente una serie.

La fila debe mantenerse en una sola línea:

```text
btnRestarDescanso | tvTiempoDescanso | btnSumarDescanso | btnOmitirDescanso
```

Los cuatro componentes deben estar alineados verticalmente al centro.

Dar mayor peso horizontal a `tvTiempoDescanso` para que `03:00` sea el elemento visual principal. Mantener los botones compactos para que la fila quepa en pantallas pequeñas.

## Layout `item_ejercicio_entrenamiento.xml`

Cada ejercicio debe mostrarse como una tarjeta compacta.

Estructura:

```text
LinearLayout vertical
├── cabecera horizontal
│   ├── nombre del ejercicio
│   └── opción para quitar
├── cabecera de columnas
│   ├── espacio del número
│   ├── “PESO”
│   ├── “REPS”
│   └── espacio del check
├── LinearLayout vertical con las series
└── botón “AGREGAR OTRA SERIE”
```

Ids sugeridos:

```text
cardEjercicioEntrenamiento
layoutCabeceraEjercicioEntrenamiento
tvNombreEjercicioEntrenamiento
btnQuitarEjercicioEntrenamiento
layoutCabeceraSeriesEntrenamiento
tvCabeceraPesoEntrenamiento
tvCabeceraRepeticionesEntrenamiento
layoutSeriesEntrenamiento
btnAgregarSerieEntrenamiento
```

La opción para quitar puede ser un `ImageButton` con un vector de papelera o un botón compacto con texto. Si se usa `ImageButton`, mantener el prefijo `btn`, igual que los botones de imagen existentes del proyecto, y agregar una descripción de contenido.

No agregar reordenamiento de ejercicios en esta tarea.

## Layout `item_serie_entrenamiento.xml`

Cada serie debe estar en una sola fila y conservar este orden:

```text
┌───┐ ┌────────────┐ ┌──────────┐ ┌───┐
│ 1 │ │    peso    │ │   reps   │ │ ✓ │
└───┘ └────────────┘ └──────────┘ └───┘
```

Ids sugeridos:

```text
layoutSerieEntrenamiento
tvNumeroSerieEntrenamiento
etPesoSerieEntrenamiento
etRepeticionesSerieEntrenamiento
cbSerieCompletada
```

Requisitos:

- El número aparece primero, dentro de un recuadro.
- Reutilizar `bg_number_badge.xml` si encaja con el diseño.
- El peso acepta decimales.
- Las repeticiones aceptan únicamente enteros.
- El check aparece inmediatamente después de las repeticiones.
- Todos los controles permanecen alineados verticalmente.
- Peso puede tener un poco más de ancho que repeticiones.
- Usar hints breves definidos en `strings.xml`.

## Cálculo del resumen

No aumentar o disminuir manualmente contadores acumulados, porque eso puede producir errores al desmarcar series o quitar ejercicios.

Crear un método `recalcularResumen()` que recorra toda la lista desde cero cada vez que cambie algo relevante.

### Duración

Formato:

```text
HH:MM:SS
```

Ejemplos:

```text
00:00:00
00:18:34
01:05:09
```

Guardar el instante de inicio con `SystemClock.elapsedRealtime()` y calcular la diferencia. Usar un `Handler` y un `Runnable` para refrescar el texto cada segundo.

No implementar un `Service` para esta primera versión.

### Volumen

Para cada serie completada:

```text
volumenSerie = peso × repeticiones
```

El volumen total es:

```text
volumenTotal = suma de volumenSerie de todas las series completadas
```

Ejemplo:

```text
60 kg × 10 reps = 600 kg
65 kg × 8 reps = 520 kg
volumen total = 1120 kg
```

Las series sin check no participan en el volumen.

Si el peso contiene decimales, mostrar decimales solamente cuando sean necesarios. Usar `Locale` al formatear; no concatenar formatos complejos directamente.

### Series

Mostrar:

```text
series completadas / series totales
```

Ejemplo:

```text
2/6
```

Agregar una serie aumenta el total. Completarla aumenta las completadas. Desmarcarla reduce las completadas. Quitar un ejercicio elimina del cálculo todas sus series.

## Comportamiento del check

Al marcar `cbSerieCompletada`:

1. Leer peso y repeticiones.
2. Validar que ambos campos estén completos.
3. Validar que el peso sea un número igual o mayor que cero.
4. Validar que las repeticiones sean un entero mayor que cero.
5. Si existe un error:
   - Mantener la serie sin completar.
   - Mostrar un error claro.
   - No iniciar el descanso.
6. Si los datos son válidos:
   - Guardarlos en el modelo.
   - Marcar la serie como completada.
   - Deshabilitar los dos `EditText` para congelar el registro.
   - Recalcular volumen y series.
   - Iniciar el descanso desde `03:00`.

Al desmarcar una serie:

- Marcarla como no completada.
- Volver a habilitar peso y repeticiones.
- Recalcular volumen y series.
- No iniciar otro descanso.
- No detener automáticamente un descanso que ya se encuentre activo.

Evitar ciclos al modificar el check desde código. Quitar temporalmente el listener o usar una bandera sencilla mientras se revierte un check inválido.

## Temporizador de descanso

### Estado inicial

- El panel está oculto.
- El tiempo base de cada descanso es 180 segundos.

### Inicio

Después de completar correctamente una serie:

- Establecer el tiempo restante en 180 segundos.
- Mostrar el panel.
- Mostrar `03:00`.
- Comenzar la cuenta regresiva.

Si se completa otra serie mientras el descanso anterior sigue activo, reiniciar el contador desde `03:00`.

### Restar quince segundos

`btnRestarDescanso` debe:

- Restar 15 segundos.
- Nunca permitir un valor negativo.
- Si llega a cero, finalizar el descanso y ocultar el panel.

### Sumar quince segundos

`btnSumarDescanso` debe:

- Agregar 15 segundos al valor actual.
- Actualizar el texto inmediatamente.
- No imponer un máximo que el usuario no haya solicitado.

### Omitir

`btnOmitirDescanso` debe:

- Detener la cuenta regresiva.
- Establecer el tiempo restante en cero.
- Ocultar el panel.

### Finalización automática

Cuando el contador llegue a `00:00`:

- Detener la cuenta regresiva.
- Ocultar el panel.

No agregar sonido, vibración ni notificaciones en esta tarea.

Puede usarse un `Handler` con un `Runnable` para mantener el código sencillo. Si se usa `CountDownTimer`, debe cancelarse y recrearse correctamente después de `-15`, `+15` o un reinicio. Preferir la alternativa más fácil de entender y mantener.

## Agregar ejercicios

`btnAgregarEjercicioEntrenamiento` debe abrir el `EjerciciosFragment` existente mediante `MainActivity.mostrarSelectorEjercicios()`.

`EntrenamientoActivoFragment` debe registrar un `FragmentResultListener` usando las claves ya publicadas por `EjerciciosFragment`.

Cuando reciba un ejercicio:

1. Crear `EjercicioEntrenamiento` con su nombre y grupo muscular.
2. Agregar automáticamente una primera serie vacía.
3. Añadirlo al adapter.
4. Recalcular el total de series.

`EjerciciosFragment` ya funciona como selector y no necesita un modo adicional.

Actualizar su comentario sobre las claves para que no mencione exclusivamente a `CrearRutinaFragment`; ahora el resultado puede ser consumido por cualquier pantalla que haya abierto el selector.

Permitir ejercicios repetidos solamente si ese es el comportamiento actual acordado para crear rutinas. No introducir una regla diferente entre las dos pantallas sin necesidad. La implementación actual de `CrearRutinaFragment` permite repetirlos, así que mantener esa consistencia en esta primera versión.

## Agregar series

Al pulsar `btnAgregarSerieEntrenamiento`:

1. Agregar una `SerieEntrenamiento` vacía al ejercicio correspondiente.
2. Actualizar solamente la tarjeta necesaria.
3. Numerar la nueva serie según su posición.
4. Recalcular el total de series.

Agregar una serie a un ejercicio no debe modificar ningún otro ejercicio.

## Quitar ejercicios

Al pulsar la opción para quitar un ejercicio:

1. Mostrar un `AlertDialog` de confirmación.
2. Incluir el nombre del ejercicio en el mensaje mediante un string con formato.
3. Si el usuario cancela, no cambiar nada.
4. Si confirma, eliminar el ejercicio y todas sus series.
5. Notificar al adapter.
6. Recalcular volumen y cantidad de series.

Si el ejercicio eliminado contenía series completadas, su volumen deja de formar parte del resumen.

No es necesario detener el descanso actual al eliminar un ejercicio.

## Toolbar y navegación inferior

### Configuración de la toolbar

En `EntrenamientoActivoFragment.onResume()`:

```java
MainActivity activity = (MainActivity) requireActivity();
activity.mostrarToolbarSecundaria(
        getString(R.string.tvToolbarTituloEntrenamientoActivo),
        true,
        getString(R.string.btnTerminarEntrenamiento)
);
activity.setAccionToolbar(this::confirmarTerminarEntrenamiento);
activity.ocultarNavegacionInferior();
```

La acción “Terminar” desaparece automáticamente cuando otro fragment vuelve a configurar la toolbar.

En esta pantalla, sustituir temporalmente el icono izquierdo por un vector con forma de chevron hacia abajo y cambiar su descripción de contenido a “Minimizar entrenamiento”. No mostrar `ic_arrow_back.xml` mientras el entrenamiento esté abierto. Crear `ic_expand_more.xml` o un nombre equivalente si no existe un recurso apropiado.

### Navegación inferior

Agregar a `MainActivity` métodos públicos simples:

```text
ocultarNavegacionInferior()
mostrarNavegacionInferior()
```

Estos métodos solo cambian la visibilidad de `bottomNavigation`.

No restaurar la navegación inferior desde `onPause()`, porque `onPause()` también ocurre al abrir el selector de ejercicios. Debe permanecer oculta durante ese flujo.

Restaurarla cuando el entrenamiento se minimice, termine o descarte.

### Chevron hacia abajo para minimizar

El control izquierdo debe mostrar un chevron apuntando hacia abajo, no una flecha hacia la izquierda.

Agregar a `MainActivity` un método sencillo para configurarlo temporalmente, por ejemplo:

```text
mostrarControlMinimizarEntrenamiento(Runnable accion)
```

El método debe:

- Mostrar el icono apuntando hacia abajo.
- Usar la descripción de contenido “Minimizar entrenamiento”.
- Ejecutar la acción proporcionada por `EntrenamientoActivoFragment`.

Al pulsarlo:

1. No pedir confirmación.
2. No terminar ni descartar la sesión.
3. Conservar `EntrenamientoEnCurso` en `MainActivity`.
4. Regresar a la pantalla anterior.
5. Volver a mostrar la navegación inferior.
6. Mostrar la barra compacta del entrenamiento activo.

Al configurar cualquier otra toolbar, restaurar `ic_arrow_back`, su descripción normal y el comportamiento `onBackPressed()`. El icono hacia abajo no debe quedar visible en otras pantallas.

El botón físico de retroceso, mientras se muestre `EntrenamientoActivoFragment`, debe realizar la misma acción de minimizar. Usar el mecanismo AndroidX existente con una implementación breve y sin Navigation Component.

### Barra compacta del entrenamiento minimizado

Agregar en `activity_main.xml`, inmediatamente encima de `bottomNavigation`, un contenedor inicialmente oculto:

```text
layoutEntrenamientoMinimizado
├── tvEntrenamientoMinimizado
├── tvTiempoEntrenamientoMinimizado
├── btnVolverEntrenamiento
└── btnDescartarEntrenamientoMinimizado
```

Representación:

```text
┌────────────────────────────────────────┐
│ Entrenamiento activo       00:18:34    │
│        [ VOLVER ]  [ DESCARTAR ]        │
└────────────────────────────────────────┘
```

- La barra solo aparece si existe una sesión activa y su Fragment está minimizado.
- `btnVolverEntrenamiento` reabre la misma sesión, oculta la barra y vuelve a ocultar la navegación inferior.
- `btnDescartarEntrenamientoMinimizado` pide confirmación, limpia la sesión y oculta la barra.
- El tiempo se calcula usando el instante inicial guardado; no mantener un segundo cronómetro independiente.

## Abrir el entrenamiento

Agregar en `MainActivity` un método público:

```text
mostrarEntrenamientoActivo(...)
```

Debe:

- Abrir `EntrenamientoActivoFragment` con back stack.
- Ocultar la navegación inferior.
- Ocultar la barra compacta si el entrenamiento estaba minimizado.
- Entregar únicamente datos simples mediante argumentos cuando sea necesario.

### Desde `HomeFragment`

Reemplazar el Toast provisional de `iniciarEntrenamiento()` por la navegación al entrenamiento activo después del pequeño estado de carga existente.

La pantalla de inicio contiene actualmente una rutina de demostración. Para esta primera versión puede entregar nombres de ejercicios de ejemplo al nuevo fragment. Dejar un `TODO` para reemplazarlos por la rutina asignada real.

### Desde `RutinaAdapter`

Reemplazar el Toast provisional de `btnIniciarRutina` por un callback hacia `RutinasFragment`.

El adapter no debe navegar directamente ni conocer `MainActivity`.

Flujo:

```text
RutinaAdapter
    └── callback con la Rutina seleccionada
            └── RutinasFragment
                    └── MainActivity.mostrarEntrenamientoActivo(...)
```

Como los modelos actuales no implementan `Parcelable`, no pasar `Rutina` directamente en un `Bundle`. Convertir únicamente los datos simples necesarios, por ejemplo:

- Nombre de la rutina.
- Lista de nombres de ejercicios.

Cada ejercicio puede comenzar con una serie editable en esta primera versión. Dejar un `TODO` para cargar cantidades y objetivos estructurados cuando el modelo de rutinas deje de guardar las series como un texto de presentación.

No guardar una referencia a `Rutina` dentro de `MainActivity`.

## Terminar entrenamiento

Al pulsar “Terminar” en la toolbar:

1. Verificar que exista al menos una serie completada.
2. Si no hay ninguna, informar al usuario y mantener la pantalla abierta.
3. Si existe al menos una, mostrar un `AlertDialog` de confirmación.
4. Mostrar en el diálogo un resumen breve de duración, volumen y series completadas.
5. Si el usuario cancela, continuar el entrenamiento.
6. Si confirma:
   - Detener el reloj general.
   - Detener el descanso.
   - Ocultar el panel de descanso.
   - Restaurar la navegación inferior.
   - Mostrar un Toast provisional de confirmación.
   - Regresar a la pantalla anterior.

Dejar un `TODO` para guardar el entrenamiento real y actualizar los últimos entrenamientos del perfil cuando exista persistencia.

## Descartar entrenamiento

`btnDescartarEntrenamiento` debe usar `FitTrack.Button.Danger` y mostrar una confirmación.

Si el usuario confirma:

- Detener ambos temporizadores.
- Descartar los cambios en memoria.
- Restaurar la navegación inferior.
- Regresar a la pantalla anterior.

Si cancela, no hacer nada.

El chevron hacia abajo de la toolbar no descarta el entrenamiento: solamente lo minimiza. El descarte continúa disponible en este botón y en la barra compacta.

## Ciclo de vida y temporizadores

- No dejar callbacks ejecutándose después de destruir el Fragment.
- Quitar los `Runnable` del `Handler` en `onDestroy()`.
- Al abrir `EjerciciosFragment`, conservar el instante de inicio y los datos actuales en los campos del Fragment.
- Al regresar del selector, volver a calcular la duración usando el instante original.
- El tiempo transcurrido mientras se selecciona un ejercicio debe seguir contando como parte del entrenamiento.
- El descanso también debe calcularse con una marca de tiempo final para que no se congele visualmente mientras se abre el selector.
- Al regresar, si el descanso ya terminó, ocultar el panel.
- Al minimizar, conservar ejercicios, series, checks, hora de inicio y descanso en `EntrenamientoEnCurso`.
- Al reabrir desde la barra compacta, reconstruir las vistas usando el mismo estado.
- No implementar persistencia ante cierre del proceso en esta primera versión.

Usar `SystemClock.elapsedRealtime()` para evitar errores si cambia la hora del dispositivo.

## Textos en `strings.xml`

Agregar todos los textos visibles. Como mínimo:

```text
tvToolbarTituloEntrenamientoActivo
btnTerminarEntrenamiento
cdMinimizarEntrenamiento
tvEntrenamientoMinimizado
tvTiempoEntrenamientoMinimizado
btnVolverEntrenamiento
btnDescartarEntrenamientoMinimizado
tvLabelDuracionEntrenamiento
tvDuracionEntrenamiento
tvLabelVolumenEntrenamiento
tvVolumenEntrenamiento
tvLabelSeriesEntrenamiento
tvSeriesEntrenamiento
tvTituloEjerciciosEntrenamiento
btnAgregarEjercicioEntrenamiento
btnDescartarEntrenamiento
tvNombreEjercicioEntrenamiento
cdQuitarEjercicioEntrenamiento
tvCabeceraPesoEntrenamiento
tvCabeceraRepeticionesEntrenamiento
btnAgregarSerieEntrenamiento
tvNumeroSerieEntrenamiento
etPesoSerieEntrenamiento_hint
etRepeticionesSerieEntrenamiento_hint
cbSerieCompletada
tvTituloDescansoEntrenamiento
btnRestarDescanso
tvTiempoDescanso
btnSumarDescanso
btnOmitirDescanso
```

Agregar también strings para los diálogos:

```text
tvTituloQuitarEjercicio
tvMensajeQuitarEjercicio
btnConfirmarQuitarEjercicio
btnCancelarQuitarEjercicio
tvTituloTerminarEntrenamiento
tvMensajeTerminarEntrenamiento
btnConfirmarTerminarEntrenamiento
btnCancelarTerminarEntrenamiento
tvTituloDescartarEntrenamiento
tvMensajeDescartarEntrenamiento
btnConfirmarDescartarEntrenamiento
btnCancelarDescartarEntrenamiento
```

Si un texto pertenece a un componente concreto, su nombre debe coincidir con el id. Para valores dinámicos, usar placeholders de formato en los recursos.

No hardcodear textos visibles en Java o XML, excepto Toasts y Snackbars, tal como permite `AGENTS.md`.

## Diseño visual

Seguir `guia_uso_recursos_visuales.md` y el mockup de sesión activa como referencia, sin copiarlo literalmente.

Usar principalmente:

- `@color/colorBackground` para el fondo.
- `@color/colorSurface` para tarjetas y panel inferior.
- `@color/colorSurfaceHeader` para cabeceras.
- `@color/colorPrimary` para identidad y serie completada.
- `@color/colorActionPrimary` para acciones principales.
- `@color/colorActionConfirm` para confirmación y descanso.
- `@color/colorSuccessContainer` para una serie completada si se necesita distinguirla.
- `@color/colorErrorText` para quitar o descartar.
- `@color/colorTextPrimary` para datos principales.
- `@color/colorTextSecondary` y `colorTextTertiary` para etiquetas.
- `@dimen/spacing_*` para separación.
- `@style/FitTrack.Container.Card` para tarjetas.
- `@style/FitTrack.Button.Compact` para `-15 s`, `+15 s` y `OMITIR`.
- `@style/FitTrack.Button.Danger` para descartar.
- `@style/FitTrack.Text.Metric` para los valores del resumen.

Reutilizar:

- `bg_card.xml`.
- `bg_input.xml`.
- `bg_input_error.xml`.
- `bg_number_badge.xml`.
- Los recursos de botones existentes.

Crear un icono vectorial de eliminación solo si no existe un recurso apropiado. No usar imágenes descargadas ni librerías externas.

Mantener una interfaz Android clásica, con tarjetas compactas y la paleta teal/morado/turquesa del proyecto.

## Convenciones obligatorias

- Java y vistas XML tradicionales.
- Una sola `MainActivity`.
- Pantallas como fragments.
- Código organizado por feature.
- `ScrollView` con prefijo `sv`.
- `View` separador con prefijo `view`.
- Variables con nombres descriptivos.
- Todos los ids con el prefijo correspondiente.
- Todos los textos visibles en `strings.xml`.
- Sin dependencias nuevas.
- Sin ViewModel, repositorios, servicios o patrones avanzados para esta primera versión.
- Métodos cortos y fáciles de leer.
- Clases y métodos públicos documentados con Javadoc.
- No modificar archivos no relacionados.
- No dibujar otra toolbar ni otra navegación inferior dentro del fragment.

## Orden recomendado de implementación

1. Crear `SerieEntrenamiento.java`.
2. Crear `EjercicioEntrenamiento.java`.
3. Crear `item_serie_entrenamiento.xml`.
4. Crear `item_ejercicio_entrenamiento.xml`.
5. Crear `EntrenamientoEjercicioAdapter.java`.
6. Crear `fragment_entrenamiento_activo.xml`.
7. Agregar todos los textos necesarios a `strings.xml`.
8. Crear `EntrenamientoActivoFragment.java`.
9. Implementar el reloj general.
10. Implementar el resumen de volumen y series.
11. Implementar completar y desmarcar series.
12. Implementar el temporizador de descanso.
13. Implementar agregar y quitar series o ejercicios.
14. Reutilizar `EjerciciosFragment` como selector.
15. Agregar métodos de navegación y visibilidad a `MainActivity`.
16. Agregar el chevron para minimizar y la barra compacta en `activity_main.xml`.
17. Implementar `EntrenamientoEnCurso` y la reapertura de una sesión minimizada.
18. Conectar el inicio desde `HomeFragment`.
19. Conectar `btnIniciarRutina` mediante callback desde `RutinaAdapter` y `RutinasFragment`.
20. Implementar terminar y descartar.
21. Revisar estáticamente recursos, referencias y ciclo de vida.

## Criterios de aceptación

La tarea se considera terminada cuando:

- Iniciar un entrenamiento abre `EntrenamientoActivoFragment`.
- La navegación inferior desaparece durante la sesión.
- La toolbar muestra un chevron hacia abajo, “Entrenamiento activo” y “Terminar”.
- El chevron hacia abajo minimiza el entrenamiento sin pedir confirmación ni perder su estado.
- Al minimizar aparecen la navegación inferior y la barra compacta del entrenamiento.
- “Volver” desde la barra compacta reabre la misma sesión.
- “Descartar” desde la barra compacta pide confirmación y elimina la sesión.
- “Terminar” no aparece fuera de este fragment.
- La duración comienza en cero y se actualiza cada segundo.
- El volumen usa exclusivamente `peso × repeticiones` de series completadas.
- La métrica de series muestra completadas sobre totales.
- Los ejercicios aparecen en un RecyclerView.
- Cada ejercicio puede quitarse con confirmación.
- Cada serie muestra número, peso, repeticiones y check, en ese orden.
- El número aparece dentro de un recuadro.
- Una serie inválida no puede marcarse como completada.
- Completar una serie actualiza volumen y cantidad.
- Desmarcar una serie revierte correctamente sus aportes.
- Cada ejercicio permite agregar sus propias series.
- El botón “Agregar ejercicio” está inmediatamente después del último ejercicio.
- Agregar ejercicio abre el selector existente y conserva el entrenamiento actual.
- “Descartar entreno” aparece debajo de “Agregar ejercicio”.
- El panel de descanso está oculto inicialmente.
- Completar una serie muestra el panel y comienza en `03:00`.
- La fila de descanso mantiene el orden `-15 s | tiempo | +15 s | OMITIR`.
- `-15 s` nunca produce tiempo negativo.
- `+15 s` actualiza el contador inmediatamente.
- “Omitir” detiene y oculta el descanso.
- El panel se oculta automáticamente al llegar a cero.
- Terminar requiere al menos una serie completada y pide confirmación.
- Descartar pide confirmación.
- Salir restaura la navegación inferior.
- No quedan callbacks de temporizadores ejecutándose después de destruir el fragment.
- No se creó otra Activity.
- No se agregaron dependencias.
- No existen textos visibles hardcodeados fuera de las excepciones permitidas.
- Todos los recursos referenciados existen.

## Verificación final

No ejecutar Gradle ni compilar el proyecto, porque `AGENTS.md` indica que solamente debe hacerse cuando el usuario lo solicite explícitamente.

Realizar una revisión estática:

1. Revisar paquetes e imports.
2. Revisar ids y prefijos.
3. Revisar strings, drawables, colores y dimensiones.
4. Buscar textos visibles hardcodeados.
5. Confirmar que el panel de descanso esté fuera del `ScrollView`.
6. Confirmar que la navegación inferior permanezca oculta al abrir el selector.
7. Confirmar que minimizar muestre la navegación y la barra compacta, y que terminar o descartar limpien la sesión.
8. Confirmar que el volumen se recalcule desde todos los modelos y no mediante incrementos acumulativos frágiles.
9. Confirmar que solamente las series completadas participen en el volumen.
10. Confirmar que el adapter guarde los cambios de peso y repeticiones en los modelos.
11. Confirmar que el contenedor de series se limpie antes de volver a inflar filas.
12. Confirmar que los callbacks de ambos temporizadores se cancelen correctamente.
13. Confirmar que no se hayan agregado dependencias ni Activities.
14. Informar qué archivos fueron creados o modificados y qué `TODO` quedan pendientes.
