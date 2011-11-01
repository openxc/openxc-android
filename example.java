VehicleMeasurementService mVehicleService;

ServiceConnection mConnection = new ServiceConnectino() {
    @Override
    public void onServiceConnected(ComponentName className,
            IBinder service) {
        mVehicleService = binder.getService();
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
    }
};

Intent intent = new Intent(this, VehicleMeasurementService.class);
bindService(intent, mCOnnection, Context.BIND_AUTO_CREATE);


public class VehicleMeasurementService  extends Service {

    public Measurement<TheUnit extends Unit, ID extends String>
}

public class Measurement {
}

Measurement<Unit.MperS, signals.VEHICLE_SPEED_ID> -> VehicleSpeed
