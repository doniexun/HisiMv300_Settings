package com.android.settings.iptv.netinfo;

import com.android.settings.BaseActivity;
import com.android.settings.R;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by xixi on 2018/3/14.
 */

public class NetInfoActivity extends BaseActivity implements View.OnFocusChangeListener {

    private Context mContext ;
    private FragmentManager mFragmentManager;
    private Button bt_netinfo;
    //网络信息详情页
    private NetInfoFragment netInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_netinformation);
        initView();
    }

    private void initView(){
        mContext = NetInfoActivity.this;
        mFragmentManager = getFragmentManager();
        bt_netinfo = (Button) findViewById(R.id.netinformation);
        bt_netinfo.setOnFocusChangeListener(this);
        bt_netinfo.requestFocus();
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        switch (view.getId()){
            case R.id.netinformation:
                if(b){
                    if(netInfoFragment == null){
                        netInfoFragment = new NetInfoFragment(mContext);
                    }
                    transaction.replace(R.id.netinformation_frameLayout,netInfoFragment);
                }
                break;
            default:
                break;

        }
        transaction.commit();
    }
}
