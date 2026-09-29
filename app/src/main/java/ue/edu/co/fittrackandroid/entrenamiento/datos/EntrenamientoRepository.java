package ue.edu.co.fittrackandroid.entrenamiento.datos;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoCrearRequest;
import ue.edu.co.fittrackandroid.entrenamiento.modelo.EntrenamientoDetalleResponse;
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
     * Prepara la actualización de un entrenamiento guardado.
     *
     * @param entrenamientoId identificador del entrenamiento
     * @param entrenamientoRequest datos completos con la nota actualizada
     * @return llamada que devuelve el detalle actualizado
     */
    public Call<EntrenamientoDetalleResponse> actualizarEntrenamiento(
            Long entrenamientoId,
            EntrenamientoCrearRequest entrenamientoRequest) {
        return entrenamientoApiService.actualizarEntrenamiento(
                entrenamientoId,
                entrenamientoRequest
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
}
