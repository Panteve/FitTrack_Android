package ue.edu.co.fittrackandroid.login.modelo;

/** Datos de sesión devueltos por el backend después de autenticar al usuario. */
public class LoginResponse {
    private String token;
    private Long usuarioId;
    private String nombre;
    private String fotoPerfilUrl;

    public LoginResponse(String token, Long usuarioId, String nombre, String fotoPerfilUrl) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }
}
