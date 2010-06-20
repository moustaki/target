package org.moustaki.target;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class Target extends MapActivity {
    
    private static final int MENU_ADD_OBJECTIVES = 0;
    private static final int MENU_QUIT = 1;
    private static final int MENU_START_GAME = 2;
    private static final int MENU_GET_OBJECTIVE = 3; 
    
    private LocationManager lm;
    private TargetLocationListener ll;
    private MapController mc;
    private MapView mv;
    private ObjectivesOverlay objectives;
    private Game game;
    private boolean gameStarted = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Setting default zoom level
        this.mv = (MapView) findViewById(R.id.mapview);
        this.mv.setBuiltInZoomControls(true);
        this.mc = this.mv.getController();
        this.mc.setZoom(16);
        
        // Setting location tracking
        this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.ll = new TargetLocationListener(this);
        this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll); // Should be LocationManager.GPS_PROVIDER
        
        // Setting objectives overlay
        Drawable drawable = this.getResources().getDrawable(R.drawable.obj1);
        this.objectives = ObjectivesOverlay.getObjectivesOverlay(drawable, this);
        
        // Setting up game context
        // @todo - shouldn't be hardcoded here
        this.game = new Game("http://192.168.1.67:1234");
        
        // Registering user
        this.game.register();
        CharSequence notification = "Registered as user " + this.game.getPlayerId();
        Toast.makeText(this, notification, Toast.LENGTH_SHORT).show();
        
        // Bad guy or good guy?
        this.pickSide();
    }
    
    public MapView getMapView() {
       return this.mv; 
    }
    
    public MapController getMapController() {
        return this.mc;
    }
    
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (!this.gameStarted) {
            // Game not yet started
            menu.add(0, MENU_ADD_OBJECTIVES, 0, "New objectives");
            if (this.objectives.size() > 0) {
                // Some objectives available
                menu.add(0, MENU_START_GAME, 0, "Start game");
            }
        } else {
            // Game started
            // Need to tell to the user if the GPS connection has dropped, here
            if (this.objectives.getClosestObjectiveInRange(this.ll.getCurrentLocation(), 20.0) != null) {
                menu.add(0, MENU_GET_OBJECTIVE, 0, "Commit robbery");
            }
        }
        menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ADD_OBJECTIVES:
            this.objectives.addObjectives(10);
            List<Overlay> overlays = this.mv.getOverlays();
            overlays.add(this.objectives);
            return true;
        case MENU_QUIT:
            this.finish();
            return true;
        case MENU_START_GAME:
            this.gameStarted = true;
        }
        return false;
    }
    
    public Game getGame() {
        return this.game;
    }
    
    public boolean pickSide() {
        final CharSequence[] items = {"Police", "Thieves"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a side");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Context context = getApplicationContext();
                getGame().setSide(item);
                Toast.makeText(context, items[getGame().getPlayerSide()], Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}