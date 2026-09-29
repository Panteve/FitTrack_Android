package ue.edu.co.fittrackandroid.rutinas.datos;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinasResponse;

public interface RutinaApiService {

    @GET("rutinas/{id}")
    Call<RutinaResponse> getRutinaById(@Path("id") Long id);

    @GET("rutinas")
    Call<List<RutinasResponse>> getRutinas();

    /** Crea una rutina con sus ejercicios y series. El backend responde 201 con el detalle. */
    @POST("rutinas")
    Call<RutinaResponse> crearRutina(@Body RutinaCrearRequest rutinaCrearRequest);

    /**
     * Actualiza una rutina existente. Recibe exactamente el mismo cuerpo que la creación,
     * porque el backend reemplaza toda la configuración de la rutina por la que se envía.
     */
    @PUT("rutinas/{id}")
    Call<RutinaResponse> actualizarRutina(
            @Path("id") Long rutinaId,
            @Body RutinaCrearRequest rutinaRequest
    );

    /** Elimina lógicamente una rutina. El backend responde 204 sin cuerpo. */
    @DELETE("rutinas/{id}")
    Call<Void> eliminarRutina(@Path("id") Long rutinaId);

}
