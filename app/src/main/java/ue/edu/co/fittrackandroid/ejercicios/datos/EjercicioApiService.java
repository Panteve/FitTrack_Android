package ue.edu.co.fittrackandroid.ejercicios.datos;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioRequest;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjerciciosDisponiblesResponse;

public interface EjercicioApiService {

    // El encabezado Authorization lo agrega AutenticacionInterceptor.
    @GET("ejercicios/mis-ejercicios")
    Call<List<EjercicioResponse>> getMisEjercicios();

    // Trae los ejercicios del sistema y los del usuario ya separados.
    @GET("ejercicios/disponibles")
    Call<EjerciciosDisponiblesResponse> getEjerciciosDisponibles();

    // Registra un ejercicio nuevo con el nombre y el grupo muscular elegidos.
    @POST("ejercicios")
    Call<EjercicioResponse> crearEjercicio(@Body EjercicioRequest ejercicioRequest);
}
