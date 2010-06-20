package org.moustaki.target;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class ObjectivesOverlay extends ItemizedOverlay {

    private ArrayList<OverlayItem> overlays = new ArrayList<OverlayItem>();
    private Context context;
    private static ObjectivesOverlay instance = null;

    public ObjectivesOverlay(Drawable d, Context c) {
        super(boundCenterBottom(d));
        this.context = c;
    }
    
    public static ObjectivesOverlay getObjectivesOverlay(Drawable d, Context c) {
        if (instance == null) {
            instance = new ObjectivesOverlay(d, c);
        }
        return instance;
    }
    
    public void clear() {
        this.overlays.clear();
    }

    public void addOverlay(OverlayItem overlay) {
        this.overlays.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return this.overlays.get(i);
    }

    @Override
    public int size() {
        return this.overlays.size();
    }

    @Override
    protected boolean onTap(int index) {
        OverlayItem item = overlays.get(index);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();
        return true;
    }
}
