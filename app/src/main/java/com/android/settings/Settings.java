package com.android.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.settings.iptv.util.Loger;


/**
 * @author libeibei
 * For Hisi Settings Home Activity;
 */
public class Settings extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {
    private Context mContext;
    private FrameLayout layout_lan, layout_wifi, layout_netinfo, layout_nettest;
    private FrameLayout layout_display,layout_info, layout_itv, layout_media, layout_other;
    private ImageButton bt_lan, bt_wifi, bt_netinfo, bt_nettest;
    private ImageButton bt_display,bt_info, bt_itv, bt_media, bt_other;
    private static final int INIT_VIEW = 10001;
    private static final int INIT_WIFI = 10002;
    private static final String DISPLAY_ACTION = "android.intent.action.Settings.Display";
    private static final String SYSTEMINFO_ACTION = "android.intent.action.Settings.SystemInfo";
    private static final String NETTEST_ACTION = "android.intent.action.Settings.Nettest";
    private static final String ETHERNET_ACTION = "android.intent.action.Settings.EtherNet";
    private static final String WIFI_ACTION = "android.intent.action.Settings.Wifi";
    private static final String ITV_ACTION = "android.intent.action.Settings.itv";
    private static final String OTHER_ACTION = "android.intent.action.Settings.Other";
    private static final String NETINFO_ACTION = "android.intent.action.Settings.NetInfo";
    private Loger loger = new Loger(Settings.class);

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_VIEW:
                    initView();
                    break;
                case INIT_WIFI:
                    initWifi();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = Settings.this;
        handler.sendEmptyMessage(INIT_VIEW);
        handler.sendEmptyMessage(INIT_WIFI);
    }


    private void initView() {

        layout_lan = (FrameLayout) findViewById(R.id.framelayout_lan);
        layout_wifi = (FrameLayout) findViewById(R.id.framelayout_wifi);
        layout_netinfo = (FrameLayout) findViewById(R.id.framelayout_netinfo);
        layout_nettest = (FrameLayout) findViewById(R.id.framelayout_nettest);
        layout_display = (FrameLayout) findViewById(R.id.framelayout_display);
        layout_info = (FrameLayout) findViewById(R.id.framelayout_systeminfo);
        layout_itv = (FrameLayout) findViewById(R.id.framelayout_itv);
        layout_media = (FrameLayout) findViewById(R.id.framelayout_mediaplay);
        layout_other = (FrameLayout) findViewById(R.id.framelayout_other);

        bt_lan = (ImageButton) findViewById(R.id.lan);
        bt_wifi = (ImageButton) findViewById(R.id.wifi);
        bt_netinfo = (ImageButton) findViewById(R.id.netinfo);
        bt_nettest = (ImageButton) findViewById(R.id.net_test);
        bt_display = (ImageButton) findViewById(R.id.display);
        bt_info = (ImageButton) findViewById(R.id.systeminfo);
        bt_itv = (ImageButton) findViewById(R.id.itv);
        bt_media = (ImageButton) findViewById(R.id.mediaplay);
        bt_other = (ImageButton) findViewById(R.id.other);

        bt_lan.setOnClickListener(this);
        bt_wifi.setOnClickListener(this);
        bt_netinfo.setOnClickListener(this);
        bt_nettest.setOnClickListener(this);
        bt_display.setOnClickListener(this);
        bt_info.setOnClickListener(this);
        bt_itv.setOnClickListener(this);
        bt_media.setOnClickListener(this);
        bt_other.setOnClickListener(this);

        bt_lan.setOnFocusChangeListener(this);
        bt_wifi.setOnFocusChangeListener(this);
        bt_netinfo.setOnFocusChangeListener(this);
        bt_nettest.setOnFocusChangeListener(this);
        bt_display.setOnFocusChangeListener(this);
        bt_info.setOnFocusChangeListener(this);
        bt_itv.setOnFocusChangeListener(this);
        bt_media.setOnFocusChangeListener(this);
        bt_other.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {

            case R.id.lan:
                intent.setAction(ETHERNET_ACTION);
                startActivity(intent);
                break;
            case R.id.wifi:
                intent.setAction(WIFI_ACTION);
                startActivity(intent);
                break;
            case R.id.netinfo:
                intent.setAction(NETINFO_ACTION);
                startActivity(intent);
                break;
            case R.id.net_test:
                intent.setAction(NETTEST_ACTION);
                startActivity(intent);
                break;
            case R.id.display:
                intent.setAction(DISPLAY_ACTION);
                startActivity(intent);
                break;
            case R.id.systeminfo:
                intent.setAction(SYSTEMINFO_ACTION);
                startActivity(intent);
                break;
            case R.id.itv:
                intent.setAction(ITV_ACTION);
                startActivity(intent);
                break;
            case R.id.mediaplay:
                //本地媒体播放器
                intent.setClassName("com.android.smart.terminal.nativeplayer",
                        "com.android.smart.terminal.nativeplayer.CHLMMainUI");
                startActivity(intent);
                break;
            case R.id.other:
                intent.setAction(OTHER_ACTION);
                startActivity(intent);
                break;
            default:
                break;

        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        FrameLayout layout;
        switch (v.getId()) {
            case R.id.lan:
                layout = layout_lan;
                break;
            case R.id.wifi:
                layout = layout_wifi;
                break;
            case R.id.netinfo:
                layout = layout_netinfo;
                break;
            case R.id.display:
                layout = layout_display;
                break;
            case R.id.net_test:
               layout = layout_nettest;
                break;
            case R.id.systeminfo:
                layout=layout_info;
                break;
            case R.id.itv:
                layout=layout_itv;
                break;
            case R.id.mediaplay:
                layout=layout_media;
                break;
            case R.id.other:
                layout=layout_other;
                break;
            default:
                layout = layout_lan;
                break;
        }

        if(hasFocus){
            setViewZoomIn(layout);
        }else{
            setViewZoomOut(layout);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setViewZoomIn(View v) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(250);
        animation.setFillAfter(true);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animationSet);
    }

    private void setViewZoomOut(View v) {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation animation = new ScaleAnimation(1.1f, 1.0f, 1.1f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(250);
        animation.setFillAfter(true);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animationSet);
    }
    /**
     * 系统第一次开机
     * 默认wifi是没有打开过的
     * 那么网络信息中获取到的无线mac就为空
     * 所以这里这里做一次先开启wifi
     * 再关闭wifi的操作
     */
    private void initWifi() {
        try{
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String wifimac = info.getMacAddress();
        if (TextUtils.isEmpty(wifimac)) {
            wifiManager.setWifiEnabled(true);
            wifiManager.setWifiEnabled(false);
        }
        }catch (Exception e){
            loger.e(e.toString());
        }
    }

}
