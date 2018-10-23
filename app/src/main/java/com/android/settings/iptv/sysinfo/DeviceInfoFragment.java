package com.android.settings.iptv.sysinfo;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settings.R;

/**
 * Created by libeibei on 2018/1/18 0018.
 */

@SuppressLint("ValidFragment")
public class DeviceInfoFragment extends Fragment {

    private Context mContext;
    private View root;
    private static final String TAG = "SYSINFO";
    private TextView textViewModel,
            manufacturer,
            textViewCPU,
            textViewRAM,
            textViewFlash,
            textSwVer,
            textHwVer,
            textAndroidVer,
            textViewMac,
            textViewWifiMac,
            textViewSTBid;


    public DeviceInfoFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_deviceinfo, null);
        initView();
        showInfo();
        return root;
    }

    private void initView() {
        //本机型号
        textViewModel = (TextView) root.findViewById(R.id.textViewModel);
        //生产厂家
        manufacturer = (TextView) root.findViewById(R.id.manufacturer);
        //CPU
        textViewCPU = (TextView) root.findViewById(R.id.cpu);
        //RAM
        textViewRAM = (TextView) root.findViewById(R.id.ram);
        //Flash
        textViewFlash = (TextView) root.findViewById(R.id.flash);
        //软件版本
        textSwVer = (TextView) root.findViewById(R.id.swVer);
        //硬件版本
        textHwVer = (TextView) root.findViewById(R.id.hwVer);
        //安卓版本
        textAndroidVer = (TextView) root.findViewById(R.id.androidVer);
        //有线MAC
        textViewMac = (TextView) root.findViewById(R.id.textViewMac);
        //无线MAC
        textViewWifiMac = (TextView) root.findViewById(R.id.textViewWIFImac);
        //STBID
        textViewSTBid = (TextView) root.findViewById(R.id.stbid);


    }

    private void showInfo() {
        /**为控件设置数据*/
        //产品型号
        String model = SystemProperties.get("ro.product.model", "CM201-2");
        textViewModel.setText(model);

        //生产厂家
        String manu = SystemProperties.get("ro.product.manufacturer", "CMDC");
        manufacturer.setText(manu);
        //CPU
        String cpu = SystemProperties.get("ro.product.device", "Hi3798MV300");
        textViewCPU.setText(cpu);
        //RAM
        textViewRAM.setText("1G");
        //Flash
        textViewFlash.setText("8G");
        //安卓版本
        String av = SystemProperties.get("ro.build.version.release", "4.4.2");
        textAndroidVer.setText("Android " + av);
        //软件版本
        String sv = SystemProperties.get("ro.build.version.incremental", " ");
        textSwVer.setText(sv);
        //硬件版本
        String hv = SystemProperties.get("ro.build.hardware.id", " ");
        textHwVer.setText(hv);
        //MAC地址
        String MAC = SystemProperties.get("ro.mac", " ");
        textViewMac.setText(MAC);

        //无线MAC
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String wifimac = exChange(info.getMacAddress());
        textViewWifiMac.setText(wifimac);

        //STBID
        String STBID = SystemProperties.get("ro.serialno", " ");
        textViewSTBid.setText(STBID);


    }

    //把一个字符串中的小写转换为大写
    public static String exChange(String str) {
        StringBuffer sb = new StringBuffer();
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (Character.isLowerCase(c)) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

}
