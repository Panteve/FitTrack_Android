package ue.edu.co.fittrackandroid.perfil.modelo;

public class UsuarioResponse {

    private Long id;
    private String nombre;
    private Boolean status;

    public UsuarioResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
