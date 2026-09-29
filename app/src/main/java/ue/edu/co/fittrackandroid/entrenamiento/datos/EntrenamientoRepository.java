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
     * Devuelve la llamada para consultar un entrenamiento guardado. El repositorio solo
     * entrega la llamada: ejecutarla con enqueue es responsabilidad de la pantalla.
     *
     * @param id identificador del entrenamiento guardado.
     */
    public Call<EntrenamientoDetalleResponse> getEntrenamientoById(Long id) {
        return entrenamientoApiService.getEntrenamientoById(id);
    }
}
