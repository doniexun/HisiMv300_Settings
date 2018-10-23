package com.android.settings.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.android.settings.iptv.nettest.NetDiagnosisActivity;
import com.android.settings.iptv.util.NetDiagnosisHelper;

/**
 * Created by Administrator on 2017/12/21 0021.
 */

public class NetIpService extends Service {
    private Context mcontext;
    private boolean isWifiAllow =false;
    public static boolean isRunning = false;
    private static final int START_TEST= 0;
    private static final int END_TEST= 1;
    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_TEST:
                    startChekIp();
                    break;
                case END_TEST:
                    callBack.onNetIpServiceStoped(isWifiAllow);
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    };

    private NetDiagnosisActivity.CallBack callBack;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mcontext = NetIpService.this;
        callBack = NetDiagnosisActivity.getCallBack();
        isRunning = true;
        handler.sendEmptyMessage(START_TEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    /**
     * 第二步，检测是否能正常获取网络IP地址
     */
    private void startChekIp(){
        GetIpAddress thread =new GetIpAddress();
        thread.start();
        boolean request  = NetDiagnosisHelper.getInstance(mcontext).checkIP();
        isWifiAllow = request;
    }

    class GetIpAddress extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(END_TEST);
        }
    }

}
