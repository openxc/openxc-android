package com.openxc.enabler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.openxc.messages.ExactKeyMatcher;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.MessageKey;
import com.openxc.messages.VehicleMessage;

public abstract class KeyedMessageAdapter extends VehicleMessageAdapter {
    private Map<MessageKey, KeyedMessage> mMessages;

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
                if(exactMatcher.matches(
                            ((KeyedMessage)mValues.get(i)).getKey())) {
                    mValues.set(i, message);
                    break;
                }
            }
        } else {
            // Need to recreate values list because positions will be shifted
            mMessages.put(key, message);
            mValues = new ArrayList<VehicleMessage>(mMessages.values());
            Collections.sort(mValues);
        }
        notifyDataSetChanged();
    }
}
