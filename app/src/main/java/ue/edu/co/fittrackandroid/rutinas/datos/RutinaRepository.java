package ue.edu.co.fittrackandroid.rutinas.datos;

import android.content.Context;

import java.util.List;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinasResponse;

public class RutinaRepository {

    private final RutinaApiService rutinaApiService;

    public RutinaRepository(Context context) {
        this.rutinaApiService = RetrofitClient.getInstance(context).create(RutinaApiService.class);
    }

    public Call<RutinaResponse> getRutinaById(Long id) {
        return rutinaApiService.getRutinaById(id);
    }

    public Call<List<RutinasResponse>> getRutinas() {
        return rutinaApiService.getRutinas();
    }


}
