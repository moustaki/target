package org.moustaki.target;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Target extends MapActivity {
    
    private static final int MENU_ADD_OBJECTIVES = 0;
    private static final int MENU_QUIT = 1;
    private static final int MENU_START_GAME = 2;
    private static final int MENU_GET_OBJECTIVE = 3; 
    
    private LocationManager lm;
    private LocationListener ll;
    private MapController mc;
    private MapView mv;
    private boolean gameStarted = false;
    private int numberOfObjectives = 0;
    
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
        this.lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll); // Should be LocationManager.GPS_PROVIDER
    }
    
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (!this.gameStarted) {
            // Game not yet started
            menu.add(0, MENU_ADD_OBJECTIVES, 0, "New objectives");
            if (this.numberOfObjectives > 0) {
                // Some objectives available
                menu.add(0, MENU_START_GAME, 0, "Start game");
            }
        } else {
            // Game started
            menu.add(0, MENU_GET_OBJECTIVE, 0, "Commit robbery");
        }
        menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ADD_OBJECTIVES:
            this.numberOfObjectives = this.addObjectives(10);
            return true;
        case MENU_QUIT:
            this.finish();
            return true;
        case MENU_START_GAME:
            this.gameStarted = true;
        }
        return false;
    }

    public int addObjectives(int n) {
        List<Overlay> overlays = this.mv.getOverlays();
        overlays.clear();
        Drawable drawable = this.getResources().getDrawable(R.drawable.obj1);
        ObjectivesOverlay objectives = new ObjectivesOverlay(drawable, this);
        OverlayItem objective = null;
        GeoPoint point = null;
        // Adding n objectives
        for (int i=0;i<n;i++) {
            point = this.getRandomLocationInCurrentMap();
            objective = new OverlayItem(point, "Objective " + Integer.toString(i), "Bank");
            objectives.addOverlay(objective);
        }
        overlays.add(objectives);
        return n;
    }

    public GeoPoint getRandomLocationInCurrentMap() {
        int latitudeSpan = this.mv.getLatitudeSpan();
        int longitudeSpan = this.mv.getLongitudeSpan();
        GeoPoint centre = this.mv.getMapCenter();
        int minLatitude = centre.getLatitudeE6() - latitudeSpan/2;
        int minLongitude = centre.getLongitudeE6() - longitudeSpan/2;
        
        Random r = new Random();
        int randomLatitude = r.nextInt(latitudeSpan) + minLatitude;
        int randomLongitude = r.nextInt(longitudeSpan) + minLongitude;
        GeoPoint randomPoint = new GeoPoint(randomLatitude, randomLongitude);
        
        return randomPoint;
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}