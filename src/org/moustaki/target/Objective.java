package org.moustaki.target;

import java.util.ArrayList;

import android.app.AlertDialog;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Objective extends OverlayItem {
    
    private static ArrayList<Objective> objectives = new ArrayList<Objective>();
    private int id;
    
    public Objective(int id, GeoPoint point, String title, String description) {
        super(point, title, description);
        this.id = id;
        objectives.add(this);
    }
    
    public static int getNumberOfObjectives() {
        return objectives.size();
    }
    
    public static Objective getClosestObjectiveInRange(GeoPoint point, double range) {
        if (point != null) {
            double mindistance = 0;
            Objective closest = null;
            for (Objective objective : objectives) {
                double distance = DistanceCalculator.distance(point, objective.getPoint());
                if (mindistance == 0) {
                    mindistance = distance;
                    closest = objective;
                } else {
                    if (distance < mindistance) {
                        mindistance = distance;
                        closest = objective;
                    }
                }
            }
            if (mindistance < range) {
                return closest;
            }
        }
        return null;
    }
    
}
