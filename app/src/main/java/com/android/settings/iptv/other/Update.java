package com.android.settings.iptv.other;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.RecoverySystem;
import android.util.Log;

import com.android.settings.iptv.util.Loger;
import com.hisilicon.android.hisysmanager.HiSysManager;


public class Update {
	private String TAG        = "usbupdate";
	@SuppressWarnings("unused")
	private String verconfig  = "verconfig.xml";
	private String updatefile = "update.zip";
	private String updatePath ;
	private String Usbfile ="update.zip";
	@SuppressWarnings("unused")
	private boolean updateflag=false;
	private Context mContext;
	private Loger loger = new Loger(Update.class);
	
	public Update(Context context) {
		mContext = context;
	}
	
	public int USBUpdate(String path){
			Log.i(TAG, "-->version " + Build.VERSION.INCREMENTAL + "BRAND" + Build.BRAND + "MANUFACTURER" + Build.MANUFACTURER + "MODEL" + Build.MODEL);
			updatePath = path;
			File file = new File(updatePath);
			return search(file);
	}
	
	private int search(File file){
		
		if(file == null){
			return 0;
		}
		if(!file.isDirectory()){
			return 0;
		}
		File[] listFile = file.listFiles();
		if(listFile == null){
			return 0;
		}
		Log.i(TAG, "-->search listfile :::: "+listFile.length);
		updateflag = true;
		for(int i = 0; i<listFile.length; i++){
			Log.i(TAG, "-->search file->" + listFile[i].getName());
			if(listFile[i].getName().equals(updatefile)){
				String path = listFile[i].getAbsolutePath();
				Log.i(TAG, "-->search  install package:" + path);
				hisiupdate(path);
			}
		}
		return 0;
	}

	@SuppressWarnings("unused")
	private boolean checkVersion(String path){
		String pathtmp = path;
		try {
			
			InputStream inputstream = new FileInputStream(pathtmp);
			InputStreamReader inputreader = new InputStreamReader(inputstream);
			BufferedReader bufreader = new BufferedReader(inputreader);
			
			String verconf = bufreader.readLine();
			Log.i(TAG, "verconf1:" + verconf);
			
			if(verconf.equals("<chverconfig>")==false){	
				bufreader.close();
				return false;
			}
			
			verconf = bufreader.readLine();
			Log.i(TAG, "verconf2:" + verconf);
			
			if(verconf.startsWith("swversion")==false){
				bufreader.close();
				return false;			
			}			
			
			String swversion = verconf.substring(10);
			
			verconf = bufreader.readLine();
			if(verconf.startsWith("hdversion")==false){
				bufreader.close();
				return false;			
			}			
			
			String hdversion = verconf.substring(10);
			verconf = bufreader.readLine();
			Log.i(TAG, "verconf6:" + verconf);		
			if(verconf.equals("</chverconfig>")==false){
				bufreader.close();
				return false;
			}
			Log.i(TAG, "-->SWver:" + Build.VERSION.INCREMENTAL + "=?" + swversion  + "-->HDver:" + hdversion);
			if(swversion.equals(Build.VERSION.INCREMENTAL) == true){
				bufreader.close();
				return false;
			}
			
			Process process =  Runtime.getRuntime().exec("getprop ro.build.hardware.id");
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(ir);
            
            verconf = input.readLine();
        	Log.i(TAG, "verconf8:" + verconf);	
        	
			if(hdversion.equals(verconf) == false){
				input.close();
				bufreader.close();
				return false;
			}
			
			input.close();
			ir.close();
			
			bufreader.close();
			inputreader.close();
			inputstream.close();
			
		} catch (Exception e) {
			Log.e(TAG, "checkVersion fail");
			return false;
		}
		
		return true;
	}

    /**
     * 海思U盘升级
     * @param path
     */
	public void hisiupdate(String path) {
        Log.i("result", "update file path="+ path);
        HiSysManager hiSysManager = new HiSysManager();
        hiSysManager.upgrade(path);
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.putExtra("mount_point", path);
        mContext.sendBroadcast(intent);
	}

    /**
     * Android原生 Amlogic
	 * U盘升级
     * @param path
     */
    public void updateFromRecovery( String path) {
        loger.i("U盘升级路径："+path);
        String filePath = "/udisk/"+"update.zip";
        File updateFile = new File(filePath);
        try{
            RecoverySystem.installPackage(mContext,updateFile);
        }catch (IOException e){
            loger.i(e.toString());
        }
    }
}


