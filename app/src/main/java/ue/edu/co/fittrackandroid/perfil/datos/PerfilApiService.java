package ue.edu.co.fittrackandroid.perfil.datos;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PUT;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarContrasenaRequest;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarNombreRequest;
import ue.edu.co.fittrackandroid.perfil.modelo.FotoPerfilResponse;

/**
 * Endpoints relacionados con los datos personales del usuario.
 */
public interface PerfilApiService {

    // El encabezado Authorization lo agrega AutenticacionInterceptor.
    // El backend responde 204 sin cuerpo cuando el nombre quedó actualizado.
    @PUT("usuarios/me/nombre")
    Call<Void> cambiarNombre(@Body CambiarNombreRequest cambiarNombreRequest);

    // Un 401 significa que la contraseña actual no es la del usuario: la pantalla
    // lo trata aparte para poder señalar el primer campo.
    @PUT("usuarios/me/password")
    Call<Void> cambiarContrasena(@Body CambiarContrasenaRequest cambiarContrasenaRequest);

    /**
     * Sube o reemplaza la foto del usuario autenticado.
     *
     * @param foto parte multipart JPEG o PNG llamada {@code foto}
     * @return llamada que contiene la URL temporal de la foto guardada
     */
    @Multipart
    @PUT("usuarios/me/foto")
    Call<FotoPerfilResponse> guardarFoto(@Part MultipartBody.Part foto);

    /**
     * Quita la foto del usuario autenticado.
     *
     * @return llamada sin cuerpo que termina con HTTP 204
     */
    @DELETE("usuarios/me/foto")
    Call<Void> quitarFoto();
}
