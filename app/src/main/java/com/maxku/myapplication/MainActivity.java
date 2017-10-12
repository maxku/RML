package com.maxku.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "mysettings";

    SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.icon);
        }

        loadData();

        Button button = (Button) findViewById(R.id.SOSbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveData();
                EditText editNumber = (EditText) findViewById(R.id.number);
                EditText editMessage = (EditText) findViewById(R.id.message);
                String number = editNumber.getText().toString();
                String msg = editMessage.getText().toString();

                if (!number.equalsIgnoreCase("")) {
                    if (!msg.equalsIgnoreCase(""))
                        sendSMS(number, msg);
                    else
                        sendSMS(number, getResources().getString(R.string.SOS_message));
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.MSG_sent), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.ERR_NUM), Toast.LENGTH_SHORT).show();
            }
        });

        Button CONTbutton = (Button) findViewById(R.id.CONT_button);
        CONTbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveData();
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContact, 1);
            }
        });


        final ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditText editNumber = (EditText) findViewById(R.id.number);
                String number = editNumber.getText().toString();

                if (isChecked) {
                    saveData();
                    if (number.equalsIgnoreCase("")) {
                        toggle.setChecked(false);
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.ERR_NUM), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startService(new Intent(MainActivity.this, TimerService.class));
                } else {
                    stopService(new Intent(MainActivity.this, TimerService.class));
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                int phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String num = c.getString(phoneIndex);
                EditText editNumber = (EditText) findViewById(R.id.number);
                if (editNumber.getText().toString().equals(""))
                    editNumber.setText(num);
                else
                    editNumber.setText(editNumber.getText().toString() + ", " + num);
                saveData();
            }
            c.close();
        }
    }

    @Override
    protected void onResume() {
        loadData();
        super.onResume();
    }

    @Override
    protected void onStop() {
        saveData();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                return true;
            case R.id.menu_about:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void saveData() {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        EditText editNumber = (EditText) findViewById(R.id.number);
        EditText editMessage = (EditText) findViewById(R.id.message);
        EditText editDays = (EditText) findViewById(R.id.editDays);
        EditText editHours = (EditText) findViewById(R.id.editHours);
        EditText editMins = (EditText) findViewById(R.id.editMins);
        CheckBox checkSMS = (CheckBox) findViewById(R.id.checkSMS);
        CheckBox checkNot = (CheckBox) findViewById(R.id.checkNot);

        Editor editor = mSettings.edit();
        editor.putString("NUMBER", editNumber.getText().toString());
        editor.putString("MESSAGE", editMessage.getText().toString());
        editor.putString("TIME_DAYS", editDays.getText().toString());
        editor.putString("TIME_HOURS", editHours.getText().toString());
        editor.putString("TIME_MINS", editMins.getText().toString());
        editor.putBoolean("SMS", checkSMS.isChecked());
        editor.putBoolean("NOTIF", checkNot.isChecked());
        editor.apply();
    }

    void loadData() {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        EditText editNumber = (EditText) findViewById(R.id.number);
        EditText editMessage = (EditText) findViewById(R.id.message);
        EditText editDays = (EditText) findViewById(R.id.editDays);
        EditText editHours = (EditText) findViewById(R.id.editHours);
        EditText editMins = (EditText) findViewById(R.id.editMins);
        CheckBox checkSMS = (CheckBox) findViewById(R.id.checkSMS);
        CheckBox checkNot = (CheckBox) findViewById(R.id.checkNot);

        if (mSettings.contains("NUMBER"))
            editNumber.setText(mSettings.getString("NUMBER", ""));
        if (mSettings.contains("MESSAGE"))
            editMessage.setText(mSettings.getString("MESSAGE", ""));
        if (mSettings.contains("TIME_DAYS"))
            editDays.setText(mSettings.getString("TIME_DAYS", "0"));
        if (mSettings.contains("TIME_HOURS"))
            editHours.setText(mSettings.getString("TIME_HOURS", "0"));
        if (mSettings.contains("TIME_MINS"))
            editMins.setText(mSettings.getString("TIME_MINS", "0"));
        if (mSettings.contains("SMS"))
            checkSMS.setChecked(mSettings.getBoolean("SMS", false));
        if (mSettings.contains("NOTIF"))
            checkNot.setChecked(mSettings.getBoolean("NOTIF", false));
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
