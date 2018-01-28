package example.harsh.com.stockticker.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Harsh on 28-01-2018.
 */


//By default, all alarms are canceled when a device shuts down.
// To prevent this from happening, you can design your application to automatically restart a repeating alarm if the user reboots the device.


public class BootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            AlarmManager alarmMgr;
            PendingIntent alarmIntent;

            alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, i, 0);


            Calendar calendar = Calendar.getInstance();
          //  calendar.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
            Log.e("hr",Integer.toString(calendar.getTime().getHours()));
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 9); //first alarm at 9:30 am
            calendar.set(Calendar.MINUTE, 30);


            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000*60*60  , alarmIntent); //repeat
        }
        }
    }

