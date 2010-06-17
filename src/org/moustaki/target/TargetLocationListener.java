package org.moustaki.target;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class TargetLocationListener implements LocationListener {
	private MapController mc;
	private MapView mv;
	public TargetLocationListener(MapController mc, MapView mv) {
		this.mc = mc;
		this.mv = mv;
	}
	@Override
	public void onLocationChanged(Location loc) {
		if (loc != null) {
            GeoPoint p = new GeoPoint(
                    (int) (loc.getLatitude() * 1E6), 
                    (int) (loc.getLongitude() * 1E6)
            );
            this.mc.animateTo(p);
            this.mc.setZoom(16);       
            this.mv.invalidate();
		}
	}
	@Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    	// TODO Auto-generated method stub	
    }
}
