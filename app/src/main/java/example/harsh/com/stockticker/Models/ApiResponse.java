package example.harsh.com.stockticker.Models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Harsh on 27-01-2018.
 */

public class ApiResponse {

    @SerializedName("Time Series (1min)")
    private JsonObject results;

    public JsonObject getResults(){
        return results;
    }
}
