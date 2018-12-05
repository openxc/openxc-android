package com.openxc.enabler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openxc.NoValueException;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.SimpleV2XMessage;
import com.openxcplatform.enabler.R;

public class SimpleV2XMessageAdapter extends KeyedMessageAdapter {
	private Context mContext;

    public SimpleV2XMessageAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public SimpleV2XMessage getItem(int position) {
        return mValues.get(position).asSimpleV2XMessage();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.dashboard_list_item, parent, false);
        }

        SimpleV2XMessage message = getItem(position);
        TextView nameView = (TextView) convertView.findViewById(R.id.name);

        TextView valueView = (TextView) convertView.findViewById(R.id.value);
        try {
            Measurement measurement =
                BaseMeasurement.getMeasurementFromMessage(message);
            nameView.setText("" + measurement.getName(mContext));
            valueView.setText("" + measurement.toString());
        } catch(UnrecognizedMeasurementTypeException e) {
            nameView.setText("" + message.getLabel());
            valueView.setText("" + message.getValue());
        } catch(NoValueException e) {
        }

        return convertView;
    }

    @Override
    public boolean shouldRefreshView(KeyedMessage message, KeyedMessage existingMessage) {
        // We don't display timestamps in the Enabler for simple messages, don't
        // bother updating unless the value changed.
        return (((SimpleV2XMessage)message).getValue()).equals(
                ((SimpleV2XMessage)existingMessage).getValue());
    }
}
