package ue.edu.co.fittrackandroid.login.datos;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ue.edu.co.fittrackandroid.login.modelo.LoginRequest;
import ue.edu.co.fittrackandroid.login.modelo.LoginResponse;

public interface LoginApiService {

    @POST("auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

}
