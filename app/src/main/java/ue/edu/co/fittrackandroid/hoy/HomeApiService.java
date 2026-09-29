package ue.edu.co.fittrackandroid.hoy;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HomeApiService {

    @GET("home")
    Call<HomeResponse> getInfoHome();

}
