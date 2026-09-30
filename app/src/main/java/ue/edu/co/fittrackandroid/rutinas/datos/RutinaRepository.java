package ue.edu.co.fittrackandroid.rutinas.datos;

import android.content.Context;

import java.util.List;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.hoy.modelo.RutinaResponse;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinaCrearRequest;
import ue.edu.co.fittrackandroid.rutinas.modelo.RutinasResponse;

public class RutinaRepository {

    private final RutinaApiService rutinaApiService;

    /** Crea el repositorio obteniendo el servicio de rutinas del cliente de Retrofit. */
    public RutinaRepository(Context context) {
        this.rutinaApiService = RetrofitClient.getInstance(context).create(RutinaApiService.class);
    }

    /** Pide al servidor el detalle de la rutina indicada. */
    public Call<RutinaResponse> getRutinaById(Long id) {
        return rutinaApiService.getRutinaById(id);
    }

    /** Pide al servidor la lista de rutinas del usuario. */
    public Call<List<RutinasResponse>> getRutinas() {
        return rutinaApiService.getRutinas();
    }

    /**
     * Envía la rutina nueva al backend y devuelve la llamada con el detalle de la
     * rutina creada. La ejecuta la pantalla con enqueue.
     */
    public Call<RutinaResponse> crearRutina(RutinaCrearRequest rutinaCrearRequest) {
        return rutinaApiService.crearRutina(rutinaCrearRequest);
    }

    /**
     * Envía la rutina ya existente con los datos que hay en pantalla y devuelve la
     * llamada con el detalle de la rutina actualizada. El cuerpo es el mismo de la
     * creación porque el backend reemplaza los ejercicios y las series por los que
     * se envían.
     */
    public Call<RutinaResponse> actualizarRutina(Long rutinaId, RutinaCrearRequest rutinaCrearRequest) {
        return rutinaApiService.actualizarRutina(rutinaId, rutinaCrearRequest);
    }

    /**
     * Pide al backend eliminar lógicamente la rutina. La respuesta no trae cuerpo, por eso
     * la llamada es de tipo Void.
     */
    public Call<Void> eliminarRutina(Long rutinaId) {
        return rutinaApiService.eliminarRutina(rutinaId);
    }
}
