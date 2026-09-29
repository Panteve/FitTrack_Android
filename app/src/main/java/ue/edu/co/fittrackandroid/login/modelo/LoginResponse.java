package ue.edu.co.fittrackandroid.login.modelo;

public class LoginResponse {
    private String token;
    private String nombre;

    public LoginResponse(String token, String nombre) {
        this.token = token;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
