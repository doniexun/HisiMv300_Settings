package com.android.settings.iptv.nettest;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.NetTracePingHelper;
import com.android.settings.iptv.util.TracerouteContainer;
import com.android.settings.iptv.util.VchCommonToastDialog;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author libeibei
 *         Created by author on 2018/2/7 0007.
 */
@SuppressLint("ValidFragment")
public class NetTracertFragment extends Fragment implements View.OnClickListener {
    private static final int INIT_VIEW = 10001;
    private static final int START_TRACERT = 10002;
    private static final int SHOW_LINE = 10003;
    private static final int TRACERT_STOPED = 10004;
    private static final int NEW_TRACE_TASK = 10005;
    private boolean isRunning = false;
    private boolean already_tested = false;
    View root;
    Context mcontext;
    Button start;
    VchCommonToastDialog dialog;
    EditText et_address;
    TextView tv_line, tv_result;
    private String ip_address;
    private String ip_line = "";
    /**
     * 最大生存时间
     */
    private static int MAX_TTL = 30;
    private int ttl = 1;
    private String ipToPing;
    /**
     * ping耗时
     */
    private float elapsedTime;

    private static final String PING = "PING";
    private static final String FROM_PING = "From";
    private static final String SMALL_FROM_PING = "from";
    private static final String PARENTHESE_OPEN_PING = "(";
    private static final String PARENTHESE_CLOSE_PING = ")";
    private static final String TIME_PING = "time=";
    private static final String EXCEED_PING = "exceed";
    private static final String UNREACHABLE_PING = "100%";
    private Loger loger = new Loger(NetTracertFragment.class);

    private List<TracerouteContainer> traces = new ArrayList<TracerouteContainer>();


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_VIEW:
                    initView();
                    break;
                case START_TRACERT:
                    startTracert();
                    break;
                case NEW_TRACE_TASK:
                    break;
                case SHOW_LINE:
                    showResult();
                    break;
                case TRACERT_STOPED:
                    stopTracert();
                    break;
                default:
                    break;
            }
        }
    };


    public NetTracertFragment(Context context) {
        mcontext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.nettest_tracert_fragment, null);
        handler.sendEmptyMessage(INIT_VIEW);
        return root;
    }

    private void initView() {
        dialog = new VchCommonToastDialog(mcontext);
        dialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        et_address = (EditText) root.findViewById(R.id.edit_tracert_address);
        tv_result = (TextView) root.findViewById(R.id.tracert_result);
        tv_line = (TextView) root.findViewById(R.id.tracert_line);
        start = (Button) root.findViewById(R.id.button_start_tracert);
        start.setOnClickListener(this);
    }

    /**
     * 更新按钮内容：
     * 当测试过一次之后，按钮会变为“重新测试”
     */
    private void initButtonText() {
        if (already_tested) {
            start.setText(R.string.network_ping_restart);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_start_tracert) {
            if (!isRunning) {
                //已经测试过的，需要清空上次的测试记录
                if (already_tested) {
                    ttl = 1;
                    traces = new ArrayList<TracerouteContainer>();
                    ip_line = "";
                    tv_line.setText(ip_line);
                }
                handler.sendEmptyMessage(START_TRACERT);
            } else {
                dialog.setMessage("已在测试中");
                dialog.show();
            }
        }
    }

    private void startTracert() {
        String address = et_address.getText().toString();
        if (!NetTracePingHelper.isNetWorkConnected(mcontext)) {
            dialog.setMessage("无网络，请连接网络后再进行测试");
            dialog.show();
        } else if (!checkIsLegal(address)) {
            dialog.setMessage(R.string.network_address_erro);
            dialog.setDuration(2);
            dialog.show();
        } else {
            ip_address = address;
            tv_result.setText("正在测试...");
            new ExecuteTracerouteAsyncTask(MAX_TTL, ip_address).execute();
            loger.e( "开始Trace测试");
        }

    }

    private void stopTracert() {
        already_tested = true;
        tv_result.setText("测试完成");
        String res_line = tv_line.getText().toString();
        res_line += "\n结束";
        tv_line.setText(res_line);
        initButtonText();
    }

    /**
     * 异步线程，用于执行ping命令
     * 并得到和解析返回结果
     */
    private class ExecuteTracerouteAsyncTask extends AsyncTask<Void, Void, String> {
        private int maxTtl;
        private String url;

        public ExecuteTracerouteAsyncTask(int maxTtl, String url) {
            this.maxTtl = maxTtl;
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String res = "";
            try {
                res = launchPing(url);
            } catch (IOException e) {
                loger.e( e.toString());
            }
            TracerouteContainer trace;
            if (res.contains(UNREACHABLE_PING) && !res.contains(EXCEED_PING)) {
                trace = new TracerouteContainer("", parseIpFromPing(res), elapsedTime);
            } else {
                trace = new TracerouteContainer("", parseIpFromPing(res),
                        ttl == maxTtl ? Float.parseFloat(parseTimeFromPing(res)) : elapsedTime);
            }
            loger.e( " ip = " + parseIpFromPing(res));
            InetAddress address;
            try {
                address = InetAddress.getByName(trace.getIp());
                String hostName = address.getHostName();
                trace.setHostName(hostName);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            traces.add(trace);
            return res;
        }

        /**
         * 执行“ping”命令的函数
         * 返回值是ping的执行结果打印
         * @param url
         * @return
         * @throws IOException         */
        private String launchPing(String url) throws IOException {
            loger.e( "launch Ping ...");
            Process p;
            String command = "";
            //第一次调用的时候 ttl的值为1
            String format = "ping -c 1 -t %d ";
            command = String.format(format, ttl);
            long startTime = System.nanoTime();
            // 加上url地址
            p = Runtime.getRuntime().exec(command + url);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String s;
            String res = "";
            while ((s = stdInput.readLine()) != null) {
                res += s + "\n";
                if (s.contains(FROM_PING) || s.contains(SMALL_FROM_PING)) {
                    elapsedTime = (System.nanoTime() - startTime) / 1000000.0f;
                }
            }

            p.destroy();
            if (res.equals("")) {
                throw new IllegalArgumentException();
            }
            // 第一次调用ping命令的时候 记得把取得的最终的ip地址 赋给外面的ipToPing
            // 后面要依据这个ipToPing的值来判断是否到达ip数据报的 终点
            if (ttl == 1) {
                ipToPing = parseIpToPingFromPing(res);
                loger.e( "ipToPing = " + ipToPing);
            }
            //loger.e( "launch Ping Res = " + res);
            return res;
        }

        @Override
        protected void onPostExecute(String res) {
            if (TextUtils.isEmpty(res)) {
                return;
            }
            //当前ip地址已经到达目标ip地址，结束
            if (traces.get(traces.size() - 1).getIp().equals(ipToPing)) {
                handler.sendEmptyMessage(SHOW_LINE);
                handler.sendEmptyMessage(TRACERT_STOPED);
            } else {
                if (ttl < maxTtl) {
                    ttl++;
                    new ExecuteTracerouteAsyncTask(maxTtl, url).execute();
                }
                handler.sendEmptyMessage(SHOW_LINE);
            }
            super.onPostExecute(res);
        }
    }

    private void showResult() {
        ip_line = "目标主机 ip:(" + ipToPing + ")\n\n";
        int count = 0;
        for (TracerouteContainer trace : traces) {
            if (!TextUtils.isEmpty(trace.getIp())) {
                count++;
                ip_line += "host " + count + " : " + trace.getIp() + "\n";
            }
        }
        tv_line.setText(ip_line);
    }

    /**
     * 从结果集中解析出ip
     * @param ping
     * @return
     */
    private String parseIpFromPing(String ping) {
        String ip = "";
        if (ping.contains(FROM_PING)) {
            int index = ping.indexOf(FROM_PING);

            ip = ping.substring(index + 5);
            if (ip.contains(PARENTHESE_OPEN_PING)) {
                int indexOpen = ip.indexOf(PARENTHESE_OPEN_PING);
                int indexClose = ip.indexOf(PARENTHESE_CLOSE_PING);

                ip = ip.substring(indexOpen + 1, indexClose);
            } else {
                ip = ip.substring(0, ip.indexOf("\n"));
                if (ip.contains(":")) {
                    index = ip.indexOf(":");
                } else {
                    index = ip.indexOf(" ");
                }

                ip = ip.substring(0, index);
            }
        } else {
            int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
            int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

            ip = ping.substring(indexOpen + 1, indexClose);
        }

        return ip;
    }

    /**
     * 从结果中解析出ip值
     *
     * @param ping
     * @return
     */
    private String parseIpToPingFromPing(String ping) {
        String ip = "";
        if (ping.contains(PING)) {
            int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
            int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

            ip = ping.substring(indexOpen + 1, indexClose);
        }

        return ip;
    }

    /**
     * 从结果集中解析出time
     *
     * @param ping
     * @return
     */
    private String parseTimeFromPing(String ping) {
        String time = "";
        if (ping.contains(TIME_PING)) {
            int index = ping.indexOf(TIME_PING);

            time = ping.substring(index + 5);
            index = time.indexOf(" ");
            time = time.substring(0, index);
        }

        return time;
    }

    private boolean checkIsLegal(String address) {
        if (address == null || "".equals(address)) {
            loger.i( "输入的网址为空");
            return false;
        } else if (isURL(address)) {
            loger.i( "输入的是网址");
            return true;
        }else if (!ipCheck(address)) {
            loger.i( "Tracert 输入的ip地址是不合法的");
            return false;
        }else if (isIP(address)) {
            loger.i( "输入的网址是IP");
            return true;
        }else {
            loger.i( "输入的网址是不合法的");
            return false;
        }
    }
    /*
    private boolean checkIsLegal(String address) {
        if (address == null || "".equals(address)) {
            loger.i( "输入的网址为空");
            return false;
        } else if (ipCheck(address)) {
            loger.i( "Tracert 输入的ip地址是合法的");
            return true;
        } else {
            loger.i( "输入的网址是不合法的");
            return false;
        }
    }
    */
    /**
     * 正则表达式
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
     * 正则表达式
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
     * */
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



    /**
     * 传凯调用底层实现的Traceroute功能代码
     * 如下
     */
    private boolean hasdata = false;
    private String buffer = "";

    public String Tracert() {
        new Thread() {
            @Override
            public void run() {
                try {
                    loger.e( "Tracert()...");
                    byte[] buf = new byte[4096];
                    int count = 0;
                    int len = 0;
                    Socket socket = new Socket("127.0.0.1", 8000);
                    loger.e( "创建 Socket = " + socket.toString());

                    DataOutputStream writetoserver = new DataOutputStream(socket.getOutputStream());
                    DataInputStream get = new DataInputStream(socket.getInputStream());
                    String traceip = ip_address;
                    loger.i( "traceip:" + traceip);
                    String str = "busybox traceroute -w 1 -m 6 -q 1 " + traceip;
                    loger.i( "send to execmd:" + str);
                    writetoserver.write(str.getBytes());
                    while (true) {
                        loger.i( "begin read try :" + String.valueOf(count));
                        count++;
                        while (get.available() > 0) {
                            loger.i( " enter read data");
                            len = get.read(buf);
                            buffer += new String(buf, 0, len);
                            loger.i( "read  data:" + buffer);
                            hasdata = true;
                        }
                        if (len == 0) {
                            loger.i( " len==0 sleep");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if (count > 50) {
                                hasdata = false;
                                count = 0;
                                loger.i( " count to be max break");
                                loger.i( " get data break");
                                Intent intent = new Intent();
                                intent.setAction("com.stb.netmanager.notify");
                                intent.putExtra("action", "Tracert");
                                intent.putExtra("result", "1");
                                intent.putExtra("resultdesc", buffer);
                                mcontext.sendBroadcast(intent);
                                break;
                            }
                        }
                        if (hasdata) {
                            buffer.replace(" ", "");
                            loger.i( "send buffer:" + buffer);
                            loger.i( " get data break");
                            Intent intent = new Intent();
                            intent.setAction("com.stb.netmanager.notify");
                            intent.putExtra("action", "Tracert");
                            intent.putExtra("result", "0");
                            intent.putExtra("resultdesc", buffer);
                            mcontext.sendBroadcast(intent);
                            hasdata = false;
                            count = 0;
                            break;
                        }
                    }
                    writetoserver.close();
                    socket.close();
                    get.close();
                } catch (UnknownHostException e) {
                    loger.e( "UnknownHostException");
                    e.printStackTrace();
                } catch (IOException e) {
                    loger.e( "IOException");
                    e.printStackTrace();
                }
            }
        }.start();
        return "{\"state\":\"1\",\"errInfo\":\"\"}";
    }
}
