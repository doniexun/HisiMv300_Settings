package com.android.settings.iptv.wifi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.android.settings.R;
import com.android.settings.iptv.util.Loger;

public class WifiActivity extends Activity {

    private Context mContext;
    private FragmentManager mFragmentManager;
    private WifiFragment mWifiFragment;
    private WifiHotspot mWifiHotspot;
    private Button mWifi;
    private Button mHotspot;
    private static boolean isActive = false;
    private Loger loger = new Loger(WifiActivity.class);

    private static final int INIT_VIEW = 0x00101;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_VIEW:
                    initView();
                    break;

                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_setting);
        mFragmentManager = getFragmentManager();
        mContext = WifiActivity.this;
        handler.sendEmptyMessage(INIT_VIEW);
        isActive = true;
    }

    private void initView() {
        mWifi = (Button) this.findViewById(R.id.wlan_wifi);
        mWifi.setOnFocusChangeListener(focusChangeListener);
        mHotspot = (Button) this.findViewById(R.id.wlan_hotspot);
        mHotspot.setOnFocusChangeListener(focusChangeListener);
        mWifi.requestFocus();
    }

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            loger.i( "onFocusChange");
            FragmentTransaction transaction = mFragmentManager.beginTransaction();

            if (isActive && (v instanceof Button)) {
                Button tmp = (Button) v;
                if (mWifi.equals(tmp) && hasFocus) {
                    if (mWifiFragment == null) {
                        mWifiFragment = new WifiFragment(mContext);
                    }
                    transaction.replace(R.id.wifi_frameLayout, mWifiFragment);
                } else if (mHotspot.equals(tmp) && hasFocus) {

                    if (mWifiHotspot == null) {
                        mWifiHotspot = new WifiHotspot(mContext);
                    }
                    transaction.replace(R.id.wifi_frameLayout, mWifiHotspot);
                }
                transaction.commit();
            }
        }
    };







}
