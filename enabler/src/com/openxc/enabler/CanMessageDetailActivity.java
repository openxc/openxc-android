package com.openxc.enabler;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.openxc.messages.CanMessage;

public class CanMessageDetailActivity extends Activity {
    public final static String EXTRA_CAN_MESSAGE = "EXTRA_CAN_MESSAGE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.can_message_details);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            CanMessage message = extras.getParcelable(EXTRA_CAN_MESSAGE);

            TextView timestampView = (TextView) findViewById(R.id.timestamp);
            timestampView.setText("" + message.getTimestamp());

            TextView busView = (TextView) findViewById(R.id.can_message_bus);
            busView.setText("" + message.getBus());

            TextView idView = (TextView) findViewById(R.id.can_message_id);
            idView.setText("0x" + Integer.toHexString(message.getId()));

            TextView dataView = (TextView) findViewById(R.id.can_message_data);
            dataView.setText("0x" + CanMessageAdapter.bytesToHex(message.getData()));
        } else {
            finish();
        }
    }
}
