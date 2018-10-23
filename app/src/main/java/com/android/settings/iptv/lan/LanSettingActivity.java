package com.android.settings.iptv.lan;

import com.android.settings.BaseActivity;
import com.android.settings.R;
import com.android.settings.iptv.util.Loger;

import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;

public class LanSettingActivity extends BaseActivity {

    private Context mContext = LanSettingActivity.this;

    FragmentManager mFragmentManager;
    PPPoEFragment mPPPoEFragment;
    LanFragment mLanFragment;
    DHCPFragment mDHCPFragment;
    IPOEFragment mIPOEFragment;

    public static Button mPPPoE, mLan, mDHCP, mIPOE;
    private boolean isActive = false;

    private static WifiManager mWifiManager;
    private static EthernetManager mEthManager;
    private Loger loger = new Loger(LanSettingActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lan_setting);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mEthManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        mFragmentManager = getFragmentManager();

        findView();
        isActive = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                isActive = false;
                this.finish();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void findView() {

        mPPPoE = (Button) this.findViewById(R.id.pppoe);
        mLan = (Button) this.findViewById(R.id.lan);
        mDHCP = (Button) this.findViewById(R.id.nativedhcp);
        mIPOE = (Button) this.findViewById(R.id.nativeipoe);

        String tempStr = mEthManager.getEthernetMode();

        FragmentTransaction transaction;


        if (tempStr.equals(EthernetManager.ETHERNET_CONNECT_MODE_DHCP)
                && mEthManager.getDhcpOption60State() == EthernetManager.OPTION60_STATE_DISABLED) {
            mDHCP.requestFocus();
            mDHCPFragment = new DHCPFragment(mContext);
            transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.lan_frameLayout, mDHCPFragment);
            transaction.commit();
        } else if (tempStr.equals(EthernetManager.ETHERNET_CONNECT_MODE_PPPOE)) {
            mPPPoE.requestFocus();
            mPPPoEFragment = new PPPoEFragment(mContext);
            transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.lan_frameLayout, mPPPoEFragment);
            transaction.commit();
        } else if (tempStr.equals(EthernetManager.ETHERNET_CONNECT_MODE_DHCP)
                && mEthManager.getDhcpOption60State() == EthernetManager.OPTION60_STATE_ENABLED) {
            mIPOE.requestFocus();
            mIPOEFragment = new IPOEFragment(mContext);
            transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.lan_frameLayout, mIPOEFragment);
            transaction.commit();
        } else if (tempStr.equals(EthernetManager.ETHERNET_CONNECT_MODE_MANUAL)) {
            mLan.requestFocus();
            mLanFragment = new LanFragment(mContext);
            transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.lan_frameLayout, mLanFragment);
            transaction.commit();
        } else {
            mIPOE.requestFocus();
            mIPOEFragment = new IPOEFragment(mContext);
            transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.lan_frameLayout, mIPOEFragment);
            transaction.commit();
        }

        mPPPoE.setOnFocusChangeListener(focusChangeListener);
        mLan.setOnFocusChangeListener(focusChangeListener);
        mDHCP.setOnFocusChangeListener(focusChangeListener);
        mIPOE.setOnFocusChangeListener(focusChangeListener);

    }

    public static void closeWifi() {

        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }

        if (mEthManager.getEthernetState() == EthernetManager.ETHERNET_STATE_DISABLED) {
            mEthManager.setEthernetEnabled(true);
            mEthManager.enableEthernet(true);
        }
    }

    private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (isActive && (v instanceof Button)) {
                Button tmp = (Button) v;
                if (mPPPoE.equals(tmp) && hasFocus) {
                    if (PPPoEFragment.mPasswordVisiable != null) {
                        PPPoEFragment.mPasswordVisiable.setChecked(false);
                    }
                    if (mPPPoEFragment == null) {
                        mPPPoEFragment = new PPPoEFragment(mContext);
                    }
                    transaction.replace(R.id.lan_frameLayout, mPPPoEFragment);

                } else if (mLan.equals(tmp) && hasFocus) {

                    if (PPPoEFragment.mPasswordVisiable != null) {
                        PPPoEFragment.mPasswordVisiable.setChecked(false);
                    }

                    if (mLanFragment == null) {
                        mLanFragment = new LanFragment(mContext);
                    }
                    transaction.replace(R.id.lan_frameLayout, mLanFragment);

                } else if (mDHCP.equals(tmp) && hasFocus) {

                    if (PPPoEFragment.mPasswordVisiable != null) {
                        PPPoEFragment.mPasswordVisiable.setChecked(false);
                    }

                    if (mDHCPFragment == null) {
                        mDHCPFragment = new DHCPFragment(mContext);
                    }
                    transaction.replace(R.id.lan_frameLayout, mDHCPFragment);
                } else if (mIPOE.equals(tmp) && hasFocus) {

                    if (PPPoEFragment.mPasswordVisiable != null) {
                        PPPoEFragment.mPasswordVisiable.setChecked(false);
                    }

                    if (mIPOEFragment == null) {
                        mIPOEFragment = new IPOEFragment(mContext);
                    }
                    transaction.replace(R.id.lan_frameLayout, mIPOEFragment);
                }

                transaction.commit();
            }
        }
    };

}
