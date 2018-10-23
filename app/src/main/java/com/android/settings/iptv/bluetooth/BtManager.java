package com.android.settings.iptv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author libeibei
 * 2018/05/08
 */
public class BtManager {

    private static final String TAG = "BlueTooth";

    private BtManager() {
    }

    public static BtManager getInstance() {
        return new BtManager();
    }

    /**
     * 在使用蓝牙BLE之前，需要确认Android设备是否支持BLE feature(required为false时)，
     * 另外要需要确认蓝牙是否打开。
     *
     * @param context
     */
    public void initBltManager(Context context) {
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }


    /**
     * 蓝牙管理器
     */
    private BluetoothManager bluetoothManager;
    /**
     * 蓝牙适配器
     * BluetoothAdapter是Android系统中所有蓝牙操作都需要的，
     * 它对应本地Android设备的蓝牙模块，
     * 在整个系统中BluetoothAdapter是单例的。
     * 当你获取到它的实例之后，就能进行相关的蓝牙操作了。
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * 配对成功后的蓝牙套接字
     */
    private BluetoothSocket mBluetoothSocket;

    /**
     * 蓝牙状态接口
     */
    private OnRegisterBltReceiver onRegisterBltReceiver;

    public interface OnRegisterBltReceiver {

        void onBtConnecting(BluetoothDevice device);//建立连接中

        void onBtConnected(BluetoothDevice device);//连接建立成功

        void onBtDisConnecting(BluetoothDevice device);//断开连接中

        void onBtDisConnected(BluetoothDevice device);//断开连接成功

        void onBtOn();//蓝牙功能已打开

        void onBtOff();//蓝牙功能已关闭

        void onBtTurningOn();//蓝牙功能正在打开中

        void onBtTurningOff();//蓝牙功能正在关闭中

        void onStartScan();//开始搜索

        void onFinishScan();//搜索完成

        void onBluetoothDevice(BluetoothDevice device);//搜索到新设备

        void onBltIng(BluetoothDevice device);//配对中

        void onBltEnd(BluetoothDevice device);//配对完成

        void onBltNone(BluetoothDevice device);//取消配对
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothSocket getmBluetoothSocket() {
        return mBluetoothSocket;
    }

    /**
     * 注册广播来接收蓝牙配对信息
     *
     * @param context
     */
    public void registerBltReceiver(Context context, OnRegisterBltReceiver onRegisterBltReceiver) {
        this.onRegisterBltReceiver = onRegisterBltReceiver;
        // 用BroadcastReceiver来取得搜索结果
        IntentFilter intent = new IntentFilter();
        //搜索发现设备
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        //状态改变
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //行动扫描模式改变了
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //动作状态发生了变化
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //连接状态发生改变
        intent.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        //蓝牙开始扫描
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //蓝牙扫描结束
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        context.registerReceiver(searchDevices, intent);
    }

    /**
     * 反注册广播取消蓝牙的配对
     *
     * @param context
     */
    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(searchDevices);
        if (getmBluetoothAdapter() != null) {
            getmBluetoothAdapter().cancelDiscovery();
        }
    }

    /**
     * 蓝牙接收广播
     */
    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        //接收
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            if (b != null) {
                Object[] lstName = b.keySet().toArray();
                // 显示所有收到的消息及其细节
                for (int i = 0; i < lstName.length; i++) {
                    String keyName = lstName[i].toString();
                    Log.e(TAG, keyName + ">>>" + String.valueOf(b.get(keyName)));
                }
            }

            BluetoothDevice device;
            // 搜索发现设备时，取得设备的信息；注意，这里有可能重复搜索同一设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e(TAG, "收到新设备广播：BluetoothDevice.ACTION_FOUND");
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onRegisterBltReceiver.onBluetoothDevice(device);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                //状态改变时
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        //正在配对
                        Log.d(TAG, "正在配对......");
                        onRegisterBltReceiver.onBltIng(device);
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        //配对结束
                        Log.d(TAG, "完成配对");
                        onRegisterBltReceiver.onBltEnd(device);
                        break;
                    case BluetoothDevice.BOND_NONE:
                        //取消配对/未配对
                        Log.d(TAG, "取消配对");
                        onRegisterBltReceiver.onBltNone(device);
                    default:
                        break;
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //开始扫描
                Log.d(TAG, "扫描开始");
                onRegisterBltReceiver.onStartScan();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //结束扫描
                Log.d(TAG, "扫描结束");
                onRegisterBltReceiver.onFinishScan();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        onRegisterBltReceiver.onBtTurningOn();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        onRegisterBltReceiver.onBtTurningOff();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        onRegisterBltReceiver.onBtOn();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        onRegisterBltReceiver.onBtOff();
                        break;
                    default:
                        break;
                }
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (state) {
                    case BluetoothAdapter.STATE_CONNECTING:
                        onRegisterBltReceiver.onBtConnecting(device);
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        onRegisterBltReceiver.onBtConnected(device);
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING:
                        onRegisterBltReceiver.onBtDisConnecting(device);
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        onRegisterBltReceiver.onBtDisConnected(device);
                        break;
                    default:
                        break;
                }

            }
        }
    };

    public String bltStatus(int status) {
        String a = "未知状态";
        switch (status) {
            case BluetoothDevice.BOND_BONDING:
                a = "连接中";
                break;
            case BluetoothDevice.BOND_BONDED:
                a = "连接完成";
                break;
            case BluetoothDevice.BOND_NONE:
                a = "未连接/取消连接";
                break;
        }
        return a;
    }

    /**
     * 获得系统保存的配对成功过的设备，并尝试连接
     */
    private void getBltList() {
        if (getmBluetoothAdapter() == null) {
            return;
        }
        //获得已配对的远程蓝牙设备的集合
        Set<BluetoothDevice> devices = getmBluetoothAdapter().getBondedDevices();
        if (devices.size() > 0) {
            for (Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext(); ) {
                BluetoothDevice device = it.next();
                //自动连接已有蓝牙设备
                createBond(device, null);
            }
        }
    }

    /**
     * 尝试配对和连接
     *
     * @param btDev
     */
    public void createBond(BluetoothDevice btDev, Handler handler) {
        if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
            //如果这个设备取消了配对，则尝试配对
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                btDev.createBond();
            }
        } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
            //如果这个设备已经配对完成，则尝试连接
            connect(btDev, handler);
        }
    }

    /**
     * 尝试连接一个设备，子线程中完成，因为会线程阻塞
     *
     * @param btDev   蓝牙设备对象
     * @param handler 结果回调事件
     * @return
     */
    private void connect(BluetoothDevice btDev, Handler handler) {
        try {
            //通过和服务器协商的uuid来进行连接
            mBluetoothSocket = btDev.createRfcommSocketToServiceRecord(BltContant.SPP_UUID);
            if (mBluetoothSocket != null) {
                //全局只有一个bluetooth，所以我们可以将这个socket对象保存在appliaction中
                BltAppliaction.bluetoothSocket = mBluetoothSocket;
            }
            //通过反射得到bltSocket对象，与uuid进行连接得到的结果一样，但这里不提倡用反射的方法
            //mBluetoothSocket = (BluetoothSocket) btDev.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(btDev, 1);
            Log.d(TAG, "<设备连接> 连接开始...");
            //在建立之前调用
            if (getmBluetoothAdapter().isDiscovering()) {
                //停止搜索
                Log.d(TAG, "<设备连接> 停止搜索...");
                getmBluetoothAdapter().cancelDiscovery();
            }
            //如果当前socket处于非连接状态则调用连接
            if (!mBluetoothSocket.isConnected()) {
                //你应当确保在调用connect()时设备没有执行搜索设备的操作。
                // 如果搜索设备也在同时进行，那么将会显著地降低连接速率，并很大程度上会连接失败。
                Log.d(TAG, "<设备连接> 之前无连接，开始连接...");
                mBluetoothSocket.connect();
            }
            Log.d(TAG, "<设备连接> 已经连接.");
            if (handler == null) {
                return;
            }
            //结果回调
            Message message = new Message();
            message.what = 4;
            message.obj = btDev;
            handler.sendMessage(message);
        } catch (Exception e) {
            Log.e(TAG, "<设备连接> 连接失败" + e.toString());
            try {
                getmBluetoothSocket().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    /**
     * 蓝牙操作事件
     *
     * @param context
     * @param status
     */
    public void clickBlt(Context context, int status) {
        switch (status) {
            case BltContant.BLUE_TOOTH_SEARTH:
                //搜索蓝牙设备，在BroadcastReceiver显示结果
                startSearthBltDevice(context);
                break;
            case BltContant.BLUE_TOOTH_OPEN:

                //本机蓝牙启用
                if (getmBluetoothAdapter() != null) {
                    //启用
                    getmBluetoothAdapter().enable();
                }
                break;
            case BltContant.BLUE_TOOTH_CLOSE:
                //本机蓝牙禁用
                if (getmBluetoothAdapter() != null) {
                    //禁用
                    getmBluetoothAdapter().disable();
                }
                break;
            case BltContant.BLUE_TOOTH_MY_SEARTH:
                //本机蓝牙可以在300s内被搜索到
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                context.startActivity(discoverableIntent);
                break;
            case BltContant.BLUE_TOOTH_CLEAR:
                //本机蓝牙关闭当前连接
                try {
                    if (getmBluetoothSocket() != null) {
                        getmBluetoothSocket().close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 搜索蓝牙设备
     * 通过调用BluetoothAdapter的startLeScan()搜索BLE设备。
     */
    private boolean startSearthBltDevice(Context context) {
        //开始搜索设备，当搜索到一个设备的时候就应该将它添加到设备集合中，保存起来
        //checkBleDevice(context);
        //如果当前发现了新的设备，则停止继续扫描，当前扫描到的新设备会通过广播推向新的逻辑
        if (getmBluetoothAdapter().isDiscovering()) {
            stopSearthBltDevice();
        }
        Log.i(TAG, "本机蓝牙地址：" + getmBluetoothAdapter().getAddress());
        //开始搜索
        Log.i(TAG, "开始搜索蓝牙设备");
        getmBluetoothAdapter().startDiscovery();
        return true;
    }

    public boolean stopSearthBltDevice() {
        //暂停搜索设备
        return getmBluetoothAdapter().cancelDiscovery();
    }

    /**
     * 判断是否支持蓝牙，并打开蓝牙
     * 获取到BluetoothAdapter之后，还需要判断是否支持蓝牙，以及蓝牙是否打开。
     * 如果没打开，需要让用户打开蓝牙：
     */
    public void checkBleDevice(Context context) {
        if (getmBluetoothAdapter() != null) {
            if (!getmBluetoothAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            }
        } else {
            Log.i(TAG, "该手机不支持蓝牙");
        }
    }

}
