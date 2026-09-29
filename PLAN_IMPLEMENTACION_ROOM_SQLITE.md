# Plan de implementación: Room/SQLite para el entrenamiento activo

## Objetivo

Implementar un CRUD local real sin agregar una funcionalidad nueva a FitTrack. La base de datos guardará el borrador del entrenamiento que ya está en curso, con sus ejercicios y series, para poder recuperarlo si Android cierra el proceso.

Room será la capa de acceso y SQLite será el motor local. Por eso, para la sustentación, esta parte se puede presentar como **“CRUD local en SQLite implementado mediante Room”**.

## Alcance

Se guardará únicamente el entrenamiento activo. Los entrenamientos terminados seguirán almacenándose en el backend PostgreSQL.

El borrador local incluirá:

- Usuario propietario del borrador.
- ID y nombre de la rutina.
- Notas del entrenamiento.
- Fecha y hora de inicio.
- Fecha y hora de finalización del descanso actual, si existe.
- Ejercicios agregados a la sesión.
- Orden de los ejercicios.
- Series, peso, repeticiones, orden y estado de completado.

No se guardarán localmente:

- Contraseñas.
- Token JWT dentro de Room; seguirá en `SharedPreferences`.
- Rutinas completas descargadas del servidor.
- Historial remoto de entrenamientos.

## Razón para elegir este caso de uso

La aplicación ya conserva `EntrenamientoEnCurso` solamente en memoria. Actualmente hay puntos pendientes en `MainActivity` y `EntrenamientoActivoFragment` para:

- Guardar la sesión al iniciarla.
- Recuperarla después de cerrar el proceso.
- Guardar cambios en peso y repeticiones.
- Guardar series completadas.
- Guardar el estado del descanso.
- Eliminar el borrador al finalizar o descartar.

Room resolverá esos puntos sin crear pantallas adicionales.

## Estructura de carpetas

Todo debe permanecer dentro del feature `entrenamiento`:

```text
app/src/main/java/ue/edu/co/fittrackandroid/entrenamiento/
├── datos/
│   ├── EntrenamientoApiService.java
│   ├── EntrenamientoRepository.java
│   └── local/
│       ├── FitTrackDatabase.java
│       ├── EntrenamientoBorradorDao.java
│       ├── EntrenamientoBorradorRepository.java
│       ├── EntrenamientoBorradorCompleto.java
│       ├── EntrenamientoBorradorEntity.java
│       ├── EjercicioBorradorEntity.java
│       └── SerieBorradorEntity.java
├── modelo/
└── vista/
```

No se creará un feature genérico de base de datos porque estos datos pertenecen exclusivamente a entrenamiento.

## Dependencias

El proyecto es Java, así que Room 2.x permite utilizar el procesador de anotaciones normal, sin agregar Kotlin ni KSP.

Versiones verificadas en la documentación oficial de Android el 29 de septiembre de 2026:

```toml
# gradle/libs.versions.toml
[versions]
room = "2.8.5"

[libraries]
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
}
```

No se necesita `room-ktx`, RxJava, LiveData ni otra librería para este CRUD.

Referencia oficial: <https://developer.android.com/jetpack/androidx/releases/room>

## Diseño de la base de datos

### Tabla `entrenamiento_borrador`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `long` | Clave primaria autogenerada |
| `correoUsuario` | `String` | Separa los borradores de las cuentas usadas en el dispositivo |
| `idRutina` | `Long` | ID remoto de la rutina, puede ser nulo |
| `nombreRutina` | `String` | Nombre mostrado durante la sesión |
| `notas` | `String` | Notas escritas por el usuario |
| `fechaHoraInicio` | `long` | Instante real de inicio en milisegundos |
| `fechaHoraFinDescanso` | `long` | Fin real del descanso o cero |
| `activa` | `boolean` | Indica que es un borrador recuperable |

Debe existir como máximo un borrador activo por usuario. Antes de insertar uno nuevo se comprobará si ya existe otro.

### Tabla `ejercicio_borrador`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `long` | Clave primaria autogenerada |
| `entrenamientoId` | `long` | Clave foránea del borrador |
| `rutinaEjercicioId` | `Long` | ID del ejercicio dentro de la rutina |
| `nombre` | `String` | Nombre visible |
| `grupoMuscular` | `String` | Grupo muscular visible |
| `posicion` | `int` | Orden del ejercicio |

La clave foránea debe eliminar los ejercicios en cascada cuando se borre el entrenamiento.

### Tabla `serie_borrador`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `long` | Clave primaria autogenerada |
| `ejercicioBorradorId` | `long` | Clave foránea del ejercicio |
| `numeroSerie` | `int` | Orden de la serie |
| `peso` | `double` | Peso registrado |
| `repeticiones` | `int` | Repeticiones registradas |
| `completada` | `boolean` | Estado de la serie |

La clave foránea debe eliminar las series en cascada cuando se borre el ejercicio.

## Relación de entidades

```text
entrenamiento_borrador 1
          │
          ├── N ejercicio_borrador 1
          │             │
          │             └── N serie_borrador
          │
          └── pertenece a un correo de usuario
```

`EntrenamientoBorradorCompleto` será una clase de lectura con relaciones `@Relation`. No será otra tabla.

## Operaciones CRUD que se demostrarán

### Create

Cuando `MainActivity` crea `EntrenamientoEnCurso`:

1. Insertar `EntrenamientoBorradorEntity`.
2. Recuperar su ID local.
3. Insertar los ejercicios con ese ID.
4. Insertar las series de cada ejercicio.

La operación completa debe ejecutarse dentro de una transacción.

### Read

Al iniciar la aplicación o entrar en `EntrenamientoActivoFragment`:

1. Buscar el borrador activo del correo autenticado.
2. Leer sus ejercicios y series ordenados.
3. Convertir las entidades a `EntrenamientoEnCurso`.
4. Restaurar la referencia usada por `MainActivity`.

Si no existe un borrador, se conserva el comportamiento actual.

### Update

Actualizar la base local cuando ocurra alguno de estos eventos:

- Se cambia el peso de una serie.
- Se cambian las repeticiones.
- Se marca o desmarca una serie.
- Se agrega una serie.
- Se agrega o elimina un ejercicio.
- Se modifican las notas.
- Comienza, termina o se cancela el descanso.

Para los campos de texto conviene esperar aproximadamente 300–500 ms después de la última edición antes de escribir, evitando una operación por cada tecla.

### Delete

Eliminar el borrador completo:

- Después de que el backend confirme que el entrenamiento finalizado fue guardado.
- Cuando el usuario confirme “Descartar entrenamiento”.
- Al cerrar sesión, únicamente para el usuario que está cerrando sesión.

Gracias a las claves foráneas con borrado en cascada, una eliminación quitará también ejercicios y series.

## DAO propuesto

El DAO debe incluir, como mínimo:

```java
@Insert
long insertarEntrenamiento(EntrenamientoBorradorEntity entrenamiento);

@Insert
long insertarEjercicio(EjercicioBorradorEntity ejercicio);

@Insert
void insertarSeries(List<SerieBorradorEntity> series);

@Transaction
@Query("SELECT * FROM entrenamiento_borrador "
        + "WHERE correoUsuario = :correoUsuario AND activa = 1 LIMIT 1")
EntrenamientoBorradorCompleto obtenerActivo(String correoUsuario);

@Update
void actualizarEntrenamiento(EntrenamientoBorradorEntity entrenamiento);

@Update
void actualizarSerie(SerieBorradorEntity serie);

@Query("DELETE FROM entrenamiento_borrador WHERE id = :entrenamientoId")
void eliminarEntrenamiento(long entrenamientoId);
```

También harán falta consultas pequeñas para agregar/eliminar ejercicios y series individuales.

## Repositorio local y ejecución en segundo plano

Room no debe ejecutarse en el hilo principal. Para mantener el código sencillo se usará:

- Un solo `ExecutorService` con un hilo para las operaciones locales.
- Un `Handler` del hilo principal para devolver el resultado a la pantalla.
- Callbacks pequeños para éxito o error.

No se activará `allowMainThreadQueries()`.

`EntrenamientoBorradorRepository` será responsable de:

- Ejecutar el DAO en segundo plano.
- Guardar el grafo completo dentro de una transacción.
- Convertir entre entidades Room y los modelos actuales.
- Avisar a la vista cuando la lectura o escritura termine.

## Manejo correcto del tiempo

`SystemClock.elapsedRealtime()` funciona bien mientras el dispositivo no se reinicie, pero su valor no debe persistirse como referencia definitiva.

En Room se guardarán tiempos de reloj real:

- `fechaHoraInicio`, usando `System.currentTimeMillis()`.
- `fechaHoraFinDescanso`, usando `System.currentTimeMillis()`.

Al recuperar el borrador:

- Duración transcurrida: hora actual menos `fechaHoraInicio`.
- Descanso restante: `fechaHoraFinDescanso` menos la hora actual.
- Si el resultado del descanso es negativo, se restaura sin descanso activo.

El modelo en memoria puede continuar usando `elapsedRealtime()` después de reconstruirse.

## Integración por etapas

### Etapa 1: infraestructura

- Agregar dependencias.
- Crear las tres entidades.
- Crear relaciones, DAO y `FitTrackDatabase` singleton.
- Activar claves foráneas mediante las anotaciones de Room.

### Etapa 2: creación y lectura

- Guardar el borrador al iniciar un entrenamiento.
- Recuperarlo al recrear la aplicación.
- Restaurar la isla de entrenamiento minimizado.

### Etapa 3: actualizaciones

- Persistir series, ejercicios, notas y descanso.
- Evitar escrituras duplicadas mientras otra operación está en curso.

### Etapa 4: eliminación

- Borrar después de finalizar correctamente.
- Borrar al descartar.
- Manejar el cierre de sesión sin afectar borradores de otras cuentas.

## Verificación manual

No se crearán pruebas unitarias ni instrumentadas para esta implementación. La comprobación se realizará manualmente con estos escenarios:

1. Iniciar entrenamiento, cerrar el proceso y volver a abrir: debe recuperarse.
2. Cambiar peso y repeticiones, cerrar y abrir: deben conservarse.
3. Completar una serie: debe conservar su estado.
4. Iniciar descanso y recrear la pantalla: debe conservar el tiempo restante.
5. Agregar y eliminar ejercicios o series: Room debe reflejarlo.
6. Finalizar y guardar en el backend: el borrador local debe desaparecer.
7. Descartar: el borrador debe desaparecer.
8. Cambiar de cuenta: una cuenta no debe recuperar el borrador de otra.
9. Simular un reinicio del dispositivo: los tiempos no deben resultar negativos ni enormes.

## Criterios de terminado

- Existe un CRUD local demostrable en SQLite mediante Room.
- No se pierde el entrenamiento si Android cierra el proceso.
- Todas las operaciones se realizan fuera del hilo principal.
- Los hijos se eliminan en cascada.
- El borrador está separado por usuario.
- Finalizar o descartar limpia la información local.
- No se modifica el comportamiento del CRUD remoto de entrenamientos.
- No se agregan pantallas nuevas.
