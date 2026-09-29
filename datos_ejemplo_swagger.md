# Datos de ejemplo para FitTrack usando Swagger

Esta guía permite crear ejercicios y rutinas de prueba utilizando los endpoints del backend, sin insertar datos directamente en Supabase.

## 1. Abrir Swagger

Backend desplegado:

```text
https://fittrack-backend-kjyc.onrender.com
```

Swagger UI:

```text
https://fittrack-backend-kjyc.onrender.com/api/swagger-ui/index.html
```

Si Render está suspendido, la primera carga puede tardar un poco.

## 2. Iniciar sesión

Usar:

```text
POST /auth/login
```

Body:

```json
{
  "correo": "TU_CORREO",
  "contrasena": "TU_CONTRASENA"
}
```

La respuesta será parecida a:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "nombre": "Tu nombre"
}
```

Copia el valor de `token`. Pulsa el botón **Authorize** de Swagger y escribe:

```text
Bearer TU_TOKEN
```

Si Swagger agrega `Bearer` automáticamente, pega únicamente el token.

## 3. Crear una cuenta de prueba, si todavía no tienes una

Este paso es opcional. Usar:

```text
POST /auth/register
```

Body:

```json
{
  "nombre": "Usuario de prueba",
  "correo": "usuario.prueba@fittrack.com",
  "contrasena": "Prueba1234"
}
```

La contraseña debe tener entre 8 y 72 caracteres. Después del registro, copia el token de la respuesta y autoriza Swagger.

## 4. Crear los ejercicios

Usar este endpoint para cada ejercicio:

```text
POST /ejercicios
```

### Ejercicio 1: Sentadilla

```json
{
  "nombre": "Sentadilla",
  "grupoMuscular": "Pierna"
}
```

### Ejercicio 2: Peso muerto rumano

```json
{
  "nombre": "Peso muerto rumano",
  "grupoMuscular": "Pierna"
}
```

### Ejercicio 3: Press de banca

```json
{
  "nombre": "Press de banca",
  "grupoMuscular": "Pecho"
}
```

### Ejercicio 4: Remo con barra

```json
{
  "nombre": "Remo con barra",
  "grupoMuscular": "Espalda"
}
```

### Ejercicio 5: Press militar

```json
{
  "nombre": "Press militar",
  "grupoMuscular": "Hombro"
}
```

Cada respuesta tendrá esta forma:

```json
{
  "id": 1,
  "nombre": "Sentadilla",
  "grupoMuscular": "Pierna"
}
```

Anota los IDs reales obtenidos:

| Ejercicio | ID obtenido |
|---|---:|
| Sentadilla | |
| Peso muerto rumano | |
| Press de banca | |
| Remo con barra | |
| Press militar | |

También puedes consultar todos los ejercicios creados con:

```text
GET /ejercicios/mis-ejercicios
```

## 5. Crear la rutina de pierna

Usar:

```text
POST /rutinas
```

Reemplaza `ID_SENTADILLA` e `ID_PESO_MUERTO` por los IDs obtenidos en el paso anterior. Los IDs deben escribirse como números, sin comillas.

```json
{
  "nombre": "Pierna completa",
  "descripcion": "Entrenamiento de cuádriceps y femorales",
  "diaSemana": "LUNES",
  "ejercicios": [
    {
      "ejercicioId": ID_SENTADILLA,
      "orden": 1,
      "series": [
        {
          "numeroSerie": 1,
          "repeticionesObjetivo": 12,
          "pesoObjetivo": 30.00
        },
        {
          "numeroSerie": 2,
          "repeticionesObjetivo": 10,
          "pesoObjetivo": 40.00
        },
        {
          "numeroSerie": 3,
          "repeticionesObjetivo": 8,
          "pesoObjetivo": 50.00
        }
      ]
    },
    {
      "ejercicioId": ID_PESO_MUERTO,
      "orden": 2,
      "series": [
        {
          "numeroSerie": 1,
          "repeticionesObjetivo": 12,
          "pesoObjetivo": 30.00
        },
        {
          "numeroSerie": 2,
          "repeticionesObjetivo": 10,
          "pesoObjetivo": 40.00
        },
        {
          "numeroSerie": 3,
          "repeticionesObjetivo": 10,
          "pesoObjetivo": 40.00
        }
      ]
    }
  ]
}
```

## 6. Crear la rutina de pecho y hombro

Usar nuevamente:

```text
POST /rutinas
```

Reemplaza `ID_PRESS_BANCA` e `ID_PRESS_MILITAR` por números reales.

```json
{
  "nombre": "Pecho y hombro",
  "descripcion": "Rutina de empuje para el tren superior",
  "diaSemana": "MIERCOLES",
  "ejercicios": [
    {
      "ejercicioId": ID_PRESS_BANCA,
      "orden": 1,
      "series": [
        {
          "numeroSerie": 1,
          "repeticionesObjetivo": 12,
          "pesoObjetivo": 20.00
        },
        {
          "numeroSerie": 2,
          "repeticionesObjetivo": 10,
          "pesoObjetivo": 30.00
        },
        {
          "numeroSerie": 3,
          "repeticionesObjetivo": 8,
          "pesoObjetivo": 35.00
        }
      ]
    },
    {
      "ejercicioId": ID_PRESS_MILITAR,
      "orden": 2,
      "series": [
        {
          "numeroSerie": 1,
          "repeticionesObjetivo": 12,
          "pesoObjetivo": 10.00
        },
        {
          "numeroSerie": 2,
          "repeticionesObjetivo": 10,
          "pesoObjetivo": 15.00
        },
        {
          "numeroSerie": 3,
          "repeticionesObjetivo": 10,
          "pesoObjetivo": 15.00
        }
      ]
    }
  ]
}
```

## 7. Crear la rutina de espalda

Usar nuevamente:

```text
POST /rutinas
```

Reemplaza `ID_REMO_BARRA` por el ID real.

```json
{
  "nombre": "Espalda",
  "descripcion": "Entrenamiento básico de espalda",
  "diaSemana": "VIERNES",
  "ejercicios": [
    {
      "ejercicioId": ID_REMO_BARRA,
      "orden": 1,
      "series": [
        {
          "numeroSerie": 1,
          "repeticionesObjetivo": 12,
          "pesoObjetivo": 20.00
        },
        {
          "numeroSerie": 2,
          "repeticionesObjetivo": 10,
          "pesoObjetivo": 25.00
        },
        {
          "numeroSerie": 3,
          "repeticionesObjetivo": 8,
          "pesoObjetivo": 30.00
        }
      ]
    }
  ]
}
```

## 8. Verificar las rutinas creadas

Usar:

```text
GET /rutinas
```

Para consultar el detalle de una rutina específica:

```text
GET /rutinas/{id}
```

Reemplaza `{id}` por el identificador que devolvió `POST /rutinas`.

## Días válidos

El campo `diaSemana` solamente acepta estos valores, escritos en mayúsculas y sin tilde:

```text
LUNES
MARTES
MIERCOLES
JUEVES
VIERNES
SABADO
DOMINGO
```

## Errores frecuentes

- **401:** falta autorizar Swagger o el token expiró. Inicia sesión nuevamente.
- **400:** revisa que los IDs sean números, que cada serie tenga valores positivos y que el día sea válido.
- **404 al crear una rutina:** alguno de los ejercicios no existe o no pertenece al usuario autenticado.
- No uses IDs de ejercicios pertenecientes a otra cuenta.
- No escribas los textos `ID_SENTADILLA`, `ID_PESO_MUERTO`, etc. Debes reemplazarlos por los números reales.
