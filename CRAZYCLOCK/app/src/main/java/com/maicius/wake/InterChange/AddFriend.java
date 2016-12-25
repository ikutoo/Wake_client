package com.maicius.wake.InterChange;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.maicius.wake.alarmClock.R;
import com.maicius.wake.web.WebService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class AddFriend extends Activity implements ActionBar.TabListener, ContactInfoFragment.CallBackValue,
        ConditionSearchFriend.CallBackInputValue {
    private static Handler handler = new Handler();
    private SerializableMap contactsInfo;
    private ProgressDialog dialog;
    private Fragment fragment;
    private AlertDialog.Builder warningDialog;
    private String returnInfo, nickName, phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);


        dialog = new ProgressDialog(AddFriend.this);
        dialog.setTitle("提示");
        dialog.setMessage("正在获取联系人信息，请稍后...");
        dialog.setCancelable(false);

        warningDialog = new AlertDialog.Builder(this);
        warningDialog.setTitle("警告")
                .setIcon(R.drawable.ic_warning)
                .setMessage("确定添加他为好友？");
        warningDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        warningDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        warningDialog.create();
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText("条件查找").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("联系人导入").setTabListener(this));
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (tab.getPosition() == 0) {
            fragment = new ConditionSearchFriend();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        } else if (tab.getPosition() == 1) {
            fragment = new ContactInfoFragment();

            dialog.show();
            //创建一个新的线程，用来获取本地的联系人信息
            new Thread(new MyThread()).start();
        } else {
            fragment = new ConditionSearchFriend();
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void SendMessageValue(String value) {
        //Toast.makeText(AddFriend.this, value, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void SendInputValue(String nickName, String phone) {
        //Toast.makeText(AddFriend.this, "NickName:"+nickName+" Phone:"+phone, Toast.LENGTH_SHORT).show();
        this.nickName = nickName;
        this.phone = phone;
        //warningDialog.show();
        dialog.setMessage("正在查找，请稍后...");
        dialog.show();
        new Thread(new SearchFriendThread()).start();
    }

    public SerializableMap getContactsInfo() {
        ContentResolver resolver = getContentResolver();
        SerializableMap contacts = new SerializableMap();
        Map<String, String> tmp = new HashMap<String, String>();
        //使用resolver查找联系人
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            //获取联系人的ID
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //获取联系人的名字
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            //使用resolver查找联系人的电话号码
            Cursor phones = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
            //虽然可能一个用户多个电话，但是只取第一个
            String telnum = "";
            while(phones.moveToNext()) {
                telnum = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                break;//只读一个
            }
            //将获取到的用户名和电话插入map中
            Log.v("sss", "Name:" + name + " Tel:" + telnum);
            tmp.put(name, telnum);
            phones.close();
        }
        cursor.close();
        contacts.setMap(tmp);
        return contacts;
    }

    private class MyThread implements Runnable {
        @Override
        public void run() {
            //获取本地联系人信息
            contactsInfo = getContactsInfo();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("contacts", contactsInfo);
                    //向fragment传入参数
                    fragment.setArguments(bundle);
                    //Toast.makeText(AddFriend.this, "Map size:" + contactsInfo.getMapSize(), Toast.LENGTH_SHORT).show();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container, fragment);
                    ft.commit();
                }
            });
        }
    }

    private class SearchFriendThread implements Runnable {
        @Override
        public void run() {
            returnInfo = WebService.friendOperation(nickName, phone, WebService.State.SearchFriend);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AddFriend.this, returnInfo, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    if (returnInfo.equals("failed")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddFriend.this);
                        alertDialog.setTitle("错误信息").setMessage("错误！请检查输入是否正确");
                        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();
                        return;
                    } else if (returnInfo.equals("nothing")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddFriend.this);
                        alertDialog.setTitle("提示信息").setMessage("并未找到此好友\n他还不是用户或者请检查输入是否有效");
                        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();
                        return;
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("info", returnInfo);
                        //向fragment传入参数
                        fragment = new ConditionSearchFriend();
                        fragment.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.container, fragment);
                        ft.commit();
                    }
                }
            });
        }
    }

}
