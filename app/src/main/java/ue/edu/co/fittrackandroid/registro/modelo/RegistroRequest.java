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
     * Crea el cuerpo del registro con el nombre que se mostrará en el perfil, el correo
     * con el que se iniciará sesión y la contraseña sin recortar tal como la escribió
     * el usuario.
     */
    public RegistroRequest(String nombre, String correo, String contrasena) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    /** Obtiene el nombre que se mostrará en el perfil del usuario nuevo. */
    public String getNombre() {
        return nombre;
    }

    /** Cambia el nombre que se mostrará en el perfil del usuario nuevo. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Obtiene el correo con el que se iniciará sesión la cuenta creada. */
    public String getCorreo() {
        return correo;
    }

    /** Cambia el correo con el que se iniciará sesión la cuenta creada. */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /** Obtiene la contraseña que se envía al servidor, sin recortar. */
    public String getContrasena() {
        return contrasena;
    }

    /** Cambia la contraseña que se enviará al servidor. */
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
