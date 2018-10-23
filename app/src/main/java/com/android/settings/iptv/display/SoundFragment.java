package com.android.settings.iptv.display;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.android.settings.R;
import com.hisilicon.android.hiaudiomanager.HiAudioManager;

@SuppressLint("ValidFragment")
public class SoundFragment extends Fragment {
    private View root;
    private Context context;
    private static final int MESSAGE_INIT = 101;
    private MainHandler mMainHandler = null;

    private RadioGroup sound_HDMI;
    private RadioGroup sound_SPDIF;
    private HiAudioManager mAudioManager;
    private static final int SOUND_HDMI = 1;
    private static final int SOUND_SPDIF = 2;

    public SoundFragment(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.display_sound, null);
        mMainHandler = new MainHandler(SoundFragment.this);
        mMainHandler.sendEmptyMessage(MESSAGE_INIT);
        return root;
    }

    static class MainHandler extends Handler {

        SoundFragment mContext = null;

        public MainHandler(SoundFragment context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INIT: {
                    mContext.initView();
                }
                break;

                default:
                    break;
            }
        }
    }

    ;

    private void initView() {
        sound_HDMI = (RadioGroup) root.findViewById(R.id.sound_hdmi);
        mAudioManager = new HiAudioManager();
        sound_HDMI.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.auto_HDMI:
                        mAudioManager.setAudioOutput(SOUND_HDMI, HiAudioManager.AUDIO_OUTPUT_MODE_AUTO);
                        break;
                    case R.id.HDMI:
                        mAudioManager.setAudioOutput(SOUND_HDMI, HiAudioManager.AUDIO_OUTPUT_MODE_RAW);
                        break;
                    case R.id.no_hdmi:
                        mAudioManager.setAudioOutput(SOUND_HDMI, HiAudioManager.AUDIO_OUTPUT_MODE_LPCM);
                        break;
                    default:
                        break;
                }

            }
        });
        sound_HDMI.check(getSoundChecked(mAudioManager.getAudioOutput(SOUND_HDMI),
                HiAudioManager.AUDIO_OUTPUT_PORT_HDMI));

        sound_SPDIF = (RadioGroup) root.findViewById(R.id.sound_spdif);
        sound_SPDIF.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.SPDIF:
                        mAudioManager.setAudioOutput(SOUND_SPDIF, HiAudioManager.AUDIO_OUTPUT_MODE_RAW);
                        break;
                    case R.id.no_spdif:
                        mAudioManager.setAudioOutput(SOUND_SPDIF, HiAudioManager.AUDIO_OUTPUT_MODE_LPCM);
                        break;

                    default:
                        break;
                }

            }
        });
        sound_SPDIF.check(getSoundChecked(mAudioManager.getAudioOutput(SOUND_SPDIF),
                HiAudioManager.AUDIO_OUTPUT_PORT_SPDIF));
    }

    private int getSoundChecked(int get, int mode) {
        int index = R.id.HDMI;
        switch (mode) {
            //HDMI
            case 1:
                if (get == HiAudioManager.AUDIO_OUTPUT_MODE_AUTO) {
                    index = R.id.auto_HDMI;
                } else if (get == HiAudioManager.AUDIO_OUTPUT_MODE_RAW) {
                    index = R.id.HDMI;
                } else {
                    index = R.id.no_hdmi;
                }
                break;
            //SDPIF
            case 2:
                if (get == HiAudioManager.AUDIO_OUTPUT_MODE_RAW) {
                    index = R.id.SPDIF;
                } else {
                    index = R.id.no_spdif;
                }
                break;

            default:
                break;
        }
        return index;
    }


}
