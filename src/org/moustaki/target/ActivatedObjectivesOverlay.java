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
    }

    public void addObjective(Objective objective) {
        this.objectives.add(objective);
        populate();
    }
    
    public ArrayList<Objective> getObjectives() {
        return this.objectives;
    }
    
    public void draw(Canvas canvas, MapView mv, boolean shadow) {
        Paint paint = new Paint();
        paint.setARGB(150, 255, 255, 255);
        Projection projection = mv.getProjection();
        Point point = new Point();
        for (Objective objective : this.objectives) {
            GeoPoint gp = objective.getPoint();
            projection.toPixels(gp, point);
            int radius = this.metersToRadius(100, mv, (gp.getLatitudeE6() / 1000000.0));
            canvas.drawCircle(point.x, point.y, radius, paint);
        }
        super.draw(canvas, mv, shadow);
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
