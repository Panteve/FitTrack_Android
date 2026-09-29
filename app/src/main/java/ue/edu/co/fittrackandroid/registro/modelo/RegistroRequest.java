package ue.edu.co.fittrackandroid.registro.modelo;

/**
 * Cuerpo JSON que envía el registro de una cuenta nueva al backend.
 * Los nombres de los campos deben coincidir con lo que espera el endpoint
 * auth/register: nombre, correo y contrasena. La contraseña viaja solo en la
 * petición: no se guarda en el dispositivo ni en logs.
 */
public class RegistroRequest {

    private String nombre;
    private String correo;
    private String contrasena;

    /**
     * @param nombre      nombre mostrado en el perfil.
     * @param correo      correo con el que se inicia sesión.
     * @param contrasena  contraseña sin recortar, tal como la escribió el usuario.
     */
    public RegistroRequest(String nombre, String correo, String contrasena) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
