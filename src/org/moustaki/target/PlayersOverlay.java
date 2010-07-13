package org.moustaki.target;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class PlayersOverlay extends ItemizedOverlay {
    
    private ArrayList<Player> displayedPlayers = new ArrayList<Player>();
    private ArrayList<Player> players = new ArrayList<Player>();
    private Target context;
    
    public PlayersOverlay(Drawable d, Target c) {
        super(boundCenterBottom(d));
        this.context = c;
        populate();
    }

    public void updatePlayer(Player player, boolean display) {
        Player p = this.findPlayerById(player.getId());
        if (p != null) {
            this.players.remove(p);
            if (display) this.displayedPlayers.remove(p);
        }
        this.players.add(player);
        if (display) this.displayedPlayers.add(player);
        populate();
    }
    
    public Player findDisplayedPlayerById(int id) {
        for (Player player : displayedPlayers) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }
    
    public Player findPlayerById(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }
    
    public void clear() {
        this.displayedPlayers.clear();
        this.players.clear();
    }
    
    @Override
    protected OverlayItem createItem(int i) {
        return this.displayedPlayers.get(i);
    }

    @Override
    public int size() {
        return this.displayedPlayers.size();
    }
    
    //@Override
    //protected boolean onTap(int index) {
    //    OverlayItem item = this.players.get(index);
    //    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
    //    dialog.setTitle(item.getTitle());
    //    dialog.setMessage(item.getSnippet());
    //    dialog.show();
    //    return true;
    //}
}
