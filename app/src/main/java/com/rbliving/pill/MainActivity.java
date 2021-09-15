package com.rbliving.pill;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Map<Integer, ToggleButton> buttons = new HashMap<>();
    private Spinner spinner;
    private PackDAO dao;
    private SharedPreferences prefs;
    private PackBean loadedPack;
    private TextView[] weekTextViews = new TextView[7];
    private EditText configPackName, configPackTime, notificationTime;
    private CardView configCardView;
    private SwitchCompat notificationSwitch;

    private OnBackPressedCallback callback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.title_packs));
        setContentView(R.layout.activity_main);

        createNotificationChannel();


        dao = new PackDAO(this);
        prefs = this.getSharedPreferences("prefs", MODE_PRIVATE);

        LayoutInflater inflater = getLayoutInflater();

        configPackName = findViewById(R.id.id_config_pack_name);
        configPackTime = findViewById(R.id.id_config_pack_time);
        configPackTime.setOnClickListener(this);

        notificationTime = findViewById(R.id.id_notification_time);
        notificationTime.setOnClickListener(this);

        notificationSwitch = findViewById(R.id.id_notification_switch);
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAlarm();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(getResources().getString(R.string.saved_notification_state),notificationSwitch.isChecked());
                editor.apply();
            }
        });

        findViewById(R.id.id_btn_confirm_new_pack).setOnClickListener(this);

        initNotification();

        //set new pack button back callback
        configCardView = findViewById(R.id.id_card_view_settings);

        callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                Util.collapse(configCardView);
                this.setEnabled(false);
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);

        //create week text views
        LinearLayout weekLayout = findViewById(R.id.id_linear_layout_week);
        for (int i = 0; i < 7; i++){
            TextView txt = (TextView)inflater.inflate(R.layout.week_textview, weekLayout, false);
            weekTextViews[i] = txt;
            weekLayout.addView(txt);
        }

        //create pill buttons
        LinearLayout verticalLinearLayout = findViewById(R.id.id_linear_layout_pills);
        int toggleButtonXML = R.layout.pill_button;

        for (int i = 0; i < 4; i++){
            LinearLayout horizontalLinearLayout = new LinearLayout(this);
            horizontalLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            verticalLinearLayout.addView(horizontalLinearLayout);
            for (int j = 0; j < 7; j++){
                if(buttons.size() == 24){
                    toggleButtonXML = R.layout.pill_button_alternate;
                }
                ToggleButton btn = (ToggleButton)inflater.inflate(toggleButtonXML, horizontalLinearLayout, false);
                btn.setId(View.generateViewId());
                btn.setOnClickListener(this);


                buttons.put((i*7+j),btn);
                horizontalLinearLayout.addView(btn);
            }
        }

        //set spinner
        spinner = findViewById(R.id.id_spinner_packs);
        ArrayList<String> packsList = dao.getNames();

        Calendar cal = Calendar.getInstance();
        String name = String.format(Locale.ENGLISH,"%s - %02d/%02d",getResources().getString(R.string.default_new_pack_name),cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH)+1);

        if (packsList.size() == 0) {
            createNewPack(name, cal.getTimeInMillis());
        }
        else
            updateSpinner(prefs.getString(getResources().getString(R.string.saved_position),""));

        if (spinner.getCount() <= 1){
            Button btn = findViewById(R.id.id_btn_remove);
            btn.setEnabled(false);
        }

        spinner.setOnItemSelectedListener(this);


    }

    private void loadPack(PackBean pack){
        loadedPack = pack;
        for (int i = 0; i < 28; i++){
            buttons.get(i).setTextOn(pack.getTextOn(i));
            buttons.get(i).setTextOff(pack.getTextOff(i));
            buttons.get(i).setChecked(pack.getChecked(i));
            updateTextColor(buttons.get(i));
        }
    }

    private void updateTextColor(ToggleButton btn){

        if (!btn.isChecked()) {
            btn.setTextColor(getResources().getColor(R.color.black));
            return;
        }

        int id = btn.getId();
        for (int i = 0; i < 28; i++) {
            ToggleButton tg_btn = buttons.get(i);
            if (tg_btn.getId() == id) {
                int expectedDay = Util.getDayOfYear(loadedPack.getPillExpectedTime(i));
                int takenDay = Util.getDayOfYear(loadedPack.getPillTakenTime(i));
                if (expectedDay == takenDay){
                    btn.setTextColor(getResources().getColor(R.color.colorPrimary700));
                }
                else
                    btn.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();


        //notificationTime
        if (id == R.id.id_notification_time){
            Log.d("timePicker","timePicker");
            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                    String time = String.format(Locale.getDefault(),"%02d:%02d",hourOfDay,minutes);
                    notificationTime.setText(time);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(getResources().getString(R.string.saved_notification_time),time);
                    editor.apply();
                    setAlarm();
                }
            }, 0, 0, true);


            timePickerDialog.show();
        }

        //confirmNewPack
        if(id == R.id.id_btn_confirm_new_pack) {
            if ((configPackName.getText()+"").equals("")){
                Toast.makeText(this, getResources().getString(R.string.errmsg_blank_name), Toast.LENGTH_SHORT).show();
                return;
            }
            if ((configPackTime.getText()+"").equals("")){
                Toast.makeText(this, getResources().getString(R.string.errmsg_blank_date), Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("write new pack","button pressed");
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            try {
                cal.setTime(sdf.parse(""+configPackTime.getText()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String name = String.format(Locale.ENGLISH,"%s - %02d/%02d",configPackName.getText(),cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH)+1);

            if(createNewPack(name,cal.getTimeInMillis())==-1){//errorLevel for existing name
                Toast.makeText(this, getResources().getString(R.string.errmsg_duplicate_pack), Toast.LENGTH_SHORT).show();
                return;
            }
            cancelNewPack();
        }

        //configPackTime onClick
        if(id == R.id.id_config_pack_time){
            Util.closeKeyboard(view);
            Calendar mcurrentDate = Calendar.getInstance();
            int mYear=mcurrentDate.get(Calendar.YEAR);
            int mMonth=mcurrentDate.get(Calendar.MONTH);
            int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog mDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                    configPackTime.setText(String.format(Locale.getDefault(),"%02d/%02d/%04d",selectedday,selectedmonth+1,selectedyear));
                }
            },mYear, mMonth, mDay);
            mDatePicker.show();
        }



        //pills
        for (int i = 0; i < 28; i++){
            ToggleButton btn = buttons.get(i);
            if (id == btn.getId()) {

                loadedPack.setChecked(i, btn.isChecked());
                Calendar cal = Calendar.getInstance();
                loadedPack.setPillTakenTime(i, cal.getTimeInMillis());

                btn.setTextOn(loadedPack.getTextOn(i));
                updateTextColor(btn);
                btn.setChecked(loadedPack.getChecked(i));

                dao.update(loadedPack);

                cancelNotification(100);

                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getResources().getString(R.string.saved_position),(String)spinner.getSelectedItem());
        editor.apply();
        PackBean pack = dao.getPack((String)spinner.getSelectedItem());
        loadPack(pack);
        updateWeekTextViews();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void createNewPack(){
        PackBean newPack = new PackBean();
        dao.write(newPack);
        updateSpinner(newPack.getName());
        if (spinner.getCount() > 1){
            Button btn = findViewById(R.id.id_btn_remove);
            btn.setEnabled(true);
        }
    }

    private int createNewPack(String name, long startDateMilli){
        PackBean newPack = new PackBean(name, startDateMilli);
        int errorLevel = dao.write(newPack);
        updateSpinner(newPack.getName());
        if (spinner.getCount() > 1){
            Button btn = findViewById(R.id.id_btn_remove);
            btn.setEnabled(true);
        }
        return errorLevel;
    }

    private void updateSpinner(String name){
        ArrayList<String> newList = dao.getNames();
        Collections.reverse(newList);
        int newSelection = newList.indexOf(name);
        if (newSelection == -1)
            newSelection = 0;

        ArrayAdapter<String> newAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, newList);
        spinner.setAdapter(newAdapter);
        spinner.setSelection(newSelection);
    }

    public void createNewPack(View v){
        if(configCardView.getVisibility() == View.GONE){
            Util.expand(configCardView);
            callback.setEnabled(true);
            Util.openKeyboard(configPackName);
        }
        else {
            cancelNewPack();
        }

    }

    public void cancelNewPack(View v){
        cancelNewPack();
    }

    private void cancelNewPack(){
        Util.collapse(configCardView);
        callback.setEnabled(false);
        Util.closeKeyboard(configPackName);
    }

    public void removePack(final View v) {

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.btn_remove))
                .setMessage(getResources().getString(R.string.delete_message) + "\n\n\t\t" + spinner.getSelectedItem().toString())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dao.delete((String) spinner.getSelectedItem());
                        int pos = spinner.getSelectedItemPosition();
                        updateSpinner((String) spinner.getItemAtPosition(pos + ((spinner.getCount() > pos + 1) ? 1 : -1)));
                        if (spinner.getCount() <= 1) {
                            v.setEnabled(false);
                        }
                    }})
                .setNegativeButton(android.R.string.no, null).show();

    }

    public void updateWeekTextViews(){
        for (int i = 0; i < 7; i++){
            String str = "err";
            switch (loadedPack.getWeekDay(i)){
                case 1:
                    weekTextViews[i].setTextColor(getResources().getColor(R.color.colorAccent));
                    str = getResources().getString(R.string.sunday_short);
                    break;
                case 2:
                    weekTextViews[i].setTextColor(getResources().getColor(R.color.colorPrimary700));
                    str = getResources().getString(R.string.monday_short);
                    break;
                case 3:
                    weekTextViews[i].setTextColor(getResources().getColor(R.color.colorPrimary700));
                    str = getResources().getString(R.string.tuesday_short);
                    break;
                case 4:
                    weekTextViews[i].setTextColor(getResources().getColor(R.color.colorPrimary700));
                    str = getResources().getString(R.string.wednesday_short);
                    break;
                case 5:
                    weekTextViews[i].setTextColor(getResources().getColor(R.color.colorPrimary700));
                    str = getResources().getString(R.string.thursday_short);
                    break;
                case 6:
                    weekTextViews[i].setTextColor(getResources().getColor(R.color.colorPrimary700));
                    str = getResources().getString(R.string.friday_short);
                    break;
                case 7:
                    weekTextViews[i].setTextColor(getResources().getColor(R.color.colorAccent));
                    str = getResources().getString(R.string.saturday_short);
                    break;
            }
            weekTextViews[i].setText(str);
        }
    }

    private void setAlarm(){
        if (notificationSwitch.isChecked())
            activateAlarm();
        else
            cancelAlarm();
    }

    private void activateAlarm(){
        Log.d("setAlarm","setAlarm");
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        String[] time = (notificationTime.getText()+"").split(":");
        cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(time[0]));
        cal.set(Calendar.MINUTE,Integer.parseInt(time[1]));

        if (now > cal.getTimeInMillis())
            cal.add(Calendar.DAY_OF_MONTH,1);


        alarmMgr.cancel(alarmIntent);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);


    }

    private void cancelAlarm(){
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.cancel(alarmIntent);
    }

    private void initNotification(){
        notificationTime.setText(prefs.getString(getResources().getString(R.string.saved_notification_time),""));
        if ((notificationTime.getText()+"").equals("")){
            Calendar cal = Calendar.getInstance();
            String time = String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            notificationTime.setText(time);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getResources().getString(R.string.saved_notification_time),time);
            editor.apply();
        }
        notificationSwitch.setChecked(prefs.getBoolean(getResources().getString(R.string.saved_notification_state),false));
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void cancelNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}
