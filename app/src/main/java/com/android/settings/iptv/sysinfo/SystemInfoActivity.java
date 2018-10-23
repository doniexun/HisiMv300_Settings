package com.android.settings.iptv.sysinfo;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.settings.R;
import com.android.settings.BaseActivity;

/**
 * @author libeibei
 * Created by libeibei on 2018/1/18 0018.
 */

public class SystemInfoActivity extends BaseActivity implements View.OnFocusChangeListener{

    private Context mContext;
    private FragmentManager mFragmentManager;
    private Button bt_device;
    private DeviceInfoFragment deviceInfoFragment;//设备信息详情页


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sysinfo);
        initView();
    }

    private void initView(){
        mContext = SystemInfoActivity.this;
        mFragmentManager = getFragmentManager();
        bt_device = (Button) findViewById(R.id.deviceInfo);
        bt_device.setOnFocusChangeListener(this);
        bt_device.requestFocus();
    }

    @Override
    public void onFocusChange(View view, boolean b) {

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        switch (view.getId()){
            case R.id.deviceInfo:
                if(b){
                    if(deviceInfoFragment == null){
                        deviceInfoFragment = new DeviceInfoFragment(mContext);
                    }
                    transaction.replace(R.id.sysInfo_frameLayout,deviceInfoFragment);
                }
                break;
            /*case R.id.netInfo:
                if(b){
                    if(netInfoFragment == null){
                        netInfoFragment = new NetInfoFragment(mContext);
                    }
                    transaction.replace(R.id.sysInfo_frameLayout,netInfoFragment);
                }
                break;

            case  R.id.version_Info:
                if(b){
                    if(versionInfoFragment == null){
                        versionInfoFragment = new VersionInfoFragment(mContext);
                    }
                    transaction.replace(R.id.sysInfo_frameLayout,versionInfoFragment);
                }
                break;*/

            default:
                break;

        }
        transaction.commit();
    }

}
