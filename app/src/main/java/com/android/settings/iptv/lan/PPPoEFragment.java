package com.android.settings.iptv.lan;

import com.android.settings.R;
import com.android.settings.iptv.util.CustomeDialog;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.VchCommonToastDialog;

import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View.OnFocusChangeListener;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.net.ethernet.EthernetManager;
import android.net.pppoe.PppoeManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

@SuppressLint("ValidFragment")
public class PPPoEFragment extends Fragment {
	
	private View root;
	
	private Context mContext;
	
	private static final int MESSAGE_INIT = 101;
	private static final int MESSAGE_UPDATEUI = 102;
	private MainHandler mMainHandler = null;
	
	private EditText mUserName, mPassword;
	private String mUserNameString, mPasswordString;
	private static Button mSubmit;
	public static CheckBox mPasswordVisiable;
		
	private PppoeManager mPppoeManager;
	private EthernetManager mEthernetManager;

    private static final int DHCP  = 1;
    private static final int PPPoE = 2;
    private static final int LAN  = 3;
    private static final int IPOE = 4;
	
	CustomeDialog mCustomeDialog;
	static VchCommonToastDialog mToastDialog = null;
	private InputMethodManager inputMethodManager;
	private Loger loger = new Loger(PPPoEFragment.class);
	
	public PPPoEFragment(Context context) {
		mContext = context;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		root = inflater.inflate(R.layout.lan_pppoe, null);
		inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

		mToastDialog = new VchCommonToastDialog(mContext);
		mToastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
		mToastDialog.setDuration(10);
		mToastDialog.getWindow().setType(2003);
		
		mMainHandler = new MainHandler(PPPoEFragment.this);
		mMainHandler.sendEmptyMessage(MESSAGE_INIT);
		
		return root;
	}
	
	static class MainHandler extends Handler {

		PPPoEFragment mContext = null;

		public MainHandler(PPPoEFragment context) {
			mContext = context;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_INIT: {
				mContext.initView();
			}
				break;
			case MESSAGE_UPDATEUI:
				String pppoe = SystemProperties.get("pppoe.result");
				if(!pppoe.isEmpty() && Integer.valueOf(pppoe) == 11) {
					mToastDialog.setMessage(R.string.pppoesuccess);
					mToastDialog.setDuration(1);
				} else {
					mToastDialog.setMessage(R.string.pppoefailed);
					mToastDialog.setDuration(1);
				}
				break;
			case 1:
				mSubmit.setFocusable(true);
				mSubmit.setClickable(true);
				mSubmit.setEnabled(true);
			default:
				break;
			}
		}
	};	
	
	private void initView() {
		mPppoeManager = (PppoeManager) mContext.getSystemService(Context.PPPOE_SERVICE);
		mEthernetManager =  (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
		mUserName = (EditText) root.findViewById(R.id.pppoe_username);
		mPassword = (EditText) root.findViewById(R.id.pppoe_password);
		mSubmit = (Button) root.findViewById(R.id.pppoe_submit);
		mPasswordVisiable = (CheckBox) root.findViewById(R.id.pppoe_pass_visiable);
		mPasswordVisiable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked) {
					mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
			}
		});
		if (mPppoeManager.getPppoeUsername() != null) {
			mUserName.setText(mPppoeManager.getPppoeUsername());
		}

		if (mPppoeManager.getPppoePassword() != null)
		{
			mPassword.setText(mPppoeManager.getPppoePassword());
		}
		
		mUserName.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mCustomeDialog = new CustomeDialog(getActivity());
					mCustomeDialog.setCancelable(false);
					mCustomeDialog.show();
				}
			}
		});
		
		mUserName.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				Editable editable = mUserName.getText();
				int index = mUserName.getSelectionStart();
				String userName = mUserName.getText().toString();
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					if(keyCode == KeyEvent.KEYCODE_BACK) {

						if ( userName.isEmpty() || index == 0) {
							 
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
		
		mPassword.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				Editable editable = mPassword.getText();
				int index = mPassword.getSelectionStart();
				String passwd = mPassword.getText().toString();
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					loger.i("PPPoE...OnKeyListener-----keyCode = " + keyCode);
					if(keyCode == KeyEvent.KEYCODE_BACK) {

						if ( passwd.isEmpty() || index == 0) {
							 
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
		
		mSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mUserNameString = mUserName.getText().toString();
				mPasswordString = mPassword.getText().toString();
				loger.i("PPPoE-----dhcpNetMode = " );
				loger.i("PPPoE-----NetMode = ");
		
				LanSettingActivity.closeWifi();
				if (mUserNameString.isEmpty() || mUserName == null) {
					mToastDialog.setMessage(R.string.username_empty);
					mToastDialog.setDuration(1);
					mToastDialog.show();
					return;
				} else {
					mPppoeManager.setPppoeUsername(mUserNameString);
				}
				if (mPasswordString.isEmpty() || mPassword == null) {
					mToastDialog.setMessage(R.string.password_empty);
					mToastDialog.setDuration(1);
					mToastDialog.show();
					return;
				} else {
					mPppoeManager.setPppoePassword(mPasswordString);
				}
				
				mToastDialog.setMessage(R.string.change_to_pppoe);
				mToastDialog.setDuration(16);
				mToastDialog.show();
				mEthernetManager.setAutoReconnectState(false);
				mPppoeManager.disconnect("eht0");
				
				if(!EthernetManager.ETHERNET_CONNECT_MODE_PPPOE.equals(mEthernetManager.getEthernetMode())) {
					mEthernetManager.setEthernetEnabled(false);
					mEthernetManager.setEthernetMode(EthernetManager.ETHERNET_CONNECT_MODE_PPPOE, null);
					mEthernetManager.setEthernetEnabled(true);
				} else {
					mPppoeManager.connect(mUserNameString, mPasswordString, "eth0");
				}
				new Thread(){
					 @Override
					 public void run() {
						 try {
							 for(int i = 0 ;i < 14;i++) {
								 sleep(1000);
								 String pppoeResult = SystemProperties.get("pppoe.result");
								 String ipaddress = SystemProperties.get("pppoe.ppp0.ipaddress");
                                 Log.e("PPPoE","pppoeResult = "+pppoeResult+"pppoeIp = "+ipaddress);
								 if(!pppoeResult.isEmpty() && Integer.valueOf(pppoeResult) == 11
									&& !ipaddress.isEmpty()) {
									 break;
								 }
							 }
						 } catch (InterruptedException e) {
							 e.printStackTrace();
						 }
						 mMainHandler.sendEmptyMessage(MESSAGE_UPDATEUI);
					 }
				 }.start();
			}
		});
	}

}
