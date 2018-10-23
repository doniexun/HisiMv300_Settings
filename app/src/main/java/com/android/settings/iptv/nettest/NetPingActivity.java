package com.android.settings.iptv.nettest;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.android.settings.BaseActivity;
import com.android.settings.R;

/**
 * @author libeibei
 * Created by libeibei on 2018/2/1 0001.
 * 对应移动公司的要求
 * 在系统设置中添加Ping测试模块；
 */

public class NetPingActivity extends BaseActivity implements View.OnFocusChangeListener {

    private static final int INIT_VIEW = 10001;
    private FragmentManager fragmentManager;
    private NetPingFragment netPingFragment;
    private NetTracertFragment netTracertFragment;
    private Context context;
    private Button titlebt_ping, titlebt_trace;
    private boolean isActive = false;


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_VIEW:
                    initView();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nettest_ping_activity);
        handler.sendEmptyMessage(INIT_VIEW);
        isActive = true;
    }

    private void initView() {
        context = NetPingActivity.this;
        fragmentManager = getFragmentManager();

        titlebt_ping = (Button) findViewById(R.id.title_button_ping);
        titlebt_trace = (Button) findViewById(R.id.title_button_trace);
        titlebt_ping.setOnFocusChangeListener(this);
        titlebt_trace.setOnFocusChangeListener(this);
        titlebt_ping.requestFocus();
        initFragment();
    }

    private void initFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        netPingFragment = new NetPingFragment(context);
        transaction.add(R.id.ping_trace_frameLayout, netPingFragment);
        transaction.commit();
    }

    @Override
    public void onFocusChange(View view, boolean hasfocus) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (isActive) {

            if (view instanceof Button) {
                Button tmp = (Button) view;

                if (titlebt_ping.equals(tmp) && hasfocus) {
                    if (netPingFragment == null) {
                        netPingFragment = new NetPingFragment(context);
                    }
                    transaction.replace(R.id.ping_trace_frameLayout, netPingFragment);

                } else if (titlebt_trace.equals(tmp) && hasfocus) {
                    if (netTracertFragment == null) {
                        netTracertFragment = new NetTracertFragment(context);
                    }
                    transaction.replace(R.id.ping_trace_frameLayout, netTracertFragment);
                }

                transaction.commit();
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ESCAPE:
                    isActive = false;
                    this.finish();
                    break;
                case KeyEvent.KEYCODE_F5: {
                    return true;
                }
                case KeyEvent.KEYCODE_MENU: {
                    return true;
                }

                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
