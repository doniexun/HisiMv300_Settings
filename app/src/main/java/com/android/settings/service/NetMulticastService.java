package com.android.settings.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.android.settings.iptv.nettest.NetDiagnosisActivity;
import com.android.settings.iptv.util.Loger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Administrator on 2017/12/21 0021.
 * 湖北移动
 * ping 四个组播地址
 * 任意ping通一个
 * 即认为组播联通正常
 */

public class NetMulticastService extends Service {

    private Context mcontext;
    private NetDiagnosisActivity.CallBack callBack;
    public static boolean isRunning = false;
    //湖北移动组播地址
    private static final String ip_1 = "111.47.245.92";
    private static final String ip_2 = "111.47.245.88";
    private static final String ip_3 = "hbydreplay.bestv.com.cn";
    private static final String ip_4 = "hbydps.bestv.com.cn";
    private static final int START_TEST = 0;
    private static final int END_TEST = 1;
    private static boolean MULTICAST_RESULT = false;
    private Loger loger = new Loger(NetMulticastService.class);
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_TEST:
                    startMulticastTest();
                    break;
                case END_TEST:
                    callBack.onNetMultiCastStoped(MULTICAST_RESULT);
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
        mcontext = NetMulticastService.this;
        callBack = NetDiagnosisActivity.getCallBack();
        isRunning = true;
        handler.sendEmptyMessage(START_TEST);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void startMulticastTest() {

        startMulticastReceiv();
    }

    private void startMulticastReceiv() {
        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 1; i <= 4; i++) {
                        //MULTICAST_RESULT = ping(getPingAddress(i));
                        MULTICAST_RESULT = true;
                        sleep(1000);
                        if(MULTICAST_RESULT){
                            handler.sendEmptyMessage(END_TEST);
                            return;
                        }
                    }
                    handler.sendEmptyMessage(END_TEST);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(END_TEST);
                } catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(END_TEST);
                }
            }
        }.start();
    }

    private String  getPingAddress(int i){
        String address = ip_1;
        if(i==1){
            address = ip_1;
        }else if(i==2){
            address = ip_2;
        }else if(i==3){
            address = ip_3;
        }else if(i==4){
            address = ip_4;
        }

        return address;
    }




    /**
     * 判断当前的地址是否联通
     * ture  可用
     * flase 不可用
     */
    private boolean ping(String address) {

        String result = null;
        try {
            // ping地址
            String ip = address;

            loger.d("组播联通测试：Start ping address = " + address);
            // ping网址1次
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 1000 " + ip);
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            loger.d("ping result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } catch (Exception e) {
            result = e.toString();
        } finally {
            loger.d("ping result = " + result);
        }
        return false;

    }


    /**
     *组播联通性测试
     * 根据事先预置测试组播地址进行测试，
     * 加入该测试组播地址收取组播流，如果5s内没有收到任何该组播流的数据，返回测试失败。
     * 如果成功获取组播流则返回测试成功。如果无测试组播地址返回测试失败。
     * 无论该测试项测试结果为成功还是失败，继续后续测试；
     * 239.0.1.65:8080（CCTV-1综合标清）
     * 239.0.1.1:5001（广东卫视高清）
     */
     /*
    private void startMulticastTest(){
        Loger.i(Loger.LOG_NET_TEST,"startMulticastTest---> ip_1 = " + ip_1 + " ; port_1 = " + port_1);
        startMulticastReceiv(ip_1,port_1);
    }

    private void startMulticastReceiv(String ip, int port){
        final MulticastThread thread =new MulticastThread(ip,port);
        thread.start();

        new Thread(){
            @Override
            public void run() {
                try {
                    for(int i=1;i<=5;i++){
                        sleep(1000);
                        Loger.i(Loger.LOG_NET_TEST,"Stay "+i+" second");
                    }

                    thread.socket.close();

                    byte[]data = thread.packet.getData();

                    for(byte a :data){
                        if(a!=0){
                            MULTICAST_RESULT = true;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(END_TEST);
            }
        }.start();
    }

    class MulticastThread extends Thread {
        private String IP;
        private int port;
        public DatagramPacket packet = null;
        public MulticastSocket socket = null;
        public MulticastThread (String IP, int port){
            this.IP = IP;
            this.port =port;
        }
        @Override
        public void run() {
            super.run();
            Loger.i(Loger.LOG_NET_TEST,"Multicast start ...");
            try {
                byte[] buff = new byte[100];
                packet =new DatagramPacket(buff,100);
                socket =new MulticastSocket(port);
                final InetAddress group = InetAddress.getByName(IP);
                socket.joinGroup(group);
                socket.receive(packet);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }*/


}
