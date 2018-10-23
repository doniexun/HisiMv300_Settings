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


/**@author libeibei
 * Created by Administrator on 2017/12/21 0021.
 */

public class NetDnsService extends Service {
    private Context mcontext;
    private NetDiagnosisActivity.CallBack callBack;
    private static final int START_TEST_1= 0;
    private static final int START_TEST_2= 1;
    private static final int END_TEST= 2;
    private boolean result = false;
    public static boolean isRunning = false;
    private final String URL_1 = "http://eds1.unicomgd.com:8082/EDS/jsp/AuthenticationURL";
    private final String URL_2 = "http://eds2.unicomgd.com:8082/EDS/jsp/AuthenticationURL";
    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_TEST_1:
                    startDnsTest_1();
                    break;
                case START_TEST_2:
                    startDnsTest_2();
                    break;
                case END_TEST:
                    callBack.onNetDnsServiceStoped(result);
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
        mcontext = NetDnsService.this;
        callBack = NetDiagnosisActivity.getCallBack();
        isRunning = true;
        handler.sendEmptyMessage(START_TEST_1);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
    }

    /**
     *DNS联通性测试
     * 根据事先预置的测试域名进行测试，向DNS服务器请求域名解析；
     * 只要有一个返回结果就算解析成功，返回测试成功，否则返回测试失败。
     * 如果无测试域名返回测试失败。该测试项测试完成后测试结束。
     * http://eds1.unicomgd.com:8082/EDS/jsp/AuthenticationURL
     * http://eds2.unicomgd.com:8082/EDS/jsp/AuthenticationURL
     * 终端网管域名：http://tmc.unicomgd.com:37020/acs
     */
    private void startDnsTest_1(){
        DnsSleep1 thread = new DnsSleep1();
        thread.start();
        NetDiagnosisHelper.getInstance(mcontext).startDnsRequest(URL_1);


    }
    private void startDnsTest_2(){
        DnsSleep2 thread = new DnsSleep2();
        thread.start();
        NetDiagnosisHelper.getInstance(mcontext).startDnsRequest(URL_2);


    }
    class DnsSleep1 extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = NetDiagnosisHelper.DNS_RESULT;
            if(result){
                handler.sendEmptyMessage(END_TEST);
            }else{
                handler.sendEmptyMessage(START_TEST_2);
            }
        }
    }
    class DnsSleep2 extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = NetDiagnosisHelper.DNS_RESULT;
            handler.sendEmptyMessage(END_TEST);
        }
    }

}
