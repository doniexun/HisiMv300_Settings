package com.android.settings.iptv.wifi;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.iptv.util.Loger;


@SuppressLint("ValidFragment")
public class WifiFragment extends Fragment {
    private View root;
    private Context mContext;
    private ListView wifiSearchListView;
    private TextView wifiListState;
    private CheckBox mWifiSwitch;
    private WifiManager mWifiManager;
    private wifiMethods mWifiMethods;
    private static final int INIT_WifiMethods = 0x0001;
    private static final int REFRESH_wifi = 0x0002;
    private static boolean isActive = false;
    private Loger loger = new Loger(WifiFragment.class);


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_WifiMethods:
                    initWifiMethods();
                    break;

                default:
                    break;
            }
        }
    };

    public WifiFragment(Context context) {
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        loger.e( "onCreateView()");
        root = inflater.inflate(R.layout.wifi_fragment, null);
        initView();
        handler.sendEmptyMessage(INIT_WifiMethods);
        isActive = true;
        return root;
    }

    private void initWifiMethods() {
        loger.e( "initWifiMethods()");
        mWifiMethods = new wifiMethods(mContext, null, mWifiManager, wifiSearchListView,wifiListState);
        if (mWifiSwitch.isChecked()) {
            mWifiMethods.enable_wifi();
        } else {
            mWifiMethods.disable_wifi();
        }
        if (mWifiMethods != null) {
            mWifiMethods.registerWifiFilter();
            mWifiMethods.resumeConnect();
            mWifiMethods.updateWifiAcessPoints();
        }

    }

    @Override
    public void onPause() {
        loger.e( "onPause()");
        if (mWifiMethods != null) {
            mWifiMethods.unregisterWifiFilter();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        loger.e( "onResume()");
        if (mWifiMethods != null) {
            mWifiMethods.registerWifiFilter();
            mWifiMethods.resumeConnect();
            mWifiMethods.updateWifiAcessPoints();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        loger.e( "onDestroy()");
        isActive = false;
        super.onDestroy();
    }

    private void initView() {

        loger.e( "initView()");
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        /**
         * 初始化wifi列表
         */
        wifiSearchListView = (ListView) root.findViewById(R.id.wifi_list);
        wifiSearchListView.setOnItemClickListener(mWifiItemListClick);

        /**
         * 初始化wifi列表状态
         */
        wifiListState = (TextView) root.findViewById(R.id.wifi_list_state);

        /**
         * 初始化wifi开关
         */
        mWifiSwitch = (CheckBox) root.findViewById(R.id.wifi_switch);
        if (mWifiManager.isWifiEnabled() && mWifiSwitch.isChecked()) {
            mWifiSwitch.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.switch_on), null, null, null);
        } else {
            mWifiSwitch.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.switch_off), null, null, null);
            mWifiSwitch.setChecked(false);
        }

        mWifiSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    AdapterView.OnItemClickListener mWifiItemListClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View mView, int index, long lindex) {

            if (mWifiMethods != null) {
                wifiAccessPoint mWifiAcessPoint = mWifiMethods.GetAccessPointByIndex(index);
                if (mWifiAcessPoint != null) {
                    loger.i( "ITEM CLICK == " + mWifiAcessPoint.ssid);
                    mWifiMethods.ShowConnectDialog(mWifiAcessPoint, false);
                }
            }
        }
    };


    CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                mWifiSwitch.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.switch_on), null, null, null);
                mWifiMethods.enable_wifi();
                wifiListState.setText(R.string.wifi_opening);
                wifiSearchListView.requestFocus();
            } else {
                mWifiSwitch.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.switch_off), null, null, null);
                mWifiMethods.disable_wifi();
            }
        }
    };


}
