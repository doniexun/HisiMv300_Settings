package com.android.settings.iptv.display;


import com.android.settings.R;
import com.hisilicon.android.hidisplaymanager.DispFmt;
import com.hisilicon.android.hidisplaymanager.HiDisplayManager;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * 用来设置PAL和NTSC的格式
 * 当前不再设置这两个格式的变更，故当前本页面不再显示
 */
@SuppressLint("ValidFragment")
public class MediaSetFragment extends Fragment {

    private View root;
    private static final String TAG = "MediaSet";
    private Context mContext;
    private HiDisplayManager displayManager;
    private ListView listView_media;
    private ArrayList<String> arrayList;
    private MediaAdapter adapter;
    private int type = 1;
    private static final int SET_LISTADAPTER = 0x0001;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_LISTADAPTER:
                    adapter = new MediaAdapter(mContext, arrayList);
                    listView_media.setAdapter(adapter);
                    listView_media.setOnItemClickListener(new ItemClickListener());
                    getCheckedItem();
                    break;
                default:
                    break;
            }
        }
    };


    public MediaSetFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.display_media_set, null);
        initView();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * UI控件初始化
     */
    private void initView() {
        displayManager = new HiDisplayManager();
        listView_media = (ListView) root.findViewById(R.id.media_list);
        arrayList = new ArrayList<String>();
        initSuppList();
    }

    /**
     * 获取当前设备
     * 支持的分辨率列表
     */
    private void initSuppList() {
        type = displayManager.getDisplayDeviceType();
        Log.e(TAG, " getDisplayDeviceType() = " + type);
        if (type < 1) {
            // type = 0 时，是AV输出，显示PAL_NTSC界面;
            arrayList.add(MediaConstant.ENC_FMT_PAL);
            arrayList.add(MediaConstant.ENC_FMT_NTSC);
            handler.sendEmptyMessage(SET_LISTADAPTER);
            return;
        }
        DispFmt dispFmt = displayManager.getDisplayCapability();
        if (dispFmt == null) {
            arrayList.add(MediaConstant.ENC_FMT_PAL);
            arrayList.add(MediaConstant.ENC_FMT_NTSC);
            arrayList.add(MediaConstant.ENC_FMT_720P_50);
            arrayList.add(MediaConstant.ENC_FMT_720P_60);
            arrayList.add(MediaConstant.ENC_FMT_1080i_50);
            arrayList.add(MediaConstant.ENC_FMT_1080i_60);
            arrayList.add(MediaConstant.ENC_FMT_1080P_24);
            arrayList.add(MediaConstant.ENC_FMT_1080P_30);
            arrayList.add(MediaConstant.ENC_FMT_1080P_50);
            arrayList.add(MediaConstant.ENC_FMT_1080P_60);
            Log.e(TAG, "获取显示设备分辨率为空,dispFmt == null");
        } else {
            if (dispFmt.ENC_FMT_PAL == 1) {
                arrayList.add(MediaConstant.ENC_FMT_PAL);
            }
            if (dispFmt.ENC_FMT_NTSC == 1) {
                arrayList.add(MediaConstant.ENC_FMT_NTSC);
            }
            if (dispFmt.ENC_FMT_480P_60 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_480P_60);
            }
            if (dispFmt.ENC_FMT_576P_50 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_576P_50);
            }
            if (dispFmt.ENC_FMT_720P_50 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_720P_50);
            }
            if (dispFmt.ENC_FMT_720P_60 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_720P_60);
            }
            if (dispFmt.ENC_FMT_1080i_50 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_1080i_50);
            }
            if (dispFmt.ENC_FMT_1080i_60 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_1080i_60);
            }
            if (dispFmt.ENC_FMT_1080P_24 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_1080P_24);
            }
            if (dispFmt.ENC_FMT_1080P_25 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_1080P_25);
            }
            if (dispFmt.ENC_FMT_1080P_30 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_1080P_30);
            }
            if (dispFmt.ENC_FMT_1080P_50 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_1080P_50);
            }
            if (dispFmt.ENC_FMT_1080P_60 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_1080P_60);
            }
            if (dispFmt.ENC_FMT_3840X2160_24 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_3840X2160_24);
            }
            if (dispFmt.ENC_FMT_3840X2160_25 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_3840X2160_25);
            }
            if (dispFmt.ENC_FMT_3840X2160_30 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_3840X2160_30);
            }
            if (dispFmt.ENC_FMT_3840X2160_50 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_3840X2160_50);
            }
            if (dispFmt.ENC_FMT_3840X2160_60 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_3840X2160_60);
            }
            if (dispFmt.ENC_FMT_4096X2160_24 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_4096X2160_24);
            }
            if (dispFmt.ENC_FMT_4096X2160_25 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_4096X2160_25);
            }
            if (dispFmt.ENC_FMT_4096X2160_30 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_4096X2160_30);
            }
            if (dispFmt.ENC_FMT_4096X2160_50 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_4096X2160_50);
            }
            if (dispFmt.ENC_FMT_4096X2160_60 == 1) {
                arrayList.add(MediaConstant.ENC_FMT_4096X2160_60);
            }
        }
        //分辨率列表获取完成后，更新界面list
        handler.sendEmptyMessage(SET_LISTADAPTER);
    }

    /**
     * 初始化，当前所选择的分辨率
     */
    private void getCheckedItem() {
        int lastFmt = displayManager.getFmt();
        switch (lastFmt) {
            case HiDisplayManager.ENC_FMT_PAL:
                checkitem(MediaConstant.ENC_FMT_PAL);
                break;
            case HiDisplayManager.ENC_FMT_NTSC:
                checkitem(MediaConstant.ENC_FMT_NTSC);
                break;
            case HiDisplayManager.ENC_FMT_480P_60:
                checkitem(MediaConstant.ENC_FMT_480P_60);
                break;
            case HiDisplayManager.ENC_FMT_576P_50:
                checkitem(MediaConstant.ENC_FMT_576P_50);
                break;
            case HiDisplayManager.ENC_FMT_720P_50:
                checkitem(MediaConstant.ENC_FMT_720P_50);
                break;
            case HiDisplayManager.ENC_FMT_720P_60:
                checkitem(MediaConstant.ENC_FMT_720P_60);
                break;
            case HiDisplayManager.ENC_FMT_1080i_50:
                checkitem(MediaConstant.ENC_FMT_1080i_50);
                break;
            case HiDisplayManager.ENC_FMT_1080i_60:
                checkitem(MediaConstant.ENC_FMT_1080i_60);
                break;
            case HiDisplayManager.ENC_FMT_1080P_24:
                checkitem(MediaConstant.ENC_FMT_1080P_24);
                break;
            case HiDisplayManager.ENC_FMT_1080P_25:
                checkitem(MediaConstant.ENC_FMT_1080P_25);
                break;
            case HiDisplayManager.ENC_FMT_1080P_30:
                checkitem(MediaConstant.ENC_FMT_1080P_30);
                break;
            case HiDisplayManager.ENC_FMT_1080P_50:
                checkitem(MediaConstant.ENC_FMT_1080P_50);
                break;
            case HiDisplayManager.ENC_FMT_1080P_60:
                checkitem(MediaConstant.ENC_FMT_1080P_60);
                break;
            case HiDisplayManager.ENC_FMT_3840X2160_24:
                checkitem(MediaConstant.ENC_FMT_3840X2160_24);
                break;
            case HiDisplayManager.ENC_FMT_3840X2160_25:
                checkitem(MediaConstant.ENC_FMT_3840X2160_25);
                break;
            case HiDisplayManager.ENC_FMT_3840X2160_30:
                checkitem(MediaConstant.ENC_FMT_3840X2160_30);
                break;
            case HiDisplayManager.ENC_FMT_3840X2160_50:
                checkitem(MediaConstant.ENC_FMT_3840X2160_50);
                break;
            case HiDisplayManager.ENC_FMT_3840X2160_60:
                checkitem(MediaConstant.ENC_FMT_3840X2160_60);
                break;
            case HiDisplayManager.ENC_FMT_4096X2160_24:
                checkitem(MediaConstant.ENC_FMT_4096X2160_24);
                break;
            case HiDisplayManager.ENC_FMT_4096X2160_25:
                checkitem(MediaConstant.ENC_FMT_4096X2160_25);
                break;
            case HiDisplayManager.ENC_FMT_4096X2160_30:
                checkitem(MediaConstant.ENC_FMT_4096X2160_30);
                break;
            case HiDisplayManager.ENC_FMT_4096X2160_50:
                checkitem(MediaConstant.ENC_FMT_4096X2160_50);
                break;
            case HiDisplayManager.ENC_FMT_4096X2160_60:
                checkitem(MediaConstant.ENC_FMT_4096X2160_60);
                break;
            default:
                break;
        }
    }

    private void checkitem(String str) {
        int position = arrayList.indexOf(str);
        Log.e(TAG, "Get checked position = " + position);
        adapter.setCheckedId(position);
        adapter.notifyDataSetChanged();
    }


    class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            String str = arrayList.get(position);
            Log.e(TAG, "选择了分辨率：" + str);
            if (TextUtils.isEmpty(str)) {
                Toast.makeText(mContext, "获取显示设备分辨率出错1", Toast.LENGTH_LONG).show();
            } else if (str.equals(MediaConstant.ENC_FMT_PAL)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_PAL);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_NTSC)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_NTSC);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_480P_60)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_480P_60);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_576P_50)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_576P_50);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_720P_50)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_720P_50);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_720P_60)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_720P_60);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_1080i_50)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_1080i_50);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_1080i_60)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_1080i_60);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_1080P_24)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_1080P_24);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_1080P_25)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_1080P_25);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_1080P_30)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_1080P_30);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_1080P_50)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_1080P_50);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_1080P_60)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_1080P_60);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_3840X2160_24)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_3840X2160_24);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_3840X2160_25)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_3840X2160_25);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_3840X2160_30)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_3840X2160_30);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_3840X2160_50)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_3840X2160_50);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_3840X2160_60)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_3840X2160_60);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_4096X2160_24)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_4096X2160_24);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_4096X2160_25)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_4096X2160_25);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_4096X2160_30)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_4096X2160_30);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_4096X2160_50)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_4096X2160_50);
                displayManager.saveParam();
            } else if (str.equals(MediaConstant.ENC_FMT_4096X2160_60)) {
                displayManager.setFmt(HiDisplayManager.ENC_FMT_4096X2160_60);
                displayManager.saveParam();
            } else {
                Toast.makeText(mContext, "获取显示设备分辨率出错2", Toast.LENGTH_LONG).show();
            }
            adapter.setCheckedId(position);
            adapter.notifyDataSetChanged();
        }
    }


}
