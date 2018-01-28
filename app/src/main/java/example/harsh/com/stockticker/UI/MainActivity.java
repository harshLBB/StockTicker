package example.harsh.com.stockticker.UI;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import example.harsh.com.stockticker.R;
import example.harsh.com.stockticker.Receivers.AlarmReceiver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextView currentOrClosing;
    TextView codeTv;
    TextView date;
    TextView high_tv;
    TextView low_tv;
    TextView currOrClos_label;
    String latestTradingDay;
    SharedPreferences prefs;
    private String code;
    private double currentPrice;
    private double closingPrice;
    private double closed;
    private static final String PROGRESS_DIALOG = "ProgressDialog";
    SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
    Date closingTime;
    private Button btn_codeChange;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent i = new Intent(this, SettingsActivity.class);
        currentOrClosing = (TextView) findViewById(R.id.currOrClos);
        codeTv = (TextView) findViewById(R.id.code);
        date = (TextView) findViewById(R.id.date);
        high_tv = (TextView) findViewById(R.id.high);
        low_tv = (TextView) findViewById(R.id.low);
        btn_codeChange = (Button) findViewById(R.id.btn_codeChange);
        currOrClos_label = (TextView) findViewById(R.id.currOrClos_label);
        prefs = getSharedPreferences("stock1", MODE_PRIVATE);

       if( !prefs.contains("firstInstall")) {  //register the alarm service on first install

           setAlarm();
           prefs.edit().putString("firstInstall","0").apply();
       }

        btn_codeChange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(i);
            }
        });


        if(prefs.contains("code")){
            code = prefs.getString("code", "nocode");
        }
        else {
            prefs.edit().putString("code", "GOOG").commit();
            code = "GOOG";  //if no selected code, GOOG is the default value.
        }
    }



    @Override
    protected void onResume(){
        super.onResume();
        code = prefs.getString("code", "GOOG");
        Log.e("onresume", "main");

        if(prefs.contains(code)) {

            //if already we have saved values for the selected code, display them.

            Log.e("code", prefs.getString("code", "nocode"));
            setValues();
        }
        else {
            showProgress();
        }

        //fetch latest values.
            Log.e("apicall", "c");
            Map<String, String> query = new HashMap<>();
            query.put("function", "TIME_SERIES_INTRADAY");
            query.put("symbol",code);
            query.put("interval","1min");
            query.put("outputsize","full");
            query.put("apikey", ApplicationConstants.API_KEY);
            ApiInterface apiService =
                    ApiClient.getClient(getApplicationContext()).create(ApiInterface.class);

            Call<ApiResponse> call = apiService.getStockData(query);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    int statusCode = response.code();
                 //   apiResponse = response.body();
                    Log.e("apiresponseonresp", response.body().getResults().toString());

                    JSONObject Json=null;
                    try {
                        Json = new JSONObject(response.body().getResults().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Iterator keys = Json.keys();
                    Date responseDateTime=null;
                    int latestDay=-1;
                    int f = 0;
                    int f1 = 0;
                    double high=-1;
                    double low=-1;
                    while(keys.hasNext()) {
                        // loop to get the dynamic key
                        String key = (String)keys.next();
                        try {
                            responseDateTime = formatter1.parse(key);
                            Log.e("responseDate", responseDateTime.toString());
                            if(f==0) {
                                latestDay = responseDateTime.getDate();
                                latestTradingDay = responseDateTime.toString();
                                f=1;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                        if(responseDateTime.getDate()==latestDay) {
                            try {
                                JSONObject value = Json.getJSONObject(key);
                                Log.e("responsecount", Integer.toString(f1));
                                if(f1==0){
                                    Log.e("hrs", Integer.toString(responseDateTime.getHours()));
                                    if(responseDateTime.getHours()>=16) {
                                        closingPrice = value.getDouble("4. close");
                                        closed = 1;
                                        Log.e("closed", "closed");
                                    }
                                    else {
                                        closed = 0;
                                        currentPrice = value.getDouble("4. close");
                                    }
                                    low = value.getDouble("3. low");
                                    f1++;
                                }
                                if(value.getDouble("2. high")>high)
                                    high = value.getDouble("2. high");

                                if(value.getDouble("3. low")<low)
                                    low = value.getDouble("3. low");


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    //updating the latest values in memory.

                    HashMap<String,String> map = new HashMap<>();
                    map.put("current", "$ "+Double.toString(currentPrice));
                    map.put("high","$ "+ Double.toString(high));
                    map.put("low", "$ "+Double.toString(low));
                    map.put("closing", "$ "+Double.toString(closingPrice));
                    map.put("closed", Double.toString(closed));
                    map.put("latestTradingDay", latestTradingDay);

                    SharedPreferences prefs = getSharedPreferences("stock1", MODE_PRIVATE);
                    JSONObject jsonObject = new JSONObject(map);
                    String jsonString = jsonObject.toString();
                    SharedPreferences.Editor editor = prefs.edit();
                    if(prefs.contains(code)){
                        prefs.edit().remove(code).apply();
                    }

                    editor.putString(code, jsonString);
                    editor.commit();

                    dismissProgress();
                    setValues();


                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Log error here since request failed
                    Log.e("failure", t.toString());
                    Log.e("failure", "apifail");
                }
            });

    }

    public void setValues(){
        Log.e("setValues", "set");
        code = prefs.getString("code", "no code");
        HashMap<String, Object> map = new HashMap<>();
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("stock1", Context.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString(code, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    Object value = (Object) jsonObject.get(key);
                    map.put(key, value);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        Log.e("MAP", map.toString());

        if(Double.parseDouble(map.get("closed").toString())==1){
            currentOrClosing.setText((map.get("closing")).toString());
            Log.e("setValues", "closed--"+(map.get("closing")).toString());
            currOrClos_label.setText("Closing");
        }
        else{
            currentOrClosing.setText((map.get("current")).toString());
            Log.e("setValues", "notClosed");
        }

        codeTv.setText(code);
        high_tv.setText((map.get("high")).toString());
        low_tv.setText(((map.get("low")).toString()));
        date.setText(map.get("latestTradingDay").toString()); //displaying the latest trading date on top of screen.
    }

    public void setAlarm(){
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent in = new Intent(getApplicationContext(), AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, in, 0);


        Calendar calendar = Calendar.getInstance();
        //  calendar.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
        Log.e("hr", Integer.toString(calendar.getTime().getHours()));
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);  //first alarm at 9:30 am
        calendar.set(Calendar.MINUTE, 30);


        alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                1000 *60 * 60, alarmIntent);   //repeat alarms after every 1 hr
    }

    public void showProgress() {
        ProgressDialogFragment f = ProgressDialogFragment.getInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(f, PROGRESS_DIALOG)
                .commitAllowingStateLoss();
    }

    public void dismissProgress() {
        ProgressDialogFragment f = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG);
        if (f != null) {
            getSupportFragmentManager().beginTransaction().remove(f).commitAllowingStateLoss();
        }


    }

}
