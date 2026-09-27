package ue.edu.co.fittrackandroid.login;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class LoginRepository {

    private final LoginApiService loginApiService;

    public LoginRepository() {
        this.loginApiService = RetrofitClient.getInstance().create(LoginApiService.class);
    }

    public Call<LoginResponse> loginUser(LoginRequest loginRequest) {
        return loginApiService.loginUser(loginRequest);
    }

}
