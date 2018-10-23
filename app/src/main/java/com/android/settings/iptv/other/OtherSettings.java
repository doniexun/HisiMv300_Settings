package com.android.settings.iptv.other;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.android.settings.BaseActivity;
import com.android.settings.R;

/**
 * Created by Administrator on 2018/1/25 0025.
 */

public class OtherSettings extends BaseActivity implements View.OnFocusChangeListener {

    private Context mcontext;
    private FragmentManager fragmentManager;
    private MoreFunctionFragment moreFunctionFragment;
    private static final int INIT_VIEW = 10001;
    private Button bt_advance;

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
        setContentView(R.layout.activity_other);
        handler.sendEmptyMessage(INIT_VIEW);
    }

    private void initView() {
        mcontext = this;
        fragmentManager = this.getFragmentManager();
        bt_advance = (Button) findViewById(R.id.advance);
        show();
    }

    private void show() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (moreFunctionFragment == null) {
            moreFunctionFragment = new MoreFunctionFragment(mcontext);
        }
        fragmentTransaction.add(R.id.other_frameLayout, moreFunctionFragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onFocusChange(View view, boolean b) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (view.getId()) {
            case R.id.advance:
                if (b) {
                    if (moreFunctionFragment == null) {
                        moreFunctionFragment = new MoreFunctionFragment(mcontext);
                    }
                    fragmentTransaction.replace(R.id.other_frameLayout, moreFunctionFragment);
                }
                break;
            default:
                break;
        }
        fragmentTransaction.commit();
    }
}
