package ue.edu.co.fittrackandroid.ejercicios;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface EjercicioApiService {

    // El encabezado Authorization lo agrega AutenticacionInterceptor.
    @GET("ejercicios/mis-ejercicios")
    Call<List<EjercicioResponse>> getMisEjercicios();
}
