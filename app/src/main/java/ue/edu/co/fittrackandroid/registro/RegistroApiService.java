package ue.edu.co.fittrackandroid.registro;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegistroApiService {

    @POST("auth/register")
    Call<RegistroResponse> registerUser(@Body RegistroRequest registroRequest);
}
