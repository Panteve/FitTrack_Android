package ue.edu.co.fittrackandroid.remote;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static final String BASE_URL = "https://fittrack-backend-kjyc.onrender.com/api/";
    private static Retrofit retrofit;

    /** Impide crear instancias de esta clase, que expone una única instancia compartida de Retrofit. */
    private RetrofitClient() {
    }

    /** Crea la primera vez que se llama el cliente de Retrofit con el interceptor de autenticación y el registro de peticiones, y devuelve esa misma instancia en las siguientes llamadas. */
    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            SesionManager sesionManager =
                    new SesionManager(context);

            AutenticacionInterceptor autenticacionInterceptor =
                    new AutenticacionInterceptor(sesionManager);

            HttpLoggingInterceptor interceptorRegistro =
                    new HttpLoggingInterceptor();

            interceptorRegistro.setLevel(
                    HttpLoggingInterceptor.Level.BASIC
            );

            interceptorRegistro.redactHeader("Authorization");

            OkHttpClient clienteHttp = new OkHttpClient.Builder()
                    .addInterceptor(autenticacionInterceptor)
                    .addInterceptor(interceptorRegistro)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(clienteHttp)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        }

        return retrofit;
    }
}
