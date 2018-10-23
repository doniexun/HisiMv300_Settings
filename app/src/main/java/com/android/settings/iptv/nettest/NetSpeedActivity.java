package com.android.settings.iptv.nettest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.BaseActivity;
import com.android.settings.iptv.util.VchCommonToastDialog;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.NetSpeedHelper;

import java.util.ArrayList;

/**
 * Created by libeibei on 2017/12/19 0019.
 */
public class NetSpeedActivity extends BaseActivity implements View.OnClickListener{

    private Context mcontext;
    private TextView mtext_Speed,mtext_max_speed,mtext_max_speed_title,mtext_average_speed,mtext_average_speed_title;
    private Button start;
    //private Button stop;
    private ImageView pointer;
    private boolean isRunning;
    private ArrayList<Long> speedList;
    private long begin = 0,speed =0,average =0,max =0;
    private static final String ACTION ="com.changhong.netspeed";
    private static final int UPDATE_VIEW = 0;
    private static final int SHOW_RESULT =1;
    private MyReceiver myReceiver;
    private ConnectivityManager manager;
    private Loger loger = new Loger(NetSpeedActivity.class);

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what){
                case UPDATE_VIEW:
                    updateView();
                    break;
                case SHOW_RESULT:
                    showResult();
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nettest_speed);
        mcontext = NetSpeedActivity.this;
        initView();
    }
    private void initView(){
        manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mtext_Speed = (TextView)findViewById(R.id.textView_speed);
        mtext_average_speed=(TextView)findViewById(R.id.textView_average_speed);
        mtext_average_speed_title =(TextView)findViewById(R.id.textView_average_title);
        mtext_max_speed = (TextView)findViewById(R.id.textView_max_speed);
        mtext_max_speed_title = (TextView)findViewById(R.id.textView_max_title);
        start = (Button) findViewById(R.id.start_speed);
        //stop = (Button) findViewById(R.id.stop_speed);
        pointer = (ImageView)findViewById(R.id.iv_pointer) ;
        start.setOnClickListener(this);
        //stop.setOnClickListener(this);
        isRunning = false;
        myReceiver = new MyReceiver();
        IntentFilter filter =new IntentFilter();
        filter.addAction(ACTION);
        registerReceiver(myReceiver,filter);
    }
    /**
     *更新网速显示，更新网速表盘动画
     **/
    private void updateView(){
        mtext_average_speed.setText(KbToMb(average)+" Mbit/s");
        mtext_max_speed.setText(KbToMb(max)+" Mbit/s");
        mtext_Speed.setText(KbToMb(speed)+" Mbit/s");
        startAnimation(speed);
    }

    private void showResult(){
        /*测试结束之后，button改为开始测速；同时toast提示测试结束；修改仪表盘下面数值为0*/
        start.setText("开始测速");
        Toast.makeText(mcontext,"测速结束", Toast.LENGTH_LONG).show();
        //有问题，先设置为0之后，updateView之后还在更新这个textview
        //mtext_Speed.setText("0.00"+" Mbit/s");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.start_speed:
                start();
                break;
           /* case R.id.stop_speed:
                stop();
                break;*/
            default :
                break;

        }
    }

    private void start (){
        speedList = new ArrayList<Long>();
         if(isconnect()){
             if(isRunning){
                 Toast.makeText(mcontext,"正在测速，请稍等", Toast.LENGTH_LONG).show();
             }else{
                 isRunning = true;
                 /*开始测速，button显示正在测速*/
                 start.setText("正在测速");
                 NetSpeedHelper.getInstance(mcontext).start();
                 timer.start();
             }
         }else{
             VchCommonToastDialog dialog =new VchCommonToastDialog(this);
             dialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
             dialog.getWindow().setType(2003);
             dialog.setDuration(2);
             dialog.setMessage(R.string.network_no_net);
             dialog.show();
         }
     }

     private void stop(){
         if(isRunning) {
             isRunning = false;
             NetSpeedHelper.getInstance(mcontext).stop();
             timer.cancel();
             handler.sendEmptyMessage(SHOW_RESULT);
         }
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        unregisterReceiver(myReceiver);
    }

    /**
     * Kb/s 转换为Mb/s ，且精确到小数点后两位：0.01Mb/s；
     * @param speed
     * @return
     */
    private String KbToMb(long speed){
        double Kb = (double)speed;
        double Mb =Kb/1024;
        String M = String.format("%.2f",Mb);
        loger.i("Kb = "+Kb+", Mb = "+Mb+", M = "+M);
        return M;
    }

    /**
     * 速度表盘动画；
     * 前两个参数定义旋转的起始和结束的度数，后两个参数定义圆心的位置；
     */
    protected void startAnimation(double d) {
        AnimationSet animationSet = new AnimationSet(true);
        int end = getDuShu(d);
        RotateAnimation rotateAnimation = new RotateAnimation(begin, end, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f);
        rotateAnimation.setDuration(1000);
        animationSet.addAnimation(rotateAnimation);
        pointer.startAnimation(animationSet);
        begin = end;
    }

    public int getDuShu(double number) {
        double a = 0;
        if (number >= 0 && number <= 512) {
            a = number / 128 * 15;
        } else if (number > 521 && number <= 1024) {
            a = number / 256 * 15 + 30;
        } else if (number > 1024 && number <= 10 * 1024) {
            a = number / 512 * 5 + 80;
        } else {
            a = 180;
        }
        return (int) a;
    }

    public boolean isconnect(){
        if(manager.getActiveNetworkInfo() == null){
            return false;
        }
        boolean avilable = manager.getActiveNetworkInfo().isAvailable();
        loger.i("avilable :" + avilable);
        NetworkInfo.State a = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        loger.i("wifi state :"+a);
        return avilable;
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            if(action.equals(ACTION)){
                speed = intent.getLongExtra("speed",0);
                average = intent.getLongExtra("average",0);
                loger.i("BroadcastReceiver：speed = "+ String.valueOf(speed) + "average =" + String.valueOf(average));
                handler.sendEmptyMessage(UPDATE_VIEW);
            }

        }
    }

    /**
     * 计时器，当测速超过一分钟
     * file仍然没有完成，则终止测速，显示当前平均速度；
     *
     */
    private CountDownTimer timer =new CountDownTimer(20000,500) {
        @Override
        public void onTick(long LeftTime) {
            //每两秒钟，收集一次当前网速,存储到列表中;
            Long curspeed =new Long(speed);
            speedList.add(curspeed);
            max = getMaxSpeed(speedList).longValue();
        }

        @Override
        public void onFinish() {
            loger.i("Speed Test Time over.");
            if(isRunning)stop();
        }
    };

    private Long getMaxSpeed(ArrayList<Long> arrayList){
        Long max = new Long(0);
        for(int i =0;i<arrayList.size();i++){
            if(arrayList.get(i).longValue() > max.longValue())
                max=arrayList.get(i);
        }
        return max;
    }
}
