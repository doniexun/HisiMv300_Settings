package com.android.settings.receiver;

import java.io.File;

import com.android.settings.R;
import com.android.settings.iptv.other.Update;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.VchCommonToastDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.view.WindowManager;

public class ScanStorageReceiver extends BroadcastReceiver {

    private Context mContext;
    private String mPath = "";
    private VchCommonToastDialog toastDialog;
    private String bootstatus;
    private String Path = "";
    private File flagfile;
    private Loger loger = new Loger(ScanStorageReceiver.class);

    @Override
    public void onReceive(final Context context, Intent intent) {

        mContext = context;

        toastDialog = new VchCommonToastDialog(mContext);
        toastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        toastDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        String action = intent.getAction();

        bootstatus = SystemProperties.get("sys.boot_completed");
        loger.i( "sys.boot_completed -------> " + bootstatus);

        if (action.equals("com.changhong.iptv.usbUpdateSystem")) {

            mPath = intent.getStringExtra("updatePath");
            loger.i( "updatePath -----> " + mPath);
            new Thread() {
                @Override
                public void run() {

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Update update = new Update(mContext);
                    update.hisiupdate(mPath);
                }
            }.start();
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {

            Path = intent.getData().getPath();
            loger.i( "Path -------> " + Path);

            //针对海思平台，修改bug：恢复出厂后第一次开机时，会挂载内部存储，导致显示U盘挂载
            //添加过滤条件，两个USB插口分别对应sda和sdb
            if (bootstatus.equals("1")&&(Path.contains("sda")||Path.contains("sdb"))) {
                toastDialog.setMessage(R.string.udiskmounted);
                toastDialog.show();
            }

            flagfile = new File(Path + "/ch_usbupdate.xml");

            loger.i( "flagfile------->" + flagfile.exists());
            if (flagfile.exists()) {

                new Thread() {
                    @Override
                    public void run() {
                        while (true) {

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            bootstatus = SystemProperties.get("sys.boot_completed");
                            loger.i( "sys.boot_completed:" + bootstatus);
                            if (bootstatus.equals("1")) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                        Update update = new Update(context);
                        update.USBUpdate(Path);
                    }
                }.start();
            }
        } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
            toastDialog.setMessage(R.string.udiskremove);
            toastDialog.show();
        }
    }
}
