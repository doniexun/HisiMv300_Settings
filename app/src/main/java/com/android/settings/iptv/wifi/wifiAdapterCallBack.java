package com.android.settings.iptv.wifi;

import android.util.Log;
import android.widget.ListView;

public class wifiAdapterCallBack implements CallBackNotify{

	private ListView mListView = null;
	private wifiListAdapter mWifiListAdapter = null;
	public wifiAdapterCallBack(ListView listView, wifiListAdapter WifiListAdapter)
	{
		mListView = listView;
		mWifiListAdapter = WifiListAdapter;
	}
	@Override
	public void onCallBackNotify() {
		
		if( null != mListView &&  null != mWifiListAdapter)
		{
			Log.i("wifiAccessPoint", "enter wifiAdapterCallBack onCallBackNotify()");
			mWifiListAdapter.notifyDataSetChanged();
			//mListView.setAdapter(mWifiListAdapter);
		}
		
	}


}
