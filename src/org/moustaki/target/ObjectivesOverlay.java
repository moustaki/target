package org.moustaki.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class ObjectivesOverlay extends ItemizedOverlay {

    private ArrayList<Objective> objectives = new ArrayList<Objective>();
    private Target context;

    public ObjectivesOverlay(Drawable d, Target c) {
        super(boundCenterBottom(d));
        this.context = c;
        populate();
    }
    
    public void clear() {
        this.objectives.clear();
    }

    public void addObjective(Objective objective) {
        this.objectives.add(objective);
        populate();
    }
    
    public void removeObjective(Objective objective) {
        this.objectives.remove(objective);
        populate();
    }
    
    public ArrayList<Objective> getObjectives() {
        return this.objectives;
    }
    
    @Override
    protected OverlayItem createItem(int i) {
        return this.objectives.get(i);
    }

    @Override
    public int size() {
        return this.objectives.size();
    }

    @Override
    protected boolean onTap(int index) {
        OverlayItem item = this.objectives.get(index);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();
        return true;
    }
    
    public Objective findObjectiveById(int id) {
        for (Objective objective : objectives) {
            if (objective.getId() == id) {
                return objective;
            }
        }
        return null;
    }
    
    public Objective getClosestObjectiveInRange(GeoPoint point, double range) {
        Objective closest = null;
        if (point != null) {
            double mindistance = 0.0;
            for (Objective objective : objectives) {
                double distance = DistanceCalculator.distance(point, objective.getPoint());
                if (mindistance == 0.0) {
                    mindistance = distance;
                    closest = objective;
                } else {
                    if (distance < mindistance) {
                        mindistance = distance;
                        closest = objective;
                    }
                }
            }
            boolean isInRange = (mindistance <= range);
            if (!isInRange) {
                closest = null;
            }
        }
        return closest;
    }
    
    public int addRandomObjectives() {
        // should be configurable?
        int n = 10;
        int[] power = {300, 300, 300, 300, 300, 500, 500, 500, 1000, 1000};
        objectives.clear();
        Objective objective = null;
        GeoPoint point = null;
        // Adding n objectives
        for (int i=0;i<n;i++) {
            point = this.getRandomLocationInCurrentMap();
            objective = new Objective(i, point, power[i], 
                    this.context.getString(R.string.objective) + " "+ Integer.toString(i), 
                    power[i] + " " + this.context.getString(R.string.objective_unit));
            this.addObjective(objective);
        }
        return n;
    }
    
    public int addRandomItems(int n, String title) {
        objectives.clear();
        Objective objective = null;
        GeoPoint point = null;
        // Adding n items
        for (int i=0;i<n;i++) {
            point = this.getRandomLocationInCurrentMap();
            objective = new Objective(i, point, title + " " + i);
            this.addObjective(objective);
        }
        return n;
    }

    public GeoPoint getRandomLocationInCurrentMap() {
        MapView mv = this.context.getMapView();
        int latitudeSpan = mv.getLatitudeSpan();
        int longitudeSpan = mv.getLongitudeSpan();
        GeoPoint centre = mv.getMapCenter();
        int minLatitude = centre.getLatitudeE6() - latitudeSpan/2;
        int minLongitude = centre.getLongitudeE6() - longitudeSpan/2;
        
        Random r = new Random();
        int randomLatitude = r.nextInt(latitudeSpan) + minLatitude;
        int randomLongitude = r.nextInt(longitudeSpan) + minLongitude;
        GeoPoint randomPoint = new GeoPoint(randomLatitude, randomLongitude);
        
        return randomPoint;
    }
}
