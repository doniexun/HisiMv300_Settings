package com.android.settings.iptv.display;

import com.android.settings.R;
import com.hisilicon.android.hidisplaymanager.HiDisplayManager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.android.settings.iptv.display.ScaleAndMoveActivity.ScaleType;

public class OffSetFragment extends Fragment {
	private View root;
	private static final int MESSAGE_INIT = 101;
	private MainHandler mMainHandler = null;
	
	private Button mAreaSet;
	private RadioGroup offset_RadioGroup;
	private RadioGroup changetype_RadioGroup;
	private HiDisplayManager mDisplayManager;
	private static final int AUTO = 0;
	private static final int _4to3 = 1;
	private static final int _16to9 = 2;

	private static final int extrude = 0;
	private static final int addblack = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.display_off_set, null);
		mDisplayManager = new HiDisplayManager();
		
		mMainHandler = new MainHandler(OffSetFragment.this);
		mMainHandler.sendEmptyMessage(MESSAGE_INIT);
		return root;
	}
	static class MainHandler extends Handler {

		OffSetFragment mContext = null;

		public MainHandler(OffSetFragment context) {
			mContext = context;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_INIT: {
				mContext.initView();
			}
				break;

			default:
				break;
			}
		}
	};
	
	private void initView(){
		
		mAreaSet = (Button) root.findViewById(R.id.area_set);
		offset_RadioGroup = (RadioGroup)root.findViewById(R.id.RadioGroup_off_set);
		changetype_RadioGroup = (RadioGroup)root.findViewById(R.id.RadioGroup_off_changetype);
		
		offset_RadioGroup.setOnCheckedChangeListener(checkedChangeListener);
		changetype_RadioGroup.setOnCheckedChangeListener(checkedChangeListener);

		/*初始化两个radiogroup*/
		int ratio = mDisplayManager.getAspectRatio();
		if(ratio == -1) {
			ratio = 0;
		}
		offset_RadioGroup.check(getDisplaySet(ratio));

		//0 extrude, 1 add black, -1 otherwise
		int changetype = mDisplayManager.getAspectCvrs();
		if(changetype == -1){
			changetype = 0;
		}
		changetype_RadioGroup.check(getDisplayChange(changetype));

		mAreaSet.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DisplaySettingActivity.isAreaSetting = true;
				Intent intent = new Intent(getActivity(), ScaleAndMoveActivity.class);
				ScaleAndMoveActivity.mType = ScaleType.SCALE;
				getActivity().startActivity(intent);
			}
		});
	}



	private OnCheckedChangeListener checkedChangeListener=new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			switch (checkedId) {
				
			case R.id.display_off_set_a:
				mDisplayManager.setAspectRatio(AUTO);
				break;
			case R.id.display_off_set_b:
				mDisplayManager.setAspectRatio(_4to3);
				break;
			case R.id.display_off_set_c:
				mDisplayManager.setAspectRatio(_16to9);
				break;
			case R.id.display_off_changetype_a:
				mDisplayManager.setAspectCvrs(extrude);
				break;
			case R.id.display_off_changetype_b:
				mDisplayManager.setAspectCvrs(addblack);
				break;
			default:
				break;
			}
			mDisplayManager.saveParam();
		}
	};

	
	private int getDisplaySet(int displaySet) {
		int index = 0;
		switch (displaySet) {
		case 0:
			index = R.id.display_off_set_a;
			break;
		case 1:
			index = R.id.display_off_set_b;
			break;
		case 2:
			index = R.id.display_off_set_c;
			break;
		default:
			break;
		}
		return index;
	}

	private int getDisplayChange(int changetype) {
		int mindex = 0;
		switch (changetype){
		case 0:
			mindex = R.id.display_off_changetype_a;
			break;
		case 1:
			mindex = R.id.display_off_changetype_b;
			break;
		default:
			break;
		}
		return mindex;
	}
}
