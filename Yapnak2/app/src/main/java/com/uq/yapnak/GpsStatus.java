package com.uq.yapnak;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by Joshua on 05/11/2015.
 */
public class GpsStatus {
    Context context;

    public GpsStatus(Context context) {
        this.context = context;
    }

    public boolean isEnabled() {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (!(locationMode != Settings.Secure.LOCATION_MODE_OFF)) {
                new Alert_Dialog(context).gpsError();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch(Exception ex) {}
            if (!gps_enabled) {
                new Alert_Dialog(context).gpsError();
            }
            return gps_enabled;
        }
    }
}
