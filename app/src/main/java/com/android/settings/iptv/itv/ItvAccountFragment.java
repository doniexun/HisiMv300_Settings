package com.android.settings.iptv.itv;

import com.android.settings.R;
import com.android.settings.iptv.util.CustomeDialog;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.VchCommonToastDialog;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;

@SuppressLint("ValidFragment")
public class ItvAccountFragment extends Fragment {

    private String TAG = "ItvAccountFragment";
    private View root;
    private Context mContext;
    private static final int MESSAGE_INIT = 101;
    private MainHandler mMainHandler = null;
    private EditText mMainAuthAddr, mSecondAuthAddr, mUserName, mPassword;
    VchCommonToastDialog mToastDialog = null;
    public static CheckBox mPasswordVisiable;
    private static Button mHWsubmit;
    private static String LogIn_Uri = "content://stbconfig/authentication/";
    private String auth_url_1 = "";
    private String auth_url_2 = "";
    private String userId = "";
    private String userPassword = "";
    private Loger loger = new Loger(ItvAccountFragment.class);

    public ItvAccountFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.itv_account, null);
        mMainHandler = new MainHandler(ItvAccountFragment.this);
        mMainHandler.sendEmptyMessage(MESSAGE_INIT);

        mToastDialog = new VchCommonToastDialog(getActivity());
        mToastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        mToastDialog.getWindow().setType(2003);
        return root;
    }

    static class MainHandler extends Handler {
        ItvAccountFragment mContext;

        public MainHandler(ItvAccountFragment context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INIT:
                    mContext.initView();
                    break;
                default:
                    break;
            }
        }
    }

    ;

    private void initView() {
        mMainAuthAddr = (EditText) root.findViewById(R.id.itv_auth_url);
        mSecondAuthAddr = (EditText) root.findViewById(R.id.itv_auth_url_bk);
        mUserName = (EditText) root.findViewById(R.id.itv_username);
        mPassword = (EditText) root.findViewById(R.id.itv_password);

        mUserName.setOnKeyListener(onKeyListener);
        mPassword.setOnKeyListener(onKeyListener);

        mHWsubmit = (Button) root.findViewById(R.id.huawei_itv_submit);
        //账号和密码
        queryUserId();
        mUserName.setText(userId);
        mPassword.setText(userPassword);

        //认证地址
        if (TextUtils.isEmpty(auth_url_1)) {
            auth_url_1 = "http://100.100.0.134:8082/EDS/jsp/AuthenticationURL";
        }
        mMainAuthAddr.setText(auth_url_1);

        if (TextUtils.isEmpty(auth_url_2)) {
            auth_url_2 = "http://100.100.0.134:8082/EDS/jsp/AuthenticationURL";
        }
        mSecondAuthAddr.setText(auth_url_2);
        mPasswordVisiable = (CheckBox) root.findViewById(R.id.itv_pass_visiable);
        mPasswordVisiable.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

    }

    private OnKeyListener onKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            if (v instanceof EditText) {
                String text = ((EditText) v).getText().toString();
                Editable editable = ((EditText) v).getText();
                int index = ((EditText) v).getSelectionStart();
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    loger.i("OnKeyListener-----keyCode = " + keyCode);
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        if (text.isEmpty() || index == 0) {

                        } else {
                            editable.delete(index - 1, index);
                            return true;
                        }

                    } else if (keyCode == KeyEvent.KEYCODE_INFO) {
                        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInputFromWindow(v.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
                        return true;
                    }
                }

            }
            return false;
        }
    };

    private void queryUserId() {
        Log.i(TAG, "-------- queryUserId () ---------");
        userId = getdatabasevalue("username");
        userPassword = getdatabasevalue("password");
        if (userId == null) {
            userId = "";
        }
        if (userPassword == null) {
            userPassword = "";
        }
    }

    private void insertUserId() {
        Log.i(TAG, "-------- insertUserId () ---------");
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Uri.parse(LogIn_Uri);
        Log.i(TAG, "mPassword = " + userId);
        ContentValues DeviceCode = new ContentValues();
        DeviceCode.put("name", "username");
        DeviceCode.put("value", userId);
        resolver.update(uri, DeviceCode, "name = ?", new String[]{"username"});
    }

    private void insertPassword() {
        Log.i(TAG, "-------- insertPassword () ---------");
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Uri.parse(LogIn_Uri);
        Log.i(TAG, "mPassword = " + userPassword);
        ContentValues DeviceCode = new ContentValues();
        DeviceCode.put("name", "password");
        DeviceCode.put("value", userPassword);
        resolver.update(uri, DeviceCode, "name = ?", new String[]{"password"});
    }

    public String getdatabasevalue(String databasename) {
        Uri uri = Uri.parse(LogIn_Uri);
        String value = "";
        ContentResolver resolver = mContext.getContentResolver();
        Cursor mCursor = resolver.query(uri,
                null, null, null, null);
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                String name = mCursor.getString(mCursor.getColumnIndex("name"));
                if (name.equals(databasename)) {
                    value = mCursor.getString(mCursor.getColumnIndex("value"));
                    Log.i(TAG, "find " + databasename + ":" + value);
                    break;
                }
            }
            mCursor.close();
        }
        return value;
    }


}
