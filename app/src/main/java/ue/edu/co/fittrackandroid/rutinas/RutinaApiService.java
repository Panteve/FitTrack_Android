package ue.edu.co.fittrackandroid.rutinas;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import ue.edu.co.fittrackandroid.hoy.RutinaResponse;

public interface RutinaApiService {

    @GET("rutinas/{id}")
    Call<RutinaResponse> getRutinaById(@Path("id") Long id);

    @GET("rutinas")
    Call<List<RutinasResponse>> getRutinas();

}
