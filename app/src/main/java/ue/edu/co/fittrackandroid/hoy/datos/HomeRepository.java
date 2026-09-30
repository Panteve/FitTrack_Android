package ue.edu.co.fittrackandroid.hoy.datos;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.hoy.modelo.HomeResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class HomeRepository {

    private final HomeApiService homeApiService;

    /** Crea el repositorio obteniendo el servicio de inicio del cliente de Retrofit. */
    public HomeRepository(Context context) {
        this.homeApiService = RetrofitClient.getInstance(context).create(HomeApiService.class);
    }

    /** Pide al servidor los datos que la pantalla de inicio necesita mostrar. */
    public Call<HomeResponse> getInfoHome() {
        return homeApiService.getInfoHome();
    }

}
