package ue.edu.co.fittrackandroid.registro.datos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroRequest;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroResponse;

/**
 * Contrato del endpoint de registro del backend.
 * La ruta se resuelve contra la URL base del cliente de Retrofit, que ya termina en /api/.
 */
public interface RegistroApiService {

    /**
     * Crea una cuenta nueva.
     *
     * @param registroRequest cuerpo JSON con nombre, correo y contrasena.
     * @return llamada que entrega el token y el nombre cuando el backend responde 201.
     */
    @POST("auth/register")
    Call<RegistroResponse> registerUser(@Body RegistroRequest registroRequest);
}
