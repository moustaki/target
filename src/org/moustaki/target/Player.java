package org.moustaki.target;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Player extends OverlayItem {
    
    private int id;
    private int side;

    public Player(int id, int side, GeoPoint point, String title, String snippet) {
        super(point, title, snippet);
        this.id = id;
        this.side = side;
    }
    
    public int getId() {
        return this.id;
    }
}
