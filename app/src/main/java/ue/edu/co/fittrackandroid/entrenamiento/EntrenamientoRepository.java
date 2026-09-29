package ue.edu.co.fittrackandroid.entrenamiento;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

/** Centraliza las llamadas Retrofit relacionadas con entrenamientos. */
public class EntrenamientoRepository {

    private final EntrenamientoApiService entrenamientoApiService;

    public EntrenamientoRepository(Context context) {
        entrenamientoApiService = RetrofitClient.getInstance(context)
                .create(EntrenamientoApiService.class);
    }

    public Call<EntrenamientoDetalleResponse> crearEntrenamiento(
            EntrenamientoCrearRequest entrenamientoCrearRequest) {
        return entrenamientoApiService.crearEntrenamiento(entrenamientoCrearRequest);
    }
}
