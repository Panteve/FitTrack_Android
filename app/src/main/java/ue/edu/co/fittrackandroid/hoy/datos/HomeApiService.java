package ue.edu.co.fittrackandroid.hoy.datos;

import retrofit2.Call;
import retrofit2.http.GET;
import ue.edu.co.fittrackandroid.hoy.modelo.HomeResponse;

public interface HomeApiService {

    /** Solicita al servidor los datos de la pantalla de inicio: la próxima rutina y los últimos entrenamientos. */
    @GET("home")
    Call<HomeResponse> getInfoHome();

}
