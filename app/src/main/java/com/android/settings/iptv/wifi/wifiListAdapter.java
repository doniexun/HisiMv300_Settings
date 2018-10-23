package com.android.settings.iptv.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;

import java.util.ArrayList;

public class wifiListAdapter extends BaseAdapter {

    private Context mContext = null;
    private ArrayList<wifiAccessPoint> mArrayList = null;
    private LayoutInflater flater = null;

    public wifiListAdapter(Context context, ArrayList<wifiAccessPoint> array) {
        mContext = context;
        mArrayList = array;
        flater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (mArrayList != null) {
            return mArrayList.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mArrayList != null) {
            return mArrayList.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {

        FrameLayout layout = (FrameLayout) flater.inflate(R.layout.wifi_listview_item, null);
        TextView name = (TextView) layout.findViewById(R.id.wifi_name);
        TextView security = (TextView) layout.findViewById(R.id.wifi_secure);
        ImageView mSignalImage = (ImageView) layout.findViewById(R.id.wifi_signal);
        wifiAccessPoint point = mArrayList.get(position);
        name.setText(point.ssid + " : ");
        security.setText(point.getSummary());
        if (point.getLevel() != -1) {
            //mSignalImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.wifi_icon_f));
            mSignalImage.getDrawable().setLevel(point.getLevel());
        }
        return layout;
    }

}

