# Plan para documentar los métodos

## Qué hay que hacer

Revisa los archivos Java que están en:

```text
app/src/main/java/
```

Agrega un comentario Javadoc a cada método que hayamos creado nosotros para la aplicación. La idea es explicar de forma clara para qué sirve, sin hacer una explicación demasiado larga.

El formato debe ser así:

```java
/** Conecta la isla: la flecha y la zona central reabren el entrenamiento, y la papelera pide confirmación. */
```

Si hace falta un poco más de texto para que se entienda bien, puede escribirse en varias líneas:

```java
/**
 * Recupera el entrenamiento guardado y vuelve a mostrar sus datos cuando el usuario regresa a la aplicación.
 */
```

No agregar `@param`, `@return`, `@throws`, `@request` ni ninguna otra etiqueta. Solo debe quedar la descripción.

## Qué métodos sí hay que documentar

- Métodos públicos, privados, protegidos o sin modificador que sean parte de nuestra aplicación.
- Métodos estáticos y métodos auxiliares.
- Constructores.
- Getters y setters.
- Métodos de los modelos.
- Métodos que nosotros declaramos en interfaces, como callbacks propios o servicios de la API.
- Cada versión de un método sobrecargado.

También hay que revisar los métodos que ya tienen Javadoc. Si tienen `@param`, `@return` u otras etiquetas, quitar esas partes y dejar toda la información importante dentro de una descripción normal.

## Qué métodos no hay que documentar

No agregar estos comentarios a métodos propios de Android, Java o alguna librería.

La forma más sencilla de reconocerlos es porque normalmente tienen `@Override`, por ejemplo:

- `onCreate`
- `onResume`
- `onDestroyView`
- `onResponse`
- `onFailure`
- `run`
- `compare`
- Métodos sobrescritos de los adapters.
- Métodos sobrescritos dentro de listeners o clases anónimas.

Tampoco hay que documentar lambdas como si fueran métodos separados.

Si un método tiene `@Override` pero dentro contiene lógica nuestra, no hay que ponerle un Javadoc nuevo. Los comentarios internos que ya expliquen esa lógica se pueden conservar.

## Cómo escribir las descripciones

- Escribirlas en español.
- Empezar con un verbo como `Carga`, `Guarda`, `Valida`, `Muestra`, `Calcula`, `Obtiene` o `Convierte`.
- Terminar siempre con punto.
- Explicar para qué sirve el método, no repetir solamente su nombre.
- Mencionar qué cambia, qué devuelve o qué pantalla abre cuando eso ayude a entenderlo.
- No inventar lo que hace el método. Primero hay que leer su código y, si no es suficiente, revisar dónde se usa.
- Intentar usar una sola oración clara. Puede ser más larga si hace falta para explicar bien el método.

Por ejemplo, en vez de esto:

```java
/** Obtiene el nombre. */
```

Es mejor escribir esto:

```java
/** Obtiene el nombre del ejercicio que se muestra dentro de la rutina. */
```

## Algunos ejemplos

### Método con parámetros y retorno

Antes:

```java
/**
 * Busca una rutina por su identificador.
 *
 * @param rutinaId identificador de la rutina
 * @return llamada que devuelve la rutina encontrada
 */
public Call<RutinaResponse> obtenerRutina(Long rutinaId) {
    // ...
}
```

Después:

```java
/** Solicita al servidor la rutina correspondiente al identificador recibido. */
public Call<RutinaResponse> obtenerRutina(Long rutinaId) {
    // ...
}
```

### Método privado

```java
/** Valida el formulario y muestra los errores que impiden guardar el ejercicio. */
private boolean validarFormulario(String nombre) {
    // ...
}
```

### Getter

```java
/** Obtiene la cantidad de repeticiones registrada en la serie. */
public int getRepeticiones() {
    return repeticiones;
}
```

### Métodos sobrecargados

```java
/** Descarta el entrenamiento activo de la sesión actual. */
private void descartarEntrenamientoEnCurso() {
    // ...
}

/** Descarta el entrenamiento guardado para el correo indicado. */
private void descartarEntrenamientoEnCurso(String correoUsuario) {
    // ...
}
```

### Método que no hay que tocar

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // No agregar Javadoc aquí.
}
```

## Orden recomendado

Para que sea más fácil revisar los cambios, trabajar por grupos:

1. `utils`, `remote` e `imagenes`.
2. `login` y `registro`.
3. `hoy`.
4. `rutinas`.
5. `ejercicios`.
6. `entrenamiento`.
7. `resumen`.
8. `perfil`.
9. `MainActivity.java`.

En cada grupo hay que:

1. Buscar todos los métodos y constructores.
2. Separar los métodos nuestros de los que tienen `@Override`.
3. Leer qué hace cada método.
4. Agregar el Javadoc si falta.
5. Arreglar los Javadocs existentes que tengan etiquetas o no sean claros.

## Revisión final

Al terminar, comprobar lo siguiente:

- Todos los métodos creados por nosotros tienen una descripción.
- No se agregaron comentarios nuevos a métodos con `@Override`.
- No quedaron etiquetas como `@param`, `@return`, `@throws` o `@request` en los Javadocs de los métodos.
- Cada descripción explica lo que realmente hace el método y termina con punto.
- No se cambió código, nombres, firmas, imports ni comportamiento.
- No se modificaron archivos fuera de `app/src/main/java/`.
- El cambio final contiene solamente documentación de métodos.

No compilar ni ejecutar pruebas. Para esta tarea solo hay que revisar los archivos y los cambios realizados.
