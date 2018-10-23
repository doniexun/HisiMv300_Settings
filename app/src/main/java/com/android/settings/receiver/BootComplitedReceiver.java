package com.android.settings.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.settings.iptv.util.Loger;

/**
 * @author libeibei
 * Created by Administrator on 2018/1/30 0030.
 */

public class BootComplitedReceiver extends BroadcastReceiver {
    private Loger loger = new Loger(BootComplitedReceiver.class);
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action!=null){
            if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
                loger.e("开机正常！！");
            }
        }
    }
}
