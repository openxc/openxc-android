package com.openxc.enabler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
        boolean dataSetChanged = false;
        if(mMessages.containsKey(key)) {
            mMessages.put(key, message);
            // Already exists in values, just need to update it
            KeyMatcher exactMatcher = ExactKeyMatcher.buildExactMatcher(key);
            for(int i = 0; i < mValues.size(); i++) {
                KeyedMessage existingMessage = ((KeyedMessage)mValues.get(i));
                if(exactMatcher.matches(existingMessage.getKey())) {
                    mValues.set(i, message);
                    dataSetChanged |= shouldRefreshView(message, existingMessage);
                    break;
                }
            }
        } else {
            // Need to recreate values list because positions will be shifted
            mMessages.put(key, message);
            mValues = new ArrayList<VehicleMessage>(mMessages.values());
            Collections.sort(mValues);
            dataSetChanged = true;
        }

        if(dataSetChanged) {
            notifyDataSetChanged();
        }
    }

    /** Public: Determine if an update to a message should cause the view to be
     * refreshed.
     */
    public boolean shouldRefreshView(KeyedMessage message, KeyedMessage existingMessage) {
        // By default, we always refresh. Subclasses can do deeper inspection if
        // they want.
        return true;
    }
}
