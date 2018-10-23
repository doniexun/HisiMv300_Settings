package com.android.settings.iptv.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.settings.R;
public class UpdateCustomeDialog extends Dialog {
	
	private Context mContext;
	public static Button btnOK, btnCancle;
	private UpdateButtonClickedListener mUpdateButtonClickedListener = null;
	public  static EditText mPassword;
//	private InputMethodManager inputMethodManager;
	public  static TextView mUsbTextView;
	
	
	
	public UpdateCustomeDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public UpdateCustomeDialog(Context context, int theme) {
		super(context, theme);
	}

	public UpdateCustomeDialog(Context context) {
		this(context, R.style.dialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_dialog_password);
		findViews();
	}

	private void findViews() {
		btnOK = (Button) findViewById(R.id.buttonABC);
		btnCancle = (Button) findViewById(R.id.buttonKEY);
		mUsbTextView=(TextView)findViewById(R.id.usb_update_textview);
		btnOK.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUpdateButtonClickedListener != null) {
					mUpdateButtonClickedListener.onUpdateButtonClick("ok");
				}
			}
		});

		btnCancle.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUpdateButtonClickedListener != null) {
					mUpdateButtonClickedListener.onUpdateButtonClick("cancel");
				}
			}
		});

	}

	public void setOnClickedListener(UpdateButtonClickedListener listener) {
		mUpdateButtonClickedListener = listener;
	}

	public interface UpdateButtonClickedListener {
		void onUpdateButtonClick(String word);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_F5: {
			Intent _intent = new Intent();
			_intent.setClassName("com.changhong.iptv.sichuan", "com.changhong.iptv.jiangxi.Iptvlogin");
			_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(_intent);
			cancel();
			return true;
		}
		case KeyEvent.KEYCODE_MENU: {
			
			Intent _intent = new Intent();
			_intent.setClassName("com.changhong.storage.scanner", "com.changhong.storage.activity.ServiceActivity");
			_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(_intent);
			cancel();
			return true;
		}
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
