package org.moustaki.target;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class TargetLocationListener implements LocationListener {
    
	private MapController mc;
	private MapView mv;
	private GeoPoint currentLocation;
	private Context context;
	
	public TargetLocationListener(MapController mc, MapView mv, Context context) {
		this.mc = mc;
		this.mv = mv;
		this.context = context;
	}
	
	@Override
	public void onLocationChanged(Location loc) {
		if (loc != null) {
            GeoPoint p = new GeoPoint(
                    (int) (loc.getLatitude() * 1E6), 
                    (int) (loc.getLongitude() * 1E6)
            );
            this.currentLocation = p;
            this.mc.animateTo(p);
            this.mc.setZoom(16);       
            this.mv.invalidate();
            
            Drawable drawable = this.context.getResources().getDrawable(R.drawable.thief);
            ThiefOverlay thiefOverlay = new ThiefOverlay(drawable, this.context);
            OverlayItem thief = new OverlayItem(p, "Thief", "");
            thiefOverlay.addOverlay(thief);
            this.mv.getOverlays().add(thiefOverlay);
		}
	}
	
	public GeoPoint getCurrentLocation() {
	    return this.currentLocation;
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
