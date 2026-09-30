package ue.edu.co.fittrackandroid.entrenamiento.datos;

import android.content.Context;

import okhttp3.MultipartBody;
import retrofit2.Call;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoActualizarNotasRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoCrearRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoDetalleResponse;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoFotoResponse;
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

    /**
     * Prepara la actualización de las notas de un entrenamiento guardado.
     *
     * @param entrenamientoId identificador del entrenamiento
     * @param request notas nuevas del entrenamiento
     * @return llamada que devuelve el detalle actualizado
     */
    public Call<EntrenamientoDetalleResponse> actualizarNotas(
            Long entrenamientoId,
            EntrenamientoActualizarNotasRequest request) {
        return entrenamientoApiService.actualizarNotas(
                entrenamientoId,
                request
        );
    }

    /**
     * Devuelve la llamada para consultar un entrenamiento guardado. El repositorio solo
     * entrega la llamada: ejecutarla con enqueue es responsabilidad de la pantalla.
     *
     * @param id identificador del entrenamiento guardado.
     */
    public Call<EntrenamientoDetalleResponse> getEntrenamientoById(Long id) {
        return entrenamientoApiService.getEntrenamientoById(id);
    }

    /**
     * Prepara la eliminación lógica de un entrenamiento guardado.
     *
     * @param entrenamientoId identificador del entrenamiento
     * @return llamada sin contenido de respuesta
     */
    public Call<Void> eliminarEntrenamiento(Long entrenamientoId) {
        return entrenamientoApiService.eliminarEntrenamiento(
                entrenamientoId
        );
    }

    /**
     * Prepara el envío de la fotografía de un entrenamiento guardado. Si el
     * entrenamiento ya tenía una foto, el backend la reemplaza por esta.
     *
     * @param entrenamientoId identificador del entrenamiento
     * @param foto parte multipart con la imagen ya comprimida
     * @return llamada que devuelve la dirección de la foto guardada
     */
    public Call<EntrenamientoFotoResponse> subirFoto(
            Long entrenamientoId,
            MultipartBody.Part foto) {
        return entrenamientoApiService.subirFoto(entrenamientoId, foto);
    }
}
