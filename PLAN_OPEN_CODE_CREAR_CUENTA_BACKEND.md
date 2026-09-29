# Plan para OpenCode: conectar Crear cuenta con el backend

## Objetivo

Completar la implementación existente de `CrearCuentaFragment` para registrar una cuenta mediante el backend real, guardar la sesión recibida y abrir Inicio solamente cuando el servidor confirme el registro.

No crear otra Activity ni otro Fragment. La pantalla, el layout, los modelos y las clases de datos ya existen parcialmente y deben corregirse/reutilizarse.

## Reglas obligatorias del repositorio

Antes de modificar código, leer y respetar:

- `AGENTS.md`.
- `guia_uso_recursos_visuales.md`.
- El mockup `stitch_neobrutalist_gym_tracker/registro_android_primitivo_universidad/screen.png` solo como referencia visual.

Además:

- Mantener una sola `MainActivity` y la pantalla como Fragment.
- No agregar dependencias ni cambiar las versiones de Gradle.
- No hardcodear textos visibles nuevos en XML o Java. Los Toasts sí pueden usar texto directo, aunque se deben preferir recursos existentes cuando encajen.
- Todo recurso de texto usado por un componente debe conservar el nombre del id del componente; para mensajes alternativos usar un sufijo corto, por ejemplo `tvErrorCorreo_registrado`.
- Mantener código Java directo y fácil de entender, sin MVVM, nuevas interfaces, inyección de dependencias ni abstracciones innecesarias.
- Documentar con Javadoc las clases y métodos públicos o protegidos que se creen o modifiquen de forma relevante.
- No compilar el proyecto. La verificación solicitada es estática.
- No modificar los cambios no relacionados que ya existan en el árbol de trabajo, especialmente los de la feature `perfil`.

## Estado actual que debe conservarse

Ya existen:

- `app/src/main/java/ue/edu/co/fittrackandroid/registro/vista/CrearCuentaFragment.java`.
- `app/src/main/res/layout/fragment_crear_cuenta.xml`.
- `app/src/main/java/ue/edu/co/fittrackandroid/registro/datos/RegistroApiService.java`.
- `app/src/main/java/ue/edu/co/fittrackandroid/registro/datos/RegistroRepository.java`.
- `app/src/main/java/ue/edu/co/fittrackandroid/registro/modelo/RegistroRequest.java`.
- `app/src/main/java/ue/edu/co/fittrackandroid/registro/modelo/RegistroResponse.java`.
- `SesionManager`, `RetrofitClient` y `ManejadorErroresApi`.
- La navegación Login → Crear cuenta mediante `MainActivity.mostrarCrearCuenta()`.
- La toolbar secundaria con flecha de volver y acción Guardar.

La pantalla ya valida localmente los campos, pero actualmente muestra “Cuenta creada” y navega a Inicio sin hacer una petición. El repositorio de registro crea la llamada Retrofit, pero no la retorna ni la ejecuta.

## Contrato real del backend

Usar el cliente Retrofit ya configurado, cuya URL base termina en `/api/`.

Petición:

```http
POST auth/register
Content-Type: application/json
```

```json
{
  "nombre": "Usuario de prueba",
  "correo": "usuario@correo.com",
  "contrasena": "Prueba1234"
}
```

Respuesta correcta: HTTP `201`.

```json
{
  "token": "jwt...",
  "nombre": "Usuario de prueba"
}
```

Validaciones del backend que deben reflejarse en Android:

- `nombre`: obligatorio, máximo 255 caracteres.
- `correo`: obligatorio, formato de email, máximo 255 caracteres.
- `contrasena`: obligatoria, entre 8 y 72 caracteres.
- HTTP `400`: datos inválidos.
- HTTP `409`: el correo ya está registrado.
- Otros códigos y fallos de red: usar `ManejadorErroresApi`.

No modificar el backend: el endpoint y el contrato necesarios ya están implementados.

## Cambios por archivo

### 1. `RegistroRepository.java`

- Eliminar el import sin uso de `LoginApiService` y el punto y coma duplicado del constructor.
- Cambiar `registrarUsuario(RegistroRequest registroRequest)` para que retorne `Call<RegistroResponse>`.
- Retornar directamente `registroApiService.registerUser(registroRequest)` siguiendo el mismo patrón de `LoginRepository`.
- Agregar los imports y Javadocs necesarios.

Resultado esperado del método:

```java
public Call<RegistroResponse> registrarUsuario(RegistroRequest registroRequest)
```

### 2. `RegistroApiService.java`

- Mantener `@POST("auth/register")` y el cuerpo `RegistroRequest`.
- Conservar la respuesta como `Call<RegistroResponse>`.
- Cambiar `registerUser` a un nombre en español solo si se actualizan todas sus referencias; no es obligatorio para completar la integración.
- Añadir Javadoc breve a la interfaz o al método si se toca el archivo.

### 3. `RegistroRequest.java` y `RegistroResponse.java`

- Confirmar que Gson enviará exactamente `nombre`, `correo` y `contrasena` y leerá `token` y `nombre`.
- No agregar campos que el backend no recibe, como la confirmación de contraseña.
- La confirmación se valida únicamente en Android.
- Mantener `RegistroResponse` compatible con `LoginResponse`; no duplicar lógica de sesión.
- No guardar ni imprimir la contraseña.
- Añadir o mejorar Javadocs sin introducir una refactorización grande.

### 4. `CrearCuentaFragment.java`

#### Inicialización

- Agregar campos para `RegistroRepository` y para la llamada activa `Call<RegistroResponse>`.
- Inicializar el repositorio después de inflar la vista, usando `requireContext()`.
- Mantener la configuración actual de la toolbar y su acción Guardar.
- Actualizar el Javadoc de la clase: ya no debe decir que no existe autenticación real.

#### Validación local

Mantener la validación secuencial y clara, pero hacerla consistente con el backend:

1. Nombre vacío.
2. Nombre mayor de 255 caracteres.
3. Correo inválido.
4. Correo mayor de 255 caracteres.
5. Contraseña menor de 8 caracteres.
6. Contraseña mayor de 72 caracteres.
7. Confirmación diferente de la contraseña.

Usar `trim()` para nombre y correo. No aplicar `trim()` a la contraseña ni a su confirmación, porque cambiaría silenciosamente lo escrito por el usuario.

Cambiar `LONGITUD_MINIMA_CONTRASENA` de 6 a 8 y agregar una constante para 72. Si se usan límites de 255, declararlos con nombres descriptivos en vez de números mágicos.

Actualizar los textos del layout y los mensajes en `strings.xml` para que indiquen mínimo 8 caracteres. Los mensajes mostrados dentro de los `TextView` de error deben venir de recursos, no de literales Java.

#### Petición de registro

Cuando la validación sea correcta:

- Crear `RegistroRequest(nombreUsuario, correo, contrasena)`.
- Obtener la llamada desde `RegistroRepository.registrarUsuario(...)`.
- Activar el estado de carga antes de `enqueue`.
- Ejecutarla de forma asíncrona con `Callback<RegistroResponse>`.

Mientras la petición esté activa:

- Deshabilitar la acción Guardar mediante `MainActivity.habilitarAccionToolbar(false)` para evitar registros duplicados.
- Deshabilitar temporalmente los cuatro `EditText`.
- Si se añade un indicador, usar un `ProgressBar` nativo con id `pbCrearCuenta`, inicialmente `gone`, dentro del `FrameLayout` raíz. No agregar librerías.

Al terminar, restaurar los controles salvo que ya se haya navegado a Inicio.

#### Respuesta exitosa

Considerar éxito solamente si `response.isSuccessful()`, el body existe y el token no es nulo ni está vacío.

En ese caso:

- Crear/reutilizar `SesionManager`.
- Guardar el token con `guardarTokens(response.body().getToken())`.
- Guardar el nombre confirmado por el backend y el correo escrito con `guardarInfoPersonal(response.body().getNombre(), correo)`.
- Mostrar el mensaje de cuenta creada.
- Navegar con `((MainActivity) requireActivity()).mostrarHome()`.
- No guardar la contraseña en preferencias, logs ni argumentos del Fragment.

Si el servidor responde 2xx pero el body o el token no son válidos, mostrar `R.string.error_respuesta_invalida`, restaurar el formulario y permanecer en Crear cuenta.

#### Respuestas de error

- HTTP `409`: mostrar un error específico debajo del correo, cambiar su fondo a error, llevar el foco a ese campo y mostrar un mensaje como “Este correo ya está registrado”. Agregar el recurso alternativo con sufijo, por ejemplo `tvErrorCorreo_registrado`.
- HTTP `400`: mantener la pantalla abierta y usar `ManejadorErroresApi` para informar que los datos no son válidos. Las validaciones locales deberían prevenir los casos normales.
- Resto de códigos HTTP: usar `ManejadorErroresApi.obtenerToast(requireContext(), response.code())`.
- `onFailure`: si la llamada no fue cancelada, usar `ManejadorErroresApi.obtenerToast(requireContext(), throwable)`.
- Antes de tocar vistas o navegar en callbacks, comprobar que el Fragment siga agregado o que su vista siga disponible.

#### Ciclo de vida

- En `onDestroyView()`, cancelar la llamada activa si todavía está en ejecución.
- Limpiar la referencia de la llamada.
- No mostrar errores por una cancelación provocada al salir de la pantalla.
- Volver a habilitar la acción de la toolbar al entrar en `onResume`, para que una navegación anterior no la deje bloqueada.

### 5. `fragment_crear_cuenta.xml`

- Conservar la estructura visual actual, la paleta, la tarjeta y los ids existentes.
- No agregar toolbar ni navegación inferior dentro del Fragment.
- Corregir el hint de contraseña para indicar 8 caracteres.
- Si se implementa `pbCrearCuenta`, ubicarlo centrado sobre el contenido aprovechando el `FrameLayout` raíz y usar el prefijo `pb` exigido por el proyecto.
- Todo texto visible debe apuntar a `strings.xml`.

### 6. `strings.xml`

- Actualizar `etContrasenaRegistro_hint` y `tvErrorContrasenaRegistro` de 6 a 8 caracteres.
- Agregar mensajes para longitud máxima de nombre, correo y contraseña si se muestran en la interfaz.
- Agregar el mensaje específico de correo ya registrado.
- Mantener nombres asociados al id del componente y usar sufijos para sus variantes.
- Reutilizar `error_respuesta_invalida` y los mensajes generales que ya usa `ManejadorErroresApi`.

### 7. Documentación del mapa de pantallas

Actualizar únicamente la sección “Crear cuenta” de `MAPA_PANTALLAS_Y_NAVEGACION.md`:

- Indicar que Guardar llama a `POST /auth/register`.
- Indicar que, tras un `201`, se guardan token, nombre y correo y se abre Inicio.
- Eliminar la afirmación de que el registro todavía es provisional o no usa backend.

No corregir otras secciones desactualizadas fuera de este alcance.

## Orden recomendado de implementación

1. Corregir el repositorio para que entregue la llamada Retrofit.
2. Ajustar strings y límites de validación al contrato real.
3. Integrar la petición asíncrona en el Fragment.
4. Implementar carga, errores HTTP y cancelación por ciclo de vida.
5. Añadir el `ProgressBar` opcional sin rediseñar la pantalla.
6. Actualizar solo la sección correspondiente del mapa de navegación.
7. Revisar estáticamente referencias, imports y recursos.

## Verificación estática obligatoria

No ejecutar Gradle ni compilar. Revisar manualmente:

- No queda el TODO que simula el registro.
- No se navega a Inicio antes de recibir un `201` válido.
- `RegistroRepository.registrarUsuario` retorna la llamada y esta se ejecuta con `enqueue`.
- El JSON usa los nombres `nombre`, `correo` y `contrasena`.
- La contraseña se valida entre 8 y 72 caracteres.
- Un `409` no inicia sesión y deja visible el error de correo duplicado.
- Un fallo de red restaura los controles y permite reintentar.
- Un toque doble en Guardar no crea dos llamadas simultáneas.
- La llamada se cancela al destruir la vista y la cancelación no muestra un Toast de error.
- El éxito guarda token, nombre y correo mediante `SesionManager`.
- Ninguna contraseña se guarda ni se registra en logs.
- No hay texto visible nuevo hardcodeado fuera de Toast/Snackbar.
- Todos los ids XML respetan los prefijos del proyecto.
- No se agregó ninguna Activity, dependencia o cambio al backend.
- Los cambios existentes y no relacionados de `perfil` permanecen intactos.

## Casos de aceptación manual para una ejecución posterior

Estos casos describen el comportamiento esperado, pero no deben ejecutarse durante esta tarea de OpenCode si se mantiene la regla de no compilar:

1. Datos válidos y correo nuevo: muestra confirmación, guarda sesión y abre Inicio.
2. Correo ya registrado: permanece en la pantalla y muestra el error del correo.
3. Nombre vacío o mayor de 255 caracteres: no realiza petición.
4. Correo inválido o mayor de 255 caracteres: no realiza petición.
5. Contraseña de 7 caracteres: no realiza petición.
6. Contraseña de 73 caracteres: no realiza petición.
7. Contraseñas diferentes: no realiza petición.
8. Sin conexión o servidor caído: muestra el error correspondiente y permite reintentar.
9. Salir con la flecha mientras registra: cancela la llamada y vuelve al Login sin cierre inesperado.

## Fuera de alcance

- Cambios en el endpoint o en el backend Spring Boot.
- Verificación de correo.
- Recuperación de contraseña.
- Inicio de sesión con proveedores externos.
- ViewModel, Navigation Component o una arquitectura nueva.
- Rediseño completo de Login o Crear cuenta.
- Compilación, instalación en dispositivo o pruebas instrumentadas.

