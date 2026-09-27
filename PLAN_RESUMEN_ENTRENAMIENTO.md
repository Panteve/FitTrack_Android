# Plan de implementación — Resumen de entrenamiento

## Objetivo

Implementar un Fragment de solo lectura que muestre el resultado de un entrenamiento terminado.

La pantalla debe incluir, en este orden:

1. Nombre del entrenamiento.
2. Día y hora en que se realizó.
3. Resumen de duración, volumen y series.
4. Distribución porcentual de los grupos musculares trabajados.
5. Revisión simple y no editable de los ejercicios realizados.
6. Series numeradas de cada ejercicio con peso y repeticiones.
7. Abrirse tanto al terminar un entrenamiento como al pulsar un registro de “Últimos entrenamientos” en Inicio.

Este documento complementa `PLAN_ENTRENAMIENTO_ACTIVO.md`. En lo relacionado con la acción “Terminar”, este plan reemplaza el comportamiento provisional de mostrar un Toast y regresar directamente: después de confirmar, se debe abrir `ResumenEntrenamientoFragment`.

Antes de modificar código, leer completamente:

- `AGENTS.md`.
- `guia_uso_recursos_visuales.md`.
- `PLAN_ENTRENAMIENTO_ACTIVO.md`.
- `PLAN_ISLA_ENTRENAMIENTO_ACTIVO.md`.
- `MainActivity.java`.
- Los modelos y adapters de la feature `entrenamiento` cuando estén implementados.

No usar Jetpack Compose, Navigation Component ni dependencias externas.

## Interpretación de las series

Cada fila numerada representa una serie realizada.

Formato esperado:

```text
1   85 kg × 9 reps
2   85 kg × 8 reps
3   80 kg × 10 reps
```

La palabra “series” no debe aparecer después de las repeticiones. El número de la izquierda ya indica cuál serie es y el texto debe decir `reps`.

## Mockup aprobado

```text
┌──────────────────────────────────────────┐
│ ←        RESUMEN DEL ENTRENO             │  ← Toolbar de MainActivity
├──────────────────────────────────────────┤
│                                          │
│ Pierna y abdomen                         │
│ Sábado, 26 de septiembre • 6:30 p. m.   │
│                                          │
│ ┌───────────┬────────────┬─────────────┐ │
│ │ DURACIÓN  │  VOLUMEN   │   SERIES    │ │
│ │  00:48:20 │  6.320 kg  │    12/14    │ │
│ └───────────┴────────────┴─────────────┘ │
│                                          │
│ GRUPOS MUSCULARES TRABAJADOS             │
│                                          │
│ Pierna                            58 %    │
│ ████████████████████░░░░░░░░░░           │
│                                          │
│ Glúteos                           25 %    │
│ ████████░░░░░░░░░░░░░░░░░░░░           │
│                                          │
│ Abdomen                           17 %    │
│ █████░░░░░░░░░░░░░░░░░░░░░░░           │
│                                          │
│ EJERCICIOS REALIZADOS                    │
│                                          │
│ ┌──────────────────────────────────────┐ │
│ │ Sentadilla con barra                 │ │
│ │ Pierna                               │ │
│ │                                      │ │
│ │ ┌───┐  85 kg × 9 reps               │ │
│ │ │ 1 │                               │ │
│ │ └───┘                               │ │
│ │ ┌───┐  85 kg × 8 reps               │ │
│ │ │ 2 │                               │ │
│ │ └───┘                               │ │
│ │ ┌───┐  80 kg × 10 reps              │ │
│ │ │ 3 │                               │ │
│ │ └───┘                               │ │
│ └──────────────────────────────────────┘ │
│                                          │
│ ┌──────────────────────────────────────┐ │
│ │ Plancha                              │ │
│ │ Abdomen                              │ │
│ │                                      │ │
│ │ ┌───┐  0 kg × 45 reps               │ │
│ │ │ 1 │                               │ │
│ │ └───┘                               │ │
│ └──────────────────────────────────────┘ │
│                                          │
└──────────────────────────────────────────┘
│    Inicio       Rutinas        Perfil     │
└──────────────────────────────────────────┘
```

La pantalla no contiene botones para editar, checks, campos de texto ni acciones para agregar series.

## Decisiones confirmadas

- La pantalla será `ResumenEntrenamientoFragment`.
- Será una pantalla de solo lectura.
- Usará un `ScrollView`, siguiendo el patrón de las pantallas extensas del proyecto.
- La toolbar pertenece a `MainActivity`.
- La toolbar mostrará flecha hacia atrás y el título “Resumen del entreno”.
- No tendrá acción en el lado derecho de la toolbar.
- La navegación inferior estará visible porque el entrenamiento ya terminó.
- No se mostrará la isla del entrenamiento, porque la sesión activa ya finalizó.
- Las métricas mantendrán la misma fórmula utilizada durante el entrenamiento.
- La distribución muscular usará componentes Android nativos, no una librería de gráficos.
- Los ejercicios se mostrarán en un `RecyclerView`.
- Las series de cada ejercicio serán simples, numeradas y no editables.
- Solo deben aparecer ejercicios y series realmente completados.

## Flujo de navegación

La pantalla tiene dos puntos de entrada.

### Entrada 1: terminar el entrenamiento activo

```text
EntrenamientoActivoFragment
        │
        │ pulsa “Terminar”
        ▼
diálogo de confirmación
        │
        ├── cancelar → continúa el entrenamiento
        │
        └── confirmar
                ├── crea una copia final del resultado
                ├── detiene temporizadores
                ├── elimina la sesión activa y la isla
                ├── muestra navegación inferior
                └── abre ResumenEntrenamientoFragment
```

### Entrada 2: Últimos entrenamientos de Inicio

```text
HomeFragment
        │
        │ pulsa un elemento de rvUltimosEntrenamientos
        ▼
UltimoEntrenamientoAdapter entrega el elemento mediante callback
        │
        ▼
HomeFragment solicita a MainActivity abrir el resumen seleccionado
        │
        ▼
ResumenEntrenamientoFragment
```

Al pulsar la flecha hacia atrás desde el resumen:

- Regresar a la pantalla desde la que se inició el entrenamiento, normalmente Inicio o Rutinas.
- No reabrir `EntrenamientoActivoFragment`.
- No reconstruir la isla.

Si el usuario selecciona Inicio, Rutinas o Perfil desde la navegación inferior, `MainActivity` debe conservar su comportamiento actual de limpiar las pantallas secundarias y abrir la raíz seleccionada.

## Organización de archivos

Todo el código nuevo pertenece a la feature `entrenamiento`.

```text
app/src/main/
├── java/ue/edu/co/fittrackandroid/
│   ├── hoy/
│   │   └── MainActivity.java
│   ├── HomeFragment.java
│   ├── UltimoEntrenamiento.java
│   ├── UltimoEntrenamientoAdapter.java
│   └── entrenamiento/
│       ├── EntrenamientoActivoFragment.java
│       ├── ResumenEntrenamientoFragment.java
│       ├── ResumenEjercicioAdapter.java
│       ├── ResumenEntrenamiento.java
│       ├── EjercicioResumen.java
│       ├── SerieResumen.java
│       └── GrupoMuscularResumen.java
│
└── res/
    ├── layout/
    │   ├── fragment_resumen_entrenamiento.xml
    │   ├── item_grupo_muscular_resumen.xml
    │   ├── item_ejercicio_resumen.xml
    │   └── item_serie_resumen.xml
    └── values/
        ├── strings.xml
        └── dimens.xml, solamente si falta una medida reutilizable
```

No crear una Activity nueva ni modificar `AndroidManifest.xml`.

## Modelo final del entrenamiento

No mostrar el resumen leyendo directamente campos editables de las vistas del entrenamiento activo.

Antes de eliminar `EntrenamientoEnCurso`, crear una copia final de solo lectura mediante `ResumenEntrenamiento`.

### `ResumenEntrenamiento.java`

Debe contener:

- Nombre del entrenamiento.
- Fecha y hora de inicio en milisegundos de reloj real.
- Duración final en segundos.
- Lista de ejercicios realizados.
- Volumen total calculado.
- Cantidad de series completadas.
- Cantidad total de series que existían al terminar.

Puede calcular volumen y porcentajes mediante métodos sencillos o recibirlos ya calculados. Evitar duplicar valores que puedan quedar inconsistentes.

### `EjercicioResumen.java`

Debe contener:

- Nombre del ejercicio.
- Grupo muscular.
- Lista de `SerieResumen` completadas.

No incluir listeners, estado editable ni referencias a vistas.

### `SerieResumen.java`

Debe contener:

- Peso realizado como `double`.
- Repeticiones realizadas como `int`.

Todas las instancias representan series completadas. No necesita un campo `completada`.

El número de serie se calcula mediante la posición en la lista más uno.

### `GrupoMuscularResumen.java`

Debe contener:

- Nombre del grupo muscular.
- Cantidad de series completadas para ese grupo.
- Porcentaje calculado.

Mantener esta clase sencilla. No crear una jerarquía de estadísticas.

## Registro de la fecha y la hora

`EntrenamientoEnCurso` debe guardar dos referencias temporales distintas:

- `SystemClock.elapsedRealtime()` para calcular duración sin depender de cambios en el reloj.
- `System.currentTimeMillis()` para saber el día y la hora reales de inicio.

El resumen debe conservar `System.currentTimeMillis()` como fecha de inicio.

Formatear la fecha con `SimpleDateFormat` y `Locale("es", "CO")`, siguiendo el patrón ya utilizado en `HomeFragment`.

Formato visual recomendado:

```text
Viernes, 26 de septiembre • 6:30 p. m.
```

No guardar la fecha ya formateada en el modelo; guardar el valor numérico y formatearlo en la pantalla.

## Cálculos del resumen

### Duración

Usar la duración final capturada al confirmar “Terminar”.

Formato:

```text
HH:MM:SS
```

No mantener ningún temporizador activo dentro del resumen.

### Volumen

Mantener la misma fórmula del entrenamiento activo:

```text
volumenSerie = peso × repeticiones
volumenTotal = suma de todas las series completadas
```

Ejemplo:

```text
85 kg × 9 reps = 765 kg
85 kg × 8 reps = 680 kg
80 kg × 10 reps = 800 kg
volumen acumulado = 2.245 kg
```

No incluir series sin completar.

### Series

Mostrar:

```text
series completadas / series totales
```

Ejemplo:

```text
12/14
```

El numerador indica lo realizado. El denominador permite ver si quedaron series pendientes al finalizar.

## Porcentajes de grupos musculares

Calcular la distribución usando las series completadas, porque son las que realmente se trabajaron.

Fórmula:

```text
porcentajeGrupo = series completadas del grupo / total de series completadas × 100
```

Ejemplo:

```text
Pierna: 7 series
Glúteos: 3 series
Abdomen: 2 series
Total: 12 series

Pierna  = 7 / 12 × 100 = 58 %
Glúteos = 3 / 12 × 100 = 25 %
Abdomen = 2 / 12 × 100 = 17 %
```

Usar `Math.round(...)` para mostrar porcentajes enteros.

Ordenar los grupos del porcentaje mayor al menor usando un comparador sencillo. No usar streams complejos.

Cada ejercicio actual tiene un grupo muscular principal. Todas sus series completadas se asignan a ese grupo. No inventar distribuciones secundarias.

Si no hay series completadas, el entrenamiento no debería poder finalizar según `PLAN_ENTRENAMIENTO_ACTIVO.md`; aun así, manejar el caso mostrando el estado vacío sin dividir por cero.

## Layout `fragment_resumen_entrenamiento.xml`

Usar esta estructura:

```text
FrameLayout
└── ScrollView
    └── LinearLayout vertical
        ├── cabecera con nombre, fecha y hora
        ├── tarjeta con duración, volumen y series
        ├── título de grupos musculares
        ├── LinearLayout con porcentajes
        ├── título de ejercicios realizados
        └── RecyclerView de ejercicios
```

Ids sugeridos:

```text
layoutResumenEntrenamiento
svResumenEntrenamiento
layoutContenidoResumenEntrenamiento
layoutCabeceraResumenEntrenamiento
tvNombreResumenEntrenamiento
tvFechaResumenEntrenamiento
layoutMetricasResumenEntrenamiento
layoutDuracionResumenEntrenamiento
tvLabelDuracionResumenEntrenamiento
tvDuracionResumenEntrenamiento
layoutVolumenResumenEntrenamiento
tvLabelVolumenResumenEntrenamiento
tvVolumenResumenEntrenamiento
layoutSeriesResumenEntrenamiento
tvLabelSeriesResumenEntrenamiento
tvSeriesResumenEntrenamiento
tvTituloGruposMuscularesResumen
layoutGruposMuscularesResumen
tvTituloEjerciciosResumen
rvEjerciciosResumen
tvResumenSinEjercicios
```

Usar el prefijo `sv` para el `ScrollView`, como indica `AGENTS.md`.

Configurar `rvEjerciciosResumen` con:

- `layout_height="wrap_content"`.
- `nestedScrollingEnabled="false"`.
- `LinearLayoutManager`.

El desplazamiento general pertenece al `ScrollView`.

## Cabecera

La cabecera debe mostrar:

- Nombre del entrenamiento como título principal.
- Fecha y hora en texto secundario.

Si el entrenamiento no tiene nombre, usar un valor de respaldo definido en `strings.xml`, por ejemplo “Entrenamiento libre”.

No mostrar campos editables.

## Tarjeta de métricas

Reutilizar la misma distribución visual del resumen superior de `EntrenamientoActivoFragment`:

```text
DURACIÓN | VOLUMEN | SERIES
00:48:20 | 6.320 kg | 12/14
```

Las tres columnas deben tener el mismo ancho.

Los valores son finales y no deben actualizarse después de abrir la pantalla.

## Layout `item_grupo_muscular_resumen.xml`

Cada grupo muscular debe mostrar:

```text
Nombre del grupo                         58 %
████████████████████░░░░░░░░░░
```

Usar componentes nativos:

- Dos `TextView` en una fila para nombre y porcentaje.
- Un `ProgressBar` horizontal debajo.

Ids sugeridos:

```text
layoutGrupoMuscularResumen
tvNombreGrupoMuscularResumen
tvPorcentajeGrupoMuscularResumen
pbGrupoMuscularResumen
```

No usar una librería externa de gráficos ni crear un gráfico circular.

Las filas pueden inflarse dinámicamente dentro de `layoutGruposMuscularesResumen`; no hace falta otro RecyclerView porque habrá pocos grupos.

## Layout `item_ejercicio_resumen.xml`

Cada tarjeta debe ser sencilla y de solo lectura:

```text
LinearLayout vertical
├── nombre del ejercicio
├── grupo muscular
└── LinearLayout vertical con series realizadas
```

Ids sugeridos:

```text
cardEjercicioResumen
tvNombreEjercicioResumen
tvGrupoMuscularEjercicioResumen
layoutSeriesEjercicioResumen
```

No agregar:

- Menú de opciones.
- Botón para quitar.
- Botón para agregar serie.
- Checkbox.
- `EditText`.

## Layout `item_serie_resumen.xml`

Cada fila muestra solamente el número y el resultado:

```text
┌───┐  85 kg × 9 reps
│ 1 │
└───┘
```

Ids sugeridos:

```text
layoutSerieResumen
tvNumeroSerieResumen
tvDetalleSerieResumen
```

Reutilizar `bg_number_badge.xml` para el número si encaja visualmente.

Formar el detalle mediante un recurso con placeholders:

```xml
<string name="tvDetalleSerieResumen">%1$s kg × %2$d reps</string>
```

El peso se entrega ya formateado como texto para evitar mostrar `.0` cuando sea entero.

## `ResumenEjercicioAdapter.java`

El adapter recibe una lista de `EjercicioResumen`.

Cada `ViewHolder` debe:

1. Mostrar nombre y grupo muscular.
2. Limpiar `layoutSeriesEjercicioResumen` con `removeAllViews()`.
3. Inflar una fila `item_serie_resumen.xml` por cada serie completada.
4. Numerar las filas desde uno.
5. Mostrar peso y repeticiones.

No incluir callbacks porque la pantalla es de solo lectura.

## Creación del resumen al terminar

Antes de limpiar `EntrenamientoEnCurso`:

1. Recorrer sus ejercicios.
2. Para cada ejercicio, copiar solamente las series completadas.
3. Omitir ejercicios sin series completadas.
4. Crear `SerieResumen` con valores numéricos ya validados.
5. Crear `EjercicioResumen` con sus series copiadas.
6. Crear `ResumenEntrenamiento` con datos finales.
7. Detener temporizadores.
8. Ocultar el panel de descanso.
9. Ocultar y limpiar la isla.
10. Restaurar la navegación inferior.
11. Mostrar `ResumenEntrenamientoFragment`.

No entregar al resumen referencias mutables a las listas del entrenamiento activo.

## Comunicación y navegación

Como `ResumenEntrenamiento` contiene listas anidadas y todavía no existe persistencia, mantener temporalmente el último resumen en `MainActivity` mediante una única referencia en memoria.

Agregar métodos simples:

```text
mostrarResumenEntrenamiento(ResumenEntrenamiento resumen)
obtenerResumenEntrenamientoActual()
limpiarResumenEntrenamientoActual()
```

Esta es una solución temporal para el proyecto universitario. Dejar un `TODO` para reemplazarla por almacenamiento real cuando se implemente el historial.

`mostrarResumenEntrenamiento(...)` debe:

- Guardar temporalmente el resumen.
- Limpiar el estado de entrenamiento activo.
- Ocultar la isla.
- Mostrar la navegación inferior.
- Reemplazar `EntrenamientoActivoFragment` por `ResumenEntrenamientoFragment` sin agregar otra capa innecesaria al back stack.

Así, al pulsar atrás en el resumen se recupera la pantalla existente debajo del entrenamiento y nunca se vuelve a una sesión finalizada.

El mismo método `mostrarResumenEntrenamiento(...)` debe utilizarse cuando el resumen se abra desde Inicio. No crear un segundo Fragment ni otra pantalla para el historial.

## Toolbar

En `ResumenEntrenamientoFragment.onResume()`:

```java
MainActivity activity = (MainActivity) requireActivity();
activity.mostrarToolbarSecundaria(
        getString(R.string.tvToolbarTituloResumenEntrenamiento),
        true,
        null
);
activity.mostrarNavegacionInferior();
activity.ocultarIslaEntrenamiento();
```

La toolbar debe restaurar `ic_arrow_back`. No debe conservar el chevron hacia abajo utilizado por el entrenamiento activo.

## Acceso desde “Últimos entrenamientos” de Inicio

La sección ya existe en `HomeFragment` y utiliza:

- `rvUltimosEntrenamientos`.
- `UltimoEntrenamientoAdapter`.
- `UltimoEntrenamiento`.
- `item_ultimo_entrenamiento.xml`.

### `UltimoEntrenamientoAdapter.java`

Agregar un callback sencillo, por ejemplo:

```java
public interface OnEntrenamientoClickListener {
    void onEntrenamientoClick(UltimoEntrenamiento entrenamiento);
}
```

El adapter debe:

- Recibir el callback en el constructor.
- Ejecutarlo cuando se pulse una fila.
- Continuar encargándose únicamente de mostrar los datos.
- No conocer `MainActivity` ni realizar transacciones de fragments.

### `HomeFragment.java`

Al configurar el adapter, entregar un método como callback:

```text
abrirResumenEntrenamiento(UltimoEntrenamiento entrenamiento)
```

Ese método debe obtener el resumen completo asociado con el registro y llamar a:

```text
MainActivity.mostrarResumenEntrenamiento(...)
```

### `UltimoEntrenamiento.java`

El modelo actual solo contiene nombre, fecha formateada y duración en minutos. Eso no alcanza para construir el resumen detallado.

Para los datos temporales de demostración, ampliarlo para que pueda relacionarse con un `ResumenEntrenamiento` completo. Mantener una solución sencilla, por ejemplo un campo adicional:

```text
ResumenEntrenamiento resumen
```

Agregar su getter y documentarlo. No duplicar dentro de `UltimoEntrenamiento` todas las listas de ejercicios y series.

Cuando exista persistencia real, este campo podrá reemplazarse por un identificador del registro guardado y el resumen se cargará desde almacenamiento.

### Datos de demostración

Actualizar `crearEntrenamientosDeEjemplo()` para que cada fila tenga un resumen completo de demostración:

- Nombre.
- Fecha y hora.
- Duración.
- Ejercicios.
- Series completadas.
- Grupos musculares.

No mostrar una pantalla vacía al pulsar los registros actuales. Dejar un `TODO` para reemplazar los ejemplos por entrenamientos guardados realmente.

### Layout de la fila

Reutilizar `item_ultimo_entrenamiento.xml`. No crear otro layout.

Asegurar que su contenedor principal sea pulsable y enfocable, conservando su diseño actual. Puede mantenerse el chevron o agregarse posteriormente si existe un recurso coherente, pero no es obligatorio para esta tarea.

## Textos en `strings.xml`

Agregar como mínimo:

```text
tvToolbarTituloResumenEntrenamiento
tvNombreResumenEntrenamiento
tvNombreResumenEntrenamiento_fallback
tvFechaResumenEntrenamiento
tvLabelDuracionResumenEntrenamiento
tvDuracionResumenEntrenamiento
tvLabelVolumenResumenEntrenamiento
tvVolumenResumenEntrenamiento
tvLabelSeriesResumenEntrenamiento
tvSeriesResumenEntrenamiento
tvTituloGruposMuscularesResumen
tvTituloEjerciciosResumen
tvResumenSinEjercicios
tvNombreGrupoMuscularResumen
tvPorcentajeGrupoMuscularResumen
tvNombreEjercicioResumen
tvGrupoMuscularEjercicioResumen
tvNumeroSerieResumen
tvDetalleSerieResumen
```

Usar recursos con placeholders para fecha, volumen, series, porcentajes y detalle de serie.

No hardcodear textos visibles en Java o XML, excepto Toasts y Snackbars según la excepción de `AGENTS.md`.

## Diseño visual

Seguir `guia_uso_recursos_visuales.md`.

Usar principalmente:

- `@color/colorBackground` para el fondo.
- `@color/colorSurface` para tarjetas.
- `@color/colorSurfaceHeader` para cabeceras.
- `@color/colorPrimary` para porcentajes y barras.
- `@color/colorTextPrimary` para títulos y valores.
- `@color/colorTextSecondary` para fecha, hora y grupo muscular.
- `@color/colorDivider` para separadores.
- `@dimen/spacing_*` para márgenes y rellenos.
- `@style/FitTrack.Container.Card` para tarjetas.
- `@style/FitTrack.Text.Title` para el nombre del entrenamiento.
- `@style/FitTrack.Text.Metric` para duración, volumen y series.
- `bg_number_badge.xml` para el número de serie.

Mantener la estética Android clásica del proyecto, con fondo gris claro, tarjetas blancas compactas y detalles teal.

No agregar botones innecesarios, animaciones ni librerías de gráficos.

## Orden recomendado de implementación

1. Crear `SerieResumen.java`.
2. Crear `EjercicioResumen.java`.
3. Crear `GrupoMuscularResumen.java`.
4. Crear `ResumenEntrenamiento.java`.
5. Crear `item_serie_resumen.xml`.
6. Crear `item_ejercicio_resumen.xml`.
7. Crear `item_grupo_muscular_resumen.xml`.
8. Crear `ResumenEjercicioAdapter.java`.
9. Crear `fragment_resumen_entrenamiento.xml`.
10. Agregar los textos necesarios a `strings.xml`.
11. Crear `ResumenEntrenamientoFragment.java`.
12. Implementar el cálculo de porcentajes.
13. Actualizar `EntrenamientoEnCurso` para guardar la fecha real de inicio.
14. Crear la copia final al terminar el entrenamiento.
15. Agregar la navegación del resumen en `MainActivity`.
16. Reemplazar el Toast final de `EntrenamientoActivoFragment` por la navegación al resumen.
17. Agregar el callback de selección a `UltimoEntrenamientoAdapter`.
18. Conectar el callback desde `HomeFragment`.
19. Asociar un resumen completo con cada dato de demostración.
20. Revisar estáticamente todos los recursos y referencias.

## Criterios de aceptación

La tarea se considera terminada cuando:

- Confirmar “Terminar” abre `ResumenEntrenamientoFragment`.
- Pulsar cualquier fila de “Últimos entrenamientos” en Inicio abre `ResumenEntrenamientoFragment`.
- El adapter entrega el entrenamiento mediante callback y no navega directamente.
- Cada dato de demostración abre un resumen completo, no una pantalla vacía.
- El entrenamiento activo deja de existir después de crear su copia final.
- La isla se oculta y no vuelve a aparecer para una sesión terminada.
- La navegación inferior está visible.
- La toolbar muestra flecha atrás, título y ninguna acción derecha.
- Se muestra el nombre del entrenamiento.
- Se muestran el día y la hora reales de inicio.
- Duración, volumen y series coinciden con el entrenamiento terminado.
- El volumen usa `peso × repeticiones` de series completadas.
- Los porcentajes se calculan a partir de series completadas por grupo muscular.
- Los grupos se muestran del porcentaje mayor al menor.
- La distribución usa barras nativas y no librerías externas.
- Se muestran únicamente ejercicios con al menos una serie completada.
- Las series se muestran numeradas desde uno.
- Cada fila usa el formato `85 kg × 9 reps`.
- No existen `EditText`, checks ni botones de modificación en el resumen.
- Pulsar atrás no reabre el entrenamiento finalizado.
- No se creó una Activity adicional.
- No se agregaron dependencias.
- Todos los textos visibles están en `strings.xml`.
- Todos los ids respetan los prefijos de `AGENTS.md`.
- No quedan temporizadores activos en el resumen.

## Verificación final

No ejecutar Gradle ni compilar el proyecto, porque `AGENTS.md` indica que solamente debe hacerse cuando el usuario lo solicite explícitamente.

Realizar una revisión estática:

1. Revisar paquetes e imports.
2. Revisar ids, prefijos y nombres de archivos.
3. Revisar strings, colores, dimensiones, layouts y drawables.
4. Buscar textos visibles hardcodeados.
5. Confirmar que se copien únicamente series completadas.
6. Confirmar que el resumen no conserve referencias mutables a la sesión activa.
7. Confirmar la fórmula del volumen.
8. Confirmar la fórmula de porcentajes y el caso sin series.
9. Confirmar que el RecyclerView no tenga scroll interno.
10. Confirmar que el adapter no incluya callbacks de edición.
11. Confirmar que terminar detenga todos los temporizadores.
12. Confirmar que la isla quede oculta y la navegación inferior visible.
13. Confirmar que atrás no reabra la sesión terminada.
14. Confirmar que pulsar un entrenamiento reciente abra el registro correcto.
15. Confirmar que `UltimoEntrenamientoAdapter` no conozca `MainActivity`.
16. Confirmar que no se agregaron Activities ni dependencias.
17. Informar qué archivos se crearon o modificaron y qué `TODO` quedan pendientes.
