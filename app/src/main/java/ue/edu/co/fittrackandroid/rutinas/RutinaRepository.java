package ue.edu.co.fittrackandroid.rutinas;

import android.content.Context;

import java.util.List;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.hoy.RutinaResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class RutinaRepository {

    private final RutinaApiService rutinaApiService;

    public RutinaRepository(Context context) {
        this.rutinaApiService = RetrofitClient.getInstance(context).create(RutinaApiService.class);
    }

    public Call<RutinaResponse> getRutinaById(int id) {
        return rutinaApiService.getRutinaByid(id);
    }

    public Call<List<RutinasResponse>> getRutinas() {
        return rutinaApiService.getRutinas();
    }


}