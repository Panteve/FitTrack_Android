# Guía de uso de recursos visuales de FitTrack

Esta guía explica cómo reutilizar los colores, dimensiones y estilos definidos para FitTrack. Los recursos están pensados para vistas nativas de Android XML (`Button`, `TextView`, `EditText`, `LinearLayout`, etc.).

No usar `MaterialButton`, `MaterialCardView` ni estilos de Material 3.

## 1. Colores

Archivo: `app/src/main/res/values/colors.xml`

| Uso | Recurso |
|---|---|
| Barra superior, iconos activos y éxito | `@color/colorPrimary` |
| Barra de estado | `@color/colorPrimaryDark` |
| Acción principal: guardar, ingresar, iniciar entrenamiento | `@color/colorActionPrimary` |
| Acción de confirmación: continuar o siguiente ejercicio | `@color/colorActionConfirm` |
| Fondo general | `@color/colorBackground` |
| Tarjetas y barra inferior | `@color/colorSurface` |
| Cabecera de tarjeta | `@color/colorSurfaceHeader` |
| Bordes y separadores | `@color/colorBorder` y `@color/colorDivider` |
| Texto principal | `@color/colorTextPrimary` |
| Texto secundario | `@color/colorTextSecondary` |
| Texto auxiliar | `@color/colorTextTertiary` |
| Estado completado | `@color/colorSuccess` y `@color/colorSuccessContainer` |
| Advertencia | `@color/colorWarningContainer` y `@color/colorWarningText` |
| Error o eliminación | `@color/colorError` y `@color/colorErrorText` |

Ejemplo:

```xml
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/tvTitulo"
    android:textColor="@color/colorTextPrimary" />
```

## 2. Dimensiones

Archivo: `app/src/main/res/values/dimens.xml`

Usar la escala de espaciado para márgenes, rellenos y separaciones:

```xml
android:padding="@dimen/spacing_md"
android:layout_marginTop="@dimen/spacing_sm"
android:layout_marginHorizontal="@dimen/spacing_xl"
```

Medidas principales:

| Elemento | Recurso |
|---|---|
| Botón compacto | `@dimen/button_height_compact` |
| Botón normal | `@dimen/button_height_standard` |
| Botón de acción principal | `@dimen/button_height_primary` |
| Barra de navegación inferior | `@dimen/bottom_navigation_height` |
| Botón flotante | `@dimen/floating_action_button_size` |
| Título | `@dimen/text_size_title` |
| Texto normal | `@dimen/text_size_body` |
| Etiqueta pequeña | `@dimen/text_size_label` |
| Métrica | `@dimen/text_size_metric` |
| Métrica grande, como cronómetro | `@dimen/text_size_metric_large` |

## 3. Estilos de botones

Archivo: `app/src/main/res/values/themes.xml`

Aplicar un estilo en la propiedad `style` de un `Button` nativo. El texto siempre debe venir de `strings.xml`.

```xml
<Button
    style="@style/FitTrack.Button.Primary"
    android:id="@+id/btnGuardarSerie"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/btnGuardarSerie" />
```

| Caso de uso | Estilo |
|---|---|
| Acción principal morada | `@style/FitTrack.Button.Primary` |
| Acción secundaria gris | `@style/FitTrack.Button.Secondary` |
| Confirmar o continuar, en turquesa | `@style/FitTrack.Button.Confirm` |
| Finalizar o eliminar, en rojo | `@style/FitTrack.Button.Danger` |
| Acciones cortas, como temporizador | `@style/FitTrack.Button.Compact` |

Los estilos de botón ya aplican color, tipografía, mayúsculas y altura mínima. Cuando se necesiten bordes rojos o esquinas exactas de 3 dp, se agregará un drawable XML nativo en una etapa posterior.

## 4. Estilos de texto

```xml
<TextView
    style="@style/FitTrack.Text.Title"
    android:id="@+id/tvTituloRutina"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/tvTituloRutina" />
```

| Uso | Estilo |
|---|---|
| Título de pantalla o sección | `@style/FitTrack.Text.Title` |
| Subtítulo o nombre de rutina | `@style/FitTrack.Text.Subtitle` |
| Etiqueta de campo, estado o cabecera | `@style/FitTrack.Text.Label` |
| Peso, repeticiones o valor importante | `@style/FitTrack.Text.Metric` |
| Cronómetro o valor principal | `@style/FitTrack.Text.Metric.Large` |
| Chip de estado completado | `@style/FitTrack.Text.Chip` |

## 5. Contenedores nativos

Los siguientes estilos se pueden aplicar a `LinearLayout` o `ConstraintLayout`:

```xml
<LinearLayout
    style="@style/FitTrack.Container.Card"
    android:id="@+id/layoutResumen"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <!-- Contenido de la tarjeta -->
</LinearLayout>
```

| Uso | Estilo |
|---|---|
| Tarjeta blanca con elevación ligera | `@style/FitTrack.Container.Card` |
| Cabecera gris de una tarjeta | `@style/FitTrack.Container.CardHeader` |
| Fila de una lista | `@style/FitTrack.Container.ListRow` |
| Contenedor de navegación inferior | `@style/FitTrack.Container.BottomNavigation` |

Por ahora los contenedores usan fondo, relleno y elevación. Los bordes y radios de 2 o 3 dp se aplicarán cuando existan los drawables XML de fondo.

## 6. Tema y compatibilidad

El tema de la aplicación es claro y no usa una barra de acción automática.

- En Android 8.0 (API 26), la barra de navegación es negra porque Android todavía no admite iconos oscuros sobre una barra clara.
- Desde Android 8.1 (API 27), la barra de navegación es blanca con iconos oscuros.
- Los archivos en `values-night/` mantienen la apariencia clara cuando el dispositivo activa modo oscuro.

No es necesario configurar estos valores por pantalla: el tema se aplica a toda la app desde `AndroidManifest.xml`.
