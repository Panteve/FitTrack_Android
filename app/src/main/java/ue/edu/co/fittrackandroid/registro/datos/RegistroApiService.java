package ue.edu.co.fittrackandroid.registro.datos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroRequest;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroResponse;

public interface RegistroApiService {

    @POST("auth/register")
    Call<RegistroResponse> registerUser(@Body RegistroRequest registroRequest);
}
