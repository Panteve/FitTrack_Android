# Plan de implementación — Fragment de perfil

## Objetivo

Implementar la pantalla de perfil de FitTrack como un `Fragment`, manteniendo la arquitectura actual de una sola `MainActivity` y respetando todas las reglas definidas en `AGENTS.md`.

La pantalla debe permitir:

1. Seleccionar y mostrar una foto de perfil.
2. Cambiar y guardar el nombre del usuario.
3. Consultar el correo electrónico como información de solo lectura.
4. Ver los tres entrenamientos registrados más recientes.
5. Cerrar sesión y regresar al `LoginFragment`.

Antes de comenzar, leer completamente:

- `AGENTS.md`.
- `guia_uso_recursos_visuales.md`.
- Los recursos existentes en `app/src/main/res/values/`.
- `MainActivity.java`, para conservar el sistema actual de navegación.
- Los fragments y adapters existentes, para mantener el mismo nivel de complejidad y estilo.

No usar Jetpack Compose, Navigation Component ni dependencias externas.

## Alcance

El trabajo incluye la interfaz del perfil, la selección local de una imagen, el guardado local del nombre y la imagen, la lista de entrenamientos de ejemplo y la navegación de cierre de sesión.

La aplicación todavía no tiene una autenticación real ni una fuente persistente de entrenamientos. Por eso:

- El nombre y el URI de la foto se pueden guardar temporalmente con `SharedPreferences`.
- El correo puede obtenerse de los datos de sesión si ya existen. Si no existen, debe usarse un valor de demostración almacenado en `strings.xml` y dejarse un `TODO` claro.
- Los tres entrenamientos serán datos de demostración hasta que exista una fuente de datos real.
- Cerrar sesión debe funcionar visualmente y regresar al login, aunque la persistencia completa de autenticación siga pendiente.

No crear una base de datos, repositorio, servicio, ViewModel ni arquitectura adicional para esta tarea.

## Distribución visual

La pantalla debe tener desplazamiento vertical para funcionar correctamente en dispositivos pequeños.

Distribución esperada, de arriba hacia abajo:

```text
┌─────────────────────────────────────┐
│             FOTO                    │
│        [imagen circular]            │
│        [icono de cámara]            │
│          CAMBIAR FOTO               │
├─────────────────────────────────────┤
│ DATOS PERSONALES                    │
│                                     │
│ NOMBRE                              │
│ [ Alex Ramírez                  ]   │
│ [       GUARDAR NOMBRE          ]   │
│                                     │
│ CORREO ELECTRÓNICO                  │
│ alex@correo.com                     │
├─────────────────────────────────────┤
│ ÚLTIMOS ENTRENAMIENTOS   3 REGISTROS│
│                                     │
│ [icono] Pierna y abdomen            │
│         24 de mayo • 58 min         │
│ ─────────────────────────────────── │
│ [icono] Pecho y tríceps             │
│         22 de mayo • 45 min         │
│ ─────────────────────────────────── │
│ [icono] Espalda y bíceps            │
│         20 de mayo • 52 min         │
├─────────────────────────────────────┤
│          [ CERRAR SESIÓN ]          │
└─────────────────────────────────────┘
```

La toolbar superior y la navegación inferior ya pertenecen a `MainActivity`. No deben incluirse nuevamente en `fragment_perfil.xml`.

## Organización de archivos

Todo el código Java relacionado con el perfil debe quedar dentro del paquete de la feature `perfil`.

```text
app/src/main/
├── java/ue/edu/co/fittrackandroid/
│   ├── hoy/
│   │   └── MainActivity.java                 → modificar navegación y cierre de sesión
│   └── perfil/
│       ├── PerfilFragment.java               → lógica principal de la pantalla
│       ├── UltimoEntrenamiento.java          → modelo de cada entrenamiento
│       └── UltimoEntrenamientoAdapter.java   → adaptador de la lista
│
└── res/
    ├── layout/
    │   ├── fragment_perfil.xml               → pantalla completa
    │   └── item_ultimo_entrenamiento.xml      → fila de la lista
    ├── drawable/
    │   └── recursos nuevos solo si son necesarios
    └── values/
        ├── strings.xml                        → todos los textos visibles
        ├── dimens.xml                         → solo medidas reutilizables nuevas
        └── colors.xml                         → evitar cambios salvo necesidad real
```

No crear una nueva Activity ni registrar componentes adicionales en `AndroidManifest.xml`.

## Responsabilidad de cada archivo

### `PerfilFragment.java`

Debe encargarse únicamente de:

- Inflar `fragment_perfil.xml`.
- Obtener las referencias de las vistas.
- Configurar el `RecyclerView`.
- Crear temporalmente los tres entrenamientos de demostración.
- Mostrar el nombre y correo del usuario.
- Validar y guardar el nuevo nombre.
- Abrir el selector de imágenes.
- Mostrar y restaurar la foto seleccionada.
- Pedir confirmación antes de cerrar sesión.
- Solicitar a `MainActivity` que cierre la sesión.
- Restaurar la toolbar principal desde `onResume()`.

Separar la lógica en métodos sencillos y descriptivos. Una organización posible es:

```text
onCreate(...)
    registrar selector de imagen

onCreateView(...)
    inflar layout
    inicializar vistas
    cargar datos del usuario
    configurar lista de entrenamientos
    configurar acciones

inicializarVistas(...)
cargarDatosPerfil()
configurarUltimosEntrenamientos()
crearEntrenamientosDeEjemplo()
configurarAcciones()
abrirSelectorImagen()
guardarNombre()
guardarFotoPerfil(...)
restaurarFotoPerfil()
confirmarCierreSesion()
```

No es obligatorio usar exactamente estos nombres si existe una alternativa más clara, pero la clase no debe concentrar toda la lógica dentro de `onCreateView()`.

### `UltimoEntrenamiento.java`

Modelo simple e inmutable con los datos necesarios para una fila:

- `nombre`.
- `fecha`.
- `duracionMinutos` o un texto de detalle ya preparado.

Preferir guardar los datos por separado y formar el detalle desde recursos cuando sea sencillo. No agregar campos que la pantalla no utilice.

La clase debe tener:

- Campos privados.
- Constructor.
- Getters.
- Javadoc para la clase y sus miembros públicos.

### `UltimoEntrenamientoAdapter.java`

Debe:

- Extender `RecyclerView.Adapter`.
- Usar un `ViewHolder` sencillo.
- Recibir una lista tipada de `UltimoEntrenamiento`.
- Inflar `item_ultimo_entrenamiento.xml`.
- Asignar nombre y detalle a cada fila.
- No incluir navegación ni lógica de negocio.

La lista entregada al adapter debe contener como máximo tres elementos.

### `MainActivity.java`

Modificar solamente lo necesario:

1. Importar `PerfilFragment`.
2. En `obtenerFragment(int itemId)`, devolver `new PerfilFragment()` cuando se seleccione `R.id.navigation_perfil`.
3. Agregar un método público sencillo, por ejemplo `cerrarSesion()`, que:
   - Elimine únicamente los valores asociados con la sesión.
   - Limpie el back stack.
   - Muestre `LoginFragment` mediante la lógica existente.
   - Deje ocultas la toolbar y la navegación inferior.

No duplicar la lógica de `mostrarLogin()` y no crear otra Activity.

## Layout `fragment_perfil.xml`

### Contenedor raíz

Usar un contenedor con desplazamiento vertical, por ejemplo `NestedScrollView`, y dentro un `LinearLayout` vertical.

Ids sugeridos:

- `scrollPerfil` para el contenedor desplazable. Este prefijo no aparece todavía en la tabla de `AGENTS.md`; usarlo de manera consistente y dejar constancia de que `scroll` identifica un `NestedScrollView`.
- `layoutContenidoPerfil` para el contenido vertical.

Propiedades generales:

- Fondo `@color/colorBackground`.
- Ancho completo.
- Separación lateral usando `@dimen/spacing_lg` o `@dimen/spacing_xl`.
- Separación vertical basada en los recursos existentes.
- Espacio inferior suficiente para que el botón no quede pegado a la navegación.

### Tarjeta de perfil

Crear un contenedor con id `layoutTarjetaPerfil` y estilo `FitTrack.Container.Card`.

Debe incluir:

- `ImageView` `imgFotoPerfil`.
- `ImageView` o `ImageButton` `imgCambiarFoto`/`btnIconoCambiarFoto` para el indicador de cámara.
- `Button` `btnCambiarFoto`.
- `TextView` `tvLabelNombrePerfil`.
- `EditText` `etNombrePerfil`.
- `Button` `btnGuardarNombre`.
- `TextView` `tvLabelCorreoPerfil`.
- `TextView` `tvCorreoPerfil`.

La foto debe verse circular. Reutilizar `bg_circle_image.xml` si permite lograr el resultado esperado. No agregar una dependencia para recortar imágenes.

El correo debe verse claramente como información de solo lectura, no como un campo editable deshabilitado.

### Sección de entrenamientos

Crear una tarjeta o sección con:

- `layoutUltimosEntrenamientos`.
- `layoutCabeceraUltimosEntrenamientos`.
- `tvTituloUltimosEntrenamientos`.
- `tvCantidadUltimosEntrenamientos`.
- `rvUltimosEntrenamientos`.
- `tvSinEntrenamientos`, inicialmente oculto, para soportar el estado vacío.

Configurar el `RecyclerView` con desplazamiento interno desactivado si se encuentra dentro del contenedor desplazable principal, porque solo mostrará tres filas.

No agregar un botón “Ver todos” porque no forma parte del alcance solicitado.

### Botón de cierre de sesión

Agregar `btnCerrarSesion` al final de la pantalla.

Usar `@style/FitTrack.Button.Danger` y mantenerlo visualmente separado de la tarjeta de datos personales para evitar pulsaciones accidentales.

## Layout `item_ultimo_entrenamiento.xml`

Cada fila debe ser compacta y contener:

- Contenedor `layoutUltimoEntrenamiento`.
- `ImageView` `imgUltimoEntrenamiento`.
- `TextView` `tvNombreUltimoEntrenamiento`.
- `TextView` `tvDetalleUltimoEntrenamiento`.

El detalle puede tener el formato:

```text
24 de mayo • 58 min
```

Usar colores secundarios para el detalle y un separador inferior cuando corresponda. La fila no necesita botón ni evento de pulsación en esta primera versión.

## Textos y recursos

Todo texto visible debe declararse en `app/src/main/res/values/strings.xml`.

El nombre del string debe coincidir con el id del componente que lo utiliza. Agregar, como mínimo:

```text
btnCambiarFoto
tvLabelNombrePerfil
etNombrePerfil_hint
btnGuardarNombre
tvLabelCorreoPerfil
tvCorreoPerfil
tvTituloUltimosEntrenamientos
tvCantidadUltimosEntrenamientos
tvSinEntrenamientos
btnCerrarSesion
tvTituloCerrarSesion
tvMensajeCerrarSesion
btnConfirmarCerrarSesion
btnCancelarCerrarSesion
```

Para los datos de demostración, usar nombres claros asociados con las filas, por ejemplo:

```text
tvNombreUltimoEntrenamiento1
tvFechaUltimoEntrenamiento1
tvNombreUltimoEntrenamiento2
tvFechaUltimoEntrenamiento2
tvNombreUltimoEntrenamiento3
tvFechaUltimoEntrenamiento3
```

Si un mismo componente necesita una variante de texto, agregar un sufijo corto, como establece `AGENTS.md`.

Los Toasts y Snackbars pueden contener texto directo porque `AGENTS.md` lo permite, aunque se recomienda usar recursos cuando el mensaje pueda reutilizarse.

## Selección y persistencia de la foto

Usar la Activity Result API de AndroidX, sin permisos antiguos de almacenamiento.

Flujo esperado:

1. Registrar `ActivityResultContracts.OpenDocument` en el Fragment.
2. Al pulsar la foto o `btnCambiarFoto`, abrir el selector con el tipo `image/*`.
3. Si el resultado contiene un URI:
   - Solicitar permiso persistente de lectura cuando sea posible.
   - Mostrar la imagen con `imgFotoPerfil.setImageURI(uri)`.
   - Guardar el URI como `String` en `SharedPreferences`.
4. Cuando se abra el perfil, leer el URI guardado e intentar restaurar la imagen.
5. Si el URI no existe o ya no se puede leer, mostrar la imagen predeterminada sin cerrar la aplicación.

No usar Glide, Picasso ni otra librería externa.

Usar nombres claros para las preferencias, por ejemplo:

```text
preferencias_perfil
nombre_usuario
uri_foto_perfil
```

No guardar imágenes como Base64.

## Cambio de nombre

Flujo esperado:

1. Cargar el nombre almacenado al abrir la pantalla.
2. Si no existe, mostrar un nombre inicial de demostración definido en `strings.xml`.
3. Al pulsar `btnGuardarNombre`, obtener el contenido de `etNombrePerfil`.
4. Eliminar espacios sobrantes con `trim()`.
5. Validar que no quede vacío.
6. Si está vacío, mostrar un error en el propio `EditText` o mediante un Toast sencillo.
7. Si es válido, guardarlo en `SharedPreferences` y confirmar al usuario.

No crear una pantalla independiente para editar el nombre.

## Correo electrónico

El correo debe mostrarse en `tvCorreoPerfil` y nunca debe ser editable desde esta pantalla.

Orden de preferencia para obtenerlo:

1. Datos de sesión existentes, si el proyecto ya los tiene cuando se implemente esta tarea.
2. Un valor temporal definido en `strings.xml`.

Si se usa el valor temporal, dejar un `TODO` indicando que se sustituirá por el correo del usuario autenticado.

## Últimos tres entrenamientos

Crear una lista local de demostración dentro de `PerfilFragment`.

Los elementos deben estar ordenados del más reciente al más antiguo. Usar tres ejemplos coherentes con los mockups:

1. Pierna y abdomen — 24 de mayo — 58 minutos.
2. Pecho y tríceps — 22 de mayo — 45 minutos.
3. Espalda y bíceps — 20 de mayo — 52 minutos.

Aunque en el futuro se reciba una lista más grande, mostrar solamente los primeros tres elementos. Realizarlo de manera sencilla y legible, sin streams complejos.

Si la lista está vacía:

- Ocultar `rvUltimosEntrenamientos`.
- Mostrar `tvSinEntrenamientos`.
- Actualizar el contador a cero.

Dejar un `TODO` indicando que los datos deben reemplazarse por los entrenamientos registrados realmente.

## Cierre de sesión

Al pulsar `btnCerrarSesion`:

1. Mostrar un `AlertDialog` con título, mensaje, botón de confirmación y botón para cancelar.
2. Si se cancela, cerrar únicamente el diálogo.
3. Si se confirma, llamar a `((MainActivity) requireActivity()).cerrarSesion()`.
4. `MainActivity` debe limpiar el back stack y mostrar el login.

No borrar automáticamente el nombre ni la foto del perfil salvo que esas preferencias formen parte explícita de los datos de sesión. El cierre debe eliminar únicamente la información que representa una sesión activa.

La implementación actual de `verificarSesionActiva()` devuelve siempre `false`. No desarrollar un sistema completo de autenticación dentro de esta tarea. Dejar claramente indicado el punto pendiente para integrarlo después.

## Navegación y toolbar

En `MainActivity.obtenerFragment(...)`, reemplazar el `TODO` actual de perfil:

```java
} else if (itemId == R.id.navigation_perfil) {
    return new PerfilFragment();
}
```

Adaptar el fragment para que en `onResume()` restaure la toolbar principal:

```java
@Override
public void onResume() {
    super.onResume();
    ((MainActivity) requireActivity()).mostrarToolbarPrincipal();
}
```

No añadir este fragment al back stack cuando se abre desde la navegación inferior. Las opciones de la navegación inferior son pantallas raíz, tal como ya maneja `MainActivity`.

## Diseño visual

Seguir `guia_uso_recursos_visuales.md` y reutilizar los recursos existentes.

Usar principalmente:

- `@color/colorBackground` para el fondo.
- `@color/colorSurface` para tarjetas.
- `@color/colorPrimary` para identidad e iconos activos.
- `@color/colorActionPrimary` para acciones principales.
- `@color/colorTextPrimary` para títulos y nombre.
- `@color/colorTextSecondary` para correo, fecha y duración.
- `@color/colorErrorText` para cerrar sesión.
- `@dimen/spacing_*` para márgenes y rellenos.
- `@style/FitTrack.Text.Title` o `Subtitle` para títulos.
- `@style/FitTrack.Text.Label` para etiquetas.
- `@style/FitTrack.Container.Card` para tarjetas.
- `@style/FitTrack.Button.Primary` para guardar el nombre.
- `@style/FitTrack.Button.Danger` para cerrar sesión.

Reutilizar, si encajan con el diseño:

- `bg_circle_image.xml`.
- `bg_card.xml`.
- `ic_camera.xml`.
- `ic_person.xml`.
- `ic_fitness_center.xml`.

Crear nuevos drawables solamente cuando los existentes no permitan lograr el resultado. Mantener una apariencia Android clásica, con tarjetas blancas compactas, bordes discretos y la paleta teal/morado/turquesa de los mockups.

## Convenciones obligatorias

- Todos los ids XML deben tener un prefijo acorde con el tipo de vista.
- Usar nombres de variables descriptivos en español, como el código existente.
- No hardcodear textos visibles en XML o Java, excepto Toasts y Snackbars.
- No agregar dependencias a Gradle.
- No crear una Activity para el perfil.
- No mover la toolbar o la navegación inferior al layout del Fragment.
- No introducir MVVM, repositorios, interfaces ni clases base para esta funcionalidad.
- Usar bucles y condiciones simples cuando sean más fáciles de leer.
- Documentar clases y métodos públicos con Javadoc.
- Mantener el código dentro de la carpeta de su feature.
- No modificar archivos que no sean necesarios para esta implementación.

## Orden recomendado de implementación

1. Revisar los recursos y patrones existentes.
2. Crear `UltimoEntrenamiento.java`.
3. Crear `item_ultimo_entrenamiento.xml`.
4. Crear `UltimoEntrenamientoAdapter.java`.
5. Crear `fragment_perfil.xml`.
6. Agregar todos los textos requeridos a `strings.xml`.
7. Agregar únicamente las dimensiones o drawables que realmente falten.
8. Crear `PerfilFragment.java` y conectar sus vistas.
9. Implementar el cambio de nombre.
10. Implementar la selección y restauración de la foto.
11. Configurar la lista de los tres entrenamientos.
12. Implementar la confirmación de cierre de sesión.
13. Conectar `PerfilFragment` en `MainActivity`.
14. Revisar estáticamente todas las referencias y recursos.

## Criterios de aceptación

La tarea se considera terminada cuando:

- La opción Perfil de la navegación inferior devuelve un `PerfilFragment` válido.
- La pantalla no contiene otra toolbar ni otra navegación inferior.
- La foto predeterminada se muestra correctamente.
- El usuario puede seleccionar una imagen desde el almacenamiento mediante el selector del sistema.
- La imagen seleccionada vuelve a mostrarse al regresar al perfil.
- El nombre no puede guardarse vacío.
- El nombre válido se conserva localmente.
- El correo aparece visible y no puede editarse.
- Se muestran exactamente los tres entrenamientos de demostración, ordenados del más reciente al más antiguo.
- La lista soporta un estado vacío.
- El cierre de sesión pide confirmación.
- Confirmar el cierre muestra el login y oculta toolbar y navegación inferior.
- No se creó ninguna Activity adicional.
- No se agregó ninguna dependencia externa.
- Todos los textos visibles están en `strings.xml`.
- Todos los ids siguen las convenciones de `AGENTS.md`.
- Las clases y métodos públicos tienen Javadoc.
- No existen referencias a recursos inexistentes.

## Verificación final

No ejecutar Gradle ni compilar el proyecto, porque `AGENTS.md` indica que solamente debe compilarse si el usuario lo solicita explícitamente.

Realizar una revisión estática final:

1. Revisar los imports de las clases nuevas y modificadas.
2. Revisar que los paquetes coincidan con las carpetas.
3. Buscar textos visibles hardcodeados.
4. Confirmar que cada id XML tenga el prefijo correcto.
5. Confirmar que cada string nuevo exista y tenga un nombre coherente con su vista.
6. Confirmar que cada drawable, color, dimensión y layout referenciado exista.
7. Confirmar que el adapter nunca reciba más de tres elementos.
8. Confirmar que no se hayan agregado dependencias.
9. Confirmar que no se haya creado una nueva Activity.
10. Resumir al usuario los archivos creados y modificados, además de los `TODO` que dependan de autenticación o datos reales.
