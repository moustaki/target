package org.moustaki.target;

import java.util.ArrayList;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Objective extends OverlayItem {
    
    private int id;
    
    public Objective(int id, GeoPoint point, String title, String description) {
        super(point, title, description);
        this.id = id;
    }
}
