package ue.edu.co.fittrackandroid.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {

    private static final String BASE_URL = "https://fittrack-backend-kjyc.onrender.com/api/";
    private static Retrofit retrofit;

    private RetrofitClient() {
    }


    public static Retrofit getInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptorRegistro = new HttpLoggingInterceptor();
            interceptorRegistro.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient clienteHttp = new OkHttpClient.Builder()
                    .addInterceptor(interceptorRegistro)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(clienteHttp)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
