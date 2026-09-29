package ue.edu.co.fittrackandroid.hoy;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class HomeRepository {

    private final HomeApiService homeApiService;

    public HomeRepository(Context context) {
        this.homeApiService = RetrofitClient.getInstance(context).create(HomeApiService.class);
    }

    public Call<HomeResponse> getInfoHome() {
        return homeApiService.getInfoHome();
    }

}
