package com.rbliving.pill;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PackDBHelper extends SQLiteOpenHelper {

    //db
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PackKeeper.db";

    //pill table
    static class PillTable {
        static final String TABLE_NAME = "t_pills";
        static final String COL_ID = "_id";
        static final String COL_PACK_ID = "pack_ref_id";
        static final String COL_PILL_POSITION = "position";
        static final String COL_CHECKED = "is_checked";
        static final String COL_TIME = "time";
    }

    //packs table
    static class PackTable {
        static final String TABLE_NAME = "t_packs";
        static final String COL_ID = "_id";
        static final String COL_PACK_NAME = "name";
        static final String COL_START_DATE = "start_date";
    }

    PackDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PILL_TABLE);
        db.execSQL(SQL_CREATE_PACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PILL_TABLE);
        db.execSQL(SQL_DELETE_PACK_TABLE);
        onCreate(db);
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String NUMBER_TYPE = " INTEGER";

    private static final String SQL_CREATE_PILL_TABLE = "CREATE TABLE " +
            PillTable.TABLE_NAME + "(" +
            PillTable.COL_ID + NUMBER_TYPE + " PRIMARY KEY AUTOINCREMENT," +
            PillTable.COL_PACK_ID + NUMBER_TYPE + "," +
            PillTable.COL_PILL_POSITION + NUMBER_TYPE + "," +
            PillTable.COL_CHECKED + NUMBER_TYPE + "," +
            PillTable.COL_TIME + NUMBER_TYPE + " )";

    private static final String SQL_CREATE_PACK_TABLE = "CREATE TABLE " +
            PackTable.TABLE_NAME + " (" +
            PackTable.COL_ID + NUMBER_TYPE + " PRIMARY KEY AUTOINCREMENT," +
            PackTable.COL_PACK_NAME + TEXT_TYPE + ","+
            PackTable.COL_START_DATE + NUMBER_TYPE + " )";

    private static final String SQL_DELETE_PILL_TABLE =
            "DROP TABLE IF EXISTS " + PillTable.TABLE_NAME;

    private static final String SQL_DELETE_PACK_TABLE =
            "DROP TABLE IF EXISTS " + PackTable.TABLE_NAME;
}