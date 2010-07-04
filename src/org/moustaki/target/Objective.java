package org.moustaki.target;

import java.util.ArrayList;

import android.graphics.Canvas;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class Objective extends OverlayItem {
    
    private int id = -1;
    private int id_in_game;
    private int power;
    private boolean activated = false;
    
    public Objective(int id, int id_in_game, GeoPoint point, int power, String title, String description) {
        super(point, title, description);
        this.id = id;
        this.id_in_game = id_in_game;
        this.power = power;
    }
    
    public Objective(int id_in_game, GeoPoint point, int power, String title, String description) {
        super(point, title, description);
        this.id_in_game = id_in_game;
        this.power = power;
    }
    
    public Objective(int id, int id_in_game, GeoPoint point, String title) {
        super(point, title, "");
        this.id = id;
        this.id_in_game = id_in_game;
    }
    
    public Objective(int id_in_game, GeoPoint point, String title) {
        super(point, title, "");
        this.id_in_game = id_in_game;
    }
    
    public int getPower() {
        return this.power;
    }
    
    public int getIdInGame() {
        return this.id_in_game;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    
    public boolean isActivated() {
        return this.activated;
    }
}
