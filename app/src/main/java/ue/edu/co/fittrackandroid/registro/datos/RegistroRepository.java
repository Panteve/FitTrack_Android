package ue.edu.co.fittrackandroid.registro.datos;

import android.content.Context;

import ue.edu.co.fittrackandroid.login.datos.LoginApiService;
import ue.edu.co.fittrackandroid.registro.modelo.RegistroRequest;
import ue.edu.co.fittrackandroid.remote.RetrofitClient;

public class RegistroRepository {

    private final RegistroApiService registroApiService;

    public RegistroRepository(Context context) {
        this.registroApiService = RetrofitClient.getInstance(context).create(RegistroApiService.class);;
    }

    public void registrarUsuario(RegistroRequest registroRequest) {
        registroApiService.registerUser(registroRequest);
    }
}
