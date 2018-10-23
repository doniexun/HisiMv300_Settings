package com.android.settings.iptv.nettest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.settings.R;
import com.android.settings.BaseActivity;

/**
 * @author libeibei
 * Created by libeibei on 2018/01/17 0017.
 */

public class NetTestSettingActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener{

    private FrameLayout diagnosis,speedtest,pingtest;
    private ImageView IV_diagnosis,IV_speedtest,IV_pingtest;
    private static boolean isActive = false;
    private Context mContext = NetTestSettingActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nettest_setting);
        InitView();
        isActive = true;
    }

    private void InitView(){

        diagnosis = (FrameLayout) findViewById(R.id.framelayout_diagnosis);
        speedtest = (FrameLayout) findViewById(R.id.framelayout_speedtest);
        pingtest = (FrameLayout) findViewById(R.id.framelayout_pingtest);
        IV_diagnosis = (ImageView)findViewById(R.id.imageview_diagnosis);
        IV_speedtest = (ImageView)findViewById(R.id.imageview_speed);
        IV_pingtest = (ImageView)findViewById(R.id.imageview_ping);
        diagnosis.setOnClickListener(this);
        speedtest.setOnClickListener(this);
        pingtest.setOnClickListener(this);
        diagnosis.setOnFocusChangeListener(this);
        speedtest.setOnFocusChangeListener(this);
        pingtest.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.framelayout_diagnosis:
                Intent intent_1 = new Intent();
                intent_1.setClassName("com.android.settings",
                        "com.android.settings.iptv.nettest.NetDiagnosisActivity");
                startActivity(intent_1);
                break;

            case R.id.framelayout_speedtest:
                Intent intent_2 = new Intent();
                intent_2.setClassName("com.android.settings",
                        "com.android.settings.iptv.nettest.NetSpeedActivity");
                startActivity(intent_2);

                break;
            case R.id.framelayout_pingtest:
                Intent intent_3 = new Intent();
                intent_3.setClassName("com.android.settings",
                        "com.android.settings.iptv.nettest.NetPingActivity");
                startActivity(intent_3);
                break;
            default:
                break;
        }

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()){
            case R.id.framelayout_diagnosis:
                if(b){
                    setViewZoomIn(view);
                    IV_diagnosis.setImageResource(R.drawable.icon_wangluozhenduan_foucs);
                }else{
                    setViewZoomOut(view);
                    IV_diagnosis.setImageResource(R.drawable.icon_wangluozhenduan);
                }
                break;
            case R.id.framelayout_speedtest:
                if(b){
                    setViewZoomIn(view);
                    IV_speedtest.setImageResource(R.drawable.icon_wangluocesu_foucs);
                }else{
                    setViewZoomOut(view);
                    IV_speedtest.setImageResource(R.drawable.icon_wangluocesu);
                }
                break;
            case R.id.framelayout_pingtest:
                if(b){
                    setViewZoomIn(view);
                    IV_pingtest.setImageResource(R.drawable.icon_ping_foucs);
                }else{
                    setViewZoomOut(view);
                    IV_pingtest.setImageResource(R.drawable.icon_ping);
                }
                break;
            default:
                break;
        }
    }

    private void setViewZoomIn(View v){
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation animation = new ScaleAnimation(1.0f,1.1f,1.0f,1.1f,
                Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(350);
        animation.setFillAfter(true);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animationSet);
    }

    private  void setViewZoomOut(View v){
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation animation = new ScaleAnimation(1.1f,1.0f,1.1f,1.0f,
                Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(350);
        animation.setFillAfter(true);
        animationSet.addAnimation(animation);
        animationSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animationSet);
    }
}
