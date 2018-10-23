package com.android.settings.iptv.lan;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.ethernet.EthernetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.iptv.util.VchCommonToastDialog;


/**
 * @author libeibei
 * Created by Administrator on 2017/12/25 0025.
 */

@SuppressLint("ValidFragment")
public class IPOEFragment extends Fragment implements View.OnClickListener {
    private final String TAG = "IPOE";
    private View root;
    private Context mcontext;
    private EditText username, password;
    private Button getIP;
    private String mUsername = "";
    private String mPassword = "";
    private static TextView mIPv4Adderss;
    private static TextView mIPv4Mask;
    private static TextView mIPv4DefaultNet;
    private final static int START_IPOE = 1;
    private static final int MESSAGE_UPDATEUI = 102;
    private EthernetManager mEthManager = null;
    static VchCommonToastDialog toastDialog = null;
    private NetworkBroadcast mNetworkBroadcast;

    public IPOEFragment(Context context) {
        mcontext = context;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_IPOE:
                    startIPOE();
                    break;
                case MESSAGE_UPDATEUI:
                    if (SystemProperties.get("dhcp.eth0.result").equals("ok")) {
                        toastDialog.setMessage(R.string.ipExist);
                        toastDialog.setDuration(1);
                        toastDialog.show();
                        showAddress();
                    } else {
                        toastDialog.setMessage(R.string.IPOEfailed);
                        toastDialog.setDuration(1);
                        toastDialog.show();
                        cleanAddress();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.lan_ipoe, null);
        mEthManager = (EthernetManager) mcontext.getSystemService(Context.ETHERNET_SERVICE);
        toastDialog = new VchCommonToastDialog(mcontext);
        toastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        toastDialog.getWindow().setType(2003);
        initView();
        return root;
    }

    private void registerReceiver() {
        mNetworkBroadcast = new NetworkBroadcast();
        IntentFilter filter = new IntentFilter();
        //添加动作，监听网络
        filter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGE");
        mcontext.registerReceiver(mNetworkBroadcast, filter);
    }

    private void initView() {
        username = (EditText) root.findViewById(R.id.ipoe_username);
        password = (EditText) root.findViewById(R.id.ipoe_password);
        getIP = (Button) root.findViewById(R.id.ipoe_getip);
        getIP.setOnClickListener(this);
        mIPv4Adderss = (TextView) root.findViewById(R.id.ipoe_ipv4address);
        mIPv4Mask = (TextView) root.findViewById(R.id.ipoe_ipv4mask);
        mIPv4DefaultNet = (TextView) root.findViewById(R.id.ipoe_ipv4defaultnet);
        //当前已经处于IPOE状态时，不用重新认证就可以显示ip地址
        if (mEthManager.getEthernetMode().equals(EthernetManager.ETHERNET_CONNECT_MODE_DHCP)
                && mEthManager.getDhcpOption60State() == EthernetManager.OPTION60_STATE_ENABLED) {
            showAddress();
        } else {
            cleanAddress();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mUsername = mEthManager.getDhcpOption60Login();
        mPassword = mEthManager.getDhcpOption60Password();
        Log.e(TAG, " mUsername =" + mUsername + ",mPassword =" + mPassword);
        if (TextUtils.isEmpty(mUsername) || mUsername.contains(" ")) {
            mUsername = getUsername();
        }
        if (TextUtils.isEmpty(mPassword) || mPassword.contains(" ")) {
            mPassword = getPassword();
        }
        username.setText(mUsername);
        password.setText(mPassword);
        registerReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mNetworkBroadcast != null) {
            mcontext.unregisterReceiver(mNetworkBroadcast);
        }
    }

    @Override
    public void onClick(View view) {
        if (getIP.equals(view)) {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();
            if (checkUserPassAllowed(user) && checkUserPassAllowed(pass)) {
                mUsername = user;
                mPassword = pass;
                handler.sendEmptyMessage(START_IPOE);
            } else {
                toastDialog.setMessage("用户名和密码不合法");
                toastDialog.setDuration(1);
                toastDialog.show();
            }
        }
    }

    private String getUsername() {
        return "cmcciptv@iptv.cmcc";
    }

    private String getPassword() {
        return "cmcc10086";
    }

    private void showAddress() {
        String ipaddr = SystemProperties.get("dhcp.eth0.ipaddress");
        String mask = SystemProperties.get("dhcp.eth0.mask");
        String gateway = SystemProperties.get("dhcp.eth0.gateway");

        if (!ipaddr.isEmpty()) {
            mIPv4Adderss.setText(ipaddr);
        }

        if (!mask.isEmpty()) {
            mIPv4Mask.setText(mask);
        }
        if (!gateway.isEmpty()) {
            mIPv4DefaultNet.setText(gateway);
        }
    }

    private void cleanAddress() {
        mIPv4Adderss.setText("");
        mIPv4Mask.setText("");
        mIPv4DefaultNet.setText("");
    }

    private void startIPOE() {
        toastDialog.setMessage(R.string.change_to_ipoe);
        toastDialog.setDuration(10);
        toastDialog.show();

        mEthManager.setEthernetEnabled(false);
        Log.i(TAG, "onClick: start ipoeSetting ##########");
        mEthManager.setDhcpOption60(true, mUsername, mPassword);
        mEthManager.setDhcpOption125(false, "");
        Log.i(TAG, "onClick: the name is " + mUsername + " And the psd is " + mPassword);
        mEthManager.setEthernetMode(EthernetManager.ETHERNET_CONNECT_MODE_DHCP, null);
        mEthManager.setEthernetEnabled(true);
    }


    private boolean checkUserPassAllowed(String string) {
        if (null == string || "".equals(string)) {
            return false;
        } else if (string.contains(" ")) {
            return false;
        } else {
            return true;
        }
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
                if (isAdded() && mEthManager.getDhcpOption60State() == EthernetManager.OPTION60_STATE_ENABLED) {
                    Log.i(TAG, "getEthNetInfo: the ipoe is connect");
                    toastDialog.setMessage(R.string.IPOEsuccess);
                    toastDialog.setDuration(1);
                    toastDialog.show();
                    showAddress();
                }
                break;
            case EthernetManager.EVENT_PHY_LINK_DOWN:

                break;
            case EthernetManager.EVENT_DHCP_CONNECT_FAILED:
                if (isAdded()) {
                    toastDialog.setMessage(R.string.IPOEfailed);
                    toastDialog.setDuration(1);
                    toastDialog.show();
                    cleanAddress();
                }
                break;
        }

    }

}
