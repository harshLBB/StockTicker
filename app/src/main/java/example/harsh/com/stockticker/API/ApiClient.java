package example.harsh.com.stockticker.API;

/**
 * Created by Harsh on 27-01-2018.
 */

import android.content.Context;

import com.readystatesoftware.chuck.ChuckInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    public static final String BASE_URL = "https://www.alphavantage.co/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit==null) {
            OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(new ChuckInterceptor(context)).build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient)
                    .build();

        }
        return retrofit;
    }
}

