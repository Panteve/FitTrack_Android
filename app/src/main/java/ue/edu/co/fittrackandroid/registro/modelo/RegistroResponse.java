package ue.edu.co.fittrackandroid.registro.modelo;

import ue.edu.co.fittrackandroid.login.modelo.LoginResponse;

/**
 * Respuesta del backend al registrar una cuenta: los mismos datos de sesión que
 * devuelve el login. Extiende LoginResponse para no repetir ni la estructura
 * ni la forma de guardar la sesión.
 */
public class RegistroResponse extends LoginResponse {

    /**
     * Crea la respuesta con el token de acceso, el identificador y el nombre
     * confirmados por el backend, más la URL de la foto, que normalmente es null en
     * una cuenta recién creada.
     */
    public RegistroResponse(String token, Long usuarioId, String nombre,
                            String fotoPerfilUrl) {
        super(token, usuarioId, nombre, fotoPerfilUrl);
    }
}
