package ue.edu.co.fittrackandroid.ejercicios.datos;

import android.content.Context;

import java.util.List;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioRequest;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjercicioResponse;
import ue.edu.co.fittrackandroid.ejercicios.modelo.EjerciciosDisponiblesResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class EjercicioRepository {

    private final EjercicioApiService ejercicioApiService;

    public EjercicioRepository(Context context) {
        this.ejercicioApiService = RetrofitClient.getInstance(context)
                .create(EjercicioApiService.class);
    }

    /** Prepara la consulta de los ejercicios del usuario; el Fragmento la ejecuta. */
    public Call<List<EjercicioResponse>> getMisEjercicios() {
        return ejercicioApiService.getMisEjercicios();
    }

    /** Prepara la consulta de los ejercicios disponibles; el Fragmento la ejecuta. */
    public Call<EjerciciosDisponiblesResponse> getEjerciciosDisponibles() {
        return ejercicioApiService.getEjerciciosDisponibles();
    }

    /** Prepara el envío de un ejercicio nuevo; el Fragmento ejecuta la llamada. */
    public Call<EjercicioResponse> crearEjercicio(EjercicioRequest ejercicioRequest) {
        return ejercicioApiService.crearEjercicio(ejercicioRequest);
    }

    /**
     * Prepara la consulta de un ejercicio propio por su identificador.
     *
     * @param ejercicioId identificador del ejercicio que se quiere modificar.
     * @return llamada con el detalle del ejercicio.
     */
    public Call<EjercicioResponse> getEjercicioById(Long ejercicioId) {
        return ejercicioApiService.getEjercicioById(ejercicioId);
    }

    /**
     * Prepara el envío de los cambios de un ejercicio propio.
     * El cuerpo es el mismo de la creación porque el backend reemplaza el nombre y el
     * grupo muscular por los que se envían.
     *
     * @param ejercicioId identificador del ejercicio que se está modificando.
     * @param ejercicioRequest nombre y grupo muscular con los cambios.
     * @return llamada con el ejercicio ya actualizado.
     */
    public Call<EjercicioResponse> actualizarEjercicio(
            Long ejercicioId,
            EjercicioRequest ejercicioRequest
    ) {
        return ejercicioApiService.actualizarEjercicio(ejercicioId, ejercicioRequest);
    }

    /**
     * Prepara el borrado lógico de un ejercicio propio. La respuesta no trae cuerpo,
     * por eso la llamada es de tipo Void.
     *
     * @param ejercicioId identificador del ejercicio que se quiere borrar.
     * @return llamada que termina con un 204 cuando el ejercicio quedó borrado.
     */
    public Call<Void> eliminarEjercicio(Long ejercicioId) {
        return ejercicioApiService.eliminarEjercicio(ejercicioId);
    }
}
