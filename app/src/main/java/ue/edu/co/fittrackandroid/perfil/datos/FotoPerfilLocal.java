package ue.edu.co.fittrackandroid.perfil.datos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Administra la copia privada de la foto de perfil asociada a cada usuario. */
public final class FotoPerfilLocal {

    private static final int TAMANO_MAXIMO_FOTO_BYTES = 5 * 1024 * 1024;
    private static final int TAMANO_BUFFER = 8 * 1024;
    private static final String DIRECTORIO_PERFIL = "perfil";

    private final File directorioPerfil;

    /** Crea el administrador dentro del almacenamiento interno de la aplicación. */
    public FotoPerfilLocal(Context context) {
        directorioPerfil = new File(
                context.getApplicationContext().getFilesDir(),
                DIRECTORIO_PERFIL
        );
    }

    /** Devuelve el archivo definitivo donde se guarda la foto de esa cuenta. */
    public File obtenerArchivo(Long usuarioId) {
        validarUsuarioId(usuarioId);
        return new File(directorioPerfil, "foto_perfil_" + usuarioId + ".jpg");
    }

    /** Indica si existe una copia local de la foto de esa cuenta que no esté vacía. */
    public boolean existeFoto(Long usuarioId) {
        File archivoFoto = obtenerArchivo(usuarioId);
        return archivoFoto.isFile() && archivoFoto.length() > 0;
    }

    /**
     * Guarda bytes ya validados mediante un archivo temporal. Avisa con una excepción de
     * entrada y salida si la copia no puede escribirse o no es una imagen válida.
     */
    public void guardarDesdeBytes(Long usuarioId, byte[] contenido) throws IOException {
        if (contenido == null || contenido.length == 0
                || contenido.length > TAMANO_MAXIMO_FOTO_BYTES) {
            throw new IOException("La foto local no tiene un tamaño válido");
        }

        try (InputStream entrada = new ByteArrayInputStream(contenido)) {
            guardarDesdeStream(usuarioId, entrada);
        }
    }

    /**
     * Escribe la imagen en un temporal, valida que pueda decodificarse y después reemplaza
     * la copia anterior. Una interrupción conserva el último archivo válido. Avisa con
     * una excepción de entrada y salida si la imagen es inválida o no puede guardarse.
     */
    public void guardarDesdeStream(Long usuarioId, InputStream datos) throws IOException {
        validarUsuarioId(usuarioId);
        prepararDirectorio();

        File archivoTemporal = obtenerArchivoTemporal(usuarioId);
        File archivoDefinitivo = obtenerArchivo(usuarioId);

        try {
            escribirTemporal(datos, archivoTemporal);
            validarImagen(archivoTemporal);
            reemplazarArchivo(archivoTemporal, archivoDefinitivo);
        } finally {
            if (archivoTemporal.exists()) {
                archivoTemporal.delete();
            }
        }
    }

    /**
     * Decodifica una copia reducida para evitar cargar la imagen original completa en
     * memoria, ajustada al tamaño que pide la vista. Devuelve el bitmap reducido, o nulo
     * si no hay una copia legible.
     */
    @Nullable
    public Bitmap cargarBitmap(Long usuarioId, int anchoMaximo, int altoMaximo) {
        if (!existeFoto(usuarioId)) {
            return null;
        }

        File archivoFoto = obtenerArchivo(usuarioId);
        BitmapFactory.Options medidas = new BitmapFactory.Options();
        medidas.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivoFoto.getAbsolutePath(), medidas);

        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) {
            return null;
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = calcularMuestra(
                medidas.outWidth,
                medidas.outHeight,
                anchoMaximo,
                altoMaximo
        );
        return BitmapFactory.decodeFile(archivoFoto.getAbsolutePath(), opciones);
    }

    /** Elimina la foto definitiva y cualquier temporal pendiente del usuario. */
    public void eliminar(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            return;
        }

        File archivoFoto = obtenerArchivo(usuarioId);
        File archivoTemporal = obtenerArchivoTemporal(usuarioId);
        if (archivoFoto.exists()) {
            archivoFoto.delete();
        }
        if (archivoTemporal.exists()) {
            archivoTemporal.delete();
        }
    }

    /** Crea el directorio de las fotos de perfil y avisa si no se pudo crear. */
    private void prepararDirectorio() throws IOException {
        if (directorioPerfil.isDirectory()) {
            return;
        }
        if (!directorioPerfil.mkdirs() && !directorioPerfil.isDirectory()) {
            throw new IOException("No se pudo crear el directorio de perfil");
        }
    }

    /** Devuelve el archivo temporal donde se escribe la foto antes de validarla. */
    private File obtenerArchivoTemporal(Long usuarioId) {
        return new File(directorioPerfil, "foto_perfil_" + usuarioId + ".tmp");
    }

    /**
     * Escribe la imagen en el archivo temporal sin dejar que pase del tamaño permitido,
     * y avisa si llegó vacía o sin datos.
     */
    private void escribirTemporal(InputStream datos, File archivoTemporal) throws IOException {
        if (datos == null) {
            throw new IOException("La foto no contiene datos");
        }

        int bytesTotales = 0;
        byte[] buffer = new byte[TAMANO_BUFFER];

        try (FileOutputStream salida = new FileOutputStream(archivoTemporal, false)) {
            int bytesLeidos;
            while ((bytesLeidos = datos.read(buffer)) != -1) {
                bytesTotales += bytesLeidos;
                if (bytesTotales > TAMANO_MAXIMO_FOTO_BYTES) {
                    throw new IOException("La foto supera el tamaño permitido");
                }
                salida.write(buffer, 0, bytesLeidos);
            }
            salida.getFD().sync();
        }

        if (bytesTotales == 0) {
            throw new IOException("La foto está vacía");
        }
    }

    /** Revisa que el temporal contenga una imagen decodificable y avisa si no lo es. */
    private void validarImagen(File archivoTemporal) throws IOException {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivoTemporal.getAbsolutePath(), opciones);
        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) {
            throw new IOException("El archivo no contiene una imagen válida");
        }
    }

    /** Mueve el temporal sobre el archivo definitivo, de forma atómica cuando el sistema lo permite. */
    private void reemplazarArchivo(File temporal, File definitivo) throws IOException {
        try {
            Files.move(
                    temporal.toPath(),
                    definitivo.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(
                    temporal.toPath(),
                    definitivo.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    /** Calcula el divisor de decodificación que reduce la imagen al tamaño que pide la vista. */
    private int calcularMuestra(int anchoOriginal, int altoOriginal,
                                int anchoMaximo, int altoMaximo) {
        int anchoObjetivo = Math.max(1, anchoMaximo);
        int altoObjetivo = Math.max(1, altoMaximo);
        int muestra = 1;

        while (anchoOriginal / (muestra * 2) >= anchoObjetivo
                && altoOriginal / (muestra * 2) >= altoObjetivo) {
            muestra *= 2;
        }
        return muestra;
    }

    /** Avisa si el identificador de la cuenta no sirve para nombrar un archivo. */
    private void validarUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario no es válido");
        }
    }
}
