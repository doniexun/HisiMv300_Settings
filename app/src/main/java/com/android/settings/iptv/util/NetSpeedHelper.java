package com.android.settings.iptv.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * @author libeibei
 * Created by Administrator on 2017/12/19 0019.
 */

public class NetSpeedHelper {
    String TAG = "NetSpeedHelper";
    Context mcontext;
    byte[] fileData = null;
    private NetSpeedThread mThread;
    private DownLoadThread mDownloadThread;
    private NetWorkSpeedInfo netWorkSpeedInfo;
    private static NetSpeedHelper INSTANCE ;
    private static final int UPDATE_SPEED = 0;
    private static final int stop_UPDATE_SPEED = 1;
    private static final String ACTION ="com.changhong.netspeed";
    private static final String ningxia_speed_url = "http://211.138.62.243:6060/000000000000/test04/test.ts";
    @SuppressLint("HandlerLeak")
    private Handler handler =new Handler(){

        long tem = 0;
        long falg = 0;
        long numberTotal = 0;
        List<Long> list = new ArrayList<Long>();
        Intent intent =new Intent(ACTION);
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_SPEED:
                    tem = netWorkSpeedInfo.speed / 1024;
                    list.add(tem);
                    Log.i(TAG, "UPDATE_SPEED tem****" + tem);
                    for (Long numberLong : list) {
                        numberTotal += numberLong;
                    }
                    falg = numberTotal / list.size();
                    numberTotal = 0;

                    intent.putExtra("speed",tem);
                    intent.putExtra("average",falg);
                    mcontext.sendBroadcast(intent);
                    break;
                case stop_UPDATE_SPEED:
                    tem = netWorkSpeedInfo.speed / 1024;
                    list.add(tem);
                    Log.i(TAG, " stop_UPDATE_SPEED tem back 0" );
                    for (Long numberLong : list) {
                        numberTotal += numberLong;
                    }
                    falg = numberTotal / list.size();
                    numberTotal = 0;
                    //stop 停止之后，发消息更新仪表盘下面的textview为0
                    intent.putExtra("speed",Long.valueOf(0));
                    intent.putExtra("average",falg);
                    mcontext.sendBroadcast(intent);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 单例模式获取对象
     **/
    public static NetSpeedHelper getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new NetSpeedHelper(context);
        }
        return INSTANCE;
    }

    private NetSpeedHelper(Context context){
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context){
        mcontext = context;
        netWorkSpeedInfo = new NetWorkSpeedInfo();
    }

    public void start (){
        if(mThread == null && mDownloadThread ==null){
            mThread = new NetSpeedThread();
            mDownloadThread = new DownLoadThread();
            mThread.start();
            mDownloadThread.start();
        }
    }

    public void stop(){
        if(mThread!=null){
            mThread.stopThread();
            mThread.interrupt();
            mThread = null;
        }
        if(mDownloadThread != null){
            mDownloadThread.stopThread();
            mDownloadThread.interrupt();
            mDownloadThread = null;
        }
        try {
            sleep(1000);//延时1s再发消息
            handler.sendEmptyMessage(stop_UPDATE_SPEED);
            Log.i(TAG, " sendEmptyMessage  stop_UPDATE_SPEED");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        INSTANCE = null;
    }

    /**
     * 线程一，用于下载测速服务器上的指定文件；
     */
    private class DownLoadThread extends Thread {
        private boolean isRunning = true;
        private String speedurl = null;
        @Override
        public void run() {
            super.run();
            speedurl = ningxia_speed_url;
            Log.d("TAG", "downloadFileFromURL: speedurl = " + speedurl);
            downloadFileFromURL(speedurl,netWorkSpeedInfo);
        }


        public void stopThread(){
            isRunning = false;
        }

        /**
         * DownLoadThread线程内部的函数，用于下载文件
         * @param testUrl
         * @param netWorkSpeedInfo
         * @return
         */
        private void downloadFileFromURL(String testUrl, NetWorkSpeedInfo netWorkSpeedInfo) {
            int currentByte = 0;
            int fileLength = 0;
            long startTime = 0;
            long intervalTime = 0;

            URL urlx = null;
            URLConnection con = null;
            InputStream stream = null;
            try {
                Log.d(TAG, testUrl);
                urlx = new URL(testUrl);
                con = urlx.openConnection();
                con.setConnectTimeout(20000);
                con.setReadTimeout(20000);
                fileLength = con.getContentLength();
                stream = new BufferedInputStream(con.getInputStream());
                Log.d(TAG, String.valueOf(fileLength));
                netWorkSpeedInfo.totalBytes = fileLength;
                startTime = System.currentTimeMillis();
                while ((currentByte = stream.read()) != -1 && isRunning) {
                    netWorkSpeedInfo.hadFinishedBytes++;
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime == 0) {
                        netWorkSpeedInfo.speed = 1000;
                    } else {
                        netWorkSpeedInfo.speed = (netWorkSpeedInfo.hadFinishedBytes / intervalTime) * 100000;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + "");
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

            }
        }

    }


    /**
     * 线程二，用于计时,并以每个周期更新UI
     */
    private class NetSpeedThread extends Thread {

        private boolean isRunning = true;

        @Override
        public void run() {
            while (netWorkSpeedInfo.hadFinishedBytes < netWorkSpeedInfo.totalBytes && isRunning) {
                try {
                    //每250毫秒，更新一次测速仪表盘
                    sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(UPDATE_SPEED);
            }
            if (netWorkSpeedInfo.hadFinishedBytes == netWorkSpeedInfo.totalBytes) {
                handler.sendEmptyMessage(UPDATE_SPEED);
                netWorkSpeedInfo.hadFinishedBytes = 0;
            }
            super.run();
        }

        public void stopThread(){
            isRunning = false;
        }

    }




    public class NetWorkSpeedInfo {
        /** Network speed */
        public long speed = 0;
        /** Had finished bytes */
        public long hadFinishedBytes = 0;
        /** Total bytes of a file, default is 1024 bytes,1K */
        public long totalBytes = 1024;
        /** The net work type, 3G or GSM and so on */
        public int networkType = 0;
        /** Down load the file percent 0----100 */
        public int downloadPercent = 0;
    }

}
