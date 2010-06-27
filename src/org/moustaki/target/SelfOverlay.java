package org.moustaki.target;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class SelfOverlay extends ItemizedOverlay {
    
    private ArrayList<OverlayItem> thieves = new ArrayList<OverlayItem>();
    private Context context;
    
    public SelfOverlay(Drawable d, Context c) {
        super(boundCenterBottom(d));
        this.context = c;
        populate();
    }
    
    public void addOverlay(OverlayItem overlay) {
        this.thieves.clear();
        this.thieves.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return this.thieves.get(i);
    }

    @Override
    public int size() {
        return this.thieves.size();
    }
}
