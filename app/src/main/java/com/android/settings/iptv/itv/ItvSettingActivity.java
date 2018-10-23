package com.android.settings.iptv.itv;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;

import com.android.settings.BaseActivity;
import com.android.settings.R;
import com.android.settings.iptv.util.Loger;

public class ItvSettingActivity extends BaseActivity {

    private Context mContext = ItvSettingActivity.this;

    FragmentManager mFragmentManager;
    private ItvAccountFragment mItvAccountFragment;
    private ItvServerFragment mItvServerFragment;
    public  Button mItvAccount, mItvServer;
    private static boolean isActive = false;
    private Loger loger= new Loger(ItvSettingActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itv_setting);
        mFragmentManager = getFragmentManager();
        mItvAccountFragment = new ItvAccountFragment(ItvSettingActivity.this);
        findView();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.itv_frameLayout, mItvAccountFragment);
        transaction.commit();
        isActive = true;
    }

    private void findView() {
        mItvAccount = (Button) this.findViewById(R.id.itv_account);
        mItvServer = (Button) this.findViewById(R.id.itv_server);
        mItvAccount.setOnFocusChangeListener(focusChangeListener);
        mItvServer.setOnFocusChangeListener(focusChangeListener);
    }

    private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            loger.i("onFocusChange");
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (isActive) {
                if (v instanceof Button) {
                    Button tmp = (Button) v;
                    if (mItvAccount.equals(tmp) && hasFocus) {
                        if (mItvAccountFragment == null) {
                            mItvAccountFragment = new ItvAccountFragment(ItvSettingActivity.this);
                        }
                        transaction.replace(R.id.itv_frameLayout, mItvAccountFragment);

                    } else if (mItvServer.equals(tmp) && hasFocus) {
                        if (ItvAccountFragment.mPasswordVisiable != null) {
                            ItvAccountFragment.mPasswordVisiable.setChecked(false);
                        }
                        if (mItvServerFragment == null) {
                            mItvServerFragment = new ItvServerFragment(ItvSettingActivity.this);
                        }
                        transaction.replace(R.id.itv_frameLayout, mItvServerFragment);

                    }
                    transaction.commit();
                }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ESCAPE:
                    isActive = false;
                    this.finish();
                    break;
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
