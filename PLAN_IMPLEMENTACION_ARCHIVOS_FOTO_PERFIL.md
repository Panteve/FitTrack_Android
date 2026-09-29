# Plan de implementación: archivo local para la foto de perfil

## Objetivo

Completar el manejo de la foto de perfil utilizando dos niveles de almacenamiento:

- **Supabase Storage:** conserva la fotografía remota asociada a la cuenta.
- **Archivos internos de Android:** conserva una copia local descargada para mostrarla en `PerfilFragment`.

Este flujo permite demostrar el requisito de manejo de archivos sin agregar una funcionalidad distinta: se completa la foto de perfil que ya existe.

## Comportamiento esperado

```text
Inicio de sesión
      │
      ▼
Backend devuelve fotoPerfilUrl
      │
      ▼
Android descarga la imagen
      │
      ▼
Archivo interno asociado al usuario
      │
      ▼
PerfilFragment lee y muestra el archivo
```

Si el dispositivo está temporalmente sin conexión, la aplicación podrá mostrar la última copia local disponible para ese usuario.

## Estado actual

Actualmente `PerfilFragment`:

- Abre un documento mediante `ActivityResultContracts.OpenDocument`.
- Guarda la URI elegida en `SharedPreferences`.
- Intenta volver a leer esa URI al abrir el perfil.

La fotografía todavía:

- No se sube al backend.
- No se guarda en el usuario de PostgreSQL.
- No se descarga desde Supabase al iniciar sesión.
- No se copia al directorio interno de la aplicación.

La implementación nueva reemplazará la persistencia de la URI por una copia real en los archivos internos de Android.

## Alcance

La implementación incluirá:

- Ruta de la foto en la entidad `Usuario` del backend.
- Subida y reemplazo de la foto mediante Spring Boot.
- Almacenamiento remoto en el bucket privado de Supabase.
- URL firmada devuelta por el backend.
- URL incluida en la respuesta del inicio de sesión.
- Descarga del archivo en Android.
- Lectura del archivo desde `PerfilFragment`.
- Reemplazo de la copia local al cambiar la foto.
- Separación de archivos por usuario.
- Limpieza de la copia correspondiente cuando se cierre sesión.

No se agregará una pantalla nueva.

## Decisiones de seguridad

- La clave de servicio de Supabase permanecerá únicamente en el backend.
- Android nunca se conectará a Supabase usando la clave privada.
- El bucket permanecerá privado.
- PostgreSQL guardará la **ruta interna del objeto**, no la URL firmada.
- El backend generará una URL firmada cuando Android necesite consultar la imagen.
- Android no tratará la URL firmada como permanente porque puede caducar.
- Solo el usuario autenticado podrá subir, consultar o eliminar su propia foto.

## Cambios en el backend

### Campo nuevo en `Usuario`

Agregar a la entidad `Usuario`:

```java
@Column(name = "foto_perfil_ruta")
private String fotoPerfilRuta;
```

Este campo será nullable porque una cuenta puede no tener fotografía.

En PostgreSQL debe agregarse una columna equivalente:

```sql
ALTER TABLE usuario
ADD COLUMN foto_perfil_ruta VARCHAR(500);
```

No se guardará el contenido binario de la foto en PostgreSQL.

### Reutilización de Supabase Storage

El backend ya tiene una implementación capaz de:

- Subir bytes.
- Generar URL firmada.
- Eliminar un objeto anterior.

Como dejará de pertenecer únicamente a entrenamientos, se recomienda mover la abstracción y su implementación a una ubicación compartida del backend:

```text
src/main/java/com/fittrack/shared/storage/
├── FotoStorageService.java
├── SupabaseFotoStorageService.java
└── SupabaseStorageProperties.java
```

Después se actualizarán los imports del feature `entrenamiento`. No se duplicará el cliente de Supabase dentro de `usuario`.

### Ruta del objeto en el bucket

Usar una ruta que identifique al propietario sin exponer su correo:

```text
usuarios/{usuarioId}/perfil/{uuid}.jpg
```

o:

```text
usuarios/{usuarioId}/perfil/{uuid}.png
```

El UUID evita conflictos de caché cuando se reemplaza una imagen.

### DTO de perfil

Crear un DTO sencillo:

```java
public record PerfilDto(
        Long id,
        String nombre,
        String correo,
        String fotoPerfilUrl
) {
}
```

`fotoPerfilUrl` será `null` cuando el usuario no tenga foto.

### Respuesta del login

Ampliar `AuthResponseDto` con:

```java
Long usuarioId;
String fotoPerfilUrl;
```

Después de autenticar:

1. Consultar `fotoPerfilRuta`.
2. Si existe, generar una URL firmada.
3. Devolverla junto con token, ID y nombre.

El ID facilita que Android nombre el archivo local sin utilizar el correo.

### Endpoints

Agregar al controlador de usuario:

```text
GET    /usuarios/me/perfil
PUT    /usuarios/me/foto
DELETE /usuarios/me/foto
```

#### `GET /usuarios/me/perfil`

Devuelve `PerfilDto` con una URL firmada nueva. Este endpoint permite renovar la URL si la recibida durante el login ya expiró.

#### `PUT /usuarios/me/foto`

Recibe `multipart/form-data` con una parte llamada `foto`.

Validaciones mínimas:

- Solo JPEG o PNG.
- Tamaño máximo de 5 MB.
- Contenido no vacío.
- Usuario autenticado existente.

Flujo:

1. Validar archivo.
2. Crear una ruta nueva.
3. Subir la nueva fotografía.
4. Guardar la ruta nueva en `Usuario`.
5. Generar la URL firmada nueva.
6. Intentar eliminar el objeto anterior.
7. Devolver `PerfilDto` actualizado.

No debe borrarse primero la foto anterior. Si falla la subida, la cuenta debe conservar la foto existente.

#### `DELETE /usuarios/me/foto`

Flujo:

1. Obtener la ruta actual.
2. Dejar `fotoPerfilRuta` en `null`.
3. Guardar el usuario.
4. Intentar eliminar el objeto del bucket.
5. Responder `204 No Content`.

## Cambios en Android

### Modelo de login

Agregar a `LoginResponse`:

```java
private Long usuarioId;
private String fotoPerfilUrl;
```

El ID del usuario también puede guardarse en `SesionManager`. La URL firmada no necesita guardarse como dato permanente.

### Servicio de perfil

Agregar a `PerfilApiService`:

```java
@GET("usuarios/me/perfil")
Call<PerfilResponse> obtenerPerfil();

@Multipart
@PUT("usuarios/me/foto")
Call<PerfilResponse> cambiarFoto(
        @Part MultipartBody.Part foto
);

@DELETE("usuarios/me/foto")
Call<Void> eliminarFoto();
```

El repositorio expondrá las tres llamadas sin ejecutar lógica de vista.

### Ubicación del archivo local

La fotografía se guardará dentro del almacenamiento interno privado:

```text
files/perfil/foto_perfil_{usuarioId}.jpg
```

Ruta obtenida desde Android:

```java
File directorioPerfil = new File(context.getFilesDir(), "perfil");
File archivoFoto = new File(
        directorioPerfil,
        "foto_perfil_" + usuarioId + ".jpg"
);
```

No requiere permisos de almacenamiento porque pertenece al directorio interno de la aplicación.

### Clase responsable de archivos

Crear dentro del feature `perfil`:

```text
app/src/main/java/ue/edu/co/fittrackandroid/perfil/datos/
└── FotoPerfilLocal.java
```

Responsabilidades:

```java
public File obtenerArchivo(Long usuarioId);

public boolean existeFoto(Long usuarioId);

public void guardarDesdeStream(Long usuarioId, InputStream datos);

public void eliminar(Long usuarioId);
```

También puede ofrecer un método para guardar desde la URI elegida por el usuario.

La clase no debe conocer `ImageView`, Fragment ni elementos visuales.

### Escritura segura del archivo

La descarga no debe escribir directamente sobre la foto válida. Se utilizará un archivo temporal:

```text
foto_perfil_{usuarioId}.tmp
```

Flujo:

1. Crear el directorio si no existe.
2. Escribir la respuesta en el archivo temporal.
3. Validar que tenga contenido y pueda decodificarse como imagen.
4. Reemplazar el archivo definitivo.
5. Eliminar el temporal si ocurre un error.

Así, una descarga interrumpida no destruye la última copia correcta.

### Descarga después del login

Cuando el login sea exitoso:

1. Guardar token, ID, nombre y correo en `SesionManager`.
2. Si `fotoPerfilUrl` no es nula, iniciar la descarga en segundo plano.
3. Guardar el archivo mediante `FotoPerfilLocal`.
4. Continuar hacia Inicio sin bloquear al usuario esperando la imagen.

La descarga puede utilizar `HttpURLConnection`, disponible en Java/Android, dentro de un `ExecutorService`. No se agregará una librería de imágenes.

Reglas de descarga:

- Aceptar únicamente respuestas HTTP exitosas.
- Establecer tiempos máximos de conexión y lectura.
- Rechazar contenido mayor a 5 MB.
- Comprobar `Content-Type` JPEG o PNG cuando esté disponible.
- Cerrar siempre streams y conexiones.

Si falla la descarga:

- Conservar el archivo local anterior del mismo usuario, si existe.
- Mostrar la imagen predeterminada si no existe copia.
- No impedir el inicio de sesión.

### Presentación en `PerfilFragment`

Al abrir el perfil:

1. Consultar el ID de usuario guardado en la sesión.
2. Buscar el archivo con `FotoPerfilLocal`.
3. Si existe y es válido, mostrarlo.
4. Si no existe, mostrar `ic_person_teal`.
5. Consultar `GET /usuarios/me/perfil` para obtener una URL vigente y actualizar la copia en segundo plano cuando sea necesario.

`ImageView.setImageURI()` dejará de depender de la URI del proveedor de documentos. La imagen se cargará desde el archivo interno, cuidando reducir su tamaño al decodificarla para evitar consumir memoria innecesaria.

### Cambio de fotografía

Se conservará el selector actual. Cuando el usuario elija una imagen:

1. Abrir la URI con `ContentResolver`.
2. Validar tipo y tamaño.
3. Crear el `MultipartBody.Part`.
4. Enviar la foto al backend.
5. Esperar confirmación exitosa.
6. Copiar la imagen seleccionada al archivo interno definitivo.
7. Mostrar la copia local.

No debe reemplazarse la copia local antes de que el backend confirme la subida, para evitar mostrar como definitiva una foto que la cuenta remota no guardó.

### Cierre y cambio de sesión

Al cerrar sesión:

1. Obtener el ID antes de limpiar `SesionManager`.
2. Eliminar únicamente `foto_perfil_{usuarioId}.jpg` y su temporal.
3. Limpiar la sesión.
4. Mostrar el login.

Esto evita que otra cuenta vea la foto anterior.

Como alternativa futura se puede conservar una caché por usuario, pero para la entrega es más sencillo eliminarla al cerrar sesión.

## Integración por etapas

### Etapa 1: backend

- Agregar `fotoPerfilRuta` a `Usuario` y PostgreSQL.
- Compartir el servicio existente de Supabase Storage.
- Implementar consulta, subida y eliminación de foto de perfil.
- Ampliar la respuesta del login.

### Etapa 2: archivo local Android

- Agregar `usuarioId` y `fotoPerfilUrl` a la respuesta.
- Guardar el ID en la sesión.
- Crear `FotoPerfilLocal`.
- Descargar y escribir mediante archivo temporal.

### Etapa 3: perfil

- Leer el archivo local al abrir `PerfilFragment`.
- Sustituir el almacenamiento de URI en `SharedPreferences`.
- Subir la foto seleccionada y reemplazar la copia tras el éxito.
- Renovar la URL firmada mediante el endpoint de perfil.

### Etapa 4: limpieza y errores

- Limpiar el archivo al cerrar sesión.
- Mantener la foto anterior ante errores.
- Eliminar temporales incompletos.
- Mostrar la imagen predeterminada si no hay una copia válida.

## Verificación manual

No se crearán pruebas unitarias ni instrumentadas para esta implementación. La comprobación se realizará manualmente con estos escenarios:

1. Iniciar sesión con foto: debe descargarse y mostrarse en Perfil.
2. Cerrar la aplicación y abrirla sin conexión: debe mostrarse la copia local.
3. Iniciar sesión sin foto: debe mostrarse la imagen predeterminada.
4. Cambiar foto: debe subir a Supabase y reemplazar la copia local.
5. Interrumpir una descarga: debe conservarse la foto local anterior.
6. Cerrar sesión y entrar con otra cuenta: no debe verse la foto anterior.
7. Recibir una URL firmada vencida: el perfil debe solicitar una URL nueva.
8. Elegir un archivo que no sea JPEG o PNG: debe rechazarse.
9. Elegir una imagen mayor a 5 MB: debe rechazarse.

## Criterios de terminado

- La ruta remota se almacena en PostgreSQL y el archivo en Supabase.
- Android recibe una URL firmada sin conocer secretos de Supabase.
- La imagen descargada existe físicamente dentro de `files/perfil/`.
- `PerfilFragment` muestra la copia local.
- La copia está asociada al ID del usuario.
- Una descarga fallida no destruye la foto anterior.
- Cambiar la foto actualiza Supabase, PostgreSQL y el archivo local.
- Cerrar sesión impide que otra cuenta vea la foto anterior.
- No se guardan contraseñas ni claves de Supabase en archivos locales.
- No se agregan pantallas nuevas.
- No se crean pruebas unitarias ni instrumentadas.

