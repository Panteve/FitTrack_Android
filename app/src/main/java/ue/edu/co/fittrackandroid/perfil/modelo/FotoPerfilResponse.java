package ue.edu.co.fittrackandroid.perfil.modelo;

/** Respuesta recibida después de guardar o reemplazar la foto de perfil. */
public class FotoPerfilResponse {

    private String fotoPerfilUrl;

    /** @return la URL temporal de la foto guardada. */
    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }
}
