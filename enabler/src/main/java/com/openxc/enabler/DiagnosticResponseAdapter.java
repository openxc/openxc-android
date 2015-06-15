package com.openxc.enabler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.formatters.ByteAdapter;
import com.openxcplatform.enabler.R;

public class DiagnosticResponseAdapter extends VehicleMessageAdapter {
    private Context mContext;

    public DiagnosticResponseAdapter(Context context) {
        super();
        mContext = context;
    }

    @Override
    public DiagnosticResponse getItem(int position) {
        return mValues.get(position).asDiagnosticResponse();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.diagnostic_request_list_item, parent, false);
        }

        DiagnosticResponse message = getItem(position);

        TextView timestampView = (TextView) convertView.findViewById(R.id.timestamp);
        timestampView.setText(formatTimestamp(message));

        TextView busView = (TextView) convertView.findViewById(R.id.bus);
        busView.setText("" + message.getBusId());

        TextView idView = (TextView) convertView.findViewById(R.id.id);
        idView.setText("0x" + Integer.toHexString(message.getId()));

        TextView modeView = (TextView) convertView.findViewById(R.id.mode);
        modeView.setText("0x" + Integer.toHexString(message.getMode()));

        TextView pidView = (TextView) convertView.findViewById(R.id.pid);
        if(message.hasPid()) {
            pidView.setText("0x" + Integer.toHexString(message.getPid()));
        } else {
            pidView.setText("None");
        }

        TextView payloadView = (TextView) convertView.findViewById(R.id.payload);
        if(message.hasPayload()) {
            payloadView.setText("0x" + ByteAdapter.byteArrayToHexString(
                        message.getPayload()));
        } else if(message.hasValue()) {
            payloadView.setText("Value: " + message.getValue());
        } else {
            payloadView.setText("None");
        }

        return convertView;
    }
}
