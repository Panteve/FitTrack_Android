package ue.edu.co.fittrackandroid.entrenamiento;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** Endpoints disponibles para los entrenamientos del usuario autenticado. */
public interface EntrenamientoApiService {

    @POST("entrenamientos")
    Call<EntrenamientoDetalleResponse> crearEntrenamiento(
            @Body EntrenamientoCrearRequest entrenamientoCrearRequest);
}
