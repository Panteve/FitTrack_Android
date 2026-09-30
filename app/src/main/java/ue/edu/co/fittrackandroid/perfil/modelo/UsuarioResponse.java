package ue.edu.co.fittrackandroid.perfil.modelo;

public class UsuarioResponse {

    private Long id;
    private String nombre;
    private Boolean status;

    /** Crea el usuario vacío; el backend lo llena al deserializar la respuesta. */
    public UsuarioResponse() {
    }

    /** Devuelve el identificador del usuario en el backend. */
    public Long getId() {
        return id;
    }

    /** Guarda el identificador del usuario en el backend. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Devuelve el nombre del usuario. */
    public String getNombre() {
        return nombre;
    }

    /** Guarda el nombre del usuario. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve el estado de la cuenta que envía el backend. */
    public Boolean getStatus() {
        return status;
    }

    /** Guarda el estado de la cuenta. */
    public void setStatus(Boolean status) {
        this.status = status;
    }
}
