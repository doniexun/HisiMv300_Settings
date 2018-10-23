package com.android.settings.iptv.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.SntpClient;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * @author libeibei
 * Created by Administrator on 2017/12/20 0020.
 */

public class NetDiagnosisHelper {

    private Context mcontext;
    private WifiManager wifiManager;
    private ConnectivityManager connetManager;
    private EthernetManager ethernetManager;
    private static NetDiagnosisHelper INSTANCE;
    public String WifiIp = "";
    private static final int TYPE_NONE = ConnectivityManager.TYPE_NONE;
    private static final int TYPE_WIFI = ConnectivityManager.TYPE_WIFI;
    private static final int TYPE_ETHERNET = ConnectivityManager.TYPE_ETHERNET;
    private static final int HTTP_CODE = 200;
    public static boolean HTTP_RESULT = true;
    public static boolean NTP_RESULT = true;
    public static boolean DNS_RESULT = true;
    private Loger loger = new Loger(NetDiagnosisHelper.class);

    /**
     * 单例模式获取测试工具实例
     *
     * @param context
     */
    private NetDiagnosisHelper(Context context) {
        mcontext = context;
        init(context);
    }

    public static NetDiagnosisHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new NetDiagnosisHelper(context);
        }
        return INSTANCE;
    }

    private void init(Context context) {
        mcontext = context;
        connetManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ethernetManager = (EthernetManager) mcontext.getSystemService(Context.ETHERNET_SERVICE);
    }

    /**
     * 检查网络是否畅通
     *
     * @return
     */
    public boolean isNetWorkConnected() {

        NetworkInfo networkInfo = connetManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    public int getNetType() {
        int type = TYPE_NONE;
        NetworkInfo networkInfo = connetManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            type = networkInfo.getType();
            loger.i( "NetDiagnosis .. NET Type = " + type);
        }
        return type;
    }

    private String getEthernetType() {
        String ethMode = null;
        ethMode = ethernetManager.getEthernetMode();
        return ethMode != null ? ethMode : "null";
    }

    private boolean checkWifiIp() {
        int wifiIP = getWifiIp();
        if (wifiIP == 0) {
            return false;
        } else {
            String ipAddress = intToIp(wifiIP);
            WifiIp = ipAddress;
            loger.i( "Wifi IP Address = " + ipAddress);
            return true;
        }
    }

    private boolean checkEtherIp() {

        String ethType = getEthernetType();
        loger.i( "Ethernet connect TYPE = " + ethType);
        DhcpInfo dhcpInfo = ethernetManager.getDhcpInfo();
        //静态Ip、DHCP和IPOE情况下的IP地址获取
        String ip = ipInt2Str(dhcpInfo.ipAddress);
        //PPPOE情况下的IP地址获取
        String pppIp = SystemProperties.get("pppoe.ppp0.ipaddress");
//        String mask = ipInt2Str(dhcpInfo.netmask);
//        String gateway = ipInt2Str(dhcpInfo.gateway);
//        String dns = ipInt2Str(dhcpInfo.dns1);

        loger.i( "EtherIP = " + ip + "PppoeIP =" + pppIp);

        if (!TextUtils.isEmpty(ip) || !TextUtils.isEmpty(pppIp)) {
            return true;
        }
        return false;
    }

    /**
     * 把int转换为string
     *
     * @param ip
     * @return
     */
    public String ipInt2Str(int ip) {
        int first = ip >> 24;
        if (first < 0) {
            first = 0xff + first + 1;
        }
        int second = ip >> 16 & 0xff;
        int third = ip >> 8 & 0xff;
        int four = ip & 0xff;

        StringBuffer buf = new StringBuffer();

        buf.append(four).append(".").append(third).append(".")
                .append(second).append(".").append(first);
        return buf.toString();
    }

    /**
     * 检查是否能获取IP地址
     * IP地址是否有冲突
     *
     * @return
     */
    public boolean checkIP() {
        int netType = getNetType();
        //WIFI 连接下，获取IP
        if (netType == TYPE_WIFI) {
            return checkWifiIp();
        }
        //有线连接(DHCP 或者 静态IP)
        if (netType == TYPE_ETHERNET) {
            return checkEtherIp();
        }
        return false;
    }

    private int getWifiIp() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int wifiIP = wifiInfo.getIpAddress();
        return wifiIP;
    }

    private String getWifiMac() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifiMAC = wifiInfo.getMacAddress();
        return wifiMAC;
    }

    /**
     * int IP address change to String IP address
     *
     * @param ipInt
     * @return
     */
    private String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 中心平台网络连通性测试
     * 开始执行HTTP请求
     *
     * @param URL
     * @return
     */
    public boolean startHttpRequest(final String URL) {
        HttpThread thread = new HttpThread(URL);
        thread.start();
        loger.i( "HTTP_RESULT = " + HTTP_RESULT);
        return HTTP_RESULT;
    }

    /**
     *
     */
    public void startNtpRequest(final String address) {

        NtpThread thread = new NtpThread(address);
        thread.start();
    }


    public void startDnsRequest(final String address) {
        DnsThread thread = new DnsThread(address);
        thread.start();
    }

    /**
     * 线程二：用以执行中心平台网络连通性测试
     * 机顶盒向认证地址的约定URL发起一次HTTP请求
     * 只要收到返回的HTTP 200 OK响应则认为测试成功
     */
    class HttpThread extends Thread {
        final String URL;

        public HttpThread(final String URL) {
            this.URL = URL;
        }

        @Override
        public void run() {
            super.run();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(URL);
            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                int response = httpResponse.getStatusLine().getStatusCode();
                if (response == HTTP_CODE) {
                    HTTP_RESULT = true;
                    loger.i( "Http response = " + response);
                    loger.i( "HTTP_RESULT = " + HTTP_RESULT);
                }
            } catch (IOException ioe) {
                loger.i( ioe.toString());
            }
        }
    }

    /**
     * 线程三：NTP服务器连通性测试
     */
    class NtpThread extends Thread {
        final String address;

        public NtpThread(final String address) {
            this.address = address;
        }

        @Override
        public void run() {
            super.run();
            SntpClient client = new SntpClient();
            NTP_RESULT = client.requestTime(address, 1000);
            NTP_RESULT = true;
            loger.i( "NTP_RESULT = " + NTP_RESULT);
        }
    }

    /**
     * 线程七：DNS服务器请求解析域名：
     */
    class DnsThread extends Thread {
        final String address;

        public DnsThread(final String address) {
            this.address = address;
        }

        @Override
        public void run() {
            super.run();
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(address);
            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpGet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int a = httpResponse.getStatusLine().getStatusCode();
            loger.i( "Dns Response " + a);
            if (a == 200) {
                DNS_RESULT = true;
            }
        }
    }

}
