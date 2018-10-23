package com.android.settings.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.android.settings.iptv.nettest.NetDiagnosisActivity.CallBack;
import com.android.settings.iptv.nettest.NetDiagnosisActivity;
import com.android.settings.iptv.util.NetDiagnosisHelper;
import com.android.settings.iptv.util.Loger;


/**
 * Created by Administrator on 2017/12/21 0021.
 */

public class NetConnectService extends Service {

    private Context mcontext;
    private CallBack callback;
    private boolean CONNECT =false;
    public static boolean isRunning = false;
    private static final int START_TEST= 0;
    private static final int END_TEST= 1;
    private Loger loger = new Loger(NetConnectService.class);

    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
           switch (msg.what){
               case START_TEST:
                   startNetConnectTest();
                   break;
               case END_TEST:
                   callback.onNetConnectServiceStoped(CONNECT);
                   stopSelf();
                   break;
               default:
                   break;

           }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mcontext = NetConnectService.this;
        callback = NetDiagnosisActivity.getCallBack();
        loger.i("start net connect test ..");
        isRunning = true;
        handler.sendEmptyMessage(START_TEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    /**
     * 第一步，检测网络连接是否畅通
     */
    private void startNetConnectTest(){
        ConnectThread thread =new ConnectThread();
        thread.start();
        boolean isconn = NetDiagnosisHelper.getInstance(mcontext).isNetWorkConnected();
        CONNECT =isconn;
        loger.i("stop net connect test ..");

    }


    class ConnectThread extends Thread {
        @Override
        public void run() {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            handler.sendEmptyMessage(END_TEST);
        }
    }
}
