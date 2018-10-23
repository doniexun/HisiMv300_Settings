package com.android.settings.iptv.wifi;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;


public class CustomDialog extends Dialog {
    private final static String TAG = "libeibei";
    public final static int DIALOG_CURRENT_CONNECTED = 0;
    public final static int DIALOG_ONETIME_CONNECTED = 1;
    public final static int DIALOG_NONE_SECURITY = 2;
    public final static int DIALOG_HAVE_SECURITY = 3;
    private Context mContext;
    private EditText editText;
    private Button positiveButton, negativeButton;
    private CheckBox showPassword;
    private TextView SSIDname, showInfo;

    public CustomDialog(Context context, int WifiDialogClass) {
        super(context, R.style.TestDialog);
        mContext = context;
        setCustomDialog(WifiDialogClass);
    }

    private void setCustomDialog(int WifiDialogClass) {
        View mView = null;
        switch (WifiDialogClass) {
            case DIALOG_CURRENT_CONNECTED: {
                mView = LayoutInflater.from(getContext()).inflate(R.layout.wifi_dialog_connected, null);
                SSIDname = (TextView) mView.findViewById(R.id.id_wifi_connected_ssid);
                showInfo = (TextView) mView.findViewById(R.id.id_wifi_connected_ip);
                positiveButton = (Button) mView.findViewById(R.id.id_wifi_connected_ok);
                negativeButton = (Button) mView.findViewById(R.id.id_wifi_connected_cancle);
                positiveButton.setText(mContext.getResources().getString(R.string.wifi_forget));
                negativeButton.setText(mContext.getResources().getString(R.string.button_cancle));
            }
            break;
            case DIALOG_ONETIME_CONNECTED: {
                mView = LayoutInflater.from(getContext()).inflate(R.layout.wifi_dialog_saved, null);
                SSIDname = (TextView) mView.findViewById(R.id.id_wifi_connected_ssid);
                showInfo = (TextView) mView.findViewById(R.id.id_wifi_security);
                positiveButton = (Button) mView.findViewById(R.id.id_wifi_connected_ok);
                negativeButton = (Button) mView.findViewById(R.id.id_wifi_connected_cancle);
                positiveButton.setText(mContext.getResources().getString(R.string.wifi_connect));
                negativeButton.setText(mContext.getResources().getString(R.string.wifi_forget));
            }
            break;
            case DIALOG_NONE_SECURITY:
                mView = LayoutInflater.from(getContext()).inflate(R.layout.wifi_dialog_nosecurity, null);
                SSIDname = (TextView) mView.findViewById(R.id.id_wifi_connected_ssid);
                showInfo = (TextView) mView.findViewById(R.id.id_wifi_signal);
                positiveButton = (Button) mView.findViewById(R.id.id_wifi_connected_ok);
                negativeButton = (Button) mView.findViewById(R.id.id_wifi_connected_cancle);
                positiveButton.setText(mContext.getResources().getString(R.string.wifi_connect));
                negativeButton.setText(mContext.getResources().getString(R.string.button_cancle));
                break;
            case DIALOG_HAVE_SECURITY: {
                mView = LayoutInflater.from(getContext()).inflate(R.layout.wifi_dialog_input, null);
                SSIDname = (TextView) mView.findViewById(R.id.dialog_ssid_id);
                editText = (EditText) mView.findViewById(R.id.wifi_password_editid);
                showPassword = (CheckBox) mView.findViewById(R.id.wifi_button_showpassword_id);
                positiveButton = (Button) mView.findViewById(R.id.id_wifi_connected_ok);
                negativeButton = (Button) mView.findViewById(R.id.id_wifi_connected_cancle);
                positiveButton.setText(mContext.getResources().getString(R.string.button_sure));
                negativeButton.setText(mContext.getResources().getString(R.string.button_cancle));
                editText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int keycode, KeyEvent keyEvent) {
                        Editable editable = editText.getText();
                        int index = editText.getSelectionStart();
                        String passwd = editText.getText().toString();
                        Log.e(TAG, "KeyCode = " + keycode);
                        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keycode == KeyEvent.KEYCODE_BACK || keycode == KeyEvent.KEYCODE_DEL) {
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
                LinearLayout mLiner = (LinearLayout) mView.findViewById(R.id.wifi_laypassword_id);
                mLiner.setVisibility(View.VISIBLE);
                LinearLayout mLiner1 = (LinearLayout) mView.findViewById(R.id.wifi_layshowpassword_id);
                mLiner1.setVisibility(View.VISIBLE);
            }
            break;
            default:
                break;
        }

        super.setContentView(mView);
    }

    public View getEditText() {

        return editText;
    }

    public View getShowInfo() {
        return showInfo;
    }

    public View getSSIDname() {

        return SSIDname;
    }

    public View getCheckBox() {
        return showPassword;
    }

    @Override
    public void setContentView(int layoutResID) {
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
    }

    @Override
    public void setContentView(View view) {
    }

    /**
     * 确定键监听器
     *
     * @param listener
     */
    public void setOnPositiveListener(View.OnClickListener listener) {
        positiveButton.setOnClickListener(listener);
    }

    /**
     * 取消键监听器
     *
     * @param listener
     */
    public void setOnNegativeListener(View.OnClickListener listener) {
        negativeButton.setOnClickListener(listener);
    }

}
