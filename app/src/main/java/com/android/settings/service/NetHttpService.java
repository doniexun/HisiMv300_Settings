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

public class NetHttpService extends Service {
    private Context mcontext;

    private NetDiagnosisActivity.CallBack callBack;
    public static boolean isRunning =false;
    private static final int START_TEST_1= 0;
    private static final int START_TEST_2= 1;
    private static final int END_TEST= 2;
    private boolean result = false;
    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_TEST_1:
                    startHttpRequest_1();
                    break;
                case START_TEST_2:
                    startHttpRequest_2();
                    break;
                case END_TEST:
                    callBack.onNetHttpServiceStoped(result);
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
        mcontext = NetHttpService.this;
        callBack = NetDiagnosisActivity.getCallBack();
        isRunning = true;
        handler.sendEmptyMessage(START_TEST_1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning= false;
    }

    /**
     *中心平台网络连通性测试
     * 机顶盒分别向主备认证地址的约定URL发起一次HTTP请求
     * 只要收到一台认证服务器返回的HTTP 200 OK响应则认为测试成功，否则返回测试失败。
     * 无论该测试项测试结果为成功还是失败，继续后续测试。
     * 主认证地址：http://eds1.unicomgd.com:8082/EDS/jsp/AuthenticationURL
     * 备认证地址：http://eds2.unicomgd.com:8082/EDS/jsp/AuthenticationURL
     */
    private static final String HTTP_REQUEST_URL_1 ="http://eds1.unicomgd.com:8082/EDS/jsp/AuthenticationURL";
    private static final String HTTP_REQUEST_URL_2 ="http://eds2.unicomgd.com:8082/EDS/jsp/AuthenticationURL";
    private void  startHttpRequest_1(){
        HttpSleep1 thread =new HttpSleep1();
        thread.start();
        NetDiagnosisHelper.getInstance(mcontext).startHttpRequest(HTTP_REQUEST_URL_1);
    }
    private void  startHttpRequest_2(){
        HttpSleep2 thread =new HttpSleep2();
        thread.start();
        NetDiagnosisHelper.getInstance(mcontext).startHttpRequest(HTTP_REQUEST_URL_2);
    }
    class HttpSleep1 extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = NetDiagnosisHelper.HTTP_RESULT;
            if(result){
                handler.sendEmptyMessage(END_TEST);
            }else{
                handler.sendEmptyMessage(START_TEST_2);
            }
        }
    }
    class HttpSleep2 extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = NetDiagnosisHelper.HTTP_RESULT;
            handler.sendEmptyMessage(END_TEST);
        }
    }


}
