package com.maicius.wake.DBmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Maicius on 2016/12/21.
 */
public class DBHelpers extends SQLiteOpenHelper {
    private static final String DB_NAME = "clock.db";
    private static final int VERSION = 3;
    public DBHelpers(Context context){
        super(context, DB_NAME, null, VERSION);

    }
    public void onCreate(SQLiteDatabase db){
        String sql = "create table sqlUser(\n" +
                "\tusername varchar(255) primary key,\n" +
                "\tpassword varchar(255),\n" +
                "\tnickname varchar(255)\n" +
                ")";
        db.execSQL(sql);
        sql = "create table sleepTime(\n" +
                "\tsleep_time_id int,\n" +
                "\tusername varchar(255),\n" +
                "\tcur_time varchar(255),\n" +
                "\tprimary key(sleep_time_id),\n" +
                "\tforeign key(username) references sqlUser(username)\n" +
                ")";
        db.execSQL(sql);
        sql = "create table getUpTime(\n" +
                "\tget_up_time_id int primary key,\n" +
                "\tusername varchar(255),\n" +
                "\tget_up_time varchar(255),\n" +
                "\tforeign key(username) references appuser(username)\n" +
                ")";
        db.execSQL(sql);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS sleepTime");
        db.execSQL("DROP TABLE IF EXISTS sqlUser");
        onCreate(db);
    }

}