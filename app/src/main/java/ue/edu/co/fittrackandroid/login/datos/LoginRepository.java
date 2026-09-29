package ue.edu.co.fittrackandroid.login.datos;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.login.modelo.LoginRequest;
import ue.edu.co.fittrackandroid.login.modelo.LoginResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class LoginRepository {

    private final LoginApiService loginApiService;

    public LoginRepository(Context context) {
        this.loginApiService = RetrofitClient.getInstance(context).create(LoginApiService.class);
    }

    public Call<LoginResponse> loginUser(LoginRequest loginRequest) {
        return loginApiService.loginUser(loginRequest);
    }

}
