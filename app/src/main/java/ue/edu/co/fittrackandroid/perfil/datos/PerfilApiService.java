package ue.edu.co.fittrackandroid.perfil.datos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarContrasenaRequest;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarNombreRequest;

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
}
