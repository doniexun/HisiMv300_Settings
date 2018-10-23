package com.android.settings.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.iptv.util.VchCommonToastDialog;
import com.android.settings.iptv.wifi.CustomDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author libeibei
 * 广播监听类
 * 监听PhoneWindowManager发出的按键点击广播
 * 实现组合键监听
 * 打开和隐藏wifi功能
 */
public class KeysReceiver extends BroadcastReceiver {
    private static final String TAG = "Setting";
    private static final String ACTION = "com.chinamobile.action.KEY_PRESS_DOWN";
    private static final String ExtraKey = "keyCode";
    private static final int key_up = 19;
    private static final int key_down = 20;
    private static final int key_left = 21;
    private static final int key_right = 22;
    private static final int key_ok = 23;
    /**
     * 组合键：上上 下下 左右 左右 OK OK
     * 对应KeyCode = 19,19,20,20,21,22,21,22,23,23
     */
    private static final Integer[] keyarray = {19, 19, 20, 20, 21, 22, 21, 22, 23, 23};
    private List<Integer> inputList = new ArrayList<Integer>();
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    CustomDialog mCustomeDialog;
    static VchCommonToastDialog mToastDialog = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        preferences = context.getSharedPreferences("KEY_LIST_PRE", Context.MODE_PRIVATE);
        editor = preferences.edit();
        String action = intent.getAction();
        if (action.equals(ACTION)) {
            int keyCode = intent.getIntExtra(ExtraKey, -1);
            Log.i(TAG, "KeyCode = " + keyCode);
            checkList(context, keyCode);
        }
    }


    /**
     * 每次传入一个点击的keycode
     * 1.逐个匹配
     * 2.
     *
     * @param code
     */
    private void checkList(Context context, int code) {
        if (code == key_up || code == key_down || code == key_left || code == key_right || code == key_ok) {

            inputList = getInputList();
            int length = inputList.size();
            inputList.add(code);
            saveInputList(inputList);

            if (inputList.get(length) == keyarray[length]) {
                if (length == (keyarray.length - 1)) {
                    //数组长度相同，各元素相同，判定为触发了组合键
                    Log.e(TAG, "已经触发组合键：" + inputList.toString());
                    inputList = new ArrayList<Integer>();
                    saveInputList(inputList);
                    showDialog(context);
                }
            } else {
                inputList = new ArrayList<Integer>();
                saveInputList(inputList);
            }

        } else {
            //与任意一个值不同，从头开始
            inputList = new ArrayList<Integer>();
            saveInputList(inputList);
        }

    }

    /**
     * 确认打开wifi功能的弹窗
     */
    private void showDialog(final Context context) {
        mCustomeDialog = new CustomDialog(context, CustomDialog.DIALOG_HAVE_SECURITY);
        mCustomeDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        //   mCustomeDialog.setText("要打开wifi功能，请输入密码:");
        TextView titleView = (TextView) mCustomeDialog.getSSIDname();
        titleView.setText("暗码组合弹窗");
        final EditText mEditPassword = (EditText) mCustomeDialog.getEditText();
        final CheckBox mCheckShowPassword = (CheckBox) mCustomeDialog.getCheckBox();
        mCheckShowPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mEditPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT
                                | (((CheckBox) mCheckShowPassword).isChecked() ?
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                : InputType.TYPE_TEXT_VARIATION_PASSWORD));

            }
        });
        mCustomeDialog.setOnNegativeListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.i(TAG, "Click Negative Button!");
                mCustomeDialog.dismiss();
            }
        });
        mCustomeDialog.setOnPositiveListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String word = mEditPassword.getText().toString();
                if (word.equals("666666")) {
                    //具体弹窗功能
                    mCustomeDialog.dismiss();
                    mToastDialog = new VchCommonToastDialog(context);
                    mToastDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    mToastDialog.setMessage(R.string.wifi_founction_opened);
                    mToastDialog.show();
                } else {
                    mToastDialog = new VchCommonToastDialog(context);
                    mToastDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    mToastDialog.setMessage(R.string.error_psd);
                    mToastDialog.show();
                }
            }
        });
        mCustomeDialog.setCancelable(true);
        mCustomeDialog.show();
    }


    /**
     * 把已经输入的组合键列表存储进数据库
     *
     * @param list
     */
    private void saveInputList(List<Integer> list) {
        editor.putInt("ListNums", list.size());
        for (int i = 0; i < list.size(); i++) {
            editor.putInt("item_" + i, list.get(i));
        }
        editor.commit();
    }

    /**
     * 从数据库中读取已经存储的组合键列表
     *
     * @return
     */
    private List<Integer> getInputList() {
        List<Integer> list = new ArrayList<Integer>();
        int size = preferences.getInt("ListNums", 0);
        for (int i = 0; i < size; i++) {
            int item = preferences.getInt("item_" + i, 0);
            list.add(item);
        }
        return list;
    }


}
