package com.maicius.wake.InterChange;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.maicius.wake.alarmClock.MainActivity;
import com.maicius.wake.alarmClock.R;
import com.maicius.wake.web.WebService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendInfo extends Activity {
    private ListView listView;
    private List<Map<String, Object>> listItems;
    private String nickName;
    private String phoneNum;
    private String signature;
    private AlertDialog.Builder warningDialog;
    private String returnInfo;
    private static Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        //获取传来的数据
        Intent intent = getIntent();
        nickName = intent.getStringExtra("nickName");
        phoneNum = intent.getStringExtra("phoneNum");
        signature = intent.getStringExtra("signature");
        //Toast.makeText(FriendInfo.this, nickName + " " + phoneNum + " " + signature, Toast.LENGTH_SHORT).show();
        //显示好友信息
        TextView nickNameTextView = (TextView) findViewById(R.id.nickNameTextView);
        TextView telNumTextView = (TextView) findViewById(R.id.telNumTextView);
        TextView signatureTextView = (TextView) findViewById(R.id.signatureTextView);
        nickNameTextView.setText(nickName);
        telNumTextView.setText(phoneNum);
        signatureTextView.setText(signature);

        listItems = new ArrayList<Map<String, Object>>();
        Map<String, Object> listItem = new HashMap<String, Object>();
        listItem.put("icon", R.drawable.ic_clock_alarm_on);
        listItem.put("operateName", "查看他的起床时间");
        listItems.add(listItem);

        //创建一个SimpleAdapter
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems,
                R.layout.item_operate_list_item,
                new String[] {"icon", "operateName"},
                new int[] {R.id.iconImageView, R.id.operateNameTextView});
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0)
                {
                    Intent intent = new Intent();
                    intent.setClass(FriendInfo.this, GetUpHistory.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("username", phoneNum);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        //创建警告框
        warningDialog = new AlertDialog.Builder(this);
        warningDialog.setTitle("警告")
                .setIcon(R.drawable.ic_warning)
                .setMessage("删除好友后不能查看好友信息\n确定删除？");
        warningDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Thread(new MyThread()).start();
            }
        });
        warningDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        warningDialog.create();

        Button deleteBtn = (Button) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                warningDialog.show();
            }
        });
    }

    private class MyThread implements Runnable {
        @Override
        public void run() {
            returnInfo = WebService.friendOperation(MainActivity.s_userName, phoneNum, WebService.State.DeleteFriend);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (returnInfo.equals("success")) {
                        Toast.makeText(FriendInfo.this, "删除成功！", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(FriendInfo.this, FriendsList.class));
                        return;
                    } else if (returnInfo.equals("failed")) {
                        Toast.makeText(FriendInfo.this, "删除失败，请重试！", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        Toast.makeText(FriendInfo.this, "返回值为:" + returnInfo, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}