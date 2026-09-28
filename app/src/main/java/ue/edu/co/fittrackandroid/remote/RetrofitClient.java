package ue.edu.co.fittrackandroid.remote;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static final String BASE_URL = "https://fittrack-backend-kjyc.onrender.com/api/";
    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager =
                    new TokenManager(context);

            AutenticacionInterceptor autenticacionInterceptor =
                    new AutenticacionInterceptor(tokenManager);

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
