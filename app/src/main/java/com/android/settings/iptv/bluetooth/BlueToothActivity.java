package com.android.settings.iptv.bluetooth;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.BaseActivity;
import com.android.settings.R;
import com.android.settings.iptv.util.VchCommonToastDialog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlueToothActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "BlueTooth";
    private static final int FIND_VIEW = 0x10100;
    private static final int INIT_VIEW = 0x10101;

    private BtManager btManager;
    private Switch blue_switch;
    private Button blue_search;
    private TextView blue_info_title, blue_info;
    private ListView blue_list;
    private ProgressBar btl_bar;
    private List<BluetoothDevice> bltList;
    private MyAdapter myAdapter;
    //已经配对的设备
    private ListView bound_list;
    private List<BluetoothDevice> boundList;
    private BoundAdapter boundAdapter;
    private LinearLayout boundLayout;
    private VchCommonToastDialog toastDialog;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FIND_VIEW:
                    findView();
                    break;
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
        setContentView(R.layout.activity_bluetooth);
        btManager = BtManager.getInstance();
        btManager.initBltManager(BlueToothActivity.this);
        handler.sendEmptyMessage(FIND_VIEW);
        handler.sendEmptyMessage(INIT_VIEW);
    }


    private void findView() {
        blue_switch = (Switch) findViewById(R.id.blue_switch);
        blue_search = (Button) findViewById(R.id.blue_search);
        blue_search.setOnClickListener(this);
        //可用设备列表
        blue_list = (ListView) findViewById(R.id.bluetooth_info_list);
        //已配对设备列表
        bound_list = (ListView) findViewById(R.id.bluetooth_bound_list);
        //蓝牙关闭时的信息
        blue_info = (TextView) findViewById(R.id.bluetooth_info);
        //本机蓝牙信息
        blue_info_title = (TextView) findViewById(R.id.bluetooth_info_title);

        btl_bar = (ProgressBar) findViewById(R.id.btl_bar);


    }

    private void initView() {
        initToastDialog();
        initBoundedDevice();

        blue_list.setOnItemClickListener(new listClickListener());
        bltList = new ArrayList<>();
        myAdapter = new MyAdapter();
        blue_list.setAdapter(myAdapter);
        //注册蓝牙扫描广播
        blueToothRegister();
        //更新蓝牙开关状态
        checkBlueTooth();

        //显示本机蓝牙信息
        String name = btManager.getmBluetoothAdapter().getName();
        if (TextUtils.isEmpty(name)) {
            name = btManager.getmBluetoothAdapter().getAddress();
        }
        blue_info_title.setText(name);

    }

    /**
     * 打开界面时
     * 初始化已配对的设备
     */
    private void initBoundedDevice() {
        Set<BluetoothDevice> set = btManager.getmBluetoothAdapter().getBondedDevices();
        boundList = new ArrayList<>(set);
        boundAdapter = new BoundAdapter();
        bound_list.setAdapter(boundAdapter);
        bound_list.setOnItemClickListener(new boundedlistListener());
        bound_list.setOnItemLongClickListener(new boundedlistlongListener());

        //已配对的设备列表
        boundLayout = (LinearLayout) findViewById(R.id.layout_bounded);
        if (set == null || set.size() == 0) {
            //已配对的设备为空时
            inVisibleBoundLayout();
        } else {
            //有已配对设备
            visibleBoundLayout();
        }
        //重新扫描设备
        btManager.clickBlt(BlueToothActivity.this, BltContant.BLUE_TOOTH_SEARTH);
    }

    /**
     * 关闭蓝牙时
     * 清空蓝牙列表和其他空间
     */
    private void clearList() {
        blue_list.setVisibility(View.INVISIBLE);
    }

    /**
     * 打开蓝牙时
     * 重置蓝牙列表和其他空间
     */
    private void openList() {
        blue_list.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏已配对设备列表
     */
    private void inVisibleBoundLayout() {
        boundLayout.setVisibility(View.GONE);
    }

    /**
     * 显示已配对设备列表
     */
    private void visibleBoundLayout() {
        boundLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 检查蓝牙的开关状态
     */
    private void checkBlueTooth() {
        if (btManager.getmBluetoothAdapter() == null || !btManager.getmBluetoothAdapter().isEnabled()) {
            blue_switch.setChecked(false);
            Log.e(TAG, "11 蓝牙处于关闭状态！");
            clearList();
        } else {
            blue_switch.setChecked(true);
            Log.e(TAG, "22 蓝牙处于开启状态！");
            blue_info.setText("");
            openList();
        }
        blue_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //启用蓝牙
                    Log.e(TAG, "33 打开蓝牙!");
                    btManager.clickBlt(BlueToothActivity.this, BltContant.BLUE_TOOTH_OPEN);
                } else {
                    //禁用蓝牙
                    Log.e(TAG, "44 关闭！");
                    btManager.clickBlt(BlueToothActivity.this, BltContant.BLUE_TOOTH_CLOSE);
                    clearList();
                    //隐藏已连接设备列表
                    inVisibleBoundLayout();
                }
            }
        });
    }

    /**
     * 可用设备的点击事件
     */
    private class listClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BluetoothDevice bluetoothDevice = bltList.get(position);
            //链接的操作应该在子线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    btManager.createBond(bluetoothDevice, handler);
                }
            }).start();
        }
    }

    /**
     * 配对设备item监听点击事件
     */
    private class boundedlistListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BluetoothDevice bluetoothDevice = boundList.get(position);
            unpairDevice(bluetoothDevice);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    btManager.createBond(bluetoothDevice, handler);
//                }
//            }).start();

        }
    }

    /**
     * 配对设备item监听长点击事件
     */
    private class boundedlistlongListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final BluetoothDevice bluetoothDevice = boundList.get(position);
            unpairDevice(bluetoothDevice);
            return true;
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.blue_search:
                btManager.clickBlt(BlueToothActivity.this, BltContant.BLUE_TOOTH_SEARTH);
                break;
            default:
                break;
        }

    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(final BluetoothDevice device) {
        final WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        final View unpairDialog = View.inflate(this, R.layout.dialog_bluetooth_unpair, null);
        Button bt_ok = (Button) unpairDialog.findViewById(R.id.btn_ok_unpair);
        Button bt_cancel = (Button) unpairDialog.findViewById(R.id.btn_cancel_unpair);
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                wm.removeViewImmediate(unpairDialog);
            }
        });
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                    m.invoke(device, (Object[]) null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                wm.removeViewImmediate(unpairDialog);
            }
        });

        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.width = (int) getResources().getDimension(R.dimen.dialog_total_width);
        params.height = (int) getResources().getDimension(R.dimen.dialog_total_hight);
        params.x = Gravity.CENTER_HORIZONTAL;
        params.y = Gravity.CENTER_VERTICAL;
        params.format = PixelFormat.RGBA_8888;
        wm.addView(unpairDialog, params);
        unpairDialog.setVisibility(View.VISIBLE);
        bt_ok.setFocusable(true);
        bt_ok.setFocusableInTouchMode(true);
        bt_ok.requestFocus();
    }

    /**
     * 注册蓝牙回调广播
     */
    private void blueToothRegister() {
        btManager.registerBltReceiver(this, new BtManager.OnRegisterBltReceiver() {
            @Override
            public void onBtTurningOn() {
                Log.e(TAG, "正在打开蓝牙！");
                blue_info.setText(R.string.bluetooth_turning_on);
            }

            @Override
            public void onBtTurningOff() {
                Log.e(TAG, "正在关闭蓝牙！");
                blue_info.setText(R.string.bluetooth_turning_off);
            }

            @Override
            public void onBtOn() {
                Log.e(TAG, "蓝牙打开完成！");
                bltList = new ArrayList<>();
                blue_info.setText("");
                initBoundedDevice();
                openList();
            }

            @Override
            public void onBtOff() {
                Log.e(TAG, "蓝牙关闭完成！");
                blue_info.setText(R.string.bluetooth_closed);
            }

            @Override
            public void onStartScan() {
                Log.e(TAG, "蓝牙扫描开始！");
                btl_bar.setVisibility(View.VISIBLE);
                blue_search.setText(R.string.scaning);
                blue_search.setFocusable(false);
                blue_search.setBackground(null);
                //扫描过程中，取消蓝牙开关的点击
                blue_switch.setClickable(false);
            }

            @Override
            public void onFinishScan() {
                Log.e(TAG, "蓝牙扫描结束！");
                btl_bar.setVisibility(View.INVISIBLE);
                blue_search.setText(R.string.start_scan);
                //扫描结束后，重启蓝牙开关的点击
                blue_switch.setClickable(true);
                //扫描结束，重置搜索按钮
                blue_search.setFocusable(true);
                blue_search.setBackground(getResources().getDrawable(R.drawable.bluetooth_button_selector));
//                toastDialog.setMessage(R.string.scan_finished);
//                toastDialog.setDuration(1);
//                toastDialog.show();
            }

            /**搜索到新设备
             * @param device
             */
            @Override
            public void onBluetoothDevice(BluetoothDevice device) {
                Log.e(TAG, "发现新设备.onBluetoothDevice()");
                //设备不存在列表中，不能是已配对或者正在配对
                if (bltList != null && !bltList.contains(device)
                        && device.getBondState() != BluetoothDevice.BOND_BONDED
                        && device.getBondState() != BluetoothDevice.BOND_BONDING
                        && !TextUtils.isEmpty(device.getName())) {
                    bltList.add(device);
                }
                if (myAdapter != null) {
                    myAdapter.notifyDataSetChanged();
                }
            }

            /**连接中
             * @param device
             */
            @Override
            public void onBltIng(BluetoothDevice device) {
                Log.e(TAG, "设备配对中：" + device.getName());
                toastDialog.setMessage(R.string.device_bounding);
                toastDialog.setDuration(3);
                toastDialog.show();
            }

            /**连接完成
             * @param device
             */
            @Override
            public void onBltEnd(BluetoothDevice device) {
                Log.e(TAG, "已配对设备：" + device.getName());
                toastDialog.setMessage(R.string.device_bounded_success);
                toastDialog.show();
                //更新已配对列表
                initBoundedDevice();
                //更新可用设备列表
                if (bltList != null && bltList.contains(device)) {
                    bltList.remove(device);
                }
                if (myAdapter != null) {
                    myAdapter.notifyDataSetChanged();
                }
            }

            /**取消链接
             * @param device
             */
            @Override
            public void onBltNone(BluetoothDevice device) {
                btl_bar.setVisibility(View.GONE);
                if (boundList.contains(device)) {
                    Log.e(TAG, "取消已经配对成功的设备：" + device.getName());
                    initBoundedDevice();
                } else {
                    Log.e(TAG, "配对失败，设备：" + device.getName());
                    toastDialog.setMessage(R.string.device_bounded_failed);
                    toastDialog.show();
                }
            }

            @Override
            public void onBtConnecting(BluetoothDevice device) {
                Log.e(TAG, "设备连接中：" + device.getName());
            }

            @Override
            public void onBtConnected(BluetoothDevice device) {
                Log.e(TAG, "设备连接完成：" + device.getName());
            }

            @Override
            public void onBtDisConnected(BluetoothDevice device) {
                Log.e(TAG, "设备连接断开：" + device.getName());
            }

            @Override
            public void onBtDisConnecting(BluetoothDevice device) {
                Log.e(TAG, "设备连接断开中：" + device.getName());
            }

        });
    }


    /**
     * 可用设备的列表
     * 适配器
     */
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return bltList.size();
        }

        @Override
        public Object getItem(int position) {
            return bltList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder vh;
            BluetoothDevice device = bltList.get(position);
            int type = device.getBluetoothClass().getMajorDeviceClass();
            if (convertView == null) {
                v = getLayoutInflater().inflate(R.layout.bluetooth_item, null);
                vh = new ViewHolder();
                vh.blt_name = (TextView) v.findViewById(R.id.bluetooth_item_name);
                vh.blt_logo = (ImageView) v.findViewById(R.id.bluetooth_item_logo);
                // 将vh存储到行的Tag中
                v.setTag(vh);
            } else {
                v = convertView;
                // 取出隐藏在行中的Tag--取出隐藏在这一行中的vh控件缓存对象
                vh = (ViewHolder) convertView.getTag();
            }

            // 从ViewHolder缓存的控件中改变控件的值
            // 这里主要是避免多次强制转化目标对象而造成的资源浪费
            String name = device.getName();
            if (name == null) {
                name = device.getAddress();
            }
            vh.blt_name.setText(name);
            //Log.e(TAG, "蓝牙" + name + ",type = " + type);
            if (type == BluetoothClass.Device.Major.PHONE) {
                vh.blt_logo.setBackground(getResources().getDrawable(R.mipmap.btdev_3));
            } else if (type == BluetoothClass.Device.Major.MISC) {
                vh.blt_logo.setBackground(getResources().getDrawable(R.mipmap.btdev_2));
            } else if (type == BluetoothClass.Device.Major.COMPUTER) {
                vh.blt_logo.setBackground(getResources().getDrawable(R.mipmap.btdev_1));
            } else {
                vh.blt_logo.setBackground(null);
            }
            return v;
        }

        private class ViewHolder {
            TextView blt_name, blt_address, blt_type, blt_bond_state;
            ImageView blt_logo;
        }
    }


    /**
     * 已经配对的设备列表适配器
     */
    private class BoundAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return boundList.size();
        }

        @Override
        public Object getItem(int position) {
            return boundList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder vh;
            BluetoothDevice device = boundList.get(position);
            int type = device.getBluetoothClass().getMajorDeviceClass();
            if (convertView == null) {
                v = getLayoutInflater().inflate(R.layout.bluetooth_item, null);
                vh = new ViewHolder();
                vh.blt_name = (TextView) v.findViewById(R.id.bluetooth_item_name);
                vh.blt_logo = (ImageView) v.findViewById(R.id.bluetooth_item_logo);
                // 将vh存储到行的Tag中
                v.setTag(vh);
            } else {
                v = convertView;
                // 取出隐藏在行中的Tag--取出隐藏在这一行中的vh控件缓存对象
                vh = (ViewHolder) convertView.getTag();
            }

            // 从ViewHolder缓存的控件中改变控件的值
            // 这里主要是避免多次强制转化目标对象而造成的资源浪费
            String name = device.getName();
            if (name == null) {
                name = device.getAddress();
            }
            vh.blt_name.setText(name);
            //Log.e(TAG, "蓝牙" + name + ",type = " + type);
            if (type == BluetoothClass.Device.Major.PHONE) {
                vh.blt_logo.setBackground(getResources().getDrawable(R.mipmap.btdev_3));
            } else if (type == BluetoothClass.Device.Major.MISC) {
                vh.blt_logo.setBackground(getResources().getDrawable(R.mipmap.btdev_2));
            } else if (type == BluetoothClass.Device.Major.COMPUTER) {
                vh.blt_logo.setBackground(getResources().getDrawable(R.mipmap.btdev_1));
            } else {
                vh.blt_logo.setBackground(null);
            }

            return v;
        }

        private class ViewHolder {
            TextView blt_name, blt_address, blt_type, blt_bond_state;
            ImageView blt_logo;
        }
    }

    /**
     * 初始化ToastDialog
     */
    private void initToastDialog() {
        toastDialog = new VchCommonToastDialog(BlueToothActivity.this);
        toastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
        toastDialog.setDuration(1);
        toastDialog.getWindow().setType(2003);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //页面关闭的时候要断开蓝牙
        btManager.unregisterReceiver(this);
    }


}
