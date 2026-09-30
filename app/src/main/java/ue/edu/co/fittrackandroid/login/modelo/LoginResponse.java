package ue.edu.co.fittrackandroid.login.modelo;

/** Datos de sesión devueltos por el backend después de autenticar al usuario. */
public class LoginResponse {
    private String token;
    private Long usuarioId;
    private String nombre;
    private String fotoPerfilUrl;

    /** Crea la respuesta con el token, el identificador, el nombre y la foto que devolvió el backend. */
    public LoginResponse(String token, Long usuarioId, String nombre, String fotoPerfilUrl) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    /** Obtiene el token de acceso con el que se autenticarán las siguientes peticiones. */
    public String getToken() {
        return token;
    }

    /** Cambia el token de acceso devuelto por el servidor. */
    public void setToken(String token) {
        this.token = token;
    }

    /** Obtiene el identificador del usuario que acaba de iniciar sesión. */
    public Long getUsuarioId() {
        return usuarioId;
    }

    /** Cambia el identificador del usuario que inició sesión. */
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    /** Obtiene el nombre visible del usuario que inició sesión. */
    public String getNombre() {
        return nombre;
    }

    /** Cambia el nombre visible del usuario que inició sesión. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Obtiene la dirección de la foto de perfil del usuario, o null si todavía no tiene una. */
    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    /** Cambia la dirección de la foto de perfil del usuario. */
    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }
}
