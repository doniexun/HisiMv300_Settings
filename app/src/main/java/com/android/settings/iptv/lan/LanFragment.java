package com.android.settings.iptv.lan;

import java.net.Inet4Address;
import java.net.InetAddress;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.NetworkUtils;
import android.net.ethernet.EthernetManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;

import com.android.settings.R;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.VchCommonToastDialog;

@SuppressLint("ValidFragment")
public class LanFragment extends Fragment {
    private String TAG = "Lan";
    private Context mContext;
    private View root;
    private LinearLayout mIPv4Setting;//, mIPv6Setting;
    private RadioGroup mIPType;
    private RadioButton mIPv4;//,mIpv6;
    private Button mSetting;
    private EditText mIPv4address_4, mIPv4address_3, mIPv4address_2, mIPv4address_1;                //IPv4  ip adderss
    private EditText mIPv4Mask_4, mIPv4Mask_3, mIPv4Mask_2, mIPv4Mask_1;                            //ipv4 mask
    private EditText mIPv4DefaultNet_4, mIPv4DefaultNet_3, mIPv4DefaultNet_2, mIPv4DefaultNet_1;    //ipv4 default-gateway
    private EditText mIPv4Dns_4, mIPv4Dns_3, mIPv4Dns_2, mIPv4Dns_1;                                //ipv4 DNS
    private static String ipv4_4, ipv4_3, ipv4_2, ipv4_1;
    private static String mask_4, mask_3, mask_2, mask_1;
    private static String gateway_4, gateway_3, gateway_2, gateway_1;
    private static String dns_4, dns_3, dns_2, dns_1;
    private String ip_4 = "", ip_3 = "", ip_2 = "", ip_1 = "";
    private String gate_4 = "", gate_3 = "", gate_2 = "", gate_1 = "";
    private String netmask_4 = "", netmask_3 = "", netmask_2 = "", netmask_1 = "";
    private String netdns_4 = "", netdns_3 = "", netdns_2 = "", netdns_1 = "";
    VchCommonToastDialog mToastDialog = null;
    private static String mIPAddress;
    private static String mGateWayAddress;
    private static String mMaskAddress;
    private static String mDNSAddress;

    private EthernetManager mEthernetManager;
    private LanReceiver lanReceiver;
    private Loger loger = new Loger(LanFragment.class);

    public LanFragment(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.lan_lan, null);
        initView();
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
        if(lanReceiver!=null){
            mContext.unregisterReceiver(lanReceiver);
        }
    }

    private void registerReceiver() {
        lanReceiver = new LanReceiver();
        IntentFilter filter = new IntentFilter();
        //添加动作，监听网络
        filter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGE");
        mContext.registerReceiver(lanReceiver, filter);
    }

    private void initView() {

        mEthernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        mToastDialog = new VchCommonToastDialog(getActivity());
        mToastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        mToastDialog.getWindow().setType(2003);
        mIPType = (RadioGroup) root.findViewById(R.id.iptype);
        mIPv4 = (RadioButton) root.findViewById(R.id.ipv4);
//		mIpv6 = (RadioButton) root.findViewById(R.id.ipv6);
        mIPv4.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

//		mIpv6.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//
//				if ((DialogFlag.mDialogFlag==false)&&hasFocus) {						
//					showDialog();
//				} 
//			}
//		});

        mIPv4Setting = (LinearLayout) root.findViewById(R.id.ipv4setting);
//		mIPv6Setting = (LinearLayout) root.findViewById(R.id.ipv6setting);
        if (mIPv4.isChecked()) {
//			mIPv6Setting.setVisibility(View.GONE);
            mIPv4Setting.setVisibility(View.VISIBLE);
        } else {
            mIPv4Setting.setVisibility(View.GONE);
//			mIPv6Setting.setVisibility(View.VISIBLE);
        }
        //ipv4  ip address
        mIPv4address_4 = (EditText) root.findViewById(R.id.ipv4_4);
        mIPv4address_3 = (EditText) root.findViewById(R.id.ipv4_3);
        mIPv4address_2 = (EditText) root.findViewById(R.id.ipv4_2);
        mIPv4address_1 = (EditText) root.findViewById(R.id.ipv4_1);
        //ipv4 mask
        mIPv4Mask_4 = (EditText) root.findViewById(R.id.mask_4);
        mIPv4Mask_3 = (EditText) root.findViewById(R.id.mask_3);
        mIPv4Mask_2 = (EditText) root.findViewById(R.id.mask_2);
        mIPv4Mask_1 = (EditText) root.findViewById(R.id.mask_1);
        //ipv4 default-gateway
        mIPv4DefaultNet_4 = (EditText) root.findViewById(R.id.defaultnet_4);
        mIPv4DefaultNet_3 = (EditText) root.findViewById(R.id.defaultnet_3);
        mIPv4DefaultNet_2 = (EditText) root.findViewById(R.id.defaultnet_2);
        mIPv4DefaultNet_1 = (EditText) root.findViewById(R.id.defaultnet_1);
        //ipv4 DNS
        mIPv4Dns_4 = (EditText) root.findViewById(R.id.dnsserver_4);
        mIPv4Dns_3 = (EditText) root.findViewById(R.id.dnsserver_3);
        mIPv4Dns_2 = (EditText) root.findViewById(R.id.dnsserver_2);
        mIPv4Dns_1 = (EditText) root.findViewById(R.id.dnsserver_1);
        mIPv4address_4.setOnKeyListener(onKeyListener);
        mIPv4address_3.setOnKeyListener(onKeyListener);
        mIPv4address_2.setOnKeyListener(onKeyListener);
        mIPv4address_1.setOnKeyListener(onKeyListener);
        mIPv4Mask_4.setOnKeyListener(onKeyListener);
        mIPv4Mask_3.setOnKeyListener(onKeyListener);
        mIPv4Mask_2.setOnKeyListener(onKeyListener);
        mIPv4Mask_1.setOnKeyListener(onKeyListener);
        mIPv4DefaultNet_4.setOnKeyListener(onKeyListener);
        mIPv4DefaultNet_3.setOnKeyListener(onKeyListener);
        mIPv4DefaultNet_2.setOnKeyListener(onKeyListener);
        mIPv4DefaultNet_1.setOnKeyListener(onKeyListener);
        mIPv4Dns_4.setOnKeyListener(onKeyListener);
        mIPv4Dns_3.setOnKeyListener(onKeyListener);
        mIPv4Dns_2.setOnKeyListener(onKeyListener);
        mIPv4Dns_1.setOnKeyListener(onKeyListener);
        mSetting = (Button) root.findViewById(R.id.lansubmit);

        if (mEthernetManager.getEthernetMode().equals(EthernetManager.ETHERNET_CONNECT_MODE_MANUAL) &&
                mEthernetManager.getEthernetState() == EthernetManager.ETHERNET_CONNECT_STATE_CONNECT) {
            mToastDialog.setMessage(R.string.LANsuccess);
            mToastDialog.show();
            showStaticIP();
        } else {
            initIp();
        }

        mIPType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                switch (checkedId) {
                    case R.id.ipv4:
//					mIPv6Setting.setVisibility(View.GONE);	
                        mIPv4Setting.setVisibility(View.VISIBLE);
                        break;
//				case R.id.ipv6:
//					mIPv4Setting.setVisibility(View.GONE);	
//					mIPv6Setting.setVisibility(View.VISIBLE);
//					break;
                    default:
                        break;
                }
            }
        });

        mSetting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ipv4_4 = mIPv4address_4.getText().toString();
                ipv4_3 = mIPv4address_3.getText().toString();
                ipv4_2 = mIPv4address_2.getText().toString();
                ipv4_1 = mIPv4address_1.getText().toString();

                mask_4 = mIPv4Mask_4.getText().toString();
                mask_3 = mIPv4Mask_3.getText().toString();
                mask_2 = mIPv4Mask_2.getText().toString();
                mask_1 = mIPv4Mask_1.getText().toString();

                gateway_4 = mIPv4DefaultNet_4.getText().toString();
                gateway_3 = mIPv4DefaultNet_3.getText().toString();
                gateway_2 = mIPv4DefaultNet_2.getText().toString();
                gateway_1 = mIPv4DefaultNet_1.getText().toString();

                dns_4 = mIPv4Dns_4.getText().toString();
                dns_3 = mIPv4Dns_3.getText().toString();
                dns_2 = mIPv4Dns_2.getText().toString();
                dns_1 = mIPv4Dns_1.getText().toString();

                if (mEthernetManager.getEthernetMode().equals(EthernetManager.ETHERNET_CONNECT_MODE_MANUAL)
                        && mEthernetManager.getEthernetState() == EthernetManager.ETHERNET_CONNECT_STATE_CONNECT
                        && ip_4.equals(ipv4_4) && ip_3.equals(ipv4_3)
                        && ip_2.equals(ipv4_2) && ip_1.equals(ipv4_1)
                        && gate_4.equals(gateway_4) && gate_3.equals(gateway_3)
                        && gate_2.equals(gateway_2) && gate_1.equals(gateway_1)
                        && netmask_4.equals(mask_4) && netmask_3.equals(mask_3)
                        && netmask_2.equals(mask_2) && netmask_1.equals(mask_1)
                        && netdns_4.equals(dns_4) && netdns_3.equals(dns_3)
                        && netdns_2.equals(dns_2) && netdns_1.equals(dns_1)) {

                    mToastDialog.setMessage(R.string.not_modify);
                    mToastDialog.show();
                    return;
                } else {
                    if (ipv4_4.isEmpty() || ipv4_3.isEmpty()
                            || ipv4_2.isEmpty() || ipv4_1.isEmpty()
                            || Integer.valueOf(ipv4_1) > 255
                            || Integer.valueOf(ipv4_2) > 255
                            || Integer.valueOf(ipv4_3) > 255
                            || Integer.valueOf(ipv4_4) > 255) {

                        mToastDialog.setMessage(R.string.ipaddr_error);
                        mToastDialog.show();
                        return;
                    } else {
                        mIPAddress = ipv4_4 + "." + ipv4_3 + "." + ipv4_2 + "." + ipv4_1;
                    }

                    if (mask_4.isEmpty() || mask_3.isEmpty()
                            || mask_2.isEmpty() || mask_1.isEmpty()
                            || Integer.valueOf(mask_4) > 255
                            || Integer.valueOf(mask_3) > 255
                            || Integer.valueOf(mask_2) > 255
                            || Integer.valueOf(mask_1) > 255
                            ) {
                        mToastDialog.setMessage(R.string.mask_error);
                        mToastDialog.show();
                        return;
                    } else {
                        mMaskAddress = mask_4 + "." + mask_3 + "." + mask_2 + "." + mask_1;
                    }

                    if (gateway_4.isEmpty() || gateway_3.isEmpty()
                            || gateway_2.isEmpty() || gateway_1.isEmpty()
                            || Integer.valueOf(gateway_4) > 255
                            || Integer.valueOf(gateway_3) > 255
                            || Integer.valueOf(gateway_2) > 255
                            || Integer.valueOf(gateway_1) > 255) {

                        mToastDialog.setMessage(R.string.gateway_error);
                        mToastDialog.show();
                        return;
                    } else {
                        mGateWayAddress = gateway_4 + "." + gateway_3 + "." + gateway_2 + "." + gateway_1;
                    }

                    if (dns_4.isEmpty() || dns_3.isEmpty()
                            || dns_2.isEmpty() || dns_1.isEmpty()
                            || Integer.valueOf(dns_4) > 255
                            || Integer.valueOf(dns_3) > 255
                            || Integer.valueOf(dns_2) > 255
                            || Integer.valueOf(dns_1) > 255) {

                        mToastDialog.setMessage(R.string.dns_error);
                        mToastDialog.show();
                        return;
                    } else {
                        mDNSAddress = dns_4 + "." + dns_3 + "." + dns_2 + "." + dns_1;
                    }
                    mToastDialog.setMessage(R.string.change_to_lan);
                    mToastDialog.setDuration(1);
                    mToastDialog.show();
                    startStatic();
                }
            }
        });

    }

    /**
     * 不是静态连接时
     * 显示默认的静态地址
     */
    private void initIp() {

        mIPv4address_4.setText("100");
        mIPv4address_3.setText("101");
        mIPv4address_2.setText("47");
        mIPv4address_1.setText("135");

        mIPv4Mask_4.setText("255");
        mIPv4Mask_3.setText("255");
        mIPv4Mask_2.setText("128");
        mIPv4Mask_1.setText("0");

        mIPv4DefaultNet_4.setText("100");
        mIPv4DefaultNet_3.setText("101");
        mIPv4DefaultNet_2.setText("0");
        mIPv4DefaultNet_1.setText("1");

        mIPv4Dns_4.setText("218");
        mIPv4Dns_3.setText("203");
        mIPv4Dns_2.setText("123");
        mIPv4Dns_1.setText("116");

    }

    class LanReceiver extends BroadcastReceiver {
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
            case EthernetManager.EVENT_STATIC_CONNECT_SUCCESSED:
                if (isAdded()) {
                    Log.i(TAG, "接收到静态网络连接成功的广播");
                    mToastDialog.setMessage(R.string.LANsuccess);
                    mToastDialog.setDuration(1);
                    mToastDialog.show();
                    showStaticIP();
                }
                break;
            case EthernetManager.EVENT_PHY_LINK_DOWN:
                Log.i(TAG, "接收到有限网络失败的广播");
                break;
            case EthernetManager.EVENT_STATIC_CONNECT_FAILED:
                if (isAdded()) {
                    Log.i(TAG, "接收到静态网络连接失败的广播");
                    mToastDialog.setMessage(R.string.LANfailed);
                    mToastDialog.setDuration(1);
                    mToastDialog.show();
                    initIp();
                }
                break;
        }

    }

    private OnKeyListener onKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub
            if (v instanceof EditText) {
                String text = ((EditText) v).getText().toString();
                Editable editable = ((EditText) v).getText();
                int index = ((EditText) v).getSelectionStart();
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    loger.i( "OnKeyListener-----keyCode = " + keyCode);
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        if (text.isEmpty() || index == 0) {

                        } else {
                            editable.delete(index - 1, index);
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_INFO) {

                        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInputFromWindow(v.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
                        return true;
                    }
                }

            }
            return false;
        }
    };

    private void showStaticIP() {
        // TODO Auto-generated method stub
        DhcpInfo dhcpInfo = mEthernetManager.getSavedEthernetIpInfo();
        String mIP = NetworkUtils.intToInetAddress(dhcpInfo.ipAddress).getHostAddress();
        if (mIP != null) {
            String ip[] = mIP.split("\\.");
            ip_4 = ip[0];
            ip_3 = ip[1];
            ip_2 = ip[2];
            ip_1 = ip[3];

        }
        if (mIPv4address_4 != null && mIPv4address_3 != null
                && mIPv4address_2 != null && mIPv4address_2 != null) {

            mIPv4address_4.setText(ip_4);
            mIPv4address_3.setText(ip_3);
            mIPv4address_2.setText(ip_2);
            mIPv4address_1.setText(ip_1);
        }

        String mGateWay = NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
        if (mGateWay != null) {
            String gatewat[] = mGateWay.split("\\.");
            gate_4 = gatewat[0];
            gate_3 = gatewat[1];
            gate_2 = gatewat[2];
            gate_1 = gatewat[3];
        }
        if (mIPv4DefaultNet_4 != null && mIPv4DefaultNet_3 != null
                && mIPv4DefaultNet_2 != null && mIPv4DefaultNet_1 != null) {

            mIPv4DefaultNet_4.setText(gate_4);
            mIPv4DefaultNet_3.setText(gate_3);
            mIPv4DefaultNet_2.setText(gate_2);
            mIPv4DefaultNet_1.setText(gate_1);
        }

        String mMask = NetworkUtils.intToInetAddress(dhcpInfo.netmask).getHostAddress();
        if (mMask != null) {
            String mask[] = mMask.split("\\.");
            netmask_4 = mask[0];
            netmask_3 = mask[1];
            netmask_2 = mask[2];
            netmask_1 = mask[3];
        }
        if (mIPv4DefaultNet_4 != null && mIPv4DefaultNet_3 != null
                && mIPv4DefaultNet_2 != null && mIPv4DefaultNet_1 != null) {

            mIPv4Mask_4.setText(netmask_4);
            mIPv4Mask_3.setText(netmask_3);
            mIPv4Mask_2.setText(netmask_2);
            mIPv4Mask_1.setText(netmask_1);
        }

        String mDns = NetworkUtils.intToInetAddress(dhcpInfo.dns1).getHostAddress();
        if (mDns != null) {
            String dns[] = mDns.split("\\.");
            netdns_4 = dns[0];
            netdns_3 = dns[1];
            netdns_2 = dns[2];
            netdns_1 = dns[3];
        }
        if (mIPv4DefaultNet_4 != null && mIPv4DefaultNet_3 != null
                && mIPv4DefaultNet_2 != null && mIPv4DefaultNet_1 != null) {

            mIPv4Dns_4.setText(netdns_4);
            mIPv4Dns_3.setText(netdns_3);
            mIPv4Dns_2.setText(netdns_2);
            mIPv4Dns_1.setText(netdns_1);
        }
    }

    private void startStatic() {
        // TODO Auto-generated method stub
        mEthernetManager.enableEthernet(true);
        mEthernetManager.setEthernetEnabled(false);
        Log.i(TAG, ">>>>>----- ipaddress = " + mIPAddress);
        InetAddress ipaddr = NetworkUtils.numericToInetAddress(mIPAddress);
        InetAddress getwayaddr = NetworkUtils.numericToInetAddress(mGateWayAddress);
        InetAddress inetmask = NetworkUtils.numericToInetAddress(mMaskAddress);
        InetAddress idns1 = NetworkUtils.numericToInetAddress(mDNSAddress);

        DhcpInfo dhcpInfo = new DhcpInfo();
        dhcpInfo.ipAddress = inetAddressToInt((Inet4Address) ipaddr);
        dhcpInfo.gateway = inetAddressToInt((Inet4Address) getwayaddr);
        dhcpInfo.netmask = inetAddressToInt((Inet4Address) inetmask);
        dhcpInfo.dns1 = inetAddressToInt((Inet4Address) idns1);

        mEthernetManager.setEthernetMode(EthernetManager.ETHERNET_CONNECT_MODE_MANUAL, dhcpInfo);
        mEthernetManager.setEthernetEnabled(true);
    }

    private int inetAddressToInt(Inet4Address inetAddr)
            throws IllegalArgumentException {
        byte[] addr = inetAddr.getAddress();
        return ((addr[3] & 0xff) << 24) | ((addr[2] & 0xff) << 16) |
                ((addr[1] & 0xff) << 8) | (addr[0] & 0xff);
    }
}
