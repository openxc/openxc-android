package com.openxc.enabler;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openxc.NoValueException;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
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
        try {
            // TODO most measurements are missing from the ID cache because we
            // haven't set up an explicit listener before - use a class loader
            // to cache these on the fly
            Measurement measurement =
                BaseMeasurement.getMeasurementFromMessage(message);
            valueView.setText("" + measurement.toString());
        } catch(UnrecognizedMeasurementTypeException e) {
            valueView.setText("" + message.getValue());
        } catch(NoValueException e) {
        }

        return convertView;
    }
}
