package com.android.settings.iptv.util;

import android.util.Log;

public class Loger {
	private static String TAG = "Settings";
	private String tag_msg = "";
	public Loger(Class cls){
	    tag_msg = cls.getSimpleName();
    }

	public void i(String msg){
        Log.i(TAG,tag_msg+"--"+msg);
    }
    public void w(String msg){
        Log.i(TAG,tag_msg+"--"+msg);
    }
    public void d(String msg){
        Log.i(TAG,tag_msg+"--"+msg);
    }
    public void e(String msg){
        Log.i(TAG,tag_msg+"--"+msg);
    }
}
