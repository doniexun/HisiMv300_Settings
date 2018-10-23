package com.android.settings.iptv.display;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.settings.R;

import java.util.ArrayList;

public class MediaAdapter extends BaseAdapter {
    private Context mcontext;
    private int checkedId = -1;
    private static final String TAG = "MediaSet";
    private ArrayList<String> arraylist = null;
    private LayoutInflater flater = null;

    public MediaAdapter(Context context, ArrayList<String> list) {
        mcontext = context;
        arraylist = list;
        flater = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (arraylist != null)
            return arraylist.size();
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (arraylist != null)
            return arraylist.get(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setCheckedId(int pos) {
        checkedId = pos;
    }

    @Override
    public View getView(int arg0, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = (LinearLayout) flater.inflate(R.layout.display_list_item, null);
            viewHolder.tv = (TextView) view.findViewById(R.id.tv_resolutionselect);
            viewHolder.rb = (RadioButton) view.findViewById(R.id.rb_resolutioncheck);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tv.setText(arraylist.get(arg0));
        if (checkedId != -1 && arg0 == checkedId) {
            Log.e(TAG, "选中了：" + arg0);
            viewHolder.rb.setChecked(true);
        } else if (checkedId != -1 && arg0 != checkedId) {
            viewHolder.rb.setChecked(false);
        }
        return view;
    }

    class ViewHolder {
        public TextView tv;
        public RadioButton rb;
    }
}
