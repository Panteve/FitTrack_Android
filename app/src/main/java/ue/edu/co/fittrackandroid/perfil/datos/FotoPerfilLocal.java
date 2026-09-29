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

    /**
     * Crea el administrador dentro del almacenamiento interno de la aplicación.
     *
     * @param context contexto usado para obtener {@code filesDir}
     */
    public FotoPerfilLocal(Context context) {
        directorioPerfil = new File(
                context.getApplicationContext().getFilesDir(),
                DIRECTORIO_PERFIL
        );
    }

    /**
     * @param usuarioId identificador de la cuenta
     * @return archivo definitivo de la foto del usuario
     */
    public File obtenerArchivo(Long usuarioId) {
        validarUsuarioId(usuarioId);
        return new File(directorioPerfil, "foto_perfil_" + usuarioId + ".jpg");
    }

    /**
     * @param usuarioId identificador de la cuenta
     * @return true cuando existe una copia local no vacía
     */
    public boolean existeFoto(Long usuarioId) {
        File archivoFoto = obtenerArchivo(usuarioId);
        return archivoFoto.isFile() && archivoFoto.length() > 0;
    }

    /**
     * Guarda bytes ya validados mediante un archivo temporal.
     *
     * @param usuarioId identificador de la cuenta
     * @param contenido bytes de la imagen
     * @throws IOException si la copia no puede escribirse o no es una imagen válida
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
     * la copia anterior. Una interrupción conserva el último archivo válido.
     *
     * @param usuarioId identificador de la cuenta
     * @param datos contenido de la imagen
     * @throws IOException si la descarga es inválida o no puede guardarse
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
     * Decodifica una copia reducida para evitar cargar la imagen original completa en memoria.
     *
     * @param usuarioId identificador de la cuenta
     * @param anchoMaximo ancho aproximado requerido por la vista
     * @param altoMaximo alto aproximado requerido por la vista
     * @return bitmap reducido, o null si no hay una copia legible
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

    private void prepararDirectorio() throws IOException {
        if (directorioPerfil.isDirectory()) {
            return;
        }
        if (!directorioPerfil.mkdirs() && !directorioPerfil.isDirectory()) {
            throw new IOException("No se pudo crear el directorio de perfil");
        }
    }

    private File obtenerArchivoTemporal(Long usuarioId) {
        return new File(directorioPerfil, "foto_perfil_" + usuarioId + ".tmp");
    }

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

    private void validarImagen(File archivoTemporal) throws IOException {
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(archivoTemporal.getAbsolutePath(), opciones);
        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) {
            throw new IOException("El archivo no contiene una imagen válida");
        }
    }

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

    private void validarUsuarioId(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El identificador del usuario no es válido");
        }
    }
}
