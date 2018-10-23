package com.android.settings.iptv.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;

public class TipsDialog extends Dialog {
	
	private Context mContext;
	public static Button btnOK;
	public static int errorflag = 0; 
	private OnButtonClickListener mButtonClickedListener = null;
	private TextView mErrorTips;
	
	public TipsDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public TipsDialog(Context context, int theme) {
		super(context, theme);
	}

	public TipsDialog(Context context) {
		this(context, R.style.dialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_language);
		
		findViews();
	}

	private void findViews() {
		
		mErrorTips = (TextView) findViewById(R.id.errortips);
		btnOK = (Button) findViewById(R.id.modifylage);
		if(errorflag == 1) {
			mErrorTips.setText(R.string.sleeperror);
		}
		btnOK.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mButtonClickedListener != null) {
					mButtonClickedListener.onClicked("ok");
				}
			}
		});
	}

	public void setOnClickedListener(OnButtonClickListener listener) {
		mButtonClickedListener = listener;
	}

	public interface OnButtonClickListener {
		void onClicked(String word);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		switch (keyCode) {
		case KeyEvent.KEYCODE_F5: {
			Intent _intent = new Intent();
			_intent.setClassName("com.changhong.iptv.sichuan", "com.changhong.iptv.sichuan.Iptvlogin");
			_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(_intent);
			System.exit(0);
			return true;
		}
		case KeyEvent.KEYCODE_MENU: {
			
			Intent _intent = new Intent();
			_intent.setClassName("com.changhong.storage.scanner", "com.changhong.storage.activity.ServiceActivity");
			_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(_intent);
			this.dismiss();
			return true;
		}
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
