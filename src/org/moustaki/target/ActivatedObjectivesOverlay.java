package org.moustaki.target;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;


public class ActivatedObjectivesOverlay extends com.google.android.maps.ItemizedOverlay {
    private ArrayList<Objective> objectives = new ArrayList<Objective>();
    private Target context;

    public ActivatedObjectivesOverlay(Drawable d, Target c) {
        super(boundCenterBottom(d));
        this.context = c;
        populate();
    }
    
    public void clear() {
        this.objectives.clear();
        populate();
    }
    
    public void removeObjective(Objective objective) {
        this.objectives.remove(objective);
        populate();
    }

    public void addObjective(Objective objective) {
        if (this.findActivatedObjectiveById(objective.getId()) == null) {
            this.objectives.add(objective);
            populate();
        }
    }
    
    public Objective findActivatedObjectiveById(int id) {
        for (Objective objective : objectives) {
            if (objective.getId() == id) {
                return objective;
            }
        }
        return null;
    }
    
    public ArrayList<Objective> getObjectives() {
        return this.objectives;
    }
    
    public void draw(Canvas canvas, MapView mv, boolean shadow) {
        Paint paint = new Paint();
        paint.setARGB(80, 255, 0, 0);
        paint.setAntiAlias(true);
        Projection projection = mv.getProjection();
        Point point = new Point();
        for (Objective objective : this.objectives) {
            GeoPoint gp = objective.getPoint();
            projection.toPixels(gp, point);
            int radius = this.metersToRadius(150, mv, (gp.getLatitudeE6() / 1000000.0));
            canvas.drawCircle(point.x, point.y, radius, paint);
        }
        super.draw(canvas, mv, shadow);
        // @todo maybe boundCenter() for all items on map?
    }
    
    private int metersToRadius(float meters, MapView map, double latitude) {
        return (int) (map.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
    }
    
    @Override
    protected OverlayItem createItem(int i) {
        return this.objectives.get(i);
    }

    @Override
    public int size() {
        return this.objectives.size();
    }
}
