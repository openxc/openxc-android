package com.openxc.enabler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.openxc.messages.ExactKeyMatcher;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.MessageKey;

public abstract class KeyedMessageAdapter extends BaseAdapter {
    private Map<MessageKey, KeyedMessage> mMessages;
    protected List<KeyedMessage> mValues;

    public KeyedMessageAdapter() {
        mMessages = new LinkedHashMap<>();
    }

    public void add(KeyedMessage message) {
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
    public KeyedMessage getItem(int position) {
        return mValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
}
