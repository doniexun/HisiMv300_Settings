package com.android.settings.iptv.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 创建热点
 *
 */
public class WifiApAdmin {
	public static final String TAG = "libeibei";
	
	public static void closeWifiAp(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
		closeWifiAp(wifiManager);
	}
	
	private WifiManager mWifiManager = null;
	
	private Context mContext = null;
	public WifiApAdmin(Context context) {
		mContext = context;
		
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);  
		
		closeWifiAp(mWifiManager);
	}
	
	private String mSSID = "";
	private String mPasswd = "";
	private int mSecurity = 0;
	
	public void startWifiAp(String ssid, String passwd, int securuty) {
		mSSID = ssid;
		mPasswd = passwd;
		mSecurity = securuty;
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		} 
		
		stratWifiAp();
		
		MyTimerCheck timerCheck = new MyTimerCheck() {
			
			@Override
			public void doTimerCheckWork() {
				// TODO Auto-generated method stub
				
				if (isWifiApEnabled(mWifiManager)) {
					Log.v(TAG, "Wifi-AP enabled success!");
					this.exit();
				} else {
					Log.v(TAG, "Wifi-AP enabled failed!");
				}
			}

			@Override
			public void doTimeOutWork() {
				// TODO Auto-generated method stub
				this.exit();
			}
		};
		timerCheck.start(15, 1000);
		
	}

	public void stratWifiAp() {
		Method method = null;
		try {
			method = mWifiManager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			WifiConfiguration config = new WifiConfiguration();
			
			config.SSID = mSSID;
			if(mSecurity == 0) {
				config.wepKeys[0] = "";
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				config.wepTxKeyIndex = 0;
			} else if (mSecurity == 1) {
				config.preSharedKey = mPasswd;
				config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			} else if (mSecurity == 2) {
				config.preSharedKey = mPasswd;
				config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
				config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA2_PSK);
				// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			}
			/*
			netConfig.preSharedKey = mPasswd;

			netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);*/

			method.invoke(mWifiManager, config, true);

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private static void closeWifiAp(WifiManager wifiManager) {
		
		if (isWifiApEnabled(wifiManager)) {
			try {
				Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
				method.setAccessible(true);

				WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);

				Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
				method2.invoke(wifiManager, config, false);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	private static boolean isWifiApEnabled(WifiManager wifiManager) {
		try {
			Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (Boolean) method.invoke(wifiManager);

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}

