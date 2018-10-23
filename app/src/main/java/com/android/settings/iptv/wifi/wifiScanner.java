package com.android.settings.iptv.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.android.settings.R;


public class wifiScanner extends Handler {
	private final static String TAG = "wifi";
	public final static int WIFI_SCAN_INDEX = 0x10110;
	private int mRetry = 0;
	private Context mContext = null;
	private WifiManager mWifiManager = null;
	public wifiScanner(Context context, WifiManager wifimanager)
	{
		mContext = context;
		mWifiManager = wifimanager;
	}
	void resume() {
		if (!hasMessages(WIFI_SCAN_INDEX)) {
			sendEmptyMessage(WIFI_SCAN_INDEX);
		}
	}
	void pause() {
		mRetry = 0;
		removeMessages(WIFI_SCAN_INDEX);
	}

	@Override
	public void handleMessage(Message message)
	{
		switch (message.what) {
			case WIFI_SCAN_INDEX:
			{
				if (mWifiManager.startScan()) {
					mRetry = 0;
				} else if (++mRetry >= 3) {
					mRetry = 0;
					Toast.makeText(mContext, R.string.wifi_disabled_generic,
							Toast.LENGTH_LONG).show();
					return;
				}
				sendEmptyMessageDelayed(0, 3000);
			}				
				break;	
			default:
				break;
		}
		
	}
}
