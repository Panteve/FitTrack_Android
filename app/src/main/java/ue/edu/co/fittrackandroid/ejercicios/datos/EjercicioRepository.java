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
}
