package ue.edu.co.fittrackandroid.hoy.datos;

import retrofit2.Call;
import retrofit2.http.GET;
import ue.edu.co.fittrackandroid.hoy.modelo.HomeResponse;

public interface HomeApiService {

    @GET("home")
    Call<HomeResponse> getInfoHome();

}
