package example.harsh.com.stockticker.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import example.harsh.com.stockticker.API.ApiClient;
import example.harsh.com.stockticker.API.ApiInterface;
import example.harsh.com.stockticker.ApplicationConstants;
import example.harsh.com.stockticker.Models.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Harsh on 28-01-2018.
 */

public class AlarmReceiver extends BroadcastReceiver
{
    SharedPreferences prefs;
    private String code;
    SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
    private double currentPrice;
    private double closingPrice;
    private double closed;
    private  String latestTradingDay;



    @Override
    public void onReceive(final Context context, Intent intent)
    {
        Calendar calendar = Calendar.getInstance();
       // calendar.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
        Log.e("hr",Integer.toString(calendar.getTime().getHours()));
        if(calendar.getTime().getHours()<16&&calendar.getTime().getHours()>9) {
            // the stock market closes at 16 hrs and opens at 9:30. Therefore, a check to avoid api calls outside this duration.


            Log.e("alarm", "recieve");
            prefs = context.getSharedPreferences("stock1", MODE_PRIVATE);
            code = prefs.getString("code", "GOOG"); //default code GOOG.
            if (!prefs.contains(code)) {
                Log.e("code", prefs.getString("code", "nocode"));

                prefs.edit().putString("code", "GOOG").apply();

            }

            Log.e("apicall", "alarm");
            Map<String, String> query = new HashMap<>();
            query.put("function", "TIME_SERIES_INTRADAY");
            query.put("symbol", code);
            query.put("interval", "1min");
            query.put("outputsize", "full");
            query.put("apikey", ApplicationConstants.API_KEY);
            ApiInterface apiService =
                    ApiClient.getClient(context).create(ApiInterface.class);

            Call<ApiResponse> call = apiService.getStockData(query);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    int statusCode = response.code();
                    Log.e("apiresponseonresp", response.body().getResults().toString());

                    JSONObject Json = null;
                    try {
                        Json = new JSONObject(response.body().getResults().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Iterator keys = Json.keys();
                    Date responseDateTime = null;
                    int latestDay = -1;
                    int f = 0;
                    int f1 = 0;
                    double high = -1;
                    double low = -1;
                    while (keys.hasNext()) {
                        // loop to get the dynamic key
                        String key = (String) keys.next();
                        try {
                            responseDateTime = formatter1.parse(key);
                            Log.e("responseDate", responseDateTime.toString());
                            if (f == 0) {
                                latestDay = responseDateTime.getDate();
                                latestTradingDay = responseDateTime.toString();
                                f = 1;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (responseDateTime.getDate() == latestDay) {
                            try {
                                JSONObject value = Json.getJSONObject(key);


                                Log.e("responsecount", Integer.toString(f1));
                                if (f1 == 0) {
                                    Log.e("hrs", Integer.toString(responseDateTime.getHours()));
                                    if (responseDateTime.getHours() >= 16) {
                                        closingPrice = value.getDouble("4. close");
                                        closed = 1;
                                        Log.e("closed", "closed");
                                    } else {
                                        closed = 0;
                                        currentPrice = value.getDouble("4. close");
                                    }
                                    low = value.getDouble("3. low");
                                    f1++;
                                }
                                if (value.getDouble("2. high") > high)
                                    high = value.getDouble("2. high");

                                if (value.getDouble("3. low") < low)
                                    low = value.getDouble("3. low");


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    //updating the latest values in memory.
                    HashMap<String, String> map = new HashMap<>();
                    map.put("current", "$ "+Double.toString(currentPrice));
                    map.put("high", "$ "+Double.toString(high));
                    map.put("low", "$ "+Double.toString(low));
                    map.put("closing", "$ "+Double.toString(closingPrice));
                    map.put("closed", Double.toString(closed));
                    map.put("latestTradingDay", latestTradingDay);


                    SharedPreferences prefs = context.getSharedPreferences("stock1", MODE_PRIVATE);
                    JSONObject jsonObject = new JSONObject(map);
                    String jsonString = jsonObject.toString();
                    SharedPreferences.Editor editor = prefs.edit();
                    if (prefs.contains(code)) {
                        prefs.edit().remove(code).apply();
                    }

                    editor.putString(code, jsonString);
                    editor.commit();


                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {

                    Log.e("failure", t.toString());
                    Log.e("failure", "apifail");
                }
            });
        }

    }
}

