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

    public PerfilRepository(Context context) {
        perfilApiService = RetrofitClient.getInstance(context)
                .create(PerfilApiService.class);
    }

    /**
     * Prepara el envío del nuevo nombre; el Fragmento ejecuta la llamada.
     * La respuesta no trae cuerpo, por eso la llamada es de tipo Void.
     *
     * @param cambiarNombreRequest nombre nuevo ya validado.
     * @return llamada que termina con un 204 cuando el nombre quedó actualizado.
     */
    public Call<Void> cambiarNombre(CambiarNombreRequest cambiarNombreRequest) {
        return perfilApiService.cambiarNombre(cambiarNombreRequest);
    }

    /**
     * Prepara el envío de la contraseña nueva; el Fragmento ejecuta la llamada.
     * La respuesta no trae cuerpo, por eso la llamada es de tipo Void.
     *
     * @param cambiarContrasenaRequest contraseña actual y nueva ya validadas.
     * @return llamada que termina con un 204 cuando la contraseña quedó actualizada.
     */
    public Call<Void> cambiarContrasena(CambiarContrasenaRequest cambiarContrasenaRequest) {
        return perfilApiService.cambiarContrasena(cambiarContrasenaRequest);
    }

    /**
     * Prepara la subida o el reemplazo de la foto de perfil.
     *
     * @param foto parte multipart ya validada
     * @return llamada que devuelve la ubicación temporal de la foto
     */
    public Call<FotoPerfilResponse> guardarFoto(MultipartBody.Part foto) {
        return perfilApiService.guardarFoto(foto);
    }

    /**
     * Prepara la eliminación de la foto del usuario autenticado.
     *
     * @return llamada sin contenido de respuesta
     */
    public Call<Void> quitarFoto() {
        return perfilApiService.quitarFoto();
    }

    public Call<UsuarioResponse> eliminarUsuario() { return perfilApiService.eliminarUsuario(); }
}
