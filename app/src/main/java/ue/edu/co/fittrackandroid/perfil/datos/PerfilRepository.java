package ue.edu.co.fittrackandroid.perfil.datos;

import android.content.Context;

import okhttp3.MultipartBody;
import retrofit2.Call;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarContrasenaRequest;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarNombreRequest;
import ue.edu.co.fittrackandroid.perfil.modelo.FotoPerfilResponse;
import ue.edu.co.fittrackandroid.perfil.modelo.UsuarioResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

/**
 * Centraliza las llamadas de los datos personales del Perfil.
 */
public class PerfilRepository {

    private final PerfilApiService perfilApiService;

    /** Crea el repositorio conectando el servicio de perfil al cliente Retrofit de la aplicación. */
    public PerfilRepository(Context context) {
        perfilApiService = RetrofitClient.getInstance(context)
                .create(PerfilApiService.class);
    }

    /**
     * Prepara el envío del nuevo nombre; el fragmento ejecuta la llamada, que no trae
     * cuerpo y termina con un 204 cuando el nombre quedó actualizado.
     */
    public Call<Void> cambiarNombre(CambiarNombreRequest cambiarNombreRequest) {
        return perfilApiService.cambiarNombre(cambiarNombreRequest);
    }

    /**
     * Prepara el envío de la contraseña nueva; el fragmento ejecuta la llamada, que no
     * trae cuerpo y termina con un 204 cuando la contraseña quedó actualizada.
     */
    public Call<Void> cambiarContrasena(CambiarContrasenaRequest cambiarContrasenaRequest) {
        return perfilApiService.cambiarContrasena(cambiarContrasenaRequest);
    }

    /** Prepara la subida o el reemplazo de la foto de perfil. */
    public Call<FotoPerfilResponse> guardarFoto(MultipartBody.Part foto) {
        return perfilApiService.guardarFoto(foto);
    }

    /** Prepara la eliminación de la foto del usuario autenticado. */
    public Call<Void> quitarFoto() {
        return perfilApiService.quitarFoto();
    }

    /** Prepara el borrado definitivo de la cuenta del usuario autenticado. */
    public Call<UsuarioResponse> eliminarUsuario() { return perfilApiService.eliminarUsuario(); }
}
