package com.android.settings.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.android.settings.iptv.nettest.NetDiagnosisActivity;

/**
 * Created by Administrator on 2017/12/21 0021.
 */

public class NetGatewayService extends Service {

    private Context mcontext ;
    private NetDiagnosisActivity.CallBack callBack;
    public static boolean isRunning = false;
    private boolean Result = false;
    private static final int START_TEST= 0;
    private static final int END_TEST= 1;

    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_TEST:
                    startGatewayTest();
                    break;
                case END_TEST:
                    callBack.onNetGatewayServiceStoped(Result);
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        mcontext = NetGatewayService.this;
        callBack = NetDiagnosisActivity.getCallBack();
        isRunning = true;
        handler.sendEmptyMessage(START_TEST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startGatewayTest(){
        GatewayAddress thread = new GatewayAddress();
        thread.start();

    }

    class GatewayAddress extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Result = true;
            handler.sendEmptyMessage(END_TEST);
        }
    }

}
