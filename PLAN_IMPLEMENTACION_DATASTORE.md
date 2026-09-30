# Plan de implementación: DataStore para la duración de descanso

## Objetivo

Usar Preferences DataStore para guardar la duración de descanso predeterminada de los entrenamientos.

El usuario podrá modificar esta preferencia desde una sección nueva dentro de la pantalla de Perfil. Cuando complete una serie, el entrenamiento activo iniciará el descanso con el valor guardado.

No se creará una pantalla adicional: la configuración formará parte de `PerfilFragment`.

## Por qué este dato pertenece a DataStore

La duración de descanso es una preferencia pequeña y simple del usuario en el dispositivo. Solo se necesita guardar un número entero y no requiere:

- Tablas ni relaciones.
- Consultas complejas.
- Sincronización con el backend.
- Historial de cambios.

Room/SQLite continuará almacenando el borrador estructurado del entrenamiento activo. `SharedPreferences` seguirá manejando la sesión existente y no se migrará en esta etapa.

## Comportamiento funcional

- Valor predeterminado: `180` segundos, equivalentes a 3 minutos.
- Valor mínimo: `30` segundos.
- Valor máximo: `600` segundos, equivalentes a 10 minutos.
- Cada pulsación en los controles de Perfil aumentará o disminuirá `15` segundos.
- El valor se mostrará en formato `mm:ss`, por ejemplo `03:00`.
- El cambio se guardará inmediatamente en DataStore.
- Al abrir Perfil nuevamente, se mostrará el último valor guardado.
- Al iniciar un descanso nuevo, `EntrenamientoActivoFragment` usará el valor guardado.
- Los botones actuales de `-15 s` y `+15 s` del entrenamiento activo solo modificarán el descanso que está corriendo. No cambiarán la preferencia permanente.

Esta separación evita que un ajuste ocasional durante una serie cambie accidentalmente todos los descansos futuros.

## Interfaz en Perfil

Antes de implementar el cambio visual se debe leer `guia_uso_recursos_visuales.md` y conservar la apariencia actual del proyecto.

En `fragment_perfil.xml` se agregará una sección de configuración, ubicada después de los datos personales y antes de la lista de ejercicios del usuario.

La sección tendrá:

- Un título: “Configuración de entrenamiento”.
- Una etiqueta: “Descanso predeterminado”.
- Un texto corto que explique que se aplicará al completar una serie.
- Un botón para restar 15 segundos.
- Un `TextView` central con el tiempo seleccionado en formato `mm:ss`.
- Un botón para sumar 15 segundos.

Identificadores propuestos:

| Componente | ID | Recurso de texto |
|---|---|---|
| Título de sección | `tvTituloConfiguracionEntrenamiento` | `tvTituloConfiguracionEntrenamiento` |
| Etiqueta | `tvDescansoPredeterminado` | `tvDescansoPredeterminado` |
| Explicación | `tvDescripcionDescansoPredeterminado` | `tvDescripcionDescansoPredeterminado` |
| Restar tiempo | `btnRestarDescansoPredeterminado` | `btnRestarDescansoPredeterminado` |
| Tiempo seleccionado | `tvTiempoDescansoPredeterminado` | Se asigna con un formato definido en `strings.xml` |
| Sumar tiempo | `btnSumarDescansoPredeterminado` | `btnSumarDescansoPredeterminado` |

Todos los textos visibles se declararán en `res/values/strings.xml`. Los botones se deshabilitarán visualmente cuando el valor alcance su límite mínimo o máximo.

## Implementación elegida para Java

El proyecto está escrito en Java. Se utilizará Preferences DataStore con el adaptador oficial para RxJava 3, porque permite leer y escribir DataStore desde Java sin introducir código Kotlin ni bloquear el hilo principal.

Solo debe existir una instancia de DataStore para un mismo archivo dentro del proceso. La clase encargada se implementará como singleton y recibirá el contexto de aplicación.

Versión revisada en la documentación oficial de Android al redactar este plan: DataStore `1.2.1`.

Referencia oficial: <https://developer.android.com/topic/libraries/architecture/datastore>

## Dependencia

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

No se necesita Proto DataStore porque solo se guardará un par clave-valor.

## Estructura de carpetas

La preferencia pertenece al feature de entrenamiento, aunque se edite desde Perfil:

```text
app/src/main/java/ue/edu/co/fittrackandroid/
├── entrenamiento/
│   ├── datos/
│   │   └── PreferenciasEntrenamientoDataStore.java
│   └── vista/
│       └── EntrenamientoActivoFragment.java
└── perfil/
    └── vista/
        └── PerfilFragment.java
```

`PerfilFragment` podrá usar la clase del feature de entrenamiento porque está modificando una configuración que afecta directamente a ese feature. No se creará una abstracción adicional ni se moverá a `utils`.

## Archivo y clave

Nombre del archivo DataStore:

```text
preferencias_entrenamiento.preferences_pb
```

| Clave | Tipo | Valor predeterminado |
|---|---|---|
| `segundos_descanso_predeterminado` | `int` | `180` |

Al leer o guardar, el valor se limitará al rango de 30 a 600 segundos. Así, un dato inválido no podrá producir un descanso negativo o excesivo.

## Responsabilidad de `PreferenciasEntrenamientoDataStore`

La clase mantendrá una única instancia de:

```java
RxDataStore<Preferences>
```

La instancia se construirá una vez con el contexto de aplicación:

```java
new RxPreferenceDataStoreBuilder(
        context.getApplicationContext(),
        "preferencias_entrenamiento"
).build();
```

Constantes propuestas:

```java
public static final int SEGUNDOS_DESCANSO_PREDETERMINADO = 180;
public static final int SEGUNDOS_DESCANSO_MINIMO = 30;
public static final int SEGUNDOS_DESCANSO_MAXIMO = 600;
public static final int PASO_AJUSTE_DESCANSO = 15;
```

API pública propuesta:

```java
public Single<Integer> obtenerSegundosDescanso();

public Completable guardarSegundosDescanso(int segundosDescanso);
```

La clase no tendrá referencias a Fragments ni a vistas.

## Integración con `PerfilFragment`

### Lectura inicial

Al preparar la vista:

1. Obtener la instancia de `PreferenciasEntrenamientoDataStore`.
2. Leer una vez la duración guardada.
3. Guardarla en una variable `segundosDescansoSeleccionados`.
4. Mostrar el valor con formato `mm:ss`.
5. Habilitar o deshabilitar los botones según los límites.

Mientras termina la lectura, se puede mostrar `03:00` como valor seguro y mantener temporalmente deshabilitados los dos botones para evitar sobrescribir una preferencia que aún no se ha cargado.

Si falla la lectura, se usarán 180 segundos y la pantalla continuará operativa.

### Modificación y guardado

Al pulsar uno de los botones:

1. Sumar o restar 15 segundos.
2. Respetar el rango de 30 a 600 segundos.
3. Actualizar inmediatamente el texto `mm:ss`.
4. Actualizar el estado habilitado de ambos botones.
5. Guardar el nuevo valor en DataStore.

Si falla la escritura, se restaurará en pantalla el último valor confirmado por DataStore y se mostrará un aviso simple. No se agregará un botón “Guardar”, porque cada cambio quedará persistido al instante.

## Integración con `EntrenamientoActivoFragment`

Actualmente el fragment inicia todos los descansos con la constante fija `SEGUNDOS_DESCANSO_BASE = 180`. Esa constante se reemplazará por una variable de instancia, por ejemplo:

```java
private int segundosDescansoPredeterminado = 180;
```

Al preparar el fragment:

1. Leer el valor desde `PreferenciasEntrenamientoDataStore`.
2. Guardarlo en `segundosDescansoPredeterminado`.
3. Si la lectura falla, conservar el valor seguro de 180 segundos.
4. Cuando `iniciarDescanso()` sea llamado, usar esa variable para calcular el final del temporizador y mostrar su tiempo inicial.

La preferencia solo se consultará al entrar al entrenamiento activo. Si el usuario cambia el valor desde Perfil, se aplicará al siguiente entrenamiento que abra. No se intentará cambiar un descanso que ya esté en curso.

Los botones `btnRestarDescanso` y `btnSumarDescanso` mantendrán su comportamiento actual sobre el temporizador activo y no escribirán en DataStore.

## Manejo de RxJava

`PerfilFragment` y `EntrenamientoActivoFragment` mantendrán sus suscripciones en un `CompositeDisposable`.

Reglas:

- Agregar cada lectura y escritura al `CompositeDisposable`.
- Limpiar las suscripciones cuando se destruya la vista del Fragment.
- No usar `blockingGet()` ni bloquear el hilo principal.
- Actualizar las vistas solo si el Fragment sigue agregado y su vista continúa disponible.
- Usar 180 segundos como respaldo ante cualquier error de lectura.

## Relación con los demás almacenamientos

| Almacenamiento | Responsabilidad |
|---|---|
| `SharedPreferences` | Token JWT y datos básicos de la sesión |
| Preferences DataStore | Duración de descanso predeterminada |
| Room/SQLite | Borrador del entrenamiento activo, ejercicios y series |
| Archivos internos | Copia local de la foto de perfil |
| Supabase Storage | Foto de perfil remota |

La duración se considerará una preferencia general de la aplicación en ese dispositivo. Por eso no se borrará al cerrar sesión y no se duplicará en el backend.

## Verificación manual

No se crearán pruebas unitarias ni instrumentadas para esta implementación. La comprobación se realizará manualmente:

1. Abrir Perfil sin valor previo: debe mostrarse `03:00`.
2. Aumentar el descanso, salir de Perfil y volver: debe conservarse el nuevo valor.
3. Cerrar y abrir la aplicación: debe restaurarse el valor guardado.
4. Llegar a `00:30`: el botón de restar debe quedar deshabilitado.
5. Llegar a `10:00`: el botón de sumar debe quedar deshabilitado.
6. Configurar un valor, abrir un entrenamiento y completar una serie: el descanso debe iniciar con ese tiempo.
7. Usar `-15 s` o `+15 s` durante el descanso activo: debe cambiar solo el temporizador actual.
8. Volver a Perfil después del ajuste temporal: debe seguir apareciendo la preferencia permanente anterior.
9. Simular un error de lectura: Perfil y el entrenamiento deben usar `03:00` sin cerrarse.
10. Cerrar sesión e iniciar nuevamente: la duración debe mantenerse como preferencia del dispositivo.

## Criterios de terminado

- Perfil contiene una sección visible para modificar el descanso predeterminado.
- El valor solo puede estar entre 30 y 600 segundos.
- Cada cambio se guarda inmediatamente en DataStore.
- El valor sobrevive al cierre del proceso de la aplicación.
- Los descansos nuevos comienzan con la duración configurada.
- Los ajustes temporales del entrenamiento activo no sobrescriben la preferencia.
- Existe una sola instancia de DataStore para `preferencias_entrenamiento`.
- No se bloquea el hilo principal.
- `SesionManager` continúa usando `SharedPreferences` sin cambios.
- No se crean pruebas unitarias ni instrumentadas.
