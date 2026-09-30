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

    /** Crea el repositorio conectando el servicio de entrenamientos al cliente Retrofit de la aplicación. */
    public EntrenamientoRepository(Context context) {
        entrenamientoApiService = RetrofitClient.getInstance(context)
                .create(EntrenamientoApiService.class);
    }

    /** Crea la llamada que registra un entrenamiento terminado con sus ejercicios y series. */
    public Call<EntrenamientoDetalleResponse> crearEntrenamiento(
            EntrenamientoCrearRequest entrenamientoCrearRequest) {
        return entrenamientoApiService.crearEntrenamiento(entrenamientoCrearRequest);
    }

    /** Prepara la actualización de las notas de un entrenamiento guardado. */
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
     */
    public Call<EntrenamientoDetalleResponse> getEntrenamientoById(Long id) {
        return entrenamientoApiService.getEntrenamientoById(id);
    }

    /** Prepara la eliminación lógica de un entrenamiento guardado. */
    public Call<Void> eliminarEntrenamiento(Long entrenamientoId) {
        return entrenamientoApiService.eliminarEntrenamiento(
                entrenamientoId
        );
    }

    /**
     * Prepara el envío de la fotografía de un entrenamiento guardado. Si el
     * entrenamiento ya tenía una foto, el backend la reemplaza por esta.
     */
    public Call<EntrenamientoFotoResponse> subirFoto(
            Long entrenamientoId,
            MultipartBody.Part foto) {
        return entrenamientoApiService.subirFoto(entrenamientoId, foto);
    }
}
