package com.android.settings.iptv.itv;

import com.android.settings.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

@SuppressLint("ValidFragment")
public class ItvServerFragment extends Fragment {

    private String TAG = "ITV";
    private View root;
    private EditText tms_mainServer, tms_backupServer;
    private String tms_main, tms_backup;
    private Context mContext;
    private MainHandler mMainHandler = null;
    private static final int MESSAGE_INIT = 101;
    /*ntp server*/
    private String ntp = null;
    private EditText ntp_server;

    public ItvServerFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.itv_server, null);
        mMainHandler = new MainHandler(ItvServerFragment.this);
        mMainHandler.sendEmptyMessage(MESSAGE_INIT);
        return root;
    }


    static class MainHandler extends Handler {

        ItvServerFragment mContext = null;

        public MainHandler(ItvServerFragment context) {
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

    private void initView() {
        tms_mainServer = (EditText) root.findViewById(R.id.et_tms_mainserver);
        tms_backupServer = (EditText) root.findViewById(R.id.et_tms_backupserver);

        if (TextUtils.isEmpty(tms_main)) {
            tms_main = "http://100.100.0.139:37021/acs";
        }
        if (TextUtils.isEmpty(tms_backup)) {
            tms_backup = "http://100.100.0.140:37020/acs";
        }

        tms_mainServer.setText(tms_main);
        tms_backupServer.setText(tms_backup);

        /**
         * ntp时间服务器地址设置
         */
        ntp_server = (EditText) root.findViewById(R.id.ntptimeserver);
        ContentResolver resolver = mContext.getContentResolver();
        ntp = Settings.Global.getString(resolver, "ntp_server");
        if (TextUtils.isEmpty(ntp)) {
            Log.e(TAG,"Settings.Global.getString(resolver, ntp_server)为空！！");
            ntp = "100.100.0.134";
        }
        ntp_server.setText(ntp);

    }


}
