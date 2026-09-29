# Mapa de pantallas y navegación de FitTrack

## Propósito de este documento

Este archivo explica, en lenguaje sencillo, cómo está organizada actualmente la aplicación, qué hace cada pantalla, cómo se llega a ella y qué ocurre al usar sus botones o volver atrás.

La información se obtuvo revisando el código actual del repositorio. Los archivos `PLAN_*.md` se describen al final por separado: un plan expresa una intención o especificación, pero no demuestra por sí solo que una función esté implementada.

## Cómo está organizada la aplicación

FitTrack utiliza una sola Activity: `MainActivity`. Puede imaginarse como el marco fijo de la aplicación. Dentro de ese marco se reemplaza el Fragment visible cada vez que el usuario cambia de pantalla.

`MainActivity` es responsable de:

- Mostrar u ocultar la toolbar superior.
- Mostrar u ocultar la navegación inferior.
- Alojar el Fragment actual en `fragmentContainer`.
- Cambiar entre las pestañas principales Inicio, Rutinas y Perfil.
- Abrir pantallas secundarias y guardarlas en la pila de retroceso.
- Administrar la flecha de volver de la toolbar.
- Cerrar la sesión y regresar al login.

Archivos relacionados:

- Java: `app/src/main/java/ue/edu/co/fittrackandroid/hoy/MainActivity.java`
- Layout: `app/src/main/res/layout/activity_main.xml`
- Menú inferior: `app/src/main/res/menu/bottom_navigation_menu.xml`

La toolbar y la navegación inferior no se dibujan nuevamente dentro de cada Fragment. Ambas pertenecen a `MainActivity`.

## Flujo general actual

```text
Abrir aplicación
       |
       v
LoginFragment
       |
       | Crear cuenta
       v
CrearCuentaFragment
       |
       | Atrás
       v
LoginFragment
       |
       | Iniciar sesión (simulado)
       v
HomeFragment / Inicio
       |
       +----------------------+----------------------+
       |                      |                      |
       v                      v                      v
    Inicio                RutinasFragment        PerfilFragment
       |                      |                      |
       | Nuevo ejercicio      | Nueva rutina        | Cambiar foto
       v                      v                      | Guardar nombre
CrearEjercicioFragment   CrearRutinaFragment         | Cambiar contraseña
                               | Agregar ejercicio   | Cerrar sesión
                               v                      v
                        EjerciciosFragment      LoginFragment
                               |
                               | Elegir ejercicio
                               v
                        CrearRutinaFragment
```

La lista de rutinas tiene además su propia salida hacia la pantalla de modificación:

```text
RutinasFragment
└── VER DETALLES
    └── ModificarRutinaFragment
        ├── GUARDAR → RutinasFragment
        └── BORRAR RUTINA → confirmación → RutinasFragment
```

La lista de ejercicios propios del perfil tiene una salida parecida:

```text
PerfilFragment
└── Tocar ejercicio propio
    └── ModificarEjercicioFragment
        ├── GUARDAR → PerfilFragment
        └── BORRAR EJERCICIO → confirmación → PerfilFragment
```

En los dos casos la pantalla anterior ya vuelve a consultar su lista al regresar, porque lo hace en `onResume`. Por eso la pantalla modificada no necesita avisar con un `FragmentResult`.

El resumen de un entrenamiento terminado tiene su propia salida hacia el borrado del registro guardado:

```text
ResumenEntrenamientoFragment
└── BORRAR ENTRENAMIENTO
    └── confirmación
        ├── CANCELAR → permanece en el resumen
        └── BORRAR → elimina → regresa → actualiza historial
```

Este borrado no usa `onResume`: la pantalla del resumen envía un `FragmentResult` (`REQUEST_ENTRENAMIENTO_ELIMINADO`) y es `HomeFragment` quien vuelve a consultar su información al regresar.

El entrenamiento activo todavía no aparece en este flujo porque no existe como código de la aplicación. Actualmente, el botón de iniciar sí consulta el detalle de la rutina y abre `EntrenamientoActivoFragment`.

## Navegación inferior

La barra inferior tiene tres destinos principales:

| Opción | Pantalla que abre | Comportamiento |
|---|---|---|
| Inicio | `HomeFragment` | Limpia las pantallas secundarias abiertas y muestra Inicio. |
| Rutinas | `RutinasFragment` | Limpia las pantallas secundarias abiertas y muestra Rutinas. |
| Perfil | `PerfilFragment` | Limpia las pantallas secundarias abiertas y muestra Perfil. |

Estas tres pantallas se consideran pantallas raíz. Cuando se selecciona una opción inferior, `MainActivity` elimina la pila de pantallas secundarias antes de hacer el cambio.

## Comportamiento del botón Atrás

Hay dos clases de regreso:

1. En una pantalla secundaria, como Crear ejercicio, Crear rutina o Seleccionar ejercicio, la flecha de la toolbar llama al retroceso de `MainActivity`. Se elimina el Fragment superior de la pila y reaparece la pantalla anterior.
2. En una pantalla raíz, si no hay ninguna pantalla secundaria en la pila, el botón Atrás se entrega al comportamiento normal de Android. Normalmente esto significa salir o dejar la aplicación en segundo plano.

Casos concretos:

- Crear ejercicio → Atrás → Inicio.
- Crear rutina → Atrás → Rutinas.
- Modificar rutina → Atrás → Rutinas, sin guardar nada.
- Modificar ejercicio → Atrás → Perfil, sin guardar ni borrar nada.
- Selector de ejercicios → Atrás → Crear rutina, conservando la instancia anterior mientras permanezca en la pila.
- Selector de ejercicios → elegir un ejercicio → el selector devuelve el ejercicio y regresa automáticamente a Crear rutina.
- Selector de ejercicios abierto desde Modificar rutina → el ejercicio se agrega a la rutina que ya estaba cargada, conservando su nombre, día y ejercicios.
- Crear cuenta → Atrás → Login, que vuelve a mostrarse sin toolbar.
- Cambiar contraseña → Atrás → Perfil.
- Login no se abre usando la pila de retroceso. Al estar en Login y pulsar Atrás, se aplica el comportamiento normal de Android.
- Inicio, Login y las pestañas inferiores son raíces: al abrirlos se limpia la pila de pantallas secundarias.
- Cerrar sesión limpia las pantallas secundarias y reemplaza la pantalla actual por Login.

## Pantallas implementadas

### 1. Login

Responsabilidad:

- Recibir correo y contraseña.
- Permitir mostrar u ocultar la contraseña.
- Entrar a la aplicación mediante un inicio de sesión actualmente simulado.
- Abrir el registro de una cuenta nueva.

Cómo se abre:

- Al iniciar la aplicación, porque `MainActivity` abre el Login directamente al arrancar.
- Al confirmar “Cerrar sesión” desde Perfil.

Acciones:

- **Iniciar sesión:** desactiva brevemente el botón, muestra un estado de carga y abre Inicio. No valida realmente las credenciales.
- **Icono del ojo:** alterna entre contraseña visible y oculta.
- **Crear cuenta:** abre `CrearCuentaFragment` como pantalla secundaria.

Elementos globales:

- La toolbar y la navegación inferior permanecen ocultas. Al volver del registro, `LoginFragment` vuelve a ocultarlas.

Archivos relacionados:

- Java: `app/src/main/java/ue/edu/co/fittrackandroid/login/LoginFragment.java`
- Layout: `app/src/main/res/layout/fragment_login.xml`

Pendiente o provisional:

- No hay autenticación real.
- No se guarda una sesión al iniciar.
- El registro no envía los datos a ningún backend.
- La recuperación de contraseña ya no existe en el login: se cambió por la opción “Cambiar contraseña” del perfil.

### 2. Inicio / Hoy

Responsabilidad:

- Mostrar la fecha actual.
- Presentar el resumen visual del día definido en el layout.
- Mostrar la acción para iniciar un entrenamiento.
- Mostrar los tres entrenamientos recientes de demostración.

Cómo se abre:

- Después del login simulado.
- Desde la opción Inicio de la navegación inferior.

Acciones:

- **Iniciar entrenamiento:** desactiva el botón durante unos instantes y abre `EntrenamientoActivoFragment`.
- **Lista de últimos entrenamientos:** es informativa, sin navegación.

Al volver desde Crear ejercicio:

- Inicio restaura la toolbar principal, con el título general y sin flecha ni acción derecha.

Archivos relacionados:

- Java: `app/src/main/java/ue/edu/co/fittrackandroid/hoy/HomeFragment.java`
- Layout: `app/src/main/res/layout/fragment_home.xml`
- Modelo: `app/src/main/java/ue/edu/co/fittrackandroid/hoy/UltimoEntrenamiento.java`
- Adapter: `app/src/main/java/ue/edu/co/fittrackandroid/hoy/UltimoEntrenamientoAdapter.java`
- Layout de fila: `app/src/main/res/layout/item_ultimo_entrenamiento.xml`

Pendiente o provisional:

- El inicio del entrenamiento es una simulación visual.
- La pantalla de entrenamiento activo aún no está conectada.

### 3. Crear ejercicio

Responsabilidad:

- Recibir el nombre de un ejercicio.
- Mostrar campos visuales para grupo muscular, tipo de equipo, peso y repeticiones.
- Mostrar una opción futura para multimedia.

Cómo se abre:

- Desde el botón Nuevo ejercicio de Inicio.

Toolbar:

- Título “Crear ejercicio”.
- Flecha de volver.
- Acción “Guardar”.

Acciones:

- **Guardar:** exige que el nombre no esté vacío. Si es válido, muestra “Ejercicio guardado” y regresa a Inicio.
- **Multimedia:** solo muestra un mensaje “próximamente”.
- **Grupo muscular, tipo de equipo, peso y repeticiones:** cada campo solo muestra un mensaje “próximamente”.
- **Atrás:** regresa a Inicio sin guardar.

Archivos relacionados:

- Java: `app/src/main/java/ue/edu/co/fittrackandroid/ejercicios/CrearEjercicioFragment.java`
- Layout: `app/src/main/res/layout/fragment_crear_ejercicio.xml`

Pendiente o provisional:

- El ejercicio no se guarda en una base de datos ni se agrega a la lista del selector.
- Los campos distintos del nombre aún no permiten seleccionar valores.
- La multimedia aún no se carga.

### 4. Rutinas

Responsabilidad:

- Mostrar una lista de planes de entrenamiento.
- Mostrar los ejercicios incluidos dentro de cada tarjeta de rutina.
- Dar acceso a la creación de una rutina nueva.
- Ofrecer una acción futura para iniciar una rutina vacía.

Cómo se abre:

- Desde la opción Rutinas de la navegación inferior.

Acciones generales:

- **Nueva rutina:** abre `CrearRutinaFragment` como pantalla secundaria.
- **Empezar rutina vacía:** solo muestra un mensaje “próximamente”.

Acciones de cada tarjeta:

- **Menú de opciones:** solo muestra un Toast con el nombre de la rutina.
- **Ver detalles:** abre `ModificarRutinaFragment` como pantalla secundaria, con el identificador de esa rutina.
- **Iniciar:** consulta el detalle de la rutina y abre `EntrenamientoActivoFragment`.

Datos actuales:

- Las rutinas se leen del backend con `GET /rutinas`.
- La lista se vuelve a consultar cuando otra pantalla avisa que una rutina se creó, se modificó o se borró.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/RutinasFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_rutinas.xml`
- Adapter de rutinas: `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/RutinaAdapter.java`
- Layout de tarjeta: `app/src/main/res/layout/item_rutina.xml`
- Adapter de ejercicios internos: `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/RutinaEjercicioAdapter.java`
- Layout de ejercicio interno: `app/src/main/res/layout/item_rutina_ejercicio.xml`
- Modelos: `Rutina.java` y `EjercicioRutina.java` dentro de la carpeta `rutinas`.

### 5. Crear rutina

Responsabilidad:

- Recibir el nombre de una rutina.
- Permitir agregar ejercicios mediante el selector.
- Crear una primera serie para cada ejercicio agregado.
- Permitir agregar más series.
- Recibir peso objetivo y repeticiones por serie.

Cómo se abre:

- Desde Nueva rutina en `RutinasFragment`.

Toolbar:

- Título “Crear rutina”.
- Flecha de volver.
- Acción “Guardar”.

Estados visuales:

- Sin ejercicios: muestra un estado vacío y el botón para agregar el primero.
- Con ejercicios: muestra el RecyclerView y un botón Agregar ejercicio después de la lista.

Acciones:

- **Agregar ejercicio:** abre `EjerciciosFragment`.
- **Agregar serie:** añade una serie nueva únicamente al ejercicio correspondiente.
- **Guardar:** valida el nombre, que exista al menos un ejercicio y que los valores escritos tengan un formato válido. Después muestra un Toast y regresa a Rutinas.
- **Atrás:** regresa a Rutinas sin persistir la rutina.

Importante sobre los datos:

- La rutina creada solo vive en memoria mientras la instancia del Fragment permanezca en la pila.
- El mensaje de guardado no representa almacenamiento real.
- El mismo ejercicio puede añadirse más de una vez.
- Peso y repeticiones pueden quedar vacíos; si se escriben, deben ser valores válidos.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/CrearRutinaFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_crear_rutina.xml`
- Adapter editable: `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/CrearRutinaEjercicioAdapter.java`
- Layout de ejercicio: `app/src/main/res/layout/item_ejercicio_crear_rutina.xml`
- Layout de serie: `app/src/main/res/layout/item_serie_rutina.xml`
- Modelos: `EjercicioRutinaEditable.java` y `SerieRutina.java` dentro de la carpeta `rutinas`.

### 6. Selector de ejercicios

Responsabilidad:

- Mostrar los ejercicios disponibles.
- Buscar por texto.
- Filtrar por Todos, Pecho, Espalda o Pierna.
- Devolver el ejercicio seleccionado a la pantalla que abrió el selector.

Cómo se abre actualmente:

- Desde Agregar ejercicio en `CrearRutinaFragment`.
- Desde Agregar ejercicio en `ModificarRutinaFragment`.

Toolbar:

- Título “Seleccionar ejercicio”.
- Flecha de volver.
- Sin acción derecha.

Acciones:

- **Escribir en el buscador:** filtra la lista por nombre.
- **Elegir un grupo muscular:** combina ese filtro con el texto del buscador.
- **Tocar un ejercicio:** envía su identificador, nombre y grupo muscular mediante `FragmentResult`, regresa a la pantalla que abrió el selector y lo añade al final.
- **Atrás:** regresa a la pantalla que abrió el selector sin añadir nada.

Datos actuales:

- La lista contiene seis ejercicios de ejemplo definidos en el propio Fragment.
- Los ejercicios creados desde Crear ejercicio no aparecen aquí.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/ejercicios/EjerciciosFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_ejercicios.xml`
- Adapter: `app/src/main/java/ue/edu/co/fittrackandroid/ejercicios/EjercicioAdapter.java`
- Layout de fila: `app/src/main/res/layout/item_ejercicio.xml`
- Modelo: `app/src/main/java/ue/edu/co/fittrackandroid/ejercicios/Ejercicio.java`

### 7. Modificar rutina

Responsabilidad:

- Consultar una rutina que ya existe y abrirla con sus datos.
- Cambiar el nombre y el día de entrenamiento.
- Agregar ejercicios y series, igual que en Crear rutina.
- Guardar los cambios en el backend.
- Borrar la rutina, previa confirmación.

Cómo se abre:

- Desde **Ver detalles** en una tarjeta de `RutinasFragment`. El identificador de la rutina viaja en los argumentos del Fragment.

Toolbar:

- Título “Modificar rutina”.
- Flecha de volver.
- Acción “Guardar”, deshabilitada hasta que la rutina termina de cargar.

Estados visuales:

- Cargando: solo el indicador, con el formulario oculto.
- Con la rutina cargada: el mismo formulario de Crear rutina, con nombre, día, ejercicios y series ya escritos.
- Sin ejercicios: estado vacío con el botón para agregar el primero.
- Error: mensaje y botón **Reintentar** cuando `GET /rutinas/{id}` no responde.

Acciones:

- **Agregar ejercicio:** abre `EjerciciosFragment`. El ejercicio vuelve a la rutina y se agrega al final con su primera serie.
- **Agregar serie:** añade una serie nueva únicamente al ejercicio correspondiente.
- **Guardar:** valida el formulario y envía `PUT /rutinas/{id}` con el mismo cuerpo que usa la creación. Si el backend responde que el nombre ya existe (409), el error aparece debajo del campo nombre. Al salir bien muestra un Toast y regresa a Rutinas.
- **Borrar rutina:** pide confirmación en un diálogo y, al aceptarla, envía `DELETE /rutinas/{id}`. El borrado es lógico: la rutina deja de aparecer en la lista, pero no se borra de la base de datos.
- **Reintentar:** vuelve a consultar el detalle de la rutina.
- **Atrás:** regresa a Rutinas sin guardar ni borrar nada.

Datos actuales:

- La pantalla no muestra ni edita la descripción, pero la guarda y la vuelve a enviar al actualizar, para no borrar la que ya existía.
- Los ejercicios y las series llegan del backend en el mismo orden en que se guardaron, y ese orden se conserva.
- La pantalla se bloquea con una capa oscura mientras carga, guarda o borra, y las peticiones se cancelan si el usuario se va.
- Todavía no se pueden quitar ejercicios ni series: esa acción sigue pendiente también en Crear rutina.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/vista/ModificarRutinaFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_modificar_rutina.xml`
- Adapter editable compartido con Crear rutina: `app/src/main/java/ue/edu/co/fittrackandroid/rutinas/vista/CrearRutinaEjercicioAdapter.java`
- Modelos: `EjercicioRutinaEditable.java`, `RutinaCrearRequest.java` y `SerieRutina.java` dentro de la carpeta `rutinas`.
- API: `RutinaApiService.java` y `RutinaRepository.java` dentro de la carpeta `rutinas/datos`.

### 8. Perfil

Responsabilidad:

- Mostrar y cambiar la foto de perfil.
- Mostrar y editar el nombre.
- Mostrar el correo definido en la interfaz.
- Mostrar la lista de ejercicios de la rutina.
- Cambiar la contraseña de la cuenta.
- Cerrar sesión.

Cómo se abre:

- Desde la opción Perfil de la navegación inferior.

Acciones:

- **Foto, icono de cámara o Cambiar foto:** abre el selector de documentos de Android para elegir una imagen.
- **Guardar nombre:** valida que no esté vacío y lo guarda localmente en `SharedPreferences`.
- **Nuevo ejercicio:** abre `CrearEjercicioFragment` como pantalla secundaria.
- **Tocar un ejercicio de “Mis ejercicios”:** toda la fila es pulsable y abre `ModificarEjercicioFragment` con el identificador de ese ejercicio.
- **Cambiar contraseña:** abre `CambiarContrasenaFragment` como pantalla secundaria.
- **Cerrar sesión:** muestra un diálogo de confirmación. Al aceptar, limpia las preferencias de sesión, limpia la pila secundaria y abre Login.

Persistencia actual:

- El nombre y el URI de la foto se guardan localmente en preferencias propias del perfil.
- Cerrar sesión conserva el nombre y la foto, porque solo limpia las preferencias llamadas `sesion`.
- Si la imagen guardada deja de estar disponible, se vuelve a mostrar el avatar predeterminado.
- La lista de ejercicios se consulta al backend con `GET /ejercicios/mis-ejercicios` y se vuelve a consultar cada vez que el perfil vuelve a mostrarse, para que se vean los ejercicios creados, modificados o borrados desde otras pantallas.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/perfil/PerfilFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_perfil.xml`
- Modelo: `app/src/main/java/ue/edu/co/fittrackandroid/hoy/UltimoEntrenamiento.java`

Pendiente o provisional:

- El nombre y el correo no provienen de un usuario autenticado real.
- Los entrenamientos recientes deben reemplazarse por registros reales.
- El cierre de sesión deberá eliminar tokens o credenciales cuando exista autenticación.

### 9. Crear cuenta

Responsabilidad:

- Recibir los datos de una cuenta nueva: nombre, correo, contraseña y su confirmación.

Cómo se abre:

- Con el botón “Crear cuenta” del login, como pantalla secundaria.

Acciones:

- **Atrás:** vuelve al login, que oculta de nuevo la toolbar.
- **Guardar:** valida nombre, correo, contraseña y confirmación con las mismas reglas del backend. Si todo está correcto envía `POST auth/register`; cuando el servidor responde `201` con token y nombre, guarda token, nombre y correo en la sesión y abre Inicio. Un `409` deja la pantalla abierta señalando el correo ya registrado.

Elementos globales:

- Toolbar secundaria con título “Crear cuenta”, flecha de retroceso y acción “Guardar”.
- La navegación inferior permanece oculta.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/registro/CrearCuentaFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_crear_cuenta.xml`

Pendiente o provisional:

- La contraseña se valida entre 8 y 72 caracteres, viaja solo en la petición y nunca se guarda en el dispositivo.
- La confirmación de la contraseña se compara únicamente en pantalla, porque el backend no la recibe.

### 10. Cambiar contraseña

Responsabilidad:

- Recibir la contraseña actual, la nueva y la confirmación de la nueva.

Cómo se abre:

- Con el botón “Cambiar contraseña” del perfil, como pantalla secundaria.

Acciones:

- **Atrás:** vuelve al perfil.
- **Guardar:** valida los tres campos. Si todo está correcto muestra un mensaje y regresa al perfil.

Elementos globales:

- Toolbar secundaria con título “Cambiar contraseña”, flecha de retroceso y acción “Guardar”.
- La navegación inferior permanece visible porque la abre el perfil.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/perfil/CambiarContrasenaFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_cambiar_contrasena.xml`

Pendiente o provisional:

- La contraseña actual no se verifica contra ninguna fuente de datos y la nueva no se guarda.

### 11. Modificar ejercicio

Responsabilidad:

- Consultar un ejercicio propio y abrir el formulario con sus datos actuales.
- Cambiar el nombre y el grupo muscular.
- Guardar los cambios en el backend.
- Borrar el ejercicio, previa confirmación.

Cómo se abre:

- Al tocar cualquier ejercicio de la lista “Mis ejercicios” del perfil. Solo viaja el identificador del ejercicio, porque el nombre y el grupo muscular se consultan al backend al abrir la pantalla.

Toolbar:

- Título “Modificar ejercicio”.
- Flecha de volver.
- Acción “Guardar”, deshabilitada hasta que el ejercicio termina de cargar y mientras no haya un grupo muscular válido.

Estados visuales:

- Cargando: capa oscura con el indicador centrado y el formulario oculto.
- Con el ejercicio cargado: el mismo formulario de Crear ejercicio, con el nombre y el grupo muscular ya escritos.
- Error: mensaje y botón **Reintentar** cuando `GET /ejercicios/{id}` no responde.

Acciones:

- **Guardar:** valida el nombre y el grupo con las mismas reglas de Crear ejercicio y envía `PUT /ejercicios/{id}`. El cuerpo es el mismo de la creación, porque el backend reemplaza los datos del ejercicio por los que se envían. Al salir bien muestra un Toast y regresa al perfil.
- **Borrar ejercicio:** pide confirmación en un diálogo y, al aceptarla, envía `DELETE /ejercicios/{id}`. El borrado es lógico: el ejercicio deja de aparecer en el perfil y entre los ejercicios disponibles, pero no se borra de la base de datos.
- **Reintentar:** vuelve a consultar el ejercicio por su identificador.
- **Atrás:** regresa al perfil sin guardar ni borrar nada.

Datos actuales:

- La pantalla se bloquea con una capa oscura mientras guarda o borra, y las peticiones se cancelan si el usuario se va.
- El backend solo deja consultar, modificar y borrar ejercicios del usuario autenticado.
- Los nombres repetidos están permitidos, así que la pantalla no los revisa.
- El perfil no necesita enterarse del resultado: vuelve a consultar sus ejercicios al regresar, porque lo hace en `onResume`.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/co/fittrackandroid/ejercicios/vista/ModificarEjercicioFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_modificar_ejercicio.xml`
- Layout de fila seleccionable del perfil: `app/src/main/res/layout/item_ejercicio_perfil.xml`
- API: `EjercicioApiService.java` y `EjercicioRepository.java` dentro de la carpeta `ejercicios/datos`.
- Modelos: `EjercicioRequest.java` y `EjercicioResponse.java` dentro de la carpeta `ejercicios/modelo`.

### 12. Resumen del entrenamiento terminado

Responsabilidad:

- Mostrar en solo lectura el resultado de un entrenamiento ya terminado.
- Presentar el nombre, el día y la hora, las métricas finales, la distribución de grupos musculares y los ejercicios con sus series.
- Borrar el entrenamiento guardado, previa confirmación.

Cómo se abre:

- Al confirmar **Terminar** en `EntrenamientoActivoFragment`, una vez que el backend confirma que el entrenamiento se guardó.
- Al tocar un registro de **Últimos entrenamientos** en `HomeFragment`.

Estados visuales:

- Cargando: solo el indicador, cuando el resumen hay que reconstruirlo con `GET /entrenamientos/{id}`.
- Con el resumen: el contenido completo desplazable, con el botón de borrar al final.
- Error: mensaje y botón **Reintentar** cuando la consulta no pudo completarse.

Acciones:

- **Borrar entrenamiento:** pide confirmación en un diálogo y, al aceptarla, envía `DELETE /entrenamientos/{id}`. El borrado es lógico: el entrenamiento deja de aparecer en el historial y ya no se puede consultar, pero no se borra de la base de datos. Al salir bien muestra un Toast, avisa a `HomeFragment` con un `FragmentResult`, olvida el resumen guardado en `MainActivity` y regresa a la pantalla anterior.
- **Reintentar:** vuelve a consultar el detalle del entrenamiento.
- **Atrás:** regresa a la pantalla anterior sin borrar nada.

Datos actuales:

- El botón de borrar solo se muestra cuando existe un identificador válido. Sin él el botón se oculta y nunca se intenta eliminar nada.
- El identificador lo entrega el backend, tanto al crear el entrenamiento como al abrirlo del historial. Nunca se usa un identificador local ni el de la rutina.
- El endpoint responde `204` sin cuerpo, así que la pantalla solo revisa que el código de la respuesta sea correcto.
- Mientras se borra se reutiliza el mismo indicador de carga del detalle y se oculta el resumen. Si la eliminación falla, el resumen y el identificador se conservan para poder reintentar.
- La pantalla no modifica la toolbar: la acción de borrar vive en el contenido, no en la barra superior.
- Esta acción es distinta de **Descartar entrenamiento**, que solo borra la sesión en curso y no toca ningún registro guardado.

Archivos relacionados:

- Java de pantalla: `app/src/main/java/ue/edu/fittrackandroid/resumen/vista/ResumenEntrenamientoFragment.java`
- Layout de pantalla: `app/src/main/res/layout/fragment_resumen_entrenamiento.xml`
- API: `EntrenamientoApiService.java` y `EntrenamientoRepository.java` dentro de la carpeta `entrenamiento/datos`.
- Modelo: `ResumenEntrenamiento.java` dentro de la carpeta `resumen/modelo`.

## Funciones provisionales y TODO principales

| Área | Estado actual | Trabajo pendiente |
|---|---|---|
| Sesión | Siempre se inicia mostrando Login. | Guardar la sesión del usuario al entrar. |
| Login | Cualquier intento termina abriendo Inicio después de la espera. | Validar credenciales y manejar errores reales. |
| Registro | Valida los datos en pantalla y abre Inicio. | Enviar el registro al backend y guardar la sesión. |
| Cambio de contraseña | Valida los tres campos y vuelve al perfil. | Verificar la contraseña actual y actualizarla en el backend. |
| Crear ejercicio | Solo valida el nombre y regresa. | Guardar el ejercicio y habilitar el resto de campos. |
| Lista de ejercicios | Seis registros fijos. | Leer ejercicios guardados. |
| Rutinas | Se leen del backend con `GET /rutinas`. | Agregar filtros y orden por día. |
| Crear rutina | Se guarda con `POST /rutinas`. | Nada pendiente para guardar; falta quitar ejercicios y series. |
| Modificar rutina | Consulta con `GET /rutinas/{id}`, actualiza con `PUT` y borra con `DELETE`. | Quitar ejercicios y series desde la tarjeta. |
| Modificar ejercicio | Consulta con `GET /ejercicios/{id}`, actualiza con `PUT` y borra con `DELETE`. | Nada pendiente para consultar, modificar y borrar un ejercicio propio. |
| Iniciar entrenamiento | Solo Toasts o una animación breve. | Abrir y gestionar un entrenamiento activo. |
| Resumen del entrenamiento | Se muestra con el resumen ya construido o reconstruido con `GET /entrenamientos/{id}`; borra con `DELETE /entrenamientos/{id}`. | Nada pendiente para consultar y borrar un entrenamiento guardado. |
| Perfil | Nombre y foto locales; historial de ejemplo. | Integrar datos del usuario y entrenamientos reales. |

## Funcionalidades planificadas en documentos separados

Los siguientes archivos son especificaciones de trabajo. Deben compararse siempre con el código antes de asumir que una función existe:

### `PLAN_ENTRENAMIENTO_ACTIVO.md`

Describe la pantalla `EntrenamientoActivoFragment` con duración, volumen, series, ejercicios editables, temporizador de descanso y acciones para terminar o descartar. Esa pantalla, sus modelos, su adapter y sus layouts ya existen en `app/src/main`. El documento sigue siendo una especificación de trabajo y no debe usarse como prueba de funcionamiento.

### `PLAN_CREAR_RUTINA.md`

Describe la pantalla para crear una rutina y el selector de ejercicios. Gran parte de esa especificación ya tiene clases y layouts correspondientes en el código actual, pero el archivo sigue siendo un documento de planificación y no debe usarse como prueba de funcionamiento. En particular, el guardado definitivo continúa pendiente.

### `PLAN_OPEN_CODE_RUTINAS_FRAGMENT.md`

Describe la composición visual y funcional esperada para la lista de rutinas. Existen `RutinasFragment`, sus tarjetas y sus datos de demostración, pero varias acciones de las tarjetas y el comienzo del entrenamiento continúan como mensajes temporales.

## Entrenamiento activo al navegar

`MainActivity` es la dueña de la sesión en curso (`EntrenamientoEnCurso`), por lo que esta sobrevive a que su pantalla se destruya al abrir el selector de ejercicios o al minimizar.

Decisiones ya implementadas:

- El entrenamiento **sigue activo en memoria** al visitar otra pestaña o al abrir una pantalla secundaria.
- Se minimiza con el chevron hacia abajo de la toolbar o el botón físico de retroceso; no se pierde lo registrado.
- Se recupera desde la **isla compacta** sobre la navegación inferior, que muestra el tiempo transcurrido.
- Se puede **descartar** desde la papelera de la isla, con confirmación.
- Al cerrar sesión, la sesión en curso se descarta obligatoriamente.

Acciones que requieren confirmación previa:

- `mostrarCrearRutina()` y `mostrarEntrenamientoActivo()` pasan por `pedirConfirmacionSiHayEntrenamientoEnCurso()`.
- `mostrarModificarRutina()` no se intercepta: editar o borrar una rutina no reemplaza la sesión en curso, así que el entrenamiento sigue intacto.
- Si hay entrenamiento en curso, el diálogo ofrece tres salidas: **Descartar** (borra la sesión y continúa con la acción), **Reanudar** (vuelve a la pantalla del entrenamiento actual) y **Cancelar** (no hace nada).
- `mostrarSelectorEjercicios()` no se intercepta, porque la usa el propio entrenamiento en curso para agregar ejercicios.

Pendiente:

- La sesión **no sobrevive** a la muerte del proceso ni al cierre de la aplicación: vive solo en memoria. No hay un `ViewModel` ni `onSaveInstanceState` que la respalden.


## Resumen de responsabilidades

```text
MainActivity
├── Toolbar superior
├── Contenedor donde aparece cada Fragment
├── Navegación inferior
├── Pila de retroceso de pantallas secundarias
└── Cambio entre Login, Inicio, Rutinas, Perfil y pantallas secundarias

Fragments raíz
├── HomeFragment
├── RutinasFragment
└── PerfilFragment

Fragments secundarios
├── CrearEjercicioFragment
├── ModificarEjercicioFragment
├── CrearRutinaFragment
├── ModificarRutinaFragment
├── CrearCuentaFragment
├── CambiarContrasenaFragment
└── EjerciciosFragment

Adapters
├── Dibujan las listas y tarjetas
└── No son pantallas independientes
```
