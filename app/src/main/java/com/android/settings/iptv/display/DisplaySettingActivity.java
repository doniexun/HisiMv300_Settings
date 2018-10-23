package com.android.settings.iptv.display;


import com.android.settings.iptv.util.Loger;
import com.android.settings.BaseActivity;
import com.android.settings.R;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;

/**
 * @author libeibei for hisi display and sound settings;
 */
public class DisplaySettingActivity extends BaseActivity {
    private Context mContext;

    FragmentManager mFragmentManager;
    SoundFragment mDisplay_Sound;
    MediaSetFragment mDisplay_MediaListSet;
    OffSetFragment mDisplay_OffSet;
    private Button mSoundSet, mMediaSet, mDisplay_Off_Set,mMediaListSet;
    private static boolean isActive = false;
    public static boolean isAreaSetting = false;
    Loger loger = new Loger(DisplaySettingActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_setting);
        mFragmentManager = getFragmentManager();
        findView();
        mContext = DisplaySettingActivity.this;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        mDisplay_MediaListSet = new MediaSetFragment(DisplaySettingActivity.this);
        transaction.add(R.id.display_frameLayout, mDisplay_MediaListSet);
        transaction.commit();
        isActive = true;
    }

    private void findView() {
        mSoundSet = (Button) this.findViewById(R.id.sound_set);
        mMediaSet = (Button) this.findViewById(R.id.media_set);
        mMediaListSet = (Button) this.findViewById(R.id.display_media_set);
        mDisplay_Off_Set = (Button) this.findViewById(R.id.display_off_set);
        mSoundSet.setOnFocusChangeListener(focusChangeListener);
        mMediaSet.setOnFocusChangeListener(focusChangeListener);
        mMediaListSet.setOnFocusChangeListener(focusChangeListener);
        mDisplay_Off_Set.setOnFocusChangeListener(focusChangeListener);
    }

    private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // TODO Auto-generated method stub
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (isActive) {
                if (v instanceof Button) {
                    Button tmp = (Button) v;
                    if (mSoundSet.equals(tmp) && hasFocus) {
                        if (mDisplay_Sound == null) {
                            mDisplay_Sound = new SoundFragment(mContext);
                        }
                        transaction.replace(R.id.display_frameLayout, mDisplay_Sound);
                    }  else if (mDisplay_Off_Set.equals(tmp) && hasFocus) {
                        if (mDisplay_OffSet == null) {
                            mDisplay_OffSet = new OffSetFragment();
                        }
                        transaction.replace(R.id.display_frameLayout, mDisplay_OffSet);

                    } else if(mMediaListSet.equals(tmp) && hasFocus){
                        if(mDisplay_MediaListSet == null){
                            mDisplay_MediaListSet = new MediaSetFragment(mContext);
                        }
                        transaction.replace(R.id.display_frameLayout, mDisplay_MediaListSet);
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
                    if (!isAreaSetting) {
                        isActive = false;
                        this.finish();
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
    }
}
