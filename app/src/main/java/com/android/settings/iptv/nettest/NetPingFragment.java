package com.android.settings.iptv.nettest;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.NetTracePingHelper;
import com.android.settings.iptv.util.VchCommonToastDialog;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author libeibei
 * Created by Administrator on 2018/2/1 0001.
 */
@SuppressLint("ValidFragment")
public class NetPingFragment extends Fragment implements View.OnClickListener {
    private static final int INIT_VIEW = 10001;
    private static final int START_PING = 10002;
    private static final int SHOW_LINE = 10003;
    private static final int PING_STOPED = 10004;
    private static final int SHOW_RESULT = 10005;
    private static final int Host_Unreachable = 10006;
    private Context context;
    private View root;
    private EditText et_address;
    public boolean isRunning = false;
    private boolean already_tested = false;
    private static PingCallBack callBack;
    private NetTracePingHelper netPingHelper;
    private Button start_ping;
    private TextView ping_result, ping_line;
    private VchCommonToastDialog dialog;

    private String Line = "";
    private List<String> lineList;
    private Loger loger = new Loger(NetPingFragment.class);

    public NetPingFragment(Context context) {
        this.context = context;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_VIEW:
                    initView();
                    break;
                case START_PING:
                    startPing();
                    break;
                case SHOW_LINE:
                    showLine();
                    break;
                case PING_STOPED:
                    stopPing();
                    break;
                case SHOW_RESULT:
                    showResult();
                    break;
                case Host_Unreachable:
                    ping_result.setText(R.string.network_ping_host_error);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.nettest_ping_fragment, null);
        handler.sendEmptyMessage(INIT_VIEW);
        return root;
    }

    /**
     * 获取回调实例
     *
     * @return
     */
    public static PingCallBack getCallback() {
        return callBack;
    }


    /**
     * 初始化工具参数及控件和UI
     */
    private void initView() {

        dialog = new VchCommonToastDialog(context);
        dialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        already_tested = false;
        isRunning = false;
        callBack = new PingCallBack(context);
        netPingHelper = new NetTracePingHelper();
        start_ping = (Button) root.findViewById(R.id.button_start_ping);
        ping_line = (TextView) root.findViewById(R.id.ping_line);
        ping_result = (TextView) root.findViewById(R.id.ping_result);
        et_address = (EditText) root.findViewById(R.id.edit_ping_address);
        ping_result.setText("点击开始按钮，开始测试.");

        start_ping.setOnClickListener(this);
        et_address.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                Editable editable = et_address.getText();
                int index = et_address.getSelectionStart();
                String passwd = et_address.getText().toString();
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keycode == KeyEvent.KEYCODE_BACK || keycode == KeyEvent.KEYCODE_DEL
                            || keycode == KeyEvent.KEYCODE_FORWARD_DEL) {
                        if (passwd.isEmpty() || index == 0) {

                        } else {
                            editable.delete(index - 1, index);
                            return true;
                        }
                    }

                }

                return false;
            }
        });
    }


    private void startPing() {
        isRunning = true;
        String ping_address = et_address.getText().toString();
        if (!NetTracePingHelper.isNetWorkConnected(context)) {
            dialog.setMessage("无网络，请连接网络后再进行测试");
            dialog.show();
            isRunning = false;
        } else if (!checkIsLegal(ping_address)) {
            dialog.setMessage(R.string.network_address_erro);
            dialog.setDuration(2);
            dialog.show();
        } else {
            //-c1是指ping的次数，-w是指执行的最后期限，也就是执行的时间，单位为秒  即超时时间设置为30ms
            String ping_cmd = "ping -c 10 -w 10";
            ping_result.setText("正在ping" + " " + ping_address);
            netPingHelper.startPing(ping_cmd, ping_address);
        }
    }

    private boolean checkIsLegal(String address) {
        if (address == null || "".equals(address)) {
            loger.i("输入的网址为空");
            return false;
        } else if (isURL(address)) {
            loger.i("输入的网址是网址");
            return true;
        } else if (!ipCheck(address)) {
            loger.i("ping 输入的ip地址是不合法的");
            return false;
        } else if (isIP(address)) {
            loger.i("输入的网址是IP");
            return true;
        } else {
            loger.i("输入的网址是不合法的");
            return false;
        }
    }
    /*
    private boolean checkIsLegal(String address) {
        if (address == null || "".equals(address)) {
            loger.i("输入的网址为空");
            return false;
        }else if(ipCheck(address)){
            loger.i("ping 输入的ip地址是合法的");
            return true;
        }else{
            loger.i("输入的网址是不合法的");
            return  false;
        }
    }
    */

    /**
     * 验证传入的字符串是否网址
     *
     * @param str
     * @return
     */
    private boolean isURL(String str) {
        String regex = "([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
        return match(regex, str);
    }

    /**
     * 验证传入的字符串是否符合IP地址
     *
     * @param str
     * @return
     */
    private boolean isIP(String str) {
        String num = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        String regex = "^" + num + "\\." + num + "\\." + num + "\\." + num + "$";
        return match(regex, str);
    }

    /**
     * 判断IP地址的合法性，这里采用了正则表达式的方法来判断
     * return true，合法
     */
    public boolean ipCheck(String text) {
        if (text != null && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        return false;
    }

    /**
     * 工具函数：
     * 验证传入的字符串是否符合传入的正则表达式
     *
     * @param regex
     * @param str
     * @return
     * @author libeibei
     */
    private boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_start_ping) {
            if (!isRunning) {
                //重新开始测试时，将内容清空
                if (already_tested) {
                    Line = "";
                    lineList = null;
                    ping_line.setText("");
                }
                handler.sendEmptyMessage(START_PING);
            } else {
                Toast.makeText(context, "正在测试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 在该界面第一次测试结束后
     * 按钮文字变为重新测试
     */
    private void initButtonText() {
        if (already_tested) {
            start_ping.setText(R.string.network_ping_restart);
        }
    }

    public class PingCallBack {
        private Context mContext;

        private PingCallBack(Context context) {
            mContext = context;
        }

        public void onShowPingLine(String line) {
            Line += line + "\n";
            handler.sendEmptyMessage(SHOW_LINE);
        }

        public void onStopPing(int result, List result_message) {
            if (!result_message.isEmpty()) {
                lineList = result_message;
                loger.e("lineList.size() = " + lineList.size());
                handler.sendEmptyMessage(PING_STOPED);
                handler.sendEmptyMessage(SHOW_RESULT);
            }
        }
    }

    private void showLine() {
        ping_line.setText(Line);
    }

    private void stopPing() {
        already_tested = true;
        isRunning = false;
    }

    private void showResult() {
        initButtonText();
        String lastLine = lineList.get(lineList.size() - 1);
        if (!TextUtils.isEmpty(lastLine) && lastLine.contains("pipe")) {
            //无法访问目标主机
            handler.sendEmptyMessage(Host_Unreachable);
            return;
        }

        //获取ping测试包含结果的那一行
        String resultline = lineList.get(lineList.size() - 2);
        //丢包率
        String packetLossRate = getRate(resultline);
        //网络连通性
        String Networkconnectivity = getConn(packetLossRate);
        //获取ping值 区分ping的通与ping不通
        loger.e("packetLossRate = " + packetLossRate + ", Networkconnectivity = " + Networkconnectivity);

        if (lineList.size() > 5) {
            float ping = getPing(lineList);
            ping_result.setText("测试完成: 丢包率 = " + packetLossRate + "% , 平均Ping值 = " + ping
                    + " ms, 网络连通性: " + Networkconnectivity);
        } else {
            ping_result.setText("测试完成: 丢包率 = " + packetLossRate + "% ," + "网络连通性: " + Networkconnectivity);
        }

    }

    /**
     * 从ping结果中：
     * 10 packets transmitted, 10 received, 0% packet loss, time 9011ms
     * 解析出网络丢包率
     */
    private String getRate(String line) {
        String[] a = line.split(",");
        Log.e("LBB", "获取丢包率Line = " + line);
        //char rate = a[2].charAt(1);
        String rate = a[2].substring(1, a[2].indexOf("%"));
        return rate;
    }

    private String getConn(String rate) {
        int Rate = Integer.parseInt(rate);
        if (Rate == 0) {
            return "很好";
        } else if (Rate == 100) {
            return "连接失败";
        } else if (Rate <= 50) {
            return "一般";
        } else {
            return "较差";
        }
    }

    /**
     * 解析平均Ping值
     *
     * @param list
     * @return
     */
    private float getPing(List<String> list) {
        float ping_average = 0;
        String line = list.get(list.size() - 1);
        String[] a = line.split("/");
        String ping = a[4];
        ping_average = Float.parseFloat(ping);
        return ping_average;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }
}
