package com.android.settings.iptv.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.settings.iptv.nettest.NetPingFragment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author libeibei
 * Created by Libeibei on 2018/2/1 0001.
 * Ping测试工具类
 */

public class NetTracePingHelper {

    private NetPingFragment.PingCallBack callBack;
    private int result = 0;
    private List resultArray;
    private static ConnectivityManager connectivityManager;
    private Loger loger = new Loger(NetTracePingHelper.class);

    public NetTracePingHelper() {
        callBack = NetPingFragment.getCallback();
    }

    public void startPing(String cmd, String address) {

        final String command = cmd + " " + address;
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    resultArray = new ArrayList<String>();
                    Process process = Runtime.getRuntime().exec(command);
                    BufferedReader mReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
                    String line = "";
                    while ((line = mReader.readLine()) != null) {
                        loger.e( "NetTracePingHelper : line = " + line);
                        callBack.onShowPingLine(line);
                        resultArray.add(line);
                    }
                    result = process.waitFor();
                    callBack.onStopPing(result, resultArray);
                } catch (Exception a) {
                    loger.e(a.toString());
                }
            }
        }.start();

    }

    /**
     * 检查网络是否畅通
     * @return
     */
    public static boolean isNetWorkConnected(Context context){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null){
            return networkInfo.isAvailable();
        }
        return false;
    }


}
