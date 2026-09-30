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
import ue.edu.co.fittrackandroid.perfil.modelo.UsuarioResponse;

/**
 * Endpoints relacionados con los datos personales del usuario.
 */
public interface PerfilApiService {

    /**
     * Cambia el nombre del usuario autenticado. El encabezado Authorization lo agrega
     * AutenticacionInterceptor y el backend responde 204 sin cuerpo cuando el nombre
     * quedó actualizado.
     */
    @PUT("usuarios/me/nombre")
    Call<Void> cambiarNombre(@Body CambiarNombreRequest cambiarNombreRequest);

    /**
     * Cambia la contraseña del usuario autenticado. Un 401 significa que la contraseña
     * actual no es la del usuario: la pantalla lo trata aparte para poder señalar el
     * primer campo.
     */
    @PUT("usuarios/me/password")
    Call<Void> cambiarContrasena(@Body CambiarContrasenaRequest cambiarContrasenaRequest);

    /**
     * Sube o reemplaza la foto del usuario autenticado y devuelve la URL temporal
     * de la imagen que quedó guardada.
     */
    @Multipart
    @PUT("usuarios/me/foto")
    Call<FotoPerfilResponse> guardarFoto(@Part MultipartBody.Part foto);

    /** Quita la foto del usuario autenticado; la llamada termina con HTTP 204. */
    @DELETE("usuarios/me/foto")
    Call<Void> quitarFoto();

    /** Elimina la cuenta del usuario autenticado y devuelve los datos de lo que se borró. */
    @DELETE("usuarios/me/eliminar") Call<UsuarioResponse> eliminarUsuario();
}
