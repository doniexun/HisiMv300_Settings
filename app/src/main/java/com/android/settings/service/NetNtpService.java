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
import com.android.settings.iptv.util.Loger;

/**
 * @author libeibei
 * Created by libeibei on 2018/01/21 0021.
 */

public class NetNtpService extends Service {
    private Context mcontext;
    private Loger loger = new Loger(NetNtpService.class);
    private NetDiagnosisActivity.CallBack callBack;
    public static boolean isRunning = false;
    private static int test_frequency =0;
    private static final int START_TEST_1= 0;
    private static final int START_TEST_2= 1;
    private static final int END_TEST= 2;
    private boolean result =false;
    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_TEST_1:
                    startNtpRequest_1();
                    break;
                case START_TEST_2:
                    startNtpRequest_2();
                    break;
                case END_TEST:
                    callBack.onNetNtpServiceStoped(result);
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
        test_frequency = 0;
        mcontext = NetNtpService.this;
        callBack = NetDiagnosisActivity.getCallBack();
        isRunning = true;
        handler.sendEmptyMessage(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    /**
     *NTP联通性测试
     * 宁夏移动，时间服务器地址：
     *  100.100.0.134
     * 采用NTP协议请求获取时间(分别向各NTP服务器发出三次请求)
     * 如果有一个返回结果就算解析成功，返回测试成功，否则返回测试失败。
     * 无论该测试项测试结果为成功还是失败，继续后续测试。
     */
    //private static final String NTP_HOST_1 = "120.82.6.6";
    //private static final String NTP_HOST_2 = "120.82.7.7";
    private static final String NTP_HOST_1 = "100.100.0.134";
    private static final String NTP_HOST_2 = "100.100.0.134";

    private void  startNtpRequest_1(){
        NtpSleep thre = new NtpSleep();
        thre.start();
        NetDiagnosisHelper.getInstance(mcontext).startNtpRequest(NTP_HOST_1);
    }
    private void  startNtpRequest_2(){
        NtpSleep_2 thre = new NtpSleep_2();
        thre.start();
        NetDiagnosisHelper.getInstance(mcontext).startNtpRequest(NTP_HOST_2);
    }

    class NtpSleep extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                test_frequency++;
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = NetDiagnosisHelper.NTP_RESULT;
            loger.i("11111   test_frequency = "+test_frequency+" ,result ="+result);
            if(test_frequency >= 6){
                handler.sendEmptyMessage(END_TEST);
            }else{
                if(result){
                    handler.sendEmptyMessage(END_TEST);
                }else{
                    handler.sendEmptyMessage(START_TEST_2);
                }
            }
        }
    }

    class NtpSleep_2 extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                test_frequency++;
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = NetDiagnosisHelper.NTP_RESULT;
            loger.i("22222   test_frequency = "+test_frequency+" ,result ="+result);
            if(test_frequency>=6){
                handler.sendEmptyMessage(END_TEST);
            }else{
                if(result){
                    handler.sendEmptyMessage(END_TEST);
                }else{
                    handler.sendEmptyMessage(START_TEST_1);
                }
            }
        }
    }
}
