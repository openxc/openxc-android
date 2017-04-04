package com.openxc.measurements;

import com.openxc.units.dBm;
import com.openxc.util.Range;

/**
 * The ModemSignalStrength is the TBD.
 *
 * TBD.
 */
public class ModemSignalStrength extends BaseMeasurement<dBm> {
    private final static Range<dBm> RANGE =
        new Range<dBm>(new dBm(-121), new dBm(-51)); // http://arstechnica.com/gadgets/2013/12/the-state-of-smartphones-in-2013-part-iii-how-the-experts-use-their-phones/3/
    public final static String ID = "modem_signal_strength";

    public ModemSignalStrength(Number value) {
        super(new dBm(value), RANGE);
    }

    public ModemSignalStrength(dBm value) {
        super(value, RANGE);
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}

