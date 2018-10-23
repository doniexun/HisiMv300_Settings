package com.android.settings.iptv.util;


import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;

public class CustomeDialog extends Dialog {
	
	private Context mContext;
	private Button btnOK, btnCancle;
	private TextView tv_title;
	private ButtonClickedListener mButtonClickedListener = null;
	public  static EditText mPassword;
	private InputMethodManager inputMethodManager;
	private Toast mToast = null;
	private String title ="";

	public CustomeDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public CustomeDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomeDialog(Context context) {
		this(context, R.style.dialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_password);
		
		inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		findViews();
	}
	
	private void findViews() {
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		tv_title = (TextView) findViewById(R.id.dialog_title);
		if(!TextUtils.isEmpty(title)){
			tv_title.setText(title);
		}
		btnOK = (Button) findViewById(R.id.buttonABC);
		btnCancle = (Button) findViewById(R.id.buttonKEY);
		
		btnOK.setOnFocusChangeListener(focusChangeListener);
		btnCancle.setOnFocusChangeListener(focusChangeListener);
		
		btnOK.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mButtonClickedListener != null) {
					mButtonClickedListener.onButtonClick(mPassword.getText().toString());
				}
			}
		});
		
		
		btnCancle.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mButtonClickedListener != null) {
					mButtonClickedListener.onButtonClick("cancel");
				}
			}
		});
		
		mPassword = (EditText) findViewById(R.id.input_password);
		mPassword.requestFocus();

		mPassword.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				int index =  mPassword.getSelectionStart();
				Editable editable =	mPassword.getText();
				String	editableString = editable.toString();
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					if(keyCode == KeyEvent.KEYCODE_BACK) {

						if ( editableString.equals("") || index == 0) {
								if(mToast == null) {
									mToast = Toast.makeText(mContext, R.string.exit_setting,Toast.LENGTH_SHORT);
								} else {
									mToast.setText(R.string.exit_setting);
								}
								mToast.show();
								return true;
						} else {
							editable.delete(index-1, index);
							return true;
						}
						
					} else if(keyCode == KeyEvent.KEYCODE_INFO) {

						inputMethodManager.toggleSoftInputFromWindow(v.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);
						return true;
					}
				}
				return false;
			}
			
		});
	}

	public void setClickedListener(ButtonClickedListener listener) {
		mButtonClickedListener = listener;
	}

	public void setTitleText(String text){
		title = text;
	}

	public interface ButtonClickedListener {
		void onButtonClick(String word);
	}
	
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			Button tmp = (Button) v;
			if(hasFocus) {
				inputMethodManager.hideSoftInputFromWindow(tmp.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}; 
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {


		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU: {
			return true;
		}
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
}
