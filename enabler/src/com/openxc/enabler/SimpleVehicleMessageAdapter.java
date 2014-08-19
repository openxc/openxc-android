package com.openxc.enabler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openxc.messages.SimpleVehicleMessage;

public class SimpleVehicleMessageAdapter extends KeyedMessageAdapter {
    private Context mContext;

    public SimpleVehicleMessageAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public SimpleVehicleMessage getItem(int position) {
        return mValues.get(position).asSimpleMessage();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.dashboard_list_item, parent, false);
        }

        SimpleVehicleMessage message = getItem(position);

        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        nameView.setText("" + message.getName());

        TextView valueView = (TextView) convertView.findViewById(R.id.value);
        valueView.setText("" + message.getValue());

        return convertView;
    }
}
