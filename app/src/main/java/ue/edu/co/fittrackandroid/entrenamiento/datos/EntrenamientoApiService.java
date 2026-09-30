package ue.edu.co.fittrackandroid.entrenamiento.datos;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoCrearRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoDetalleResponse;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoFotoResponse;

/** Endpoints disponibles para los entrenamientos del usuario autenticado. */
public interface EntrenamientoApiService {

    @POST("entrenamientos")
    Call<EntrenamientoDetalleResponse> crearEntrenamiento(
            @Body EntrenamientoCrearRequest entrenamientoCrearRequest);

    /**
     * Actualiza los datos editables de un entrenamiento guardado.
     *
     * @param entrenamientoId identificador del entrenamiento
     * @param entrenamientoRequest datos completos del entrenamiento con la nota actualizada
     * @return llamada que devuelve el detalle actualizado
     */
    @PUT("entrenamientos/{id}")
    Call<EntrenamientoDetalleResponse> actualizarEntrenamiento(
            @Path("id") Long entrenamientoId,
            @Body EntrenamientoCrearRequest entrenamientoRequest);

    /**
     * Recupera el detalle completo de un entrenamiento ya guardado, incluidas sus series.
     * El token lo agrega automáticamente el interceptor de autenticación.
     *
     * @param id identificador del entrenamiento guardado.
     */
    @GET("entrenamientos/{id}")
    Call<EntrenamientoDetalleResponse> getEntrenamientoById(@Path("id") Long id);

    /**
     * Elimina lógicamente un entrenamiento ya guardado. El backend lo desactiva, así que
     * deja de aparecer en el historial y ya no se puede consultar. Responde 204 sin cuerpo.
     *
     * @param entrenamientoId identificador del entrenamiento guardado.
     */
    @DELETE("entrenamientos/{id}")
    Call<Void> eliminarEntrenamiento(
            @Path("id") Long entrenamientoId
    );

    /**
     * Sube o reemplaza la fotografía de un entrenamiento ya guardado.
     *
     * <p>El backend acepta JPEG y PNG de hasta 5 MB, y si el entrenamiento ya tenía
     * una foto la sustituye por esta.
     *
     * @param entrenamientoId identificador del entrenamiento
     * @param foto parte multipart con la imagen llamada {@code foto}
     * @return llamada con el identificador y la dirección de la foto guardada
     */
    @Multipart
    @POST("entrenamientos/{id}/foto")
    Call<EntrenamientoFotoResponse> subirFoto(
            @Path("id") Long entrenamientoId,
            @Part MultipartBody.Part foto);
}
