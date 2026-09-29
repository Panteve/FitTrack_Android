package ue.edu.co.fittrackandroid.registro.modelo;

import ue.edu.co.fittrackandroid.login.modelo.LoginResponse;

/**
 * Respuesta del backend al registrar una cuenta: los mismos datos de sesión que
 * devuelve el login. Extiende LoginResponse para no repetir ni la estructura
 * ni la forma de guardar la sesión.
 */
public class RegistroResponse extends LoginResponse {

    /**
     * @param token  token de acceso entregado por el backend.
     * @param usuarioId identificador de la cuenta creada.
     * @param nombre nombre confirmado por el backend.
     * @param fotoPerfilUrl URL temporal de la foto, normalmente null en una cuenta nueva.
     */
    public RegistroResponse(String token, Long usuarioId, String nombre,
                            String fotoPerfilUrl) {
        super(token, usuarioId, nombre, fotoPerfilUrl);
    }
}
