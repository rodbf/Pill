package com.rbliving.pill;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class PackBean {

    private String name;
    private long startDate;

    private boolean[] isChecked;
    private long[] pillTakenTime, pillExpectedTime;

    PackBean(){
        SimpleDateFormat timeFormat = new SimpleDateFormat("EEEE dd/MM HH:mm:ss", new Locale("PT", "BR"));
        init(timeFormat.format(new Date()), Calendar.getInstance().getTimeInMillis());
    }

    PackBean(long startDate){
        SimpleDateFormat timeFormat = new SimpleDateFormat("EEEE dd/MM HH:mm:ss", new Locale("PT", "BR"));
        init(timeFormat.format(new Date()), startDate);
    }

    PackBean(String name){
        init(name, Calendar.getInstance().getTimeInMillis());
    }

    PackBean(String name, Long startDate){
        init(name, startDate);
    }

    private void init(String name, long startDate){
        isChecked = new boolean[28];

        pillTakenTime = new long[28];
        pillExpectedTime = new long[28];

        this.startDate = startDate;

        for (int i = 0; i < 28; i++){
            isChecked[i] = false;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startDate);
            cal.add(Calendar.DAY_OF_MONTH, i);
            pillExpectedTime[i] = cal.getTimeInMillis();
        }

        this.name = name;
    }

    int getWeekDay(int i){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startDate);
        cal.add(Calendar.DAY_OF_MONTH,i);
        return cal.get(Calendar.DAY_OF_WEEK);
    }



    String getTextOn(int i){
        Calendar calTaken = Calendar.getInstance();
        calTaken.setTimeInMillis(pillTakenTime[i]);
        Calendar calExpected = Calendar.getInstance();
        calExpected.setTimeInMillis(pillExpectedTime[i]);
        return String.format(Locale.getDefault(),"%dh%02d\n%02d/%02d",
                calTaken.get(Calendar.HOUR_OF_DAY),
                calTaken.get(Calendar.MINUTE),
                calExpected.get(Calendar.DAY_OF_MONTH),
                calExpected.get(Calendar.MONTH)+1);
    }

    String getTextOff(int i){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(pillExpectedTime[i]);
        return (i+1)+"\n"+String.format(Locale.getDefault(),"%02d/%02d",cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH)+1);
    }

    String getName() {
        return name;
    }
    void setName(String name) {
        this.name = name;
    }

    long getPillTakenTime(int i){
        return pillTakenTime[i];
    }
    void setPillTakenTime(int i, long milliseconds){
        pillTakenTime[i] = milliseconds;
    }

    long getStartDate() {
        return startDate;
    }
    void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    boolean getChecked(int i){
        return isChecked[i];
    }
    void setChecked(int i, boolean state){
        isChecked[i] = state;
    }

    public long getPillExpectedTime(int i) {
        return pillExpectedTime[i];
    }

    @Override
    public String toString() {
        return name;
    }
}
