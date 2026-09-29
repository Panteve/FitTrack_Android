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

    public RutinaRepository(Context context) {
        this.rutinaApiService = RetrofitClient.getInstance(context).create(RutinaApiService.class);
    }

    public Call<RutinaResponse> getRutinaById(Long id) {
        return rutinaApiService.getRutinaById(id);
    }

    public Call<List<RutinasResponse>> getRutinas() {
        return rutinaApiService.getRutinas();
    }

    /**
     * Envía la rutina nueva al backend.
     * El repositorio solo devuelve la llamada, la ejecuta el Fragment con enqueue.
     *
     * @param rutinaCrearRequest datos de la rutina a registrar.
     * @return llamada con el detalle de la rutina creada.
     */
    public Call<RutinaResponse> crearRutina(RutinaCrearRequest rutinaCrearRequest) {
        return rutinaApiService.crearRutina(rutinaCrearRequest);
    }

    /**
     * Envía la rutina ya existente con los datos que hay en pantalla.
     * El cuerpo es el mismo de la creación porque el backend reemplaza los ejercicios
     * y las series por los que se envían.
     *
     * @param rutinaId identificador de la rutina que se está modificando.
     * @param rutinaCrearRequest datos de la rutina con los cambios.
     * @return llamada con el detalle de la rutina actualizada.
     */
    public Call<RutinaResponse> actualizarRutina(Long rutinaId, RutinaCrearRequest rutinaCrearRequest) {
        return rutinaApiService.actualizarRutina(rutinaId, rutinaCrearRequest);
    }

    /**
     * Pide al backend eliminar lógicamente la rutina. La respuesta no trae cuerpo, por eso
     * la llamada es de tipo Void.
     *
     * @param rutinaId identificador de la rutina que se quiere borrar.
     * @return llamada que termina con un 204 cuando la rutina quedó borrada.
     */
    public Call<Void> eliminarRutina(Long rutinaId) {
        return rutinaApiService.eliminarRutina(rutinaId);
    }
}
