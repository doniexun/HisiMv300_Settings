package com.android.settings.iptv.netinfo;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetManager;
import android.net.pppoe.PppoeManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.iptv.util.Loger;

/**
 * @author libeibei
 * Created by libeibei on 2018/1/18 0018.
 */

@SuppressLint("ValidFragment")
public class NetInfoFragment extends Fragment {

    private Context mcontext;
    private View root;
    private TextView text_netMode, text_ip, text_mask, text_gateway, text_dns;
    private static final int INIT_VIEW = 1001;
    private static final int SHOW_NETINFO = 1002;
    private static int mNetMode, mCableMode;
    private ConnectivityManager connectivityManager;
    private EthernetManager ethernetManager;
    private WifiManager wifiManager;
    private static final String nulladdress = "00.00.00.00";
    private Loger loger = new Loger(NetInfoFragment.class);

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_VIEW:
                    initView();
                    break;

                case SHOW_NETINFO:
                    showNetInfo();
                    break;

                default:
                    break;
            }

        }
    };

    public NetInfoFragment(Context context) {
        mcontext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_netinfo, null);
        handler.sendEmptyMessage(INIT_VIEW);
        return root;
    }

    private void initView() {
        connectivityManager = (ConnectivityManager) mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        ethernetManager = (EthernetManager) mcontext.getSystemService(Context.ETHERNET_SERVICE);
        wifiManager = (WifiManager) mcontext.getSystemService(Context.WIFI_SERVICE);
        text_netMode = (TextView) root.findViewById(R.id.net_mode);
        text_ip = (TextView) root.findViewById(R.id.ip_address);
        text_mask = (TextView) root.findViewById(R.id.mask_address);
        text_gateway = (TextView) root.findViewById(R.id.gateway_address);
        text_dns = (TextView) root.findViewById(R.id.dns_address);
        handler.sendEmptyMessage(SHOW_NETINFO);
    }

    /**
     * 显示当前设备的网络信息
     */
    private void showNetInfo() {
        String mode, ip, mask, gateway, dns;
        int netMode = getNetMode();
        switch (netMode) {
            //无网络连接
            case 0:
                mode = mcontext.getString(R.string.network_no_net);
                ip = mask = gateway = dns = nulladdress;
                setInfo(mode, ip, mask, gateway, dns);
                break;
            //有线网络
            case 1:
                mode = mcontext.getString(R.string.netmode_ethernet);
                if (ethernetManager.getEthernetMode().equals(EthernetManager.ETHERNET_CONNECT_MODE_MANUAL)) {
                    mode = mode.concat(getString(R.string.lan));
                    DhcpInfo dhcpInfo = ethernetManager.getDhcpInfo();
                    ip = ipInt2Str(dhcpInfo.ipAddress);
                    mask = ipInt2Str(dhcpInfo.netmask);
                    gateway = ipInt2Str(dhcpInfo.gateway);
                    dns = ipInt2Str(dhcpInfo.dns1);
                } else if (ethernetManager.getEthernetMode().equals(EthernetManager.ETHERNET_CONNECT_MODE_PPPOE)) {
                    mode = mode.concat(getString(R.string.PPPoE));
                    ip = SystemProperties.get("pppoe.ppp0.ipaddress", nulladdress);
                    mask = SystemProperties.get("pppoe.ppp0.mask", nulladdress);
                    gateway = SystemProperties.get("pppoe.ppp0.gateway", nulladdress);
                    dns = SystemProperties.get("pppoe.ppp0.dns1", nulladdress);
                } else {
                    if (ethernetManager.getDhcpOption60State() == EthernetManager.OPTION60_STATE_ENABLED) {
                        mode = mode.concat(getString(R.string.nativeIPOE));
                    } else {
                        mode = mode.concat(getString(R.string.nativeDHCP));
                    }
                    ip = SystemProperties.get("dhcp.eth0.ipaddress", nulladdress);
                    mask = SystemProperties.get("dhcp.eth0.mask", nulladdress);
                    gateway = SystemProperties.get("dhcp.eth0.gateway", nulladdress);
                    dns = SystemProperties.get("dhcp.eth0.dns1", nulladdress);
                }
                setInfo(mode, ip, mask, gateway, dns);
                break;
            //无线网络
            case 2:
                mode = mcontext.getString(R.string.netmode_wifi);
                ip = SystemProperties.get("dhcp.wlan0.ipaddress", nulladdress);
                mask = SystemProperties.get("dhcp.wlan0.mask", nulladdress);
                gateway = SystemProperties.get("dhcp.wlan0.gateway", nulladdress);
                dns = SystemProperties.get("dhcp.wlan0.dns1", nulladdress);
                setInfo(mode, ip, mask, gateway, dns);
                break;
            default:
                break;
        }

    }

    /**
     * 把静态IP的int转换为string
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


    private void setInfo(String mode, String ip, String mask, String gateway, String dns) {
        text_netMode.setText(mode);
        text_ip.setText(ip);
        text_mask.setText(mask);
        text_gateway.setText(gateway);
        text_dns.setText(dns);
    }


    /**
     * 判断当前网络有没有联网
     * 并且判断是有线还是无线
     * <p>
     * 0:无网络连接
     * 1：有线网络
     * 2：无线网络
     *
     * @return
     */
    private int getNetMode() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null) {
            return 0;
        }
        boolean iscon = info.isAvailable();
        loger.e( "网络连接 =" + iscon + "，连接方式：" + info.getType() + " ," + info.getTypeName());
        if (!iscon) {
            return 0;
        }
        if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
            return 1;
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return 2;
        } else {
            return 0;
        }
    }


}
