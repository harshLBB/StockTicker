package example.harsh.com.stockticker.API;

import java.util.Map;

import example.harsh.com.stockticker.Models.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by Harsh on 27-01-2018.
 */

public interface ApiInterface {

    @GET("query")
    Call<ApiResponse> getStockData(@QueryMap Map<String, String> options);
}
