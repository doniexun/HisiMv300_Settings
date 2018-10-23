package com.android.settings.iptv.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author hs_yataozhang, jinwei.yang
 */
public class VchCommonToastDialog extends Dialog {
    public final static int TYPE_OK = 0;
    public final static int TYPE_ERROR = 1;
    public final static int TYPE_WARN = 2;
    public final static int TYPE_MESSAGE = 3;
    public final static int TYPE_NONE = -1;

    private Timer timer;
    public MyTimerTask timerTask;
    public TextView tv;
    public LinearLayout info_layout;
    private Dialog mDialog = this;
    private int mDuration = 1;
    private Context mContext;

    private String mMessageText = "";

    private ImageView hintIcon;

    private int mInfoType = TYPE_NONE;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mDialog.show();
                    mDuration--;
                    Log.v("CHLauncher", "mDuration:" + mDuration);
                    break;
                case 0:
                    mDialog.cancel();
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public VchCommonToastDialog(Context context) {
        super(context, R.style.Theme_ToastDialog);
        // TODO Auto-generated constructor stub
        mContext = context;
        initView();
    }

    /**
     * @param context
     * @param duration
     */
    public VchCommonToastDialog(Context context, int infoType, int mesId, int duration) {
        super(context, R.style.Theme_ToastDialog);
        mContext = context;
        mDuration = duration;
        this.mInfoType = infoType;
        mMessageText = context.getString(mesId);
        initView();
    }

    /**
     * @param context
     * @param infoType
     * @param message
     * @param duration
     * @return CommonInfoDialog
     */
    public static VchCommonToastDialog makeDialog(Context context, int infoType, int mesId, int duration) {
        VchCommonToastDialog result = new VchCommonToastDialog(context, infoType, mesId, duration);

        return result;
    }


    private void initView() {
        // TODO Auto-generated method stub
        LayoutInflater layout = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = layout.inflate(R.layout.vch_common_toast_dialog, null);

        tv = (TextView) (view.findViewById(R.id.vch_common_toast_hintmessage));
        tv.setText(getMessage());

        info_layout = (LinearLayout) view.findViewById(R.id.vch_common_toast_layout);

        hintIcon = (ImageView) (view.findViewById(R.id.vch_common_toast_hinticon));
        switch (mInfoType) {
            case TYPE_OK:
                setHintIcon(0);
                break;
            case TYPE_ERROR:
                setHintIcon(0);
                break;
            case TYPE_WARN:
                setHintIcon(0);
                break;
            case TYPE_MESSAGE:
                setHintIcon(0);
            default:
                break;
        }

        this.setContentView(view);
    }

    private void startTimeTask() {
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new MyTimerTask();
        timer.schedule(timerTask, 1 * 1000, 1 * 1000);
    }

    class MyTimerTask extends TimerTask {
        public void run() {
            // what to do()
            if (mDialog != null && mDuration > 0) {
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
            } else {
                if (mDialog != null) {
                    Message message = new Message();
                    message.what = 0;
                    mHandler.sendMessage(message);
                    timer.cancel();
                    timer = null;
                    Log.v("CHLauncher", "mDialog.cancel();");
                }
            }
        }
    }

    ;

    public void setDuration(int duration) {
        // TODO Auto-generated method stub
        this.mDuration = duration;
    }


    @Override
    public void show() {
        // TODO Auto-generated method stub
        super.show();
        setCancelable(false);
        startTimeTask();
    }


    public String getMessage() {
        return mMessageText;
    }


    public void setMessage(String message) {
        this.mMessageText = message;
        tv.setText(message);
    }


    public void setMessage(int mesId) {
        if (mesId > 0)
            setMessage(mContext.getString(mesId));
        else
            setMessage("");
    }


    public void setHintIcon(int resID) {
        if (resID > 0)
            hintIcon.setImageResource(resID);
    }

    public void setHintIcon(Drawable drawable) {
        hintIcon.setImageDrawable(drawable);
    }


    public void setInfoType(int iType) {
        mInfoType = iType;
    }

    public int getInfoType() {
        return mInfoType;
    }

}
