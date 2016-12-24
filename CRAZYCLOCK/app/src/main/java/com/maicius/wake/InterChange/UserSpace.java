/**
 * User space
 * 检测网络状态
 * 当有网络时检查数据库是否有需要上传的数据
 * 如果有启动SyncDatabase;
 */
package com.maicius.wake.InterChange;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.maicius.wake.DBmanager.DBManager;
import com.maicius.wake.DBmanager.SyncDatabase;
import com.maicius.wake.alarmClock.MainActivity;
import com.maicius.wake.alarmClock.R;
import com.maicius.wake.web.ConnectionDetector;
import com.maicius.wake.web.NetEventActivity;

public class UserSpace extends NetEventActivity {

    private TextView netStateView;
    private DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("sss", "******************enter user space!");
        setContentView(R.layout.user_space);
        netStateView = (TextView)findViewById(R.id.InterDetector);
        dbManager = new DBManager(this);

        boolean netState = this.isNetConnect();
        if(netState){
            Cursor sleepTable = dbManager.query("sleep");
            if(sleepTable.getCount() !=0) {
                Intent startIntent = new Intent(this, SyncDatabase.class);
                startService(startIntent);
            }
            else{
                Intent stopIntent = new Intent(this, SyncDatabase.class);
                stopService(stopIntent);
            }
            netStateView.setVisibility(View.GONE);
        }else{
            netStateView.setVisibility(View.VISIBLE);
        }
        mInitUI();

    }
    public void onNetChange(int netState){
        super.onNetChange(netState);

        if(netState == ConnectionDetector.NETWORK_NONE){
            netStateView.setVisibility(View.VISIBLE);
        }else{
            Cursor sleepTable = dbManager.query("sleep");
            netStateView.setVisibility(View.GONE);
            if(sleepTable.getCount() !=0) {
                Intent startIntent = new Intent(this, SyncDatabase.class);
                startService(startIntent);
            }
            else{
                Intent stopIntent = new Intent(this, SyncDatabase.class);
                stopService(stopIntent);
            }
        }
    }

    private void mInitUI() {
        TextView userspace_name = (TextView) findViewById(R.id.userspace_name);
        userspace_name.setText(MainActivity.s_nickname + "的空间");

        ImageView image_userInfo = (ImageView) findViewById(R.id.userInfo);
        ImageView image_getUpTime = (ImageView) findViewById(R.id.getUpTime);
        ImageView image_exit = (ImageView) findViewById(R.id.exit);
        ImageView image_friends = (ImageView) findViewById(R.id.friendImageView);
        ImageView image_sleepHistory = (ImageView) findViewById(R.id.sleep_history_image);
        image_userInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(UserSpace.this, UserInfo.class));
            }
        });
        image_getUpTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(UserSpace.this, GetUpHistory.class);
                Bundle bundle = new Bundle();
                bundle.putString("username", MainActivity.s_userName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        image_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.s_isLogged=false;
                DBManager dbManager = new DBManager(UserSpace.this);
                dbManager.deleteAppUser("appUser");
                UserSpace.this.finish();
            }
        });
        image_friends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(UserSpace.this, FriendsList.class));
            }
        });

        image_sleepHistory.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(UserSpace.this, SleepHistory.class));
            }
        });
    }

}
