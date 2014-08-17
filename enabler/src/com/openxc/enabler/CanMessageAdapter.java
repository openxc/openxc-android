package com.openxc.enabler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.openxc.messages.CanMessage;
import com.openxc.messages.ExactKeyMatcher;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.MessageKey;

public class CanMessageAdapter extends BaseAdapter {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private Map<MessageKey, CanMessage> mMessages;
    private List<CanMessage> mValues;
    private Context mContext;

    public CanMessageAdapter(Context context) {
        mContext = context;
        mMessages = new LinkedHashMap<>();
    }

    public void add(CanMessage message) {
        ThreadPreconditions.checkOnMainThread();
        MessageKey key = message.getKey();
        if(mMessages.containsKey(key)) {
            mMessages.put(key, message);
            // Already exists in values, just need to update it
            KeyMatcher exactMatcher = ExactKeyMatcher.buildExactMatcher(key);
            for(int i = 0; i < mValues.size(); i++) {
                if(exactMatcher.matches(mValues.get(i).getKey())) {
                    mValues.set(i, message);
                    break;
                }
            }
        } else {
            // Need to recreate values list because positions will be shifted
            mMessages.put(key, message);
            mValues = new ArrayList<>(mMessages.values());
            Collections.sort(mValues);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public CanMessage getItem(int position) {
        return mValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.can_message_list_item, parent, false);
        }

        CanMessage message = getItem(position);

        TextView busView = (TextView) convertView.findViewById(R.id.can_message_bus);
        busView.setText("" + message.getBus());

        TextView idView = (TextView) convertView.findViewById(R.id.can_message_id);
        idView.setText("0x" + Integer.toHexString(message.getId()));

        TextView dataView = (TextView) convertView.findViewById(R.id.can_message_data);
        dataView.setText("0x" + bytesToHex(message.getData()));

        return convertView;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
}
}
