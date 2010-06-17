package org.moustaki.target;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class Target extends MapActivity {
	private LocationManager lm;
	private LocationListener ll;
	private MapController mc;
	private MapView mv;
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.mv = (MapView) findViewById(R.id.mapview);
		this.mv.setBuiltInZoomControls(true);
        this.mc = this.mv.getController();
        this.mc.setZoom(16);
        this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.ll = new TargetLocationListener(this.mc, this.mv);
        this.lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
	}
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}