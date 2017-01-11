package com.openxc.enabler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.openxc.messages.VehicleMessage;

public abstract class VehicleMessageAdapter extends BaseAdapter {
    protected List<VehicleMessage> mValues = new ArrayList<>();
    private static SimpleDateFormat sDateFormatter =
            new SimpleDateFormat( "HH:mm:ss.S", Locale.US);

    public void add(VehicleMessage message) {
        ThreadPreconditions.checkOnMainThread();
        // No need to sort as the list will maintain assertion order, which will
        // be the same as sorting by timestamp.
        mValues.add(0, message);
        notifyDataSetChanged();
    }

    protected static String formatTimestamp(VehicleMessage message) {
        if(message.isTimestamped()) {
            return sDateFormatter.format(message.getDate());
        }
        return "";
    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public VehicleMessage getItem(int position) {
        return mValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
}
