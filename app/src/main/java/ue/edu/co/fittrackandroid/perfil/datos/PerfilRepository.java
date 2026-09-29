package ue.edu.co.fittrackandroid.perfil.datos;

import android.content.Context;

import retrofit2.Call;
import ue.edu.co.fittrackandroid.perfil.modelo.CambiarNombreRequest;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

/**
 * Centraliza las llamadas de los datos personales del Perfil.
 */
public class PerfilRepository {

    private final PerfilApiService perfilApiService;

    public PerfilRepository(Context context) {
        perfilApiService = RetrofitClient.getInstance(context)
                .create(PerfilApiService.class);
    }

    /**
     * Prepara el envío del nuevo nombre; el Fragmento ejecuta la llamada.
     * La respuesta no trae cuerpo, por eso la llamada es de tipo Void.
     *
     * @param cambiarNombreRequest nombre nuevo ya validado.
     * @return llamada que termina con un 204 cuando el nombre quedó actualizado.
     */
    public Call<Void> cambiarNombre(CambiarNombreRequest cambiarNombreRequest) {
        return perfilApiService.cambiarNombre(cambiarNombreRequest);
    }
}
