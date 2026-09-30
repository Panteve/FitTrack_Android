package ue.edu.co.fittrackandroid.login.datos;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.login.modelo.LoginRequest;
import ue.edu.co.fittrackandroid.login.modelo.LoginResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class LoginRepository {

    private final LoginApiService loginApiService;

    /** Crea el repositorio obteniendo el servicio de inicio de sesión del cliente de Retrofit. */
    public LoginRepository(Context context) {
        this.loginApiService = RetrofitClient.getInstance(context).create(LoginApiService.class);
    }

    /** Envía las credenciales al servidor y devuelve la respuesta con el token de la sesión iniciada. */
    public Call<LoginResponse> loginUser(LoginRequest loginRequest) {
        return loginApiService.loginUser(loginRequest);
    }

}
