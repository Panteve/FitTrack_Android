package ue.edu.co.fittrackandroid.ejercicios.datos;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    // Trae un ejercicio propio por su identificador. El backend solo deja consultar
    // los ejercicios que pertenecen al usuario autenticado.
    @GET("ejercicios/{id}")
    Call<EjercicioResponse> getEjercicioById(@Path("id") Long ejercicioId);

    // Actualiza un ejercicio propio. El cuerpo es el mismo que usa la creación porque
    // el backend reemplaza los datos del ejercicio por los que se envían.
    @PUT("ejercicios/{id}")
    Call<EjercicioResponse> actualizarEjercicio(
            @Path("id") Long ejercicioId,
            @Body EjercicioRequest ejercicioRequest
    );

    // Elimina lógicamente un ejercicio propio. El backend responde 204 sin cuerpo.
    @DELETE("ejercicios/{id}")
    Call<Void> eliminarEjercicio(@Path("id") Long ejercicioId);
}
