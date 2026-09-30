package ue.edu.co.fittrackandroid.imagenes;

import android.net.Uri;

/**
 * Una imagen encontrada en el almacenamiento externo del dispositivo mediante
 * MediaStore. Guarda únicamente los campos que la galería necesita y la dirección
 * {@code content://} desde la que se puede leer, nunca la ruta física del archivo.
 */
public class ImagenDispositivo {

    private final long id;
    private final String nombre;
    private final String tipoContenido;
    private final long tamanoBytes;
    private final long fechaCreacionSegundos;
    private final Uri uri;

    /**
     * @param id                    identificador de la imagen en MediaStore
     * @param nombre                nombre con el que aparece guardada la imagen
     * @param tipoContenido         tipo MIME, {@code image/jpeg} o {@code image/png}
     * @param tamanoBytes           tamaño del archivo en bytes
     * @param fechaCreacionSegundos fecha de creación en segundos desde la época
     * @param uri                   dirección {@code content://} de la imagen
     */
    public ImagenDispositivo(long id, String nombre, String tipoContenido,
                             long tamanoBytes, long fechaCreacionSegundos, Uri uri) {
        this.id = id;
        this.nombre = nombre;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = tamanoBytes;
        this.fechaCreacionSegundos = fechaCreacionSegundos;
        this.uri = uri;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public long getFechaCreacionSegundos() {
        return fechaCreacionSegundos;
    }

    public Uri getUri() {
        return uri;
    }
}