package org.moustaki.target;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class PlayersOverlay extends ItemizedOverlay {
    
    private ArrayList<Player> players = new ArrayList<Player>();
    private Target context;
    
    public PlayersOverlay(Drawable d, Target c) {
        super(boundCenterBottom(d));
        this.context = c;
        populate();
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        populate();
    }
    
    public void clear() {
        this.players.clear();
    }
    
    @Override
    protected OverlayItem createItem(int i) {
        return this.players.get(i);
    }

    @Override
    public int size() {
        return this.players.size();
    }
    
    @Override
    protected boolean onTap(int index) {
        OverlayItem item = this.players.get(index);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();
        return true;
    }
}
