package com.android.settings.iptv.lan;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ethernet.EthernetManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.android.settings.R;
import com.android.settings.iptv.util.VchCommonToastDialog;

import android.view.View.OnFocusChangeListener;

@SuppressLint("ValidFragment")
public class DHCPFragment extends Fragment {

    private static final String TAG = "DHCP";
    private View root;
    private static Context mContext;
    private LinearLayout mIPv4Setting;
    private RadioGroup mIPType;
    private RadioButton mNativeIPv4;
    private Button mGetIP;
    private static TextView mIPv4Adderss;
    private static TextView mIPv4Mask;
    private static TextView mIPv4DefaultNet;
    private static TextView mIPv4Dns;
    static VchCommonToastDialog toastDialog = null;
    private EthernetManager mEthernetManager;
    private NetworkBroadcast mNetworkBroadcast;

    public DHCPFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.lan_dhcp, null);
        initView();
        toastDialog = new VchCommonToastDialog(mContext);
        toastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        toastDialog.setDuration(10);
        toastDialog.getWindow().setType(2003);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mNetworkBroadcast != null) {
            mContext.unregisterReceiver(mNetworkBroadcast);
        }
    }

    private void registerReceiver() {
        mNetworkBroadcast = new NetworkBroadcast();
        IntentFilter filter = new IntentFilter();
        //添加动作，监听网络
        filter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGE");
        mContext.registerReceiver(mNetworkBroadcast, filter);
    }

    private void initView() {

        mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        mIPType = (RadioGroup) root.findViewById(R.id.nativeiptype);
        mNativeIPv4 = (RadioButton) root.findViewById(R.id.nativeipv4);
        mIPv4Setting = (LinearLayout) root.findViewById(R.id.nativeipv4setting);
        mIPv4Adderss = (TextView) root.findViewById(R.id.nativeipv4address);
        mIPv4Mask = (TextView) root.findViewById(R.id.nativeipv4mask);
        mIPv4DefaultNet = (TextView) root.findViewById(R.id.nativeipv4defaultnet);
        mIPv4Dns = (TextView) root.findViewById(R.id.nativeipv4dns);
        mGetIP = (Button) root.findViewById(R.id.nativegetIP);
        if (mNativeIPv4.isChecked()) {
            mIPv4Setting.setVisibility(View.VISIBLE);
        } else {
            mIPv4Setting.setVisibility(View.GONE);
        }
        if (mEthernetManager.getEthernetMode().equals(EthernetManager.ETHERNET_CONNECT_MODE_DHCP)
                && mEthernetManager.getDhcpOption60State() != EthernetManager.OPTION60_STATE_ENABLED
                && SystemProperties.get("dhcp.eth0.result").equals("ok")) {
            showAddress();
        }

        mIPType.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                switch (checkedId) {
                    case R.id.nativeipv4:
                        mIPv4Setting.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        });

        mGetIP.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startDHCP();
            }
        });
    }

    private void startDHCP() {
        toastDialog.setMessage(R.string.change_to_dhcp);
        toastDialog.setDuration(10);
        toastDialog.show();
        mEthernetManager.enableEthernet(true);
        mEthernetManager.setEthernetEnabled(false);
        mEthernetManager.setDhcpOption60(false, null, null);
        mEthernetManager.setDhcpOption125(false, null);
        mEthernetManager.setEthernetMode(EthernetManager.ETHERNET_CONNECT_MODE_DHCP, null);
        mEthernetManager.setEthernetEnabled(true);
    }

    class NetworkBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(
                    EthernetManager.ETHERNET_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(
                        EthernetManager.EXTRA_ETHERNET_STATE, -1);

                Log.i(TAG, "receive state is  " + state);
                getEthNetInfo(state);
            }
        }
    }

    private void getEthNetInfo(int state) {
        switch (state) {
            case EthernetManager.EVENT_DHCP_CONNECT_SUCCESSED:
                if (isAdded()&&mEthernetManager.getDhcpOption60State() !=EthernetManager.OPTION60_STATE_ENABLED) {
                    Log.i(TAG, "getEthNetInfo: the DHCP is connect");
                    toastDialog.setMessage(R.string.DHCPsuccess);
                    toastDialog.setDuration(1);
                    toastDialog.show();
                    showAddress();
                }
                break;
            case EthernetManager.EVENT_PHY_LINK_DOWN:
                break;
            case EthernetManager.EVENT_DHCP_CONNECT_FAILED:
                if (isAdded()) {
                    toastDialog.setMessage(R.string.DHCPfailed);
                    toastDialog.setDuration(1);
                    toastDialog.show();
                    cleanAddress();
                }
                break;
        }

    }

    private static void showAddress() {
        String ipaddr = SystemProperties.get("dhcp.eth0.ipaddress");
        String mask = SystemProperties.get("dhcp.eth0.mask");
        String gateway = SystemProperties.get("dhcp.eth0.gateway");
        String dns = SystemProperties.get("dhcp.eth0.dns1");

        if (!ipaddr.isEmpty()) {
            mIPv4Adderss.setText(ipaddr);
        }

        if (!mask.isEmpty()) {
            mIPv4Mask.setText(mask);
        }
        if (!gateway.isEmpty()) {
            mIPv4DefaultNet.setText(gateway);
        }
        if (!dns.isEmpty()) {
            mIPv4Dns.setText(dns);
        }
    }

    private void cleanAddress() {
        mIPv4Adderss.setText("");
        mIPv4Mask.setText("");
        mIPv4DefaultNet.setText("");
        mIPv4Dns.setText("");
    }
}
