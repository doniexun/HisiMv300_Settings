package com.android.settings.iptv.other;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.pppoe.PppoeManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.settings.iptv.bluetooth.BlueToothActivity;
import com.android.settings.iptv.util.CustomeDialog;
import com.android.settings.iptv.util.CustomeDialog.ButtonClickedListener;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.UpdateCustomeDialog;
import com.android.settings.iptv.util.VchCommonToastDialog;
import com.android.settings.iptv.util.UpdateCustomeDialog.UpdateButtonClickedListener;

import com.android.settings.R;
import com.android.settings.receiver.ScanStorageReceiver;

@SuppressLint("ValidFragment")
public class MoreFunctionFragment extends Fragment {
    private String TAG = "MoreFunctionFragment";
    private static Context mContext;
    private View root;
    private static final int MESSAGE_INIT = 101;
    private static final int MESSAGE_REBOOT = 102;
    private static final int MESSAGE_CLEARCACHEOVER = 103;
    private MainHandler mMainHandler = null;
    private Button mBlueTooth, mUsbUpdate, mDeviceReset, nClearCache, applicationManager;
    CustomeDialog mCustomeDialog;
    UpdateCustomeDialog mUpdateCustomeDialog;
    static VchCommonToastDialog mToastDialog = null;

    private String updatefile = "update.zip";
    private ArrayList<String> mUsbPath = new ArrayList<String>();
    private int i = 0;
    private ScanStorageReceiver scanStorageReceiver = null;
    private Spinner mTimeOutSpinner;
    private ArrayAdapter<?> mTimeAdapter;
    /*去掉语言显示*/
    //private Spinner mLanguageSpinner;
    private ArrayAdapter<?> mLgeadapter;
    private Spinner mStyleSpinner;
    private ArrayAdapter<?> mStyleadapter;
    private String targetLocaleAsString = "";
    private String conuntry = "";
    private PppoeManager mPppoeManager;
    private static final String THEME_KEY = "theme_mode";
    private SharedPreferences sharedPreferences;
    private Button bt_media;
    private Loger loger = new Loger(MoreFunctionFragment.class);


    public MoreFunctionFragment(Context context) {
        mContext = context;
    }

    public MoreFunctionFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_other, null);
        mMainHandler = new MainHandler(MoreFunctionFragment.this);
        mMainHandler.sendEmptyMessage(MESSAGE_INIT);
        registerBroadCastReceiver();
        return root;
    }


    private void registerBroadCastReceiver() {
        scanStorageReceiver = new ScanStorageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.changhong.iptv.usbUpdateSystem");
        getActivity().registerReceiver(scanStorageReceiver, filter);
    }

    static class MainHandler extends Handler {

        MoreFunctionFragment mContext = null;

        public MainHandler(MoreFunctionFragment context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INIT: {
                    if (mContext.isAdded()) {
                        mContext.initView();
                    }
                    break;
                }
                case MESSAGE_REBOOT: {
                    new Thread() {
                        @Override
                        public void run() {

                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    break;
                }

                case MESSAGE_CLEARCACHEOVER:
                    mContext.onCacheClearedToast();
                    break;
                default:
                    break;
            }
        }
    }

    private void initView() {

        mToastDialog = new VchCommonToastDialog(mContext);
        mPppoeManager = (PppoeManager) mContext.getSystemService(Context.PPPOE_SERVICE);
        mDeviceReset = (Button) root.findViewById(R.id.detectself);

        bt_media = (Button) root.findViewById(R.id.local_media);
        bt_media.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    //本地媒体播放器
                    intent.setClassName("com.android.smart.terminal.nativeplayer",
                            "com.android.smart.terminal.nativeplayer.CHLMMainUI");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        /**
         * 蓝牙界面入口
         */
        BluetoothManager btManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = btManager.getAdapter();
        mBlueTooth = (Button) root.findViewById(R.id.bluetooth_button);
        mBlueTooth.requestFocus();
        mBlueTooth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null) {
                    Intent intent = new Intent(mContext, BlueToothActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * 自动待机时间选择设置
         */
        mTimeOutSpinner = (Spinner) root.findViewById(R.id.timeout_spinner);
        mTimeAdapter = ArrayAdapter.createFromResource(mContext, R.array.timeoutstyle, R.layout.myspinner);
        mTimeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mTimeOutSpinner.setAdapter(mTimeAdapter);

        String sleepAble = SystemProperties.get("persist.sys.autosleep");
        String time = SystemProperties.get("persist.sys.autosleeptime", "60");
        if ("enable".equals(sleepAble)) {
            if (time.equals("60")) {
                mTimeOutSpinner.setSelection(0, true);
            } else if (time.equals("120")) {
                mTimeOutSpinner.setSelection(1, true);
            } else if (time.equals("240")) {
                mTimeOutSpinner.setSelection(2, true);
            }
        } else if ("disable".equals(sleepAble)) {
            mTimeOutSpinner.setSelection(3, true);
        } else {
            mTimeOutSpinner.setSelection(0, true);
            SystemProperties.set("persist.sys.autosleep", "enable");
            SystemProperties.set("persist.sys.autosleeptime", "60");
        }

        mTimeOutSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                switch (position) {
                    case 0:
                        //1 小时
                        SystemProperties.set("persist.sys.autosleep", "enable");
                        SystemProperties.set("persist.sys.autosleeptime", "60");
                        loger.e( "set autosleep time = 1 hour");
                        break;
                    case 1:
                        //2 小时
                        SystemProperties.set("persist.sys.autosleep", "enable");
                        SystemProperties.set("persist.sys.autosleeptime", "120");
                        loger.e( "set autosleep time = 2 hour");

                        break;
                    case 2:
                        //4 小时
                        SystemProperties.set("persist.sys.autosleep", "enable");
                        SystemProperties.set("persist.sys.autosleeptime", "240");
                        loger.e( "set autosleep time = 4 hour");
                        break;
                    case 3:
                        //关闭
                        SystemProperties.set("persist.sys.autosleep", "disable");
                        loger.e( "set autosleep disable!");

                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        /**
         * 语言选择设置
         */
        /*mLanguageSpinner = (Spinner) root.findViewById(R.id.language_spinner);
        mLgeadapter = ArrayAdapter.createFromResource(mContext, R.array.menulanguage, R.layout.myspinner);
        mLgeadapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mLanguageSpinner.setAdapter(mLgeadapter);

        String language = mContext.getResources().getConfiguration().locale.getLanguage();
        if (language.toLowerCase(Locale.getDefault()).contains("zh")) {
            mLanguageSpinner.setSelection(0, true);
        } else if (language.toLowerCase(Locale.getDefault()).contains("en")) {
            mLanguageSpinner.setSelection(1, true);
        }
        mLanguageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                if (mLgeadapter.getItem(position).equals("English")) {
                    targetLocaleAsString = "en";
                } else {
                    targetLocaleAsString = "zh";
                    conuntry = "CN";
                }

                try {
                    Locale locale = new Locale(targetLocaleAsString, conuntry);
                    Class amnClass = Class.forName("android.app.ActivityManagerNative");
                    Object amn = null;
                    Configuration config = null;
                    Method methodGetDefault = amnClass.getMethod("getDefault");
                    methodGetDefault.setAccessible(true);
                    amn = methodGetDefault.invoke(amnClass);
                    Method methodGetConfiguration = amnClass.getMethod("getConfiguration");
                    methodGetConfiguration.setAccessible(true);
                    config = (Configuration) methodGetConfiguration.invoke(amn);
                    Class configClass = config.getClass();
                    Field field = configClass.getField("userSetLocale");
                    field.setBoolean(config, true);
                    // set the locale to the new value
                    config.locale = locale;
                    Method methodUpdateConfiguration = amnClass.getMethod("updateConfiguration", Configuration.class);
                    methodUpdateConfiguration.setAccessible(true);
                    methodUpdateConfiguration.invoke(amn, config);
                    restartApp();

                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });*/

        /**
         * 主题设置
         */
        sharedPreferences = mContext.getSharedPreferences(THEME_KEY, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        mStyleSpinner = (Spinner) root.findViewById(R.id.style_spinner);
        mStyleadapter = ArrayAdapter.createFromResource(mContext, R.array.menustyle, R.layout.myspinner);
        mStyleadapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mStyleSpinner.setAdapter(mStyleadapter);
        int themeMode = sharedPreferences.getInt("theme", 0);
        if (themeMode == 0) {
            //炫彩风格
            mStyleSpinner.setSelection(0, true);
        } else {
            //古典风格
            mStyleSpinner.setSelection(1, true);
        }

        mStyleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                if (position == 0) {
                    editor.putInt("theme", 0);
                } else if (position == 1) {
                    editor.putInt("theme", 1);
                } else {
                    editor.putInt("theme", 0);
                }
                editor.commit();
                restartApp();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });


        /**
         * U盘升级
         *
         */
        mUsbUpdate = (Button) root.findViewById(R.id.usb_update);
        mUsbUpdate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                mUsbPath = getOutSDPath();
                mUpdateCustomeDialog = new UpdateCustomeDialog(mContext);
                mUpdateCustomeDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                mUpdateCustomeDialog.setCancelable(false);
                for (i = 0; i < mUsbPath.size(); i++) {
                    loger.e( "mPath ------>" + mUsbPath.get(i));
                    if (search(mUsbPath.get(i)) == 2) {

                        mUpdateCustomeDialog.setOnClickedListener(new UpdateButtonClickedListener() {
                            @Override
                            public void onUpdateButtonClick(String word) {

                                if (word.equals("ok")) {
                                    File file = new File("/data/setting/login_bg.jpg");
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    File file1 = new File("/data/setting/authen.jpg");
                                    if (file1.exists()) {
                                        file1.delete();
                                    }
                                    File file2 = new File("/data/setting/bgmusic.mp3");
                                    if (file2.exists()) {
                                        file2.delete();
                                    }

                                    Intent intent = new Intent();
                                    intent.setAction("com.changhong.iptv.usbUpdateSystem");
                                    intent.putExtra("updatePath", mUsbPath.get(i));
                                    mContext.sendBroadcast(intent);
                                    mUpdateCustomeDialog.dismiss();
                                } else if (word.equals("cancel")) {
                                    mUpdateCustomeDialog.dismiss();
                                } else {
                                    mUpdateCustomeDialog.dismiss();
                                }
                            }
                        });
                        mUpdateCustomeDialog.show();
                        return;
                    }
                }

                mToastDialog = new VchCommonToastDialog(mContext);
                mToastDialog.setMessage(R.string.not_found);
                mToastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
                mToastDialog.getWindow().setType(2003);
                mToastDialog.show();
            }
        });


        /**
         * 清理缓存功能
         */
        nClearCache = (Button) root.findViewById(R.id.deleteAllCache);
        nClearCache.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mCustomeDialog = new CustomeDialog(mContext);
                mCustomeDialog.setClickedListener(new ButtonClickedListener() {
                    @Override
                    public void onButtonClick(String word) {
                        // TODO Auto-generated method stub
                        if (word.equals("10086")) {
                            final MoreFunctionFragment target = (MoreFunctionFragment) getTargetFragment();
                            final PackageManager pm = mContext.getPackageManager();
                            final List<PackageInfo> infos = pm.getInstalledPackages(0);
                            final IPackageDataObserver observer = new ClearCacheObserver(MoreFunctionFragment.this, infos.size());
                            for (PackageInfo info : infos) {
                                pm.deleteApplicationCacheFiles(info.packageName, observer);
                            }
                            mCustomeDialog.dismiss();
                        } else if (word.equals("cancel")) {
                            mCustomeDialog.dismiss();
                        } else {
                            mToastDialog.setMessage(R.string.error_psd);
                            mToastDialog.show();
                        }
                    }
                });
                mCustomeDialog.setCancelable(true);
                mCustomeDialog.show();
            }
        });
        /**
         * 长按清理缓存：
         * 打开或者关闭
         * adb服务
         */
        nClearCache.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String adb = SystemProperties.get("persist.sys.adb.enable");
                if(TextUtils.isEmpty(adb)){
                    SystemProperties.set("persist.sys.adb.enable", "true");
                    Toast.makeText(mContext, "adb服务打开成功", 0).show();
                }else {
                    if(adb.equals("true")){
                        SystemProperties.set("persist.sys.adb.enable", "false");
                        Toast.makeText(mContext, "adb服务关闭成功", 0).show();
                    }else{
                        SystemProperties.set("persist.sys.adb.enable", "true");
                        Toast.makeText(mContext, "adb服务打开成功", 0).show();
                    }
                }
                return true;
            }
        });


        /**
         * 应用管理
         */
        applicationManager = (Button) root.findViewById(R.id.applicationManager);
        applicationManager.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomeDialog = new CustomeDialog(mContext);
                mCustomeDialog.setClickedListener(new ButtonClickedListener() {

                    @Override
                    public void onButtonClick(String word) {
                        // TODO Auto-generated method stub
                        if (word.equals("10086")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setClassName("com.android.settings",
                                    "com.android.settings.ManageApplications");
                            startActivityForResult(intent, 1);
                            mCustomeDialog.dismiss();
                        } else if (word.equals("cancel")) {
                            mCustomeDialog.dismiss();
                        } else {
                            mToastDialog.setMessage(R.string.error_psd);
                            mToastDialog.show();
                        }
                    }
                });
                mCustomeDialog.setCancelable(true);
                mCustomeDialog.show();
            }
        });

        /**
         * 恢复出厂设置
         */
        mDeviceReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomeDialog = new CustomeDialog(mContext);
                mCustomeDialog.setClickedListener(new ButtonClickedListener() {
                    @Override
                    public void onButtonClick(String word) {
                        // TODO Auto-generated method stub
                        if (word.equals("10086")) {

                            if (null != mPppoeManager) {
                                mPppoeManager.setPppoeUsername("cmcciptv@iptv.cmcc");
                                mPppoeManager.setPppoePassword("cmcc10086");
                            }
                            deleteItmsData();
                            mContext.sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                        } else if (word.equals("cancel")) {
                            mCustomeDialog.dismiss();
                        } else {
                            mToastDialog.setMessage(R.string.error_psd);
                            mToastDialog.show();
                            CustomeDialog.mPassword.setText("");
                            CustomeDialog.mPassword.requestFocus();
                        }
                    }
                });
                mCustomeDialog.setCancelable(true);
                mCustomeDialog.show();
            }
        });

    }

    /**
     * 更改主题后需要
     * 重启App
     * 使主题生效
     */
    private void restartApp() {
        Intent i = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    /**
     * 清除private路径下的零配置数据
     */
    private void deleteItmsData() {
        String cmd = "rm /private/tr069userinfo.conf";
        String cmd1 = "rm /private/pppoe/pppoe-user.conf";
        String cmd2 = "rm /private/pppoe/pppoe-pass.conf";
        try {
            Runtime.getRuntime().exec(cmd);
            Runtime.getRuntime().exec(cmd1);
            Runtime.getRuntime().exec(cmd2);
        } catch (IOException e) {
            Log.e("MasterClearConfirm", "del itms data fail :rm /private/tr069userinfo.conf");
        }

    }


    /**
     * 查询U盘下的升级文件是否存在？
     *
     * @param path
     * @return
     */
    private int search(String path) {

        File file = new File(path);
        if (!file.isDirectory()) {
            return 0;
        }
        File[] listFile = file.listFiles();
        if (listFile == null) {
            return 0;
        }
        for (int i = 0; i < listFile.length; i++) {
            if (listFile[i].getName().equals(updatefile)) {
                return 2;
            }
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(scanStorageReceiver);
        super.onDestroy();
    }

    public ArrayList<String> getOutSDPath() {
        ArrayList<String> lists = new ArrayList<String>();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;

            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure")) continue;
                if (line.contains("asec")) continue;
                if (line.contains("fat") || line.contains("fuse") || line.contains("ntfs")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        lists.add(columns[1]);
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return lists;
    }

    private void onCacheClearedToast() {
        Toast.makeText(mContext, "友情提示：清除缓存结束", Toast.LENGTH_LONG).show();

    }

    private void onCacheCleared() {
        mMainHandler.sendEmptyMessage(MESSAGE_CLEARCACHEOVER);
    }

    private static class ClearCacheObserver extends IPackageDataObserver.Stub {
        private final MoreFunctionFragment mTarget;
        private int mRemaining;

        public ClearCacheObserver(MoreFunctionFragment target, int remaining) {
            mTarget = target;
            mRemaining = remaining;
        }

        @Override
        public void onRemoveCompleted(final String packageName, final boolean succeeded) {
            synchronized (this) {
                if (--mRemaining == 0) {
                    mTarget.onCacheCleared();
                }
            }
        }
    }

    /**
     * EditText获得焦点时
     * 监听back按键，用于删除字符
     */
    private View.OnKeyListener onKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
            if (view instanceof EditText) {
                String text = ((EditText) view).getText().toString();
                Editable editable = ((EditText) view).getText();
                int index = ((EditText) view).getSelectionStart();
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keycode == KeyEvent.KEYCODE_BACK) {
                        if (index == 0 || TextUtils.isEmpty(text)) {
                            return false;
                        } else {
                            editable.delete(index - 1, index);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    };


}
