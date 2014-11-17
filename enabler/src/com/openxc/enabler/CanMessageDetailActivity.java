package com.openxc.enabler;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.openxc.messages.CanMessage;
import com.openxc.messages.formatters.ByteAdapter;
import com.openxcplatform.enabler.R;

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

            TextView busView = (TextView) findViewById(R.id.bus);
            busView.setText("" + message.getBusId());

            TextView idView = (TextView) findViewById(R.id.id);
            idView.setText("0x" + Integer.toHexString(message.getId()));

            TextView dataView = (TextView) findViewById(R.id.data);
            dataView.setText("0x" + ByteAdapter.byteArrayToHexString(
                        message.getData()));
        } else {
            finish();
        }
    }
}
