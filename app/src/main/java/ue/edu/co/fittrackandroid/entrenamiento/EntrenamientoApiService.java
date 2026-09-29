package ue.edu.co.fittrackandroid.entrenamiento;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Endpoints disponibles para los entrenamientos del usuario autenticado. */
public interface EntrenamientoApiService {

    @POST("entrenamientos")
    Call<EntrenamientoDetalleResponse> crearEntrenamiento(
            @Body EntrenamientoCrearRequest entrenamientoCrearRequest);

    /**
     * Recupera el detalle completo de un entrenamiento ya guardado, incluidas sus series.
     * El token lo agrega automáticamente el interceptor de autenticación.
     *
     * @param id identificador del entrenamiento guardado.
     */
    @GET("entrenamientos/{id}")
    Call<EntrenamientoDetalleResponse> getEntrenamientoById(@Path("id") Long id);
}
