package com.rbliving.pill;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

class PackDAO {

    private SQLiteDatabase dbWritable, dbReadable;

    private Context c;

    PackDAO(Context context) {
        c = context;
        PackDBHelper dbHelper = new PackDBHelper(context);
        dbWritable = dbHelper.getWritableDatabase();
        dbReadable = dbHelper.getReadableDatabase();
    }

    int write(PackBean pack){
        int packID = getPackID(pack.getName());
        if (packID != -1){
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(PackDBHelper.PackTable.COL_PACK_NAME,pack.getName());
        values.put(PackDBHelper.PackTable.COL_START_DATE,pack.getStartDate());
        dbWritable.insert(PackDBHelper.PackTable.TABLE_NAME,null,values);

        packID = getPackID(pack.getName());

        for (int i = 0; i < 28; i++){

            values = new ContentValues();
            values.put(PackDBHelper.PillTable.COL_PACK_ID,packID);
            values.put(PackDBHelper.PillTable.COL_PILL_POSITION,i);
            if (pack.getChecked(i))
                values.put(PackDBHelper.PillTable.COL_CHECKED,1);
            else
                values.put(PackDBHelper.PillTable.COL_CHECKED,0);
            values.put(PackDBHelper.PillTable.COL_TIME,pack.getPillTakenTime(i));

            dbWritable.insert(PackDBHelper.PillTable.TABLE_NAME,null,values);
        }
        return 1;
    }

    void update(int i, PackBean pack){
        //TODO implement single line update
    }

    void update(PackBean pack){
        int packID = getPackID(pack.getName());
        if (packID == -1)
            return; //TODO create if name doesn't exist

        for (int i = 0; i < 28; i++){
            String selection = PackDBHelper.PillTable.COL_PACK_ID + " LIKE ? AND " +
                    PackDBHelper.PillTable.COL_PILL_POSITION + " = ?";
            String[] selectionArgs = { packID+"", i+""};

            ContentValues values = new ContentValues();
            if (pack.getChecked(i))
                values.put(PackDBHelper.PillTable.COL_CHECKED, 1);
            else
                values.put(PackDBHelper.PillTable.COL_CHECKED, 0);

            values.put(PackDBHelper.PillTable.COL_TIME, pack.getPillTakenTime(i));

            dbWritable.update(PackDBHelper.PillTable.TABLE_NAME,values,selection,selectionArgs);
        }
    }

    PackBean getPack(String name){
        int packID = getPackID(name);



        //get pack info
        String[] projection = {PackDBHelper.PackTable.COL_START_DATE};
        String selection = PackDBHelper.PackTable.COL_ID + " LIKE ?";
        String[] selectionArgs = {packID+""};
        Cursor cursor = dbReadable.query(PackDBHelper.PackTable.TABLE_NAME, projection,selection,selectionArgs,null,null,null);

        cursor.moveToNext();
        Long startDate = cursor.getLong(cursor.getColumnIndexOrThrow(PackDBHelper.PackTable.COL_START_DATE));
        PackBean pack = new PackBean(name, startDate);
        cursor.close();

        //get pills info
        projection = new String[]{PackDBHelper.PillTable.COL_PILL_POSITION, PackDBHelper.PillTable.COL_CHECKED, PackDBHelper.PillTable.COL_TIME};
        selection = PackDBHelper.PillTable.COL_PACK_ID + " LIKE ?";
        cursor = dbReadable.query(PackDBHelper.PillTable.TABLE_NAME, projection, selection, selectionArgs, null, null, null);


        while(cursor.moveToNext()){
            int i = cursor.getInt(cursor.getColumnIndexOrThrow(PackDBHelper.PillTable.COL_PILL_POSITION));
            if (cursor.getInt(cursor.getColumnIndexOrThrow(PackDBHelper.PillTable.COL_CHECKED)) == 0)
                pack.setChecked(i,false);
            else
                pack.setChecked(i, true);
            pack.setPillTakenTime(i,cursor.getLong(cursor.getColumnIndexOrThrow(PackDBHelper.PillTable.COL_TIME)));
        }
        cursor.close();

        return pack;
    }

    ArrayList<String> getNames(){
        String[] projection = {PackDBHelper.PackTable.COL_PACK_NAME};

        Cursor cursor = dbReadable.query(PackDBHelper.PackTable.TABLE_NAME, projection, null,null,null,null,null);

        ArrayList<String> list = new ArrayList<>();
        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow(PackDBHelper.PackTable.COL_PACK_NAME));
            list.add(name);
        }
        cursor.close();

        return list;
    }

    private int getPackID(String name){
        String[] projection = {PackDBHelper.PackTable.COL_ID};

        String selection = PackDBHelper.PackTable.COL_PACK_NAME + " LIKE ?";
        String[] selectionArgs = { name };
        Cursor cursor = dbReadable.query(PackDBHelper.PackTable.TABLE_NAME, projection, selection, selectionArgs,null, null, null);

        int id = -1;
        if (cursor.moveToNext())
            id = cursor.getInt(cursor.getColumnIndexOrThrow(PackDBHelper.PackTable.COL_ID));
        cursor.close();

        return id;
    }

    void delete(String name){
        int packID = getPackID(name);
        String selection = PackDBHelper.PackTable.COL_ID + " LIKE ?";
        String[] selectionArgs = { packID+"" };
        dbWritable.delete(PackDBHelper.PackTable.TABLE_NAME, selection, selectionArgs);
        selection = PackDBHelper.PillTable.COL_PACK_ID + " LIKE ?";
        dbWritable.delete(PackDBHelper.PillTable.TABLE_NAME, selection, selectionArgs);
    }
}
