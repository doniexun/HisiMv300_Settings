package com.android.settings.iptv.wifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.ethernet.EthernetManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.security.KeyStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressLint("NewApi")
public class wifiMethods {
    private final static String TAG = "libeibei";
    private AtomicBoolean mConnectAtomic = new AtomicBoolean(false);
    private ArrayList<wifiAccessPoint> mWifiAccessPoints = new ArrayList<wifiAccessPoint>();
    @SuppressLint("NewApi")
    private LinkProperties mLinkProperties = new LinkProperties();
    private int mLastPriority = -1;
    private int mKeyStoreNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;
    private WifiInfo mLastInfo;
    private DetailedState mLastState;
    private Context mContext = null;
    private WifiManager mWifiManager = null;
    private IntentFilter mFilter = null;
    private wifiScanner mWifiScanner = null;
    private wifiListAdapter mWifiAdapter = null;
    private wifiAdapterCallBack mCallBack = null;
    private ListView mListView = null;
    private TextView mListState = null;
    private Handler mHandler = null;

    public wifiMethods(Context context, Handler handler, WifiManager wifiManager, ListView listview, TextView textView) {
        mContext = context;
        mWifiManager = wifiManager;
        mListView = listview;
        mHandler = handler;
        mWifiScanner = new wifiScanner(mContext, wifiManager);
        mWifiAdapter = new wifiListAdapter(mContext, mWifiAccessPoints);
        mCallBack = new wifiAdapterCallBack(mListView, mWifiAdapter);
        mListView.setAdapter(mWifiAdapter);
        mListView.requestFocus();
        mListState = textView;
        mConnectListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        };
        mSaveListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {

            }
        };

        mForgetListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {

            }
        };

    }

    public void registerWifiFilter() {
        if (mContext == null) {
            Log.i(TAG, "mContext == null");
            return;
        }
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mContext.registerReceiver(mWifiReciver, mFilter);
    }

    public void unregisterWifiFilter() {
        if (mContext == null) {
            Log.i(TAG, "mContext == null");
            return;
        }
        mContext.unregisterReceiver(mWifiReciver);
    }

    public void updateWifiState(int state) {
        if (WifiManager.WIFI_STATE_ENABLED == state) {
            mWifiScanner.resume();
            /*update WIFI Access points*/
            //updateWifiAcessPoints();
        } else {
            mWifiScanner.pause();
        }

    }

    public void updateWifiAcessPoints() {
        Log.i(TAG, "updateWifiAcessPoints()------");
        if (mWifiAccessPoints != null) {
            mWifiAccessPoints.clear();
        }
        //得到配置好的网络信息
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (null != configs) {
            mLastPriority = 0;
            for (WifiConfiguration wificonfig : configs) {
                if (wificonfig.priority > mLastPriority) {
                    mLastPriority = wificonfig.priority;
                }
                wifiAccessPoint mwifiAccessPoint = new wifiAccessPoint(mContext, wificonfig);
                mwifiAccessPoint.update(mLastInfo, mLastState);
                mwifiAccessPoint.setOnCallBack(mCallBack);
                Log.i(TAG, "enter add wifiAccessPoint SSID = " + mwifiAccessPoint.ssid);
                mWifiAccessPoints.add(mwifiAccessPoint);
            }
        }
        List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hot networks.
                if (result.SSID == null || result.SSID.length() == 0
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (wifiAccessPoint accessPoint : mWifiAccessPoints) {
                    if (accessPoint.update(result)) {
                        found = true;
                    }
                }
                if (!found) {
                    mWifiAccessPoints.add(new wifiAccessPoint(mContext, result));
                }
            }
        }
        Collections.sort(mWifiAccessPoints);
        /*调试发现 notifyDataSetChanged更新不一定成功*/
        mWifiAdapter.notifyDataSetChanged();
    }

    public void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mWifiScanner.pause();
            return;
        }
        if (state == DetailedState.OBTAINING_IPADDR) {
            mWifiScanner.pause();
        } else {
            mWifiScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        for (int i = mWifiAccessPoints.size() - 1; i >= 0; --i) {
            mWifiAccessPoints.get(i).update(mLastInfo, mLastState);
        }

        if ((state == DetailedState.CONNECTED
                || state == DetailedState.DISCONNECTED || state == DetailedState.FAILED)) {
            Log.i(TAG, "updateWifiAcessPoints  with " + state);
            updateWifiAcessPoints();
        }
        //wifi获取到IP后，关闭有线网络
//        if (state == DetailedState.CONNECTED) {
//            Log.e("LBB", "关闭有线网络！！");
//            EthernetManager ethernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
//            ethernetManager.setEthernetEnabled(false);
//            ethernetManager.enableEthernet(false);
//        }

    }

    public void enable_wifi() {
        EthernetManager ethernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
//        ethernetManager.setEthernetEnabled(false);
//        ethernetManager.enableEthernet(false);
        mWifiManager.setWifiEnabled(true);
        mListState.setVisibility(View.VISIBLE);
    }

    public void disable_wifi() {
        EthernetManager ethernetManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
        ethernetManager.setEthernetEnabled(true);
        ethernetManager.enableEthernet(true);
        mWifiManager.setWifiEnabled(false);
        mListState.setVisibility(View.VISIBLE);

    }

    public void submit(final wifiAccessPoint mAP, final String mpassword) {
        final WifiConfiguration config = getConfigByAcessPoint(mAP, mpassword);
        if (config == null) {
            if (mAP != null && !requireKeyStore(mAP.getConfig())
                    && mAP.networkId != WifiConfiguration.INVALID_NETWORK_ID) {
                mWifiManager.connect(mAP.networkId, mConnectListener);
            }
        } else if (config.networkId != WifiConfiguration.INVALID_NETWORK_ID) {
            if (mAP != null) {
                mWifiManager.save(config, mSaveListener);
            }
        } else {
            if (requireKeyStore(config)) {
                mWifiManager.save(config, mSaveListener);
            } else {

                mWifiManager.connect(config, mConnectListener);
            }
        }

        if (mWifiManager.isWifiEnabled()) {
            mWifiScanner.resume();
        }
        updateWifiAcessPoints();
    }

    public void resumeConnect() {
        if (mKeyStoreNetworkId != WifiConfiguration.INVALID_NETWORK_ID
                && KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
            Log.i(TAG, "enter resumeConnect()");
            Log.i(TAG, "enter resumeConnect() mKeyStoreNetworkId:" + String.valueOf(mKeyStoreNetworkId));
            Log.i(TAG, "enter resumeConnect() WifiConfiguration.INVALID_NETWORK_ID" + String.valueOf(WifiConfiguration.INVALID_NETWORK_ID));
            mWifiManager.connect(mKeyStoreNetworkId, mConnectListener);
        }
        mKeyStoreNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
    }

    public void forget(final wifiAccessPoint mSelected) {
        if (mSelected.networkId == WifiConfiguration.INVALID_NETWORK_ID) {
            // Should not happen, but a monkey seems to triger it
            Log.e(TAG, "Failed to forget invalid network " + mSelected.getConfig());
            return;
        }
        mWifiManager.forget(mSelected.networkId, mForgetListener);

        if (mWifiManager.isWifiEnabled()) {
            mWifiScanner.resume();
        }
        updateWifiAcessPoints();
    }

    private boolean requireKeyStore(WifiConfiguration config) {
        /*PIN connect and require saved key
         * turn on will show one dialog */
        //if (/*Wifi_Dialog.requireKeyStore(config)
        //		&& */KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
        //	mKeyStoreNetworkId = config.networkId;
        //	Credentials.getInstance().unlock(mContext);
        //	return true;
        //}
        return false;
    }

    public wifiAccessPoint GetAccessPointByIndex(int index) {
        wifiAccessPoint mWifiAcessPoint = null;
        if (index < 0) {
            Log.i(TAG, "GetAccessPointByIndex() -- index == " + index);
            return null;
        }
        if (null != mWifiAccessPoints && mWifiAccessPoints.size() > 0) {
            mWifiAcessPoint = mWifiAccessPoints.get(index);
        }
        return mWifiAcessPoint;

    }

    public void ShowConnectDialog(wifiAccessPoint mAccessPoint, boolean edit) {
        final wifiAccessPoint mWifiAcessPoint = mAccessPoint;
        final DetailedState state = mWifiAcessPoint.getState();
        int level = mWifiAcessPoint.getLevel();
        if (state == null) {
            Log.i(TAG, "&&&&&&&&&&&&&&&&&&&&" + mWifiAcessPoint.networkId);
        } else {
            Log.i(TAG, "$$$$$$$$$$$$$$$$$$$" + state + " ----" + mWifiAcessPoint.networkId);
        }

        //if(mWifiAcessPoint.getConfig() != null && WifiConfiguration.DISABLED_AUTH_FAILURE == mWifiAcessPoint.getConfig().disableReason)
        //{
        //	mWifiManager.forget(mWifiAcessPoint.networkId, mForgetListener);
        //	Log.i(TAG, "122121");
        //}

        /*Connected current*/
        if (state != null && state == DetailedState.CONNECTED && mWifiAcessPoint.networkId >= 0) {
            Log.i(TAG, "wifi access point connected --" + mWifiAcessPoint.ssid);
            final CustomDialog mCustomDialog = new CustomDialog(mContext, CustomDialog.DIALOG_CURRENT_CONNECTED);
            TextView ssidView = (TextView) mCustomDialog.getSSIDname();
            ssidView.setText(mWifiAcessPoint.ssid);
            TextView ShowIPView = (TextView) mCustomDialog.getShowInfo();
            WifiConfiguration config = mWifiAcessPoint.getConfig();
            String s_IpAddress = null;
            for (InetAddress a : config.linkProperties.getAddresses()) {
                Log.i(TAG, "InetAddress  is  " + a.getHostAddress());
                s_IpAddress = a.getHostAddress();
                break;
            }
            ShowIPView.setText(s_IpAddress);
            mCustomDialog.setOnNegativeListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Log.i(TAG, "Click Negative Button!");
                    mCustomDialog.dismiss();
                }
            });
            mCustomDialog.setOnPositiveListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    forget(mWifiAcessPoint);
                    mCustomDialog.dismiss();
                }
            });
            mCustomDialog.show();
        }
        /*Connected one time and saved config */
        if (state == null && mWifiAcessPoint.networkId >= 0 || ((state != null && mWifiAcessPoint.getConfig() != null && mWifiAcessPoint.getConfig().status == WifiConfiguration.Status.DISABLED))) {
            Log.i(TAG, "Connected one time and saved config !!!");
            final CustomDialog mCustomDialog = new CustomDialog(mContext, CustomDialog.DIALOG_ONETIME_CONNECTED);
            TextView ssidView = (TextView) mCustomDialog.getSSIDname();
            ssidView.setText(mWifiAcessPoint.ssid);
            TextView ShowSecurity = (TextView) mCustomDialog.getShowInfo();
            String security = null;
            switch (mWifiAcessPoint.security) {
                case wifiAccessPoint.SECURITY_NONE:
                    security = " 无";
                    break;
                case wifiAccessPoint.SECURITY_WEP:
                    security = "SECURITY_WEP";
                    break;
                case wifiAccessPoint.SECURITY_PSK:
                    security = "SECURITY_PSK";
                    break;
                case wifiAccessPoint.SECURITY_EAP:
                    security = "SECURITY_EAP";
                    break;
                default:
                    security = "SECURITY_UNKOWN";
                    break;
            }
            ShowSecurity.setText(security);
            mCustomDialog.setOnNegativeListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Log.i(TAG, "Click Negative Button!");
                    forget(mWifiAcessPoint);
                    mCustomDialog.dismiss();
                }
            });
            mCustomDialog.setOnPositiveListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    submit(mWifiAcessPoint, "");
                    mCustomDialog.dismiss();
                }
            });
            mCustomDialog.show();
        }
        /*new Access Point Or Connected failed*/
        if ((state == null && mWifiAcessPoint.networkId < 0) /*||
				(state != null && mWifiAcessPoint.getConfig() != null &&  mWifiAcessPoint.getConfig().status == WifiConfiguration.Status.DISABLED)*/) {
            /*none security*/
            if (mWifiAcessPoint.security == wifiAccessPoint.SECURITY_NONE) {
                final CustomDialog mCustomDialog = new CustomDialog(mContext, CustomDialog.DIALOG_NONE_SECURITY);
                TextView ssidView = (TextView) mCustomDialog.getSSIDname();
                ssidView.setText(mWifiAcessPoint.ssid);
                TextView ShowSignal = (TextView) mCustomDialog.getShowInfo();
                String Signal = "";
                switch (level) {
                    case 3:
                        Signal = mContext.getResources().getString(R.string.wifi_signal3);
                        break;
                    case 2:
                        Signal = mContext.getResources().getString(R.string.wifi_signal2);
                        break;
                    case 1:
                        Signal = mContext.getResources().getString(R.string.wifi_signal1);
                        break;
                    case 0:
                        Signal = mContext.getResources().getString(R.string.wifi_signal0);
                        break;
                    case -1:
                    default:
                        Signal = mContext.getResources().getString(R.string.wifi_signal00);
                        break;
                }
                ShowSignal.setText(Signal);
                mCustomDialog.setOnNegativeListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Log.i(TAG, "Click Negative Button!");
                        mCustomDialog.dismiss();
                    }
                });
                mCustomDialog.setOnPositiveListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {

                        submit(mWifiAcessPoint, "");
                        mCustomDialog.dismiss();
                    }
                });
                mCustomDialog.show();

            }
            /*security should input password*/
            else {
                final CustomDialog mCustomDialog = new CustomDialog(mContext, CustomDialog.DIALOG_HAVE_SECURITY);
                TextView ssidView = (TextView) mCustomDialog.getSSIDname();
                ssidView.setText(mWifiAcessPoint.ssid);
                final EditText mEditPassword = (EditText) mCustomDialog.getEditText();
                final CheckBox mCheckShowPassword = (CheckBox) mCustomDialog.getCheckBox();
                mCheckShowPassword.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        mEditPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT
                                        | (((CheckBox) mCheckShowPassword).isChecked() ?
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                        : InputType.TYPE_TEXT_VARIATION_PASSWORD));

                    }
                });
                mCustomDialog.setOnNegativeListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Log.i(TAG, "Click Negative Button!");
                        mCustomDialog.dismiss();
                    }
                });

                mCustomDialog.setOnPositiveListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        String password = mEditPassword.getText().toString();
                        if (TextUtils.isEmpty(password)) {
                            Toast.makeText(mContext, "密码不能为空", Toast.LENGTH_SHORT).show();
                        } else if (password.length() < 8) {
                            Toast.makeText(mContext, "密码长度不符", Toast.LENGTH_SHORT).show();
                        } else {
                            submit(mWifiAcessPoint, mEditPassword.getText().toString());
                            mCustomDialog.dismiss();
                        }

                    }
                });
                mCustomDialog.show();
            }
        }
    }

    /**/
    @SuppressLint("NewApi")
    public WifiConfiguration getConfigByAcessPoint(wifiAccessPoint mAccessPoint, String mpassword) {

        int mSecurity = (mAccessPoint == null) ? wifiAccessPoint.SECURITY_NONE
                : mAccessPoint.security;

        if (mAccessPoint != null && mAccessPoint.networkId != -1) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        if (mAccessPoint == null) {
            // If the user adds a network manually, assume that it is hidden.
            String SSIDStr = "WifiName";
            config.SSID = wifiAccessPoint.convertToQuotedString(SSIDStr);
            config.hiddenSSID = true;
        } else if (mAccessPoint.networkId == WifiConfiguration.INVALID_NETWORK_ID) {
            config.SSID = wifiAccessPoint.convertToQuotedString(mAccessPoint.ssid);

        } else {
            config.networkId = mAccessPoint.networkId;

        }
        switch (mSecurity) {
            case wifiAccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case wifiAccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mpassword.length() != 0) {
                    int length = mpassword.length();
                    String password = mpassword;
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case wifiAccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mpassword.length() != 0) {
                    String password = mpassword;
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case wifiAccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                if (mpassword.length() != 0) {
                    config.enterpriseConfig.setPassword(mpassword);
                }
                break;
            default:
                break;
        }
        config.proxySettings = WifiConfiguration.ProxySettings.NONE;
        config.ipAssignment = WifiConfiguration.IpAssignment.DHCP;
        config.linkProperties = new LinkProperties(mLinkProperties);
        return config;

    }

    private void HandlerWifiReciver(Intent mIntent) {
        String mActionStr = mIntent.getAction();
        Log.i(TAG, "HandlerWifiReciver() getAction = " + mActionStr);
        switch (mActionStr) {
            case WifiManager.WIFI_STATE_CHANGED_ACTION: {
                /*refresh wifi state and resume or pause scanner*/
                Log.i(TAG, "111 WifiManager.WIFI_STATE_CHANGED_ACTION ");
                int mWifiStae = mIntent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                updateWifiState(mWifiStae);
                Log.e("STATE", "Wifi state = " + mWifiStae);

                if (mWifiStae == WifiManager.WIFI_STATE_ENABLING) {
                    mListState.setText(R.string.wifi_scanning);
                } else if (mWifiStae == WifiManager.WIFI_STATE_ENABLED) {

                } else if (mWifiStae == WifiManager.WIFI_STATE_DISABLING) {
                    mListState.setText(R.string.wifi_closeing);
                } else if (mWifiStae == WifiManager.WIFI_STATE_DISABLED) {
                    mListState.setText(R.string.wifi_closed);
                } else {
                    mListState.setText("");
                }
            }
            break;

            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                Log.i(TAG, "222 WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:");
                updateWifiAcessPoints();
                mListState.setVisibility(View.INVISIBLE);
                break;
            case WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION:
                Log.i(TAG, "333 WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION:");
                break;
            case WifiManager.LINK_CONFIGURATION_CHANGED_ACTION:
                Log.i(TAG, "444 WifiManager.LINK_CONFIGURATION_CHANGED_ACTION:");
                updateWifiAcessPoints();
                break;
            case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION: {
                Log.i(TAG, "555 WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:");
                SupplicantState state =
                        (SupplicantState) mIntent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                int linkWifiResult = mIntent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);

                if (!mConnectAtomic.get() && SupplicantState.isHandshakeState(state)) {
                    Log.i(TAG, "666 WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:验证状态改变");
                    updateConnectionState(WifiInfo.getDetailedStateOf(state));
                }

                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                    Log.i(TAG, "777 WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:密码错误");
                    updateWifiAcessPoints();
                }

            }
            break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION: {
                Log.i(TAG, "888 WifiManager.NETWORK_STATE_CHANGED_ACTION:");
                NetworkInfo info = (NetworkInfo) mIntent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                mConnectAtomic.set(info.isConnected());
                if (info.isConnected()) {
                    /*send message and exit network config*/
                    Log.i(TAG, "WIFI is connected! " + info.getTypeName());
                }
                updateWifiAcessPoints();
                updateConnectionState(info.getDetailedState());
            }
            break;
            case WifiManager.RSSI_CHANGED_ACTION: {
                Log.i(TAG, "999 WifiManager.RSSI_CHANGED_ACTION:");
                updateConnectionState(null);
            }
            break;

            default:
                break;
        }

    }

    private BroadcastReceiver mWifiReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            HandlerWifiReciver(intent);
        }
    };
}
