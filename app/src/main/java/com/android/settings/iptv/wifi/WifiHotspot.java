package com.android.settings.iptv.wifi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.android.settings.R;
import com.android.settings.iptv.util.Loger;
import com.android.settings.iptv.util.TipsDialog;
import com.android.settings.iptv.util.TipsDialog.OnButtonClickListener;
import com.android.settings.iptv.util.VchCommonToastDialog;

@SuppressLint("ValidFragment")
public class WifiHotspot extends Fragment {
	
	private View root;
	private static Context mContext;
	private static final int MESSAGE_INIT = 101;
	private static final int MESSAGE_UPDATEUI = 102;
	private MainHandler mMainHandler = null;
	private VchCommonToastDialog mToastDialog = null;
	private InputMethodManager inputMethodManager;
	
	private CheckBox mHotSpotSwitch;
	private Button mOk;
	private RadioGroup mRadioGroup;
	private EditText mUserName, mPassword;
	private static int security = 2;
	private static int currsecurity, oldsecurity;
	private static WifiManager mWifiManager;
	private EthernetManager mEthManager;

	private WifiApAdmin wifiAp;
	private String userName, hotspotpassword;
	
	private TipsDialog tipsDialog;
	private static VchCommonToastDialog toastDialog;
	private Loger loger = new Loger(WifiHotspot.class);
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.wifi_ap, null);
		
		toastDialog = new VchCommonToastDialog(getActivity());
		
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		mEthManager = (EthernetManager) mContext.getSystemService(Context.ETHERNET_SERVICE);
		inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		mHotSpotSwitch = (CheckBox) root.findViewById(R.id.hotspot_switch);
		mOk = (Button) root.findViewById(R.id.wifiap_ok);
		mOk.setOnFocusChangeListener(focusChangeListener);
		mUserName = (EditText) root.findViewById(R.id.hotspotname);
		mPassword = (EditText) root.findViewById(R.id.hotspotpsd);
		
		if(!mWifiManager.isWifiApEnabled()) {
			mHotSpotSwitch.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.switch_off), null, null, null);
			mHotSpotSwitch.setChecked(false);
			mOk.setFocusable(false);
			mOk.setEnabled(false);
			mUserName.setFocusable(false);
			mUserName.setEnabled(false);
			mPassword.setFocusable(false);
			mPassword.setEnabled(false);
			
			SharedPreferences pref = mContext.getSharedPreferences("params", Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("APStatus", "off");
			editor.commit();
		} else {
			mHotSpotSwitch.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.switch_on), null, null, null);
			mHotSpotSwitch.setChecked(true);
			SharedPreferences pref = mContext.getSharedPreferences("params", Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("APStatus", "on");
			editor.commit();

		}
		mMainHandler = new MainHandler(WifiHotspot.this);
		mMainHandler.sendEmptyMessage(MESSAGE_INIT);
		
		return root;
	}

	public WifiHotspot(Context context) {
		mContext = context;
	}
	
	static class MainHandler extends Handler {

		WifiHotspot mContext = null;

		public MainHandler(WifiHotspot context) {
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
				toastDialog.setMessage(R.string.ap_opened);
				toastDialog.setDuration(1);
				break;
			default:
				break;
			}
		}
	};	
	
	private void initView() {
		
		SharedPreferences pref = mContext.getSharedPreferences("params", Activity.MODE_PRIVATE);
		userName = pref.getString("APssid", "");
		hotspotpassword = pref.getString("APpwd", "");
		currsecurity = pref.getInt("APsecurity", 0);

		oldsecurity = currsecurity;
		if(userName.isEmpty()) {
			SystemInfo mSystemInfo = SystemInfo.getInstance();
			mSystemInfo.setContext(mContext);
			String mSTBID = mSystemInfo.getSTBID();
			userName = "CH_" + mSTBID.substring(mSTBID.length()-4);
			oldsecurity = -1;
		}
		loger.i( "AP status -----> " );
		loger.i( "AP security -----> " );
		loger.i( "AP SSID -----> " +userName);
		loger.i( "AP psd -----> " );

		mRadioGroup = (RadioGroup) root.findViewById(R.id.wifiap_security);
		switch (currsecurity) {
		case 0:
			mRadioGroup.check(R.id.security_none);
			mPassword.setFocusable(false);
			mPassword.setEnabled(false);
			break;
		case 1:
			mRadioGroup.check(R.id.security_wpa);
			break;
		case 2:
			mRadioGroup.check(R.id.security_wpa2);
			break;
		default:
			break;
		}
		
		mUserName.setOnFocusChangeListener(focusChangeListener);
		mPassword.setOnFocusChangeListener(focusChangeListener);
		mUserName.setOnKeyListener(keyListener);
		mPassword.setOnKeyListener(keyListener);
		
		mUserName.setText(userName);
		mPassword.setText(hotspotpassword);
		
		mHotSpotSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					
					mUserName.setFocusable(true);
					mUserName.setEnabled(true);
					if(currsecurity != 0) {
						mPassword.setFocusable(true);
						mPassword.setEnabled(true);
					}
					mOk.setFocusable(true);
					mOk.setEnabled(true);
					mHotSpotSwitch.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(R.drawable.switch_on), null, null, null);
					

					// close wifi and open lan


					if(mWifiManager.isWifiEnabled()) {
						mWifiManager.setWifiEnabled(false);
					}
					
					if(mEthManager.getEthernetState() == EthernetManager.ETHERNET_STATE_DISABLED) {
						mEthManager.setEthernetEnabled(true);
						mEthManager.enableEthernet(true);
					}
					
					wifiAp = new WifiApAdmin(mContext);
					wifiAp.startWifiAp(userName, hotspotpassword, currsecurity);
					SharedPreferences pref = mContext.getSharedPreferences("params", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = pref.edit();
					editor.putString("APStatus", "on");
					editor.commit();

					toastDialog.setMessage(R.string.ap_opening);
					toastDialog.setDuration(10);
					toastDialog.show();
					new Thread() {
						@Override
						public void run() {
							for(int i = 0 ;i < 10; i++) {
								try {
									sleep(1000);
									if(mWifiManager.isWifiApEnabled()) {
										loger.i( "isApEnabled------> " + mWifiManager.isWifiApEnabled());
										break;
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							mMainHandler.sendEmptyMessage(MESSAGE_UPDATEUI);
						}
					}.start();
				} else {
					mHotSpotSwitch.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(R.drawable.switch_off), null, null, null);
					WifiApAdmin.closeWifiAp(mContext);
					mOk.setFocusable(false);
					mOk.setEnabled(false);
					mUserName.setFocusable(false);
					mUserName.setEnabled(false);
					mPassword.setFocusable(false);
					mPassword.setEnabled(false);
					SharedPreferences pref = mContext.getSharedPreferences("params", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editor = pref.edit();
					editor.putString("APStatus", "off");
					editor.commit();

				}
			}
		});
		mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.security_none:
					security = 0;
					break;
				case R.id.security_wpa:
					security = 1;
					break;
				case R.id.security_wpa2:
					security = 2;
					break;
				default:
					break;
				}
				SharedPreferences pref = mContext.getSharedPreferences("params", Activity.MODE_PRIVATE);

				if(security != 0 && pref.getString("APStatus", "").equalsIgnoreCase("on")) {
					mPassword.setFocusable(true);
					mPassword.setEnabled(true);
				} else {
					mPassword.setFocusable(false);
					mPassword.setEnabled(false);
				}
				currsecurity = security;
			}
		});
			
		
		mOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mToastDialog = new VchCommonToastDialog(getActivity());
				mToastDialog.info_layout.setBackgroundResource(R.drawable.epg_prompt_bg);
				mToastDialog.getWindow().setType(2003);
				
				String ssid = mUserName.getEditableText().toString().trim();
				String password = mPassword.getEditableText().toString().trim();
				loger.i( "oldsecurity -----> " + oldsecurity);
				loger.i( "currsecurity -----> " + currsecurity);
				
				if(ssid.isEmpty()) {
					mToastDialog.setMessage(R.string.ssid_error);
					mToastDialog.show();
					return;
				}
				
				if(ssid.equals(userName) && password.equals(hotspotpassword) && oldsecurity == currsecurity) {
					mToastDialog.setMessage(R.string.change_no);
					mToastDialog.show();
					return;
				};
				oldsecurity = currsecurity;
				
				if(currsecurity != 0 && password.length() < 8 ) {
					mToastDialog.setMessage(R.string.hotspot_psd_error);
					mToastDialog.show();
					mPassword.requestFocus();
					return;
				}
				

				
				if(wifiAp != null) {
					wifiAp = null;
				}
				wifiAp = new WifiApAdmin(mContext);
				wifiAp.startWifiAp(ssid, password, currsecurity);

				mHotSpotSwitch.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.switch_on), null, null, null);
				mHotSpotSwitch.setChecked(true);
				SharedPreferences pref = mContext.getSharedPreferences("params", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("APssid", ssid);
				editor.putString("APpwd", password);
				editor.putInt("APsecurity", currsecurity);
				editor.commit();
				
				tipsDialog = new TipsDialog(getActivity());
				tipsDialog.setCancelable(false);
				
				tipsDialog.setOnClickedListener(new OnButtonClickListener() {
					
					@Override
					public void onClicked(String word) {
						// TODO Auto-generated method stub
						tipsDialog.dismiss();
					} 
				});
				tipsDialog.show();
			}
		});
	}
	
	private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus &&  (v instanceof EditText)) {
				EditText tmp = (EditText) v;
				tmp.setSelection(tmp.getText().length());
			} else if(hasFocus && (v instanceof Button)) {
				inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	};
	
	private OnKeyListener keyListener = new OnKeyListener() {
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if(v instanceof EditText) {
				EditText tmp = (EditText) v;
				int index = tmp.getSelectionStart();
				Editable editable = tmp.getEditableText();
				String text = editable.toString();
				if(event.getAction() == KeyEvent.ACTION_DOWN) {
					if(keyCode == KeyEvent.KEYCODE_BACK) {
						if(text.isEmpty() || index == 0) {
							
						} else {
							editable.delete(index-1, index);
							return true;
						}
					}
				}
			}
			return false;
		}
	};
}
