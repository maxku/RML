package com.maxku.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;



public class AlarmReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public static final String APP_PREFERENCES = "mysettings";
    SharedPreferences mSettings;

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle extras = intent.getExtras();
        String number = extras.getString("NUMBER");
        String msg = extras.getString("MESSAGE");
        String timeD = extras.getString("TIME_DAYS");
        if (timeD.equalsIgnoreCase("")) timeD = "0";
        String timeH = extras.getString("TIME_HOURS");
        if (timeH.equalsIgnoreCase("")) timeH = "0";
        String timeM = extras.getString("TIME_MINS");
        if (timeM.equalsIgnoreCase("")) timeM = "0";
        Boolean checkSMS = extras.getBoolean("SMS");
        Boolean checkNot = extras.getBoolean("NOTIF");
        if (checkNot)
            Toast.makeText(context, "User's device not moving for "+timeD+"d"+timeH+"h"+timeM+"m.",
                    Toast.LENGTH_LONG).show();
        if (checkSMS && !number.equalsIgnoreCase("")) {
            if (!msg.equalsIgnoreCase(""))
                sendSMS(number, msg);
            else
                sendSMS(number, context.getResources().getString(R.string.N_message)+timeD+"d "+timeH+"h "+timeM+"m.");
        }
        RestartAlarm(context);
    }

    public void RestartAlarm(Context context) {
        CancelAlarm(context);

        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        if(mSettings.contains("NUMBER"))
            intent.putExtra("NUMBER", mSettings.getString("NUMBER", ""));

        if(mSettings.contains("MESSAGE"))
            intent.putExtra("MESSAGE", mSettings.getString("MESSAGE", ""));

        if(mSettings.contains("TIME_DAYS"))
            intent.putExtra("TIME_DAYS", mSettings.getString("TIME_DAYS", "0"));

        if(mSettings.contains("TIME_HOURS"))
            intent.putExtra("TIME_HOURS", mSettings.getString("TIME_HOURS", "0"));

        if(mSettings.contains("TIME_MINS"))
            intent.putExtra("TIME_MINS", mSettings.getString("TIME_MINS", "0"));

        if(mSettings.contains("SMS"))
            intent.putExtra("SMS", mSettings.getBoolean("SMS", false));

        if(mSettings.contains("NOTIF"))
            intent.putExtra("NOTIF", mSettings.getBoolean("NOTIF", false));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT );
        am.cancel(pendingIntent);

        int time = 1;
        if(mSettings.contains("TIME_MINS") && !mSettings.getString("TIME_MINS", "0").equalsIgnoreCase(""))
            time = Integer.parseInt(mSettings.getString("TIME_MINS", "0"));
        if(mSettings.contains("TIME_HOURS") && !mSettings.getString("TIME_HOURS", "0").equalsIgnoreCase(""))
            time += Integer.parseInt(mSettings.getString("TIME_HOURS", "0")) * 60;
        if(mSettings.contains("TIME_DAYS") && !mSettings.getString("TIME_DAYS", "0").equalsIgnoreCase(""))
            time += Integer.parseInt(mSettings.getString("TIME_DAYS", "0")) * 24 * 60;
        if (time < 1) time = 1;
        if (time > 1296000) time = 1296000;

        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + time * 60 * 1000, pendingIntent);

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yy");
        String dateString = formatter.format(new Date(System.currentTimeMillis() + time * 60 * 1000));
        if (mSettings.contains("NOTIF") && mSettings.getBoolean("NOTIF", false))
            Toast.makeText(context, context.getResources().getString(R.string.MSG_alarm) + dateString, Toast.LENGTH_LONG).show();
    }
    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        if (!phoneNumber.equalsIgnoreCase("")) {
            String num = "";
            int i = 0;
            while (i < phoneNumber.length()) {
                while (i < phoneNumber.length() && phoneNumber.charAt(i) != ' '
                        && phoneNumber.charAt(i) != ',' && phoneNumber.charAt(i) != ';') {
                    num += phoneNumber.charAt(i);
                    i++;
                }
                if (!num.equals("")){
                    String res = "";
                    if (num.charAt(0) == '+')
                        res += '+';
                    res += num.replaceAll("[\\D]", "");
                    num = "";
                    if (!res.equals(""))
                        sms.sendTextMessage(res, null, message, null, null);
                }
                i++;
            }
        }
    }

}