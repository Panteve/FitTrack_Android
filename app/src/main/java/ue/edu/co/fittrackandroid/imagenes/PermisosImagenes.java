package ue.edu.co.fittrackandroid.imagenes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

/**
 * Permisos reales de almacenamiento que usa la galería propia de FitTrack.
 *
 * <p>La galería consulta MediaStore directamente, así que necesita el permiso que
 * corresponde a cada versión de Android: {@code READ_EXTERNAL_STORAGE} hasta Android 12L,
 * {@code READ_MEDIA_IMAGES} desde Android 13 y, en Android 14 o superior, además el acceso
 * parcial a las fotografías que el usuario autorice.
 */
public final class PermisosImagenes {

    private PermisosImagenes() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * @return los permisos que se deben solicitar según la versión de Android. La misma
     *         lista sirve para {@code ActivityResultContracts.RequestMultiplePermissions}.
     */
    public static String[] permisosSolicitados() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+: el segundo permiso permite el acceso parcial, cuando el
            // usuario solo autoriza algunas fotografías seleccionadas.
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            };
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        }

        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    /**
     * @return true si la aplicación ya puede leer las imágenes del dispositivo. Con
     *         acceso completo vale {@code READ_MEDIA_IMAGES}; con acceso parcial vale
     *         {@code READ_MEDIA_VISUAL_USER_SELECTED} y la consulta solo devuelve las
     *         fotografías autorizadas.
     */
    public static boolean tienePermisoLectura(Context contexto) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;
        }

        return ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Abre los ajustes de la aplicación, para cuando el permiso fue rechazado de forma
     * permanente y Android ya no permite volver a mostrar el diálogo.
     *
     * @param contexto contexto desde el que se abre la pantalla de ajustes
     */
    public static void abrirAjustes(Context contexto) {
        Intent intencion = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intencion.setData(Uri.parse("package:" + contexto.getPackageName()));
        contexto.startActivity(intencion);
    }
}