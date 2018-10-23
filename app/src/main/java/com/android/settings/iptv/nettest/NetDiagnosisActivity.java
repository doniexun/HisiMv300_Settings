package com.android.settings.iptv.nettest;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.iptv.util.VchCommonToastDialog;
import com.android.settings.service.NetConnectService;
import com.android.settings.service.NetDnsService;
import com.android.settings.service.NetGatewayService;
import com.android.settings.service.NetHttpService;
import com.android.settings.service.NetIpService;
import com.android.settings.service.NetMulticastService;
import com.android.settings.service.NetNtpService;
import com.android.settings.R;
import com.android.settings.BaseActivity;


/**
 * @author libeibei
 * Created by libeibei on 2017/12/19 0019.
 */

public class NetDiagnosisActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "NetDiagnosis";
    private  static CallBack callBack;
    private ImageView imageView_connectivity,imageView_ip,
            imageView_gateway,imageView_http,imageView_ntp,imageView_multicast,imageView_dns;
    private TextView diagnosis_result,net_connect,net_ip,net_gateway,net_http,net_ntp,net_multicast,net_dns;
    private Button startButton;
    private RadarView Radar;
    private final static int START_TEST = 1 ;
    private final static int START_IP_TEST = 2 ;
    private final static int START_GATEWAY_TEST = 3 ;
    private final static int START_HTTP_TEST = 4 ;
    private final static int START_NTP_TEST = 5 ;
    private final static int START_MULTICAST_TEST = 6 ;
    private final static int START_DNS_TEST = 7 ;
    private static boolean isRunning = false;
    private VchCommonToastDialog toastDialog =null;
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_TEST:
                    if(!isRunning){
                        initUI();
                        startConnectTest();
                        diagnosis_result.setText("正在诊断...");
                        diagnosis_result.setTextColor(Color.WHITE);
                        isRunning = true;
                        Radar.startSearching();
                        Radar.addPoint();
                    }
                    break;
                case START_IP_TEST:
                    if(isRunning){
                        startIpTest();
                        Radar.addPoint();
                    }
                    break;
                case START_GATEWAY_TEST:
                    if(isRunning) {
                        startGateWayTest();
                        Radar.addPoint();
                    }
                    break;
                case START_HTTP_TEST:
                    if(isRunning) {
                        startHttpTest();
                        Radar.addPoint();
                    }
                    break;
                case START_NTP_TEST:
                    if(isRunning) {
                        startNtpTest();
                        Radar.addPoint();
                    }
                    break;
                case START_MULTICAST_TEST:
                    if(isRunning) {
                        startMultiCastTest();
                        Radar.addPoint();
                    }
                    break;
                case START_DNS_TEST:
                    if(isRunning) {
                        startDnsTest();
                        Radar.addPoint();
                    }
                    break;

                default:
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nettest_diagnosis);
        init();
    }
    private void init(){
        callBack =new CallBack(NetDiagnosisActivity.this);

        toastDialog = new VchCommonToastDialog(this);
        toastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        toastDialog.getWindow().setType(2003);

        diagnosis_result = (TextView) findViewById(R.id.diagnosis_result);
        net_connect = (TextView) findViewById(R.id.textView_connectivity);
        net_ip = (TextView) findViewById(R.id.textView_ip);
        net_gateway = (TextView) findViewById(R.id.textView_gateway);
        net_http = (TextView) findViewById(R.id.textView_http);
        net_ntp = (TextView) findViewById(R.id.textView_ntp);
        net_multicast = (TextView) findViewById(R.id.textView_multicast);
        net_dns =(TextView) findViewById(R.id.textView_dns);

        imageView_connectivity = (ImageView) findViewById(R.id.imageview_connectivity);
        imageView_ip = (ImageView) findViewById(R.id.imageview_ip);
        imageView_gateway = (ImageView) findViewById(R.id.imageview_gateway);
        imageView_http = (ImageView) findViewById(R.id.imageview_http);
        imageView_ntp = (ImageView) findViewById(R.id.imageview_ntp);
        imageView_multicast = (ImageView) findViewById(R.id.imageview_multicast);
        imageView_dns = (ImageView) findViewById(R.id.imageview_dns);

        startButton = (Button) findViewById(R.id.diagnosis_start);
        startButton.setOnClickListener(this);

        Radar = (RadarView) findViewById(R.id.radar_view);
    }

    /**
     * 初始化测试结果
     * 将上次的测试结果清空
     */
    private void initUI(){
        imageView_connectivity.setImageResource(0);
        imageView_ip.setImageResource(0);
        imageView_gateway.setImageResource(0);
        imageView_http.setImageResource(0);
        imageView_ntp.setImageResource(0);
        imageView_multicast.setImageResource(0);
        imageView_dns.setImageResource(0);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                diagnosis_result.setText("正在终止诊断...");
                stopAllService();
                isRunning = false;
                finish();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.diagnosis_start){
            if(isRunning){
                toastDialog.setMessage(R.string.network_diagnosis_yet);
                toastDialog.setDuration(1);
                toastDialog.show();
            }else{
                handler.sendEmptyMessage(START_TEST);
            }
        }
    }

    private void stopAllService(){
        Log.i(TAG,"stopAllService().....");
        if(NetConnectService.isRunning){
            stopConnectTest();
        }if(NetIpService.isRunning){
            stopIpTest();
        }if(NetGatewayService.isRunning){
            stopGateWayTest();
        }if(NetHttpService.isRunning){
            stopHttpTest();
        }if(NetNtpService.isRunning){
            stopNtpTest();
        }if(NetMulticastService.isRunning){
            stopMultiCastTest();
        }if(NetDnsService.isRunning){
            stopDnsTest();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startConnectTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetConnectService");
        startService(intent);
    }
    private void stopConnectTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetConnectService");
        stopService(intent);
    }
    private void startIpTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetIpService");
        startService(intent);
    }
    private void stopIpTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetIpService");
        stopService(intent);
    }
    private void startGateWayTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetGatewayService");
        startService(intent);

    }
    private void stopGateWayTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetGatewayService");
        stopService(intent);

    }
    private void startHttpTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetHttpService");
        startService(intent);
    }
    private void stopHttpTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetHttpService");
        stopService(intent);
    }
    private void startNtpTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetNtpService");
        startService(intent);
    }
    private void stopNtpTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetNtpService");
        stopService(intent);
    }
    private void startMultiCastTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetMulticastService");
        startService(intent);
    }
    private void stopMultiCastTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetMulticastService");
        stopService(intent);
    }
    private void startDnsTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetDnsService");
        startService(intent);
    }
    private void stopDnsTest(){
        Intent intent = new Intent();
        intent.setAction("com.android.settings.service.NetDnsService");
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 测试完成,回调这个函数
     * 1.显示测试结果
     * 2.停止动画
     */
    private void onDiagnosisFinished(boolean isFoceFinish){
        diagnosis_result.setText("网络诊断完成");
        startButton.setEnabled(true);
        Radar.stopSearching();
        isRunning = false;
    }

    /**
     * 回调接口
     * 各个回调函数，用于网络诊断模块中
     * 一 ：异步任务，通过回调返归测试结果
     * 二：更新界面UI
     * 三：开启下一异步任务
     * @return
     */
    public static CallBack getCallBack(){
        return callBack;
    }

    public class CallBack{
        public Context mcontext;
        public CallBack(Context context){mcontext =context;}

        /**
         * 回调一
         * @param connect
         */
        public void onNetConnectServiceStoped(boolean connect){

            if(connect){
                net_connect.setTextColor(Color.WHITE);
                imageView_connectivity.setImageResource(R.drawable.common_icon_checkbox);
                handler.sendEmptyMessage(START_IP_TEST);
            }else{
                net_connect.setTextColor(Color.WHITE);
                imageView_connectivity.setImageResource(R.drawable.common_icon_error);
                onDiagnosisFinished(true);
            }
        }

        /**
         *回调二
         * @param IpAllowed
         */
        public void onNetIpServiceStoped(boolean IpAllowed){

            if(IpAllowed){
                net_ip.setTextColor(Color.WHITE);
                imageView_ip.setImageResource(R.drawable.common_icon_checkbox);
                handler.sendEmptyMessage(START_GATEWAY_TEST);
            }else{
                net_ip.setTextColor(Color.WHITE);
                imageView_ip.setImageResource(R.drawable.common_icon_error);
                onDiagnosisFinished(true);
            }
        }

        /**
         * 回调三
         * @param result
         */
        public void onNetGatewayServiceStoped(boolean result){
            if(result){
                net_gateway.setTextColor(Color.WHITE);
                imageView_gateway.setImageResource(R.drawable.common_icon_checkbox);
                handler.sendEmptyMessage(START_HTTP_TEST);
            }else{
                net_gateway.setTextColor(Color.WHITE);
                imageView_gateway.setImageResource(R.drawable.common_icon_error);
                onDiagnosisFinished(true);
            }
        }

        /**
         *回调四
         * @param request
         */
        public void onNetHttpServiceStoped(boolean request){
            if(request){
                net_http.setTextColor(Color.WHITE);
                imageView_http.setImageResource(R.drawable.common_icon_checkbox);
                handler.sendEmptyMessage(START_NTP_TEST);
            }else{
                net_http.setTextColor(Color.WHITE);
                imageView_http.setImageResource(R.drawable.common_icon_error);
                handler.sendEmptyMessage(START_NTP_TEST);
            }
        }

        /**
         *回调五
         * @param request
         */
        public void onNetNtpServiceStoped(boolean request){
            if(request){
                net_ntp.setTextColor(Color.WHITE);
                imageView_ntp.setImageResource(R.drawable.common_icon_checkbox);
                handler.sendEmptyMessage(START_MULTICAST_TEST);
            }else{
                net_ntp.setTextColor(Color.WHITE);
                imageView_ntp.setImageResource(R.drawable.common_icon_error);
                handler.sendEmptyMessage(START_MULTICAST_TEST);
            }

        }

        /**
         * 回调六
         * 组播获取
         * @param result
         */
        public void onNetMultiCastStoped(boolean result){
            if(result){
                net_multicast.setTextColor(Color.WHITE);
                imageView_multicast.setImageResource(R.drawable.common_icon_checkbox);
                handler.sendEmptyMessage(START_DNS_TEST);
            }else{
                net_multicast.setTextColor(Color.WHITE);
                imageView_multicast.setImageResource(R.drawable.common_icon_error);
                handler.sendEmptyMessage(START_DNS_TEST);
            }
        }

        /**
         *回调七
         * @param request
         */
        public void onNetDnsServiceStoped(boolean request){
            if(request){
                net_dns.setTextColor(Color.WHITE);
                imageView_dns.setImageResource(R.drawable.common_icon_checkbox);
            }else{
                net_dns.setTextColor(Color.WHITE);
                imageView_dns.setImageResource(R.drawable.common_icon_error);
            }
            onDiagnosisFinished(false);
        }



    }

}
