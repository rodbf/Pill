package com.rbliving.pill;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("reboot","onReceive");

        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        boolean alarmState = prefs.getBoolean(context.getResources().getString(R.string.saved_notification_state),false);
        if(alarmState) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent newIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 100, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar cal = Calendar.getInstance();
            long now = cal.getTimeInMillis();
            String notificationTime = prefs.getString(context.getResources().getString(R.string.saved_notification_time), "0:0");
            Log.d("reboot", notificationTime);
            String[] time = notificationTime.split(":");
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));

            if (now > cal.getTimeInMillis())
                cal.add(Calendar.DAY_OF_MONTH, 1);

            alarmMgr.cancel(alarmIntent);
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }
}
