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
     * Crea la imagen con los datos leídos de MediaStore: su identificador, nombre, tipo
     * MIME, tamaño en bytes, fecha de creación en segundos desde la época y la dirección
     * de tipo content:// desde la que se puede leer.
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

    /** Obtiene el identificador con el que la imagen aparece registrada en MediaStore. */
    public long getId() {
        return id;
    }

    /** Obtiene el nombre con el que aparece guardada la imagen en el dispositivo. */
    public String getNombre() {
        return nombre;
    }

    /** Obtiene el tipo MIME de la imagen, como image/jpeg o image/png. */
    public String getTipoContenido() {
        return tipoContenido;
    }

    /** Obtiene el tamaño del archivo de la imagen en bytes. */
    public long getTamanoBytes() {
        return tamanoBytes;
    }

    /** Obtiene la fecha de creación de la imagen en segundos desde la época. */
    public long getFechaCreacionSegundos() {
        return fechaCreacionSegundos;
    }

    /** Obtiene la dirección de tipo content:// desde la que se puede leer la imagen. */
    public Uri getUri() {
        return uri;
    }
}