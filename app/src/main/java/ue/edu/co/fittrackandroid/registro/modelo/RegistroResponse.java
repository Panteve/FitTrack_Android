package ue.edu.co.fittrackandroid.registro.modelo;

import ue.edu.co.fittrackandroid.login.modelo.LoginResponse;

/**
 * Respuesta del backend al registrar una cuenta: el mismo token y nombre que
 * devuelve el login. Extiende LoginResponse para no repetir ni la estructura
 * ni la forma de guardar la sesión.
 */
public class RegistroResponse extends LoginResponse {

    /**
     * @param token  token de acceso entregado por el backend.
     * @param nombre nombre confirmado por el backend.
     */
    public RegistroResponse(String token, String nombre) {
        super(token, nombre);
    }
}
