package ue.edu.co.fittrackandroid.ejercicios;

import android.content.Context;

import java.util.List;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class EjercicioRepository {

    private final EjercicioApiService ejercicioApiService;

    public EjercicioRepository(Context context) {
        this.ejercicioApiService = RetrofitClient.getInstance(context)
                .create(EjercicioApiService.class);
    }

    /** Prepara la consulta de los ejercicios del usuario; el Fragmento la ejecuta. */
    public Call<List<EjercicioResponse>> getMisEjercicios() {
        return ejercicioApiService.getMisEjercicios();
    }
}
