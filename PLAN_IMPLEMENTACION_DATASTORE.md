# Plan de implementación: DataStore para filtros de ejercicios

## Objetivo

Usar Preferences DataStore en una función que FitTrack ya tiene: el buscador y el filtro por grupo muscular de la pantalla de ejercicios.

Cuando el usuario vuelva a la pantalla o Android cierre el proceso, se restaurarán:

- El texto de búsqueda.
- El grupo muscular seleccionado.

No se agregará una pantalla de configuración ni una nueva funcionalidad visible.

## Por qué estos datos pertenecen a DataStore

Son solamente dos valores pequeños de configuración temporal. No necesitan:

- Tablas.
- Relaciones.
- Consultas complejas.
- Actualizaciones parciales de muchas filas.

Room se reservará para el entrenamiento activo, que sí es información estructurada. `SharedPreferences` seguirá manejando la sesión existente y no se migrará en esta etapa.

## Implementación elegida para Java

El proyecto está escrito en Java. Se utilizará Preferences DataStore con el adaptador oficial para RxJava 3, porque permite leer y escribir DataStore desde Java sin introducir código Kotlin ni bloquear el hilo principal.

La documentación oficial indica que solo debe existir una instancia de DataStore para un mismo archivo dentro del proceso. La clase propuesta será, por tanto, un singleton.

Versión verificada en la documentación oficial de Android el 29 de septiembre de 2026: DataStore `1.2.1`.

Referencia oficial: <https://developer.android.com/topic/libraries/architecture/datastore>

## Dependencias

```toml
# gradle/libs.versions.toml
[versions]
datastore = "1.2.1"

[libraries]
datastore-preferences-rxjava3 = {
    module = "androidx.datastore:datastore-preferences-rxjava3",
    version.ref = "datastore"
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(libs.datastore.preferences.rxjava3)
}
```

No se necesita Proto DataStore porque solo se guardarán pares clave-valor.

## Estructura de carpetas

DataStore pertenece al feature de ejercicios:

```text
app/src/main/java/ue/edu/co/fittrackandroid/ejercicios/
├── datos/
│   ├── EjercicioApiService.java
│   ├── EjercicioRepository.java
│   └── PreferenciasEjerciciosDataStore.java
├── modelo/
│   └── FiltrosEjercicios.java
└── vista/
    └── EjerciciosFragment.java
```

No se ubicará en `remote`, porque no contiene datos del servidor.

## Archivo y claves

Nombre del archivo DataStore:

```text
preferencias_ejercicios.preferences_pb
```

Claves:

| Clave | Tipo | Valor predeterminado |
|---|---|---|
| `texto_busqueda` | `String` | Cadena vacía |
| `grupo_muscular` | `String` | Cadena vacía, que representa “Todos” |

DataStore no guardará `null`; para el grupo sin filtro se utilizará una cadena vacía y al leerla se convertirá nuevamente a `null` para conservar el comportamiento actual de `EjerciciosFragment`.

## Modelo sencillo

`FiltrosEjercicios` será un modelo inmutable con:

```java
private final String textoBusqueda;
private final String grupoMuscular;
```

Debe tener constructor y getters. No necesita setters.

## Responsabilidad de `PreferenciasEjerciciosDataStore`

La clase tendrá una sola instancia de:

```java
RxDataStore<Preferences>
```

La instancia se construirá una vez con el contexto de aplicación:

```java
new RxPreferenceDataStoreBuilder(
        context.getApplicationContext(),
        "preferencias_ejercicios"
).build();
```

API pública propuesta:

```java
public Single<FiltrosEjercicios> obtenerFiltros();

public Completable guardarFiltros(
        String textoBusqueda,
        String grupoMuscular
);

public Completable limpiarFiltros();
```

Las escrituras deben crear una copia mutable de las preferencias recibidas, cambiar las dos claves y devolver la copia como preferencias inmutables.

## Integración con `EjerciciosFragment`

### Lectura

En `onCreate` o al preparar la pantalla:

1. Obtener la instancia de `PreferenciasEjerciciosDataStore`.
2. Leer los filtros una sola vez.
3. Asignar `textoBusqueda` y `grupoMuscularSeleccionado`.
4. Cuando la vista exista, escribir el texto en `etBuscarEjercicio`.
5. Seleccionar visualmente el chip correspondiente.
6. Aplicar los filtros a ambas listas cuando lleguen los ejercicios del backend.

La lectura debe terminar antes de presentar el estado definitivo para evitar que primero aparezca “Todos” y después cambie el filtro.

Si ocurre un error de lectura:

- Usar texto vacío y “Todos”.
- Mantener la pantalla operativa.
- No cerrar la aplicación.

### Escritura

Guardar ambos valores juntos:

- Cuando el usuario seleccione un chip.
- Cuando la pantalla pase a `onStop`, para conservar el texto final del buscador.

Guardar el texto en `onStop` evita escribir al almacenamiento por cada carácter. El estado actual del fragment seguirá actualizando las listas inmediatamente en memoria.

### Limpieza

`limpiarFiltros()` se puede usar si posteriormente se agrega una acción “Limpiar filtros”. No es necesario agregar ese botón ahora.

Cerrar sesión no necesita borrar estos filtros, porque no son información privada ni pertenecen a una cuenta específica.

## Manejo de RxJava

`EjerciciosFragment` tendrá un `CompositeDisposable` para conservar las suscripciones de lectura y escritura.

Reglas:

- Agregar cada suscripción al `CompositeDisposable`.
- Limpiarlo en `onDestroy`.
- No conservar referencias a las vistas dentro de la clase DataStore.
- Actualizar vistas únicamente si el fragment sigue agregado y la vista existe.
- En caso de error de escritura, conservar el filtro en memoria y mostrar como máximo un aviso simple.

No se debe llamar `blockingGet()` ni bloquear el hilo principal.

## Relación con SharedPreferences

No se modificará `SesionManager`.

| Almacenamiento | Responsabilidad |
|---|---|
| `SharedPreferences` | Token JWT, nombre y correo de la sesión |
| Preferences DataStore | Buscador y grupo muscular seleccionados |
| Room/SQLite | Entrenamiento activo, ejercicios y series |

Esta separación permite demostrar las tres tecnologías sin guardar la misma información varias veces.

## Verificación manual

No se crearán pruebas unitarias ni instrumentadas para esta implementación. La comprobación se realizará manualmente con estos escenarios:

1. Escribir una búsqueda, cambiar de pantalla y volver: el texto debe restaurarse.
2. Seleccionar “Pecho”, cerrar y abrir la aplicación: el chip debe seguir seleccionado.
3. Seleccionar “Todos”: debe guardarse una cadena vacía y restaurarse sin filtro.
4. Restaurar filtros antes de recibir la API: al llegar los ejercicios deben aparecer ya filtrados.
5. Simular un error de lectura: debe mostrarse la lista con valores predeterminados.
6. Cerrar sesión e iniciar otra cuenta: los filtros pueden mantenerse porque son una preferencia general del dispositivo.
7. Rotar la pantalla: no deben crearse varias instancias para el mismo archivo DataStore.

## Criterios de terminado

- Existe una única instancia de DataStore para `preferencias_ejercicios`.
- El buscador y el chip sobreviven al cierre del proceso.
- La pantalla continúa filtrando inmediatamente mientras el usuario escribe.
- No se bloquea el hilo principal.
- Los errores usan valores predeterminados seguros.
- `SesionManager` continúa funcionando con `SharedPreferences` sin cambios.
- No se agregan pantallas nuevas.

## Mejora futura opcional

Si más adelante se agrega una preferencia visible para la duración del descanso, puede guardarse en otro DataStore propio del feature de entrenamiento. No debe añadirse ahora solamente para justificar la tecnología, porque el buscador y los filtros ya ofrecen un caso de uso real.
