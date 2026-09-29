package ue.edu.co.fittrackandroid.registro.datos;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroRequest;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

/**
 * Encapsula la llamada al backend que registra una cuenta nueva.
 * No ejecuta la petición: entrega la llamada de Retrofit para que la pantalla la
 * ejecute y pueda revisar la respuesta.
 */
public class RegistroRepository {

    private final RegistroApiService registroApiService;

    /**
     * @param context contexto de la aplicación, usado por el cliente de Retrofit.
     */
    public RegistroRepository(Context context) {
        this.registroApiService = RetrofitClient.getInstance(context).create(RegistroApiService.class);
    }

    /**
     * Prepara el envío de los datos de registro al backend.
     *
     * @param registroRequest nombre, correo y contraseña tal como los escribió el usuario.
     * @return llamada de Retrofit pendiente de ejecutar con enqueue.
     */
    public Call<RegistroResponse> registrarUsuario(RegistroRequest registroRequest) {
        return registroApiService.registerUser(registroRequest);
    }
}
