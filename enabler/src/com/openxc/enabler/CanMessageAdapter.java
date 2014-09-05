package com.openxc.enabler;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openxc.messages.CanMessage;
import com.openxc.messages.formatters.ByteAdapter;

public class CanMessageAdapter extends KeyedMessageAdapter {
    private Context mContext;

    public CanMessageAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public CanMessage getItem(int position) {
        return mValues.get(position).asCanMessage();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.can_message_list_item, parent, false);
        }

        CanMessage message = getItem(position);

        TextView timestampView = (TextView)
                convertView.findViewById(R.id.timestamp);
        timestampView.setText(new SimpleDateFormat(
                    "HH:mm:ss.S", Locale.US).format(message.getDate()));

        TextView busView = (TextView) convertView.findViewById(R.id.bus);
        busView.setText("" + message.getBusId());

        TextView idView = (TextView) convertView.findViewById(R.id.id);
        idView.setText("0x" + Integer.toHexString(message.getId()));

        TextView dataView = (TextView) convertView.findViewById(R.id.data);
        dataView.setText("0x" + ByteAdapter.byteArrayToHexString(
                    message.getData()));

        return convertView;
    }
}
