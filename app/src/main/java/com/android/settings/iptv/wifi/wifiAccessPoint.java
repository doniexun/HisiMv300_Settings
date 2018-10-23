package com.android.settings.iptv.wifi;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.settings.R;


public class wifiAccessPoint implements Comparable<wifiAccessPoint> {
    static final String TAG = "libeibei";
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    ;
    PskType pskType = PskType.UNKNOWN;
    private Context mContext = null;
    private WifiConfiguration mConfig;
    public String ssid;
    public String bssid;
    public int security;
    public int networkId;
    public int mRssi;
    private WifiInfo mInfo;
    private DetailedState mState;
    public CallBackNotify mCallBackNotify;
    boolean wpsAvailable = false;

    public wifiAccessPoint(Context context, String title) {
        mContext = context;
        ssid = title;
        bssid = null;
        security = 0;
        networkId = -1;
        mRssi = Integer.MAX_VALUE;
        refresh();

    }

    public wifiAccessPoint(Context context, WifiConfiguration config) {
        mContext = context;
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        security = getSecurity(config);
        networkId = config.networkId;
        mConfig = config;
        mRssi = Integer.MAX_VALUE;
        bssid = null;
        refresh();

    }

    public wifiAccessPoint(Context context, ScanResult result) {

        mContext = context;
        ssid = result.SSID;
        security = getSecurity(result);
        networkId = -1;
        mRssi = result.level;
        bssid = result.BSSID;
        wpsAvailable = security != SECURITY_EAP && result.capabilities.contains("WPS");
        refresh();
    }

    public int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 4);
    }

    public WifiConfiguration getConfig() {
        return mConfig;
    }

    public WifiInfo getInfo() {
        return mInfo;
    }

    public DetailedState getState() {
        return mState;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    private static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public String getSecurityString(boolean concise) {
        Context context = mContext;
        switch (security) {
            case SECURITY_EAP:
                return concise ? context.getString(R.string.wifi_security_short_eap) :
                        context.getString(R.string.wifi_security_eap);
            case SECURITY_PSK:
                switch (pskType) {
                    case WPA:
                        return concise ? context.getString(R.string.wifi_security_short_wpa) :
                                context.getString(R.string.wifi_security_wpa);
                    case WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa2) :
                                context.getString(R.string.wifi_security_wpa2);
                    case WPA_WPA2:
                        return concise ? context.getString(R.string.wifi_security_short_wpa_wpa2) :
                                context.getString(R.string.wifi_security_wpa_wpa2);
                    case UNKNOWN:
                    default:
                        return concise ? context.getString(R.string.wifi_security_short_psk_generic)
                                : context.getString(R.string.wifi_security_psk_generic);
                }
            case SECURITY_WEP:
                return concise ? context.getString(R.string.wifi_security_short_wep) :
                        context.getString(R.string.wifi_security_wep);
            case SECURITY_NONE:
            default:
                return concise ? "" : context.getString(R.string.wifi_security_none);
        }
    }

    private static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    public void update(WifiInfo info, DetailedState state) {
        boolean reorder = false;
        if (info != null && networkId != -1 && networkId == info.getNetworkId()) {
            reorder = (mInfo == null);
            mRssi = info.getRssi();
            mInfo = info;
            mState = state;
            refresh();
        } else if (mInfo != null) {
            reorder = true;
            mInfo = null;
            mState = null;
            refresh();
        }
        if (reorder) {
            //notifyHierarchyChanged();
        }
    }

    private void refresh() {
        if (mCallBackNotify != null) {
            Log.i(TAG, "wifi point refresh");
            mCallBackNotify.onCallBackNotify();
        }
    }

    public boolean update(ScanResult result) {
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                mRssi = result.level;
            }
            refresh();
            return true;
        }
        return false;
    }

    public void setOnCallBack(CallBackNotify callbacknotify) {
        mCallBackNotify = callbacknotify;
    }

    public String getSummary() {
        String SummaryStr = "";

        Log.e(TAG,"wifi 网络:"+ssid);
        if(mConfig != null){
            Log.e(TAG,"mConfig.status ="+mConfig.status);
        }
        if (mConfig != null && mConfig.status == WifiConfiguration.Status.DISABLED) {
            Log.i(TAG, "refresh disableReason: " + mConfig.disableReason);
            switch (mConfig.disableReason) {
                case WifiConfiguration.DISABLED_AUTH_FAILURE:
                    SummaryStr = mContext.getString(R.string.wifi_disabled_password_failure);
                    break;
                case WifiConfiguration.DISABLED_DHCP_FAILURE:
                case WifiConfiguration.DISABLED_DNS_FAILURE:
                    SummaryStr = mContext.getString(R.string.wifi_disabled_network_failure);
                    break;
                case WifiConfiguration.DISABLED_UNKNOWN_REASON:
                    SummaryStr = mContext.getString(R.string.wifi_disabled_generic);
                    break;
                default:
                    break;
            }
        } else if (mRssi == Integer.MAX_VALUE) {
            SummaryStr = mContext.getString(R.string.wifi_not_in_range);
        } else if (mState != null) {
            Log.e(TAG,"mState = "+mState);
            SummaryStr = Summary.get(mContext, mState);
            // In range, not disabled.
        } else {
            StringBuilder summary = new StringBuilder();
            // Is saved network
            if (mConfig != null) {
                summary.append(mContext.getString(R.string.wifi_remembered));
            }

            if (security != SECURITY_NONE) {
                String securityStrFormat;
                if (summary.length() == 0) {
                    securityStrFormat = mContext.getString(R.string.wifi_secured_first_item);
                } else {
                    securityStrFormat = mContext.getString(R.string.wifi_secured_second_item);
                }
                summary.append(String.format(securityStrFormat, getSecurityString(true)));
            }
            // Only list WPS available for unsaved networks
            if (mConfig == null && wpsAvailable) {
                if (summary.length() == 0) {
                    summary.append(mContext.getString(R.string.wifi_wps_available_first_item));
                } else {
                    //summary.append(mContext.getString(R.string.wifi_wps_available_second_item));
                }
            }

            SummaryStr = summary.toString();
        }
        return SummaryStr;
    }

    @Override
    public int compareTo(wifiAccessPoint another) {
        if (!(another instanceof wifiAccessPoint)) {
            return 1;
        }

        wifiAccessPoint other = (wifiAccessPoint) another;
        // Active one goes first.
        if (mInfo != other.mInfo) {
            return (mInfo != null) ? -1 : 1;
        }
        // Reachable one goes before unreachable one.
        if ((mRssi ^ other.mRssi) < 0) {
            return (mRssi != Integer.MAX_VALUE) ? -1 : 1;
        }
        // Configured one goes before unconfigured one.
        if ((networkId ^ other.networkId) < 0) {
            return (networkId != -1) ? -1 : 1;
        }
        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(other.ssid);
    }

    public static class Summary {
        public static String get(Context context, String ssid, DetailedState state) {
            String[] formats = context.getResources().getStringArray((ssid == null)
                    ? R.array.wifi_status : R.array.wifi_status_with_ssid);
            int index = state.ordinal();

            if (index >= formats.length || formats[index].length() == 0) {
                return null;
            }
            return String.format(formats[index], ssid);
        }

        public static String get(Context context, DetailedState state) {
            return get(context, null, state);
        }
    }
}
