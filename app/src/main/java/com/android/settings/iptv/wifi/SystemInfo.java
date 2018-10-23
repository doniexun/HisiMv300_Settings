package com.android.settings.iptv.wifi;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class SystemInfo {
	private final static String TAG = "libeibei";
	private static SystemInfo sSystemInfo;
	
	public static SystemInfo getInstance(){
		if(sSystemInfo == null){
			sSystemInfo = new SystemInfo();
		}
		return sSystemInfo;
	}
	
	public void setContext(Context context){
	}
	
	public long  getTotalMemory() {
        String str = "/proc/meminfo";
        String memoInfo="";
        String totalMemory = "";
        String[] strArray;
        try {  
            FileReader fr = new FileReader(str);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            while ((memoInfo = localBufferedReader.readLine()) != null) {  
                Log.i(TAG, "---" + memoInfo.split("\\s+"));
                strArray =memoInfo.split("\\s+");
                if(memoInfo.contains("MemTotal")){
                	totalMemory = strArray[1];
                	break;
                }
            }  
            localBufferedReader.close();
        }catch (IOException e) {
        	
        }  
        return Long.parseLong(totalMemory) * 1000;
    }  
	
	public String getSTBID(){
		return Build.SERIAL;
	}
	
	public String getEthMacAddress(){
		INetworkManagementService mNwService;
		String mac = "";
		IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
		mNwService = INetworkManagementService.Stub.asInterface(b);
		
        try {
            try {

                    Log.i(this.getClass().getSimpleName(),"getHardwareAddress:"+ mNwService.getInterfaceConfig("eth0")
                            .getHardwareAddress().toString());
                    mac = mNwService.getInterfaceConfig("eth0").getHardwareAddress().toString();
                    mac = mac.toUpperCase(Locale.US);
            } catch (NullPointerException e) {
                Log.i(this.getClass().getSimpleName(), "NullPointerException  " + e);
            }
        } catch (RemoteException remote) {
            Log.i(this.getClass().getSimpleName(), "RemoteException  " + remote);
        }
		
		return mac;
	}
	private String rmMacSeperator(String mac){
		String num = mac.replace(":", "");
		
		return num;
	}
}
