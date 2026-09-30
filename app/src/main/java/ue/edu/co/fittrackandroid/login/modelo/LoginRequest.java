package ue.edu.co.fittrackandroid.login.modelo;

public class LoginRequest {
    private String correo;
    private String contrasena;

    /** Crea la petición con el correo y la contraseña que el usuario escribió en el formulario. */
    public LoginRequest(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }

    /** Obtiene el correo con el que se intenta iniciar sesión. */
    public String getCorreo() {
        return correo;
    }

    /** Cambia el correo con el que se intenta iniciar sesión. */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /** Obtiene la contraseña que se envía junto al correo. */
    public String getContrasena() {
        return contrasena;
    }

    /** Cambia la contraseña que se envía junto al correo. */
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
