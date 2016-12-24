package com.maicius.wake.InterChange;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.maicius.wake.DBmanager.DBManager;
import com.maicius.wake.DBmanager.ScreenUser;
import com.maicius.wake.DBmanager.SleepUser;
import com.maicius.wake.alarmClock.MainActivity;
import com.maicius.wake.web.WebService;

/**
 * Created by Maicius on 2016/6/14.
 */
public class Notification extends Activity {

    private ProgressDialog proDialog;
    private DBManager dbManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("maicius", "Notification Entered******************");
        AlertDialog.Builder dialog = new AlertDialog.Builder(Notification.this);
        dialog.setTitle("温馨提醒").setMessage("你真的醒了吗？");

        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Notification.this.finish();
            }
        });

        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                proDialog = new ProgressDialog(Notification.this);
                proDialog.setTitle("提示");
                proDialog.setMessage("正在上传起床时间，请稍后...");
                proDialog.setCancelable(false);
                proDialog.show();
                //创建子线程
                new Thread(new MyThread()).start();
                Notification.this.finish();
            }
        });


        dialog.create().show();
    }

    public class MyThread implements Runnable {
        public void run() {
            Calendar currentTime = Calendar.getInstance();
            String info = WebService.executeHttpGet(currentTime.getTimeInMillis(), WebService.State.GetUpTime);
            computeTimeDiff();
            Log.v("sss", info + "***");
            proDialog.dismiss();
        }
    }

    private void computeTimeDiff() {
        String sleepTime;
        DBManager dbManager = new DBManager(this);
        try {
            Cursor sleepTimeCur = dbManager.query("sleepTime");
            if (sleepTimeCur.getCount() != 0) {
                sleepTimeCur.moveToFirst();//用户最后一次解锁时间
                sleepTimeCur.moveToNext();//用户最后一次解锁时间之后的第一次屏幕关闭时间
                sleepTime = sleepTimeCur.getString(2);
            }else{
                Calendar currentTime = Calendar.getInstance();
                currentTime.add(Calendar.DAY_OF_MONTH, -1);
                int year = currentTime.get(Calendar.YEAR);
                int month = currentTime.get(Calendar.MONTH);
                int day = currentTime.get(Calendar.DAY_OF_MONTH);
                int hour = 21;
                int minute = 10;
                int second = 10;
                sleepTime = year+"-"+month+"-"+day+"-"+" "+hour+":"+minute+":"+second;
            }
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date curTime = new Date(System.currentTimeMillis());
            String getUptime = format.format(curTime);
            Date t1=format.parse(getUptime);
            Date t2 = format.parse(sleepTime);
            long diff = t1.getTime()-t2.getTime();

            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
            //long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);

            SleepUser sleepUser = new SleepUser(MainActivity.s_userName, hours);
            dbManager.insertSQL(sleepUser);
        }catch(ParseException e){
            e.printStackTrace();
        }
    }
}
