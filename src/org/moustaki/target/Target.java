package org.moustaki.target;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class Target extends MapActivity {
    
    private static final int MENU_ADD_OBJECTIVES = 0;
    private static final int MENU_QUIT = 1;
    private static final int MENU_START_GAME = 2;
    private static final int MENU_ACTIVATE_OBJECTIVE = 3;
    private static final int MENU_PICKUP_GUN = 4;
    private static final int MENU_PICKUP_BOMB = 5;
    private static final int MENU_KILL_ALIEN = 6;
    private static final int MENU_DESTROY_OBJECTIVE = 7;
    private static final int MENU_WON = 8;
    private static final int MENU_LOST = 9;
    private static final int MENU_KILLED = 10;
    
    private LocationManager lm;
    private TargetLocationListener ll;
    private MapController mc;
    private MapView mv;
    private ObjectivesOverlay objectives;
    private ActivatedObjectivesOverlay activatedObjectives;
    private PlayersOverlay humans;
    private PlayersOverlay aliens;
    private ObjectivesOverlay bombs;
    private ObjectivesOverlay takenBombs;
    private ObjectivesOverlay guns;
    private ObjectivesOverlay takenGuns;
    private Game game;
    private boolean running = true;
    private boolean danger = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Setting default zoom level
        this.mv = (MapView) findViewById(R.id.mapview);
        this.mv.setBuiltInZoomControls(true);
        this.mc = this.mv.getController();
        this.mc.setZoom(16);
        
        // Setting location tracking
        this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Drawable selfDrawable = this.getResources().getDrawable(R.drawable.self);
        SelfOverlay selfOverlay = new SelfOverlay(selfDrawable, this);
        this.mv.getOverlays().add(selfOverlay);
        this.ll = new TargetLocationListener(this, selfOverlay);
        this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll); // Should be LocationManager.GPS_PROVIDER
        
        // Setting objectives overlay
        Drawable drawable = this.getResources().getDrawable(R.drawable.persuadotron);
        this.objectives = new ObjectivesOverlay(drawable, this);
        this.mv.getOverlays().add(this.objectives);
        
        // Setting activated objectives overlay
        this.activatedObjectives = new ActivatedObjectivesOverlay(drawable, this);
        this.mv.getOverlays().add(this.activatedObjectives);
        
        // Setting bombs overlay
        Drawable drawableBomb = this.getResources().getDrawable(R.drawable.bomb);
        this.bombs = new ObjectivesOverlay(drawableBomb, this);
        this.mv.getOverlays().add(this.bombs);
        
        // Setting picked up bombs overlay
        Drawable drawablePickedUpBomb = this.getResources().getDrawable(R.drawable.bombtaken);
        this.takenBombs = new ObjectivesOverlay(drawablePickedUpBomb, this);
        this.mv.getOverlays().add(this.takenBombs);
        
        // Setting guns overlay
        Drawable drawableGun = this.getResources().getDrawable(R.drawable.skull);
        this.guns = new ObjectivesOverlay(drawableGun, this);
        this.mv.getOverlays().add(this.guns);
        
        // Setting picked up guns overlay
        Drawable drawablePickedUpGun = this.getResources().getDrawable(R.drawable.skulltaken);
        this.takenGuns = new ObjectivesOverlay(drawablePickedUpGun, this);
        this.mv.getOverlays().add(this.takenGuns);
        
        // Setting other players overlay
        Drawable drawableHuman = this.getResources().getDrawable(R.drawable.human);
        this.humans = new PlayersOverlay(drawableHuman, this);
        Drawable drawableAlien = this.getResources().getDrawable(R.drawable.alien);
        this.aliens = new PlayersOverlay(drawableAlien, this);
        
        // Setting up game context
        // @todo - shouldn't be hardcoded here
        //this.game = new Game("http://moustaki-target.appspot.com", this);
        this.game = new Game("http://192.168.1.69:1234", this);
        this.game.setActivatedObjectives(this.activatedObjectives);
        this.game.setObjectives(this.objectives);
        this.game.setGuns(this.guns);
        this.game.setTakenGuns(this.takenGuns);
        this.game.setBombs(this.bombs);
        this.game.setTakenBombs(this.takenBombs);
        this.game.setHumanPlayers(this.humans);
        this.game.setAlienPlayers(this.aliens);
        this.mv.getOverlays().add(this.humans);
        this.mv.getOverlays().add(this.aliens);
        
        // Registering user
        this.game.register();
        CharSequence notification = "Registered as user " + this.game.getPlayerId();
        Toast.makeText(this, notification, Toast.LENGTH_SHORT).show();
        
        // Human or Alien?
        this.pickSide();
        
        // Pick game identifier
        this.joinGame();
        
        // Start syncing locations
        this.syncLocations();
        
        // Start syncing objectives
        this.syncObjectivesStatus();
    }
    
    public MapView getMapView() {
       return this.mv; 
    }
    
    public MapController getMapController() {
        return this.mc;
    }
    
    public TargetLocationListener getLocationListener() {
        return this.ll;
    }
    
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (!this.game.isStarted()) {
            // Game not yet started
            if (this.game.isGameMaster()) {
                menu.add(0, MENU_ADD_OBJECTIVES, 0, this.getString(R.string.new_objectives));
            }
            if (this.objectives.size() > 0 && this.game.isGameMaster()) {
                // Some objectives available, and we are the master
                menu.add(0, MENU_START_GAME, 0, this.getString(R.string.start_game));
            }
        } else if (!this.game.isKilled()) {
            // Game started
            // @todo invert conditions
            if ((this.getObjectiveInRange() != null) 
                    && !this.getObjectiveInRange().isActivated()
                    && this.game.isAlien()) {
                menu.add(0, MENU_ACTIVATE_OBJECTIVE, 0, this.getString(R.string.objective_action));
            }
            if ((this.getGunInRange() != null)
                    && this.game.isHuman()) {
                menu.add(0, MENU_PICKUP_GUN, 0, "Get the gun");
            }
            if ((this.getBombInRange() != null)
                    && this.game.isHuman()) {
                menu.add(0, MENU_PICKUP_BOMB, 0, "Get the bomb");
            }
            if ((this.getAlienInRange() != null)
                    && this.game.isHuman()
                    && this.game.getAvailableGunsNumber() > 0) {
                menu.add(0, MENU_KILL_ALIEN, 0, "Kill!");
            }
            if (this.game.isHuman()
                    && this.getActivatedObjectiveInRange() != null
                    && this.game.getAvailableBombsNumber() > 0) {
                menu.add(0, MENU_DESTROY_OBJECTIVE, 0, "Destroy!");
            }
            if ((this.game.isEnded() && this.game.isWon())) {
                menu.add(0, MENU_WON, 0, "You won!!");
            }
            if ((this.game.isEnded() && !this.game.isWon())) {
                menu.add(0, MENU_LOST, 0, "You lost...");
            }
        } else {
            menu.add(0, MENU_KILLED, 0, "You were killed...");
        }
        menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ADD_OBJECTIVES:
            this.objectives.addRandomObjectives();
            this.guns.addRandomItems(10, "Gun");
            this.bombs.addRandomItems(10, "Bomb");
            return true;
        case MENU_QUIT:
            this.running = false;
            this.finish();
            return true;
        case MENU_START_GAME:
            Toast.makeText(this, "Starting game...", Toast.LENGTH_SHORT).show();
            this.game.start();
            return true;
        case MENU_ACTIVATE_OBJECTIVE:
            this.activateObjective();
            return true;
        case MENU_DESTROY_OBJECTIVE:
            this.destroyObjective();
            return true;
        case MENU_PICKUP_GUN:
            this.pickupGun();
            return true;
        case MENU_PICKUP_BOMB:
            this.pickupBomb();
            return true;
        case MENU_KILL_ALIEN:
            this.kill();
            return true;
        }
        return false;
    }
    
    public void win() {
        Toast.makeText(this, "You win!!!", Toast.LENGTH_LONG);
        this.game.win();
    }
    
    public void kill() {
        Player alien = this.getAlienInRange();
        if (this.game.getAvailableGunsNumber() > 0) {
            this.game.kill(alien);
            this.aliens.removePlayer(alien);
            // wins if all aliens are killed
            if (this.aliens.size() == 0) {
                this.win();
            }
            this.game.removeOneGun();
        }
    }
    
    public void pickupGun() {
        Objective gun = this.getGunInRange();
        this.takenGuns.addObjective(gun);
        this.guns.removeObjective(gun);
        this.game.activate(gun);
        this.game.addOneGun();
        Toast.makeText(Target.this, "You picked one gun!", 1).show();
    }
    
    public void pickupBomb() {
        Objective bomb = this.getBombInRange();
        this.takenBombs.addObjective(bomb);
        this.bombs.removeObjective(bomb);
        this.game.activate(bomb);
        this.game.addOneBomb();
        Toast.makeText(Target.this, "You picked one bomb!", 1).show();
    }
    
    public void destroyObjective() {
        Objective objective = this.getActivatedObjectiveInRange();
        if (objective != null) {
            this.activatedObjectives.removeObjective(objective);
            this.game.activate(objective);
            Toast.makeText(Target.this, "You destroyed the objective!", 1).show();
        }
    }
    
    public void checkIfInDangerousZone() {
        if (!danger) {
            Objective objective = this.getDangerousObjectiveInRange();
            if (objective != null) {
                danger = true;
                final ProgressDialog progress = new ProgressDialog(this);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setMessage("Infection...");
                progress.setCancelable(false);
                progress.show();
                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int elapsed = msg.what;
                        if (elapsed > 0 && elapsed < 120) {
                            progress.setProgress((elapsed * 100 / 120));
                        } else if (elapsed == -1) {
                            progress.dismiss();
                            Toast.makeText(Target.this, "You left the dangerous area.", 1).show();
                        } else if (elapsed == 999) {
                            progress.dismiss();
                            Toast.makeText(Target.this, "Arg. You're dead.", 1).show();
                            getGame().kill(getGame().getPlayer());
                        }
                    }
                };
                Thread activating = new Thread() {
                    public void run() {
                        try {
                            Objective objective = getDangerousObjectiveInRange();
                            int start = getUnixTime();
                            int current = start;
                            while (current - start < 120) {
                                if (objective != getDangerousObjectiveInRange()) {
                                    handler.sendEmptyMessage(-1);
                                    danger = false;
                                    return;
                                }
                                current = getUnixTime();
                                int elapsed = (current - start);
                                handler.sendEmptyMessage(elapsed);
                                Thread.sleep(1000);
                            }
                            handler.sendEmptyMessage(999);
                            getGame().kill(getGame().getPlayer());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Toast.makeText(this, "Entering dangerous zone... You have two minutes to leave.", Toast.LENGTH_LONG).show();
                activating.start();
            }
        }
    }
    
    public void activateObjective() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMessage("Activating...");
        progress.setCancelable(false);
        progress.show();
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int elapsed = msg.what;
                if (elapsed > 0 && elapsed < 60) {
                    progress.setProgress((elapsed * 100 / 60));
                } else if (elapsed == -1) {
                    progress.dismiss();
                    Toast.makeText(Target.this, "You left the area. Stopping the activation.", 1).show();
                } else if (elapsed == 999) {
                    progress.dismiss();
                    Toast.makeText(Target.this, "Persuadotron activated!!", 1).show();
                }
            }
        };
        Thread activating = new Thread() {
            public void run() {
                try {
                    Objective objective = getObjectiveInRange();
                    int start = getUnixTime();
                    int current = start;
                    while (current - start < 60) {
                        if (objective != getObjectiveInRange()) {
                            handler.sendEmptyMessage(-1);
                            return;
                        }
                        current = getUnixTime();
                        int elapsed = (current - start);
                        handler.sendEmptyMessage(elapsed);
                        Thread.sleep(1000);
                    }
                    handler.sendEmptyMessage(999);
                    getActivatedObjectives().addObjective(objective);
                    getGame().activate(objective);
                    // Wins the game if > 3000W
                    if (getGame().getTotalPower() > 3000) {
                        win();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Toast.makeText(this, "Activating persuadotron... Wait one minute in the area...", Toast.LENGTH_LONG).show();
        activating.start();
    }
    
    private int getUnixTime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }
    
    public Game getGame() {
        return this.game;
    }
    
    public ObjectivesOverlay getObjectives() {
        return this.objectives;
    }
    
    public ActivatedObjectivesOverlay getActivatedObjectives() {
        return this.activatedObjectives;
    }
    
    public Objective getDangerousObjectiveInRange() {
        // range for infected zone: 150m
        return this.objectives.getClosestObjectiveInRange(this.ll.getCurrentLocation(), 150.0, true);
    }
    
    public Objective getActivatedObjectiveInRange() {
        // range for activated objectives: 20m
        return this.objectives.getClosestObjectiveInRange(this.ll.getCurrentLocation(), 20.0, true);
    }
    
    public Objective getObjectiveInRange() {
        // range for objectives: 100m
        return this.objectives.getClosestObjectiveInRange(this.ll.getCurrentLocation(), 100.0, false);
    }
    
    public Objective getGunInRange() {
        // range for guns: 50m
        return this.guns.getClosestObjectiveInRange(this.ll.getCurrentLocation(), 50.0, false);
    }
    
    public Objective getBombInRange() {
        // range for bombs: 50m
        return this.bombs.getClosestObjectiveInRange(this.ll.getCurrentLocation(), 50.0, false);
    }
    
    public Player getAlienInRange() {
        // range for players: 20m
        return this.aliens.getClosestPlayerInRange(this.ll.getCurrentLocation(), 20.0);
    }
    
    public boolean pickSide() {
        final CharSequence[] items = {this.getString(R.string.good_guys), this.getString(R.string.bad_guys)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.side_picker));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Context context = getApplicationContext();
                getGame().setSide(item);
                Toast.makeText(context, items[getGame().getPlayerSide()], Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }
    
    public boolean joinGame() {
        final CharSequence[] items = {"Start new game", "Join existing game"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join a game");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Context context = getApplicationContext();
                if (item == 0) {
                    int gameId = getGame().registerNewGame();
                    getGame().joinGame(gameId);
                    Toast.makeText(context, "Joined game " + gameId, Toast.LENGTH_SHORT).show();
                } else {
                    joinExistingGame();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }
    
    public void joinExistingGame() {
        AlertDialog.Builder gameIdInput = new AlertDialog.Builder(this);
        gameIdInput.setTitle("Join game");
        gameIdInput.setMessage("Enter game identifier");
        final EditText input = new EditText(this);
        gameIdInput.setView(input);
        gameIdInput.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int button) {  
               int gameId = Integer.parseInt(input.getText().toString());
               getGame().joinGame(gameId);
               Context context = getApplicationContext();
               Toast.makeText(context, "Joined game " + gameId, Toast.LENGTH_SHORT).show();
            }  
        });
        gameIdInput.show();
    }
    
    public void syncLocations() {
        Thread postPlayerLocation = new Thread() {
            public void run() {
                try {
                    while (running) {
                        getGame().postPlayerLocation();
                        // post location every 10s
                        Thread.sleep(1000 * 10);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        postPlayerLocation.start();
        Thread updatePlayersLocation = new Thread() {
            public void run() {
                try {
                    int k = 0;
                    while (running) {
                        k += 1;
                        boolean update = false;
                        if (k == 6) {
                            k = 0;
                            update = true;
                        }
                        // update other players' locations every 10s
                        // display every 60s
                        Thread.sleep(1000 * 10);
                        getGame().updatePlayersLocations(update);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        updatePlayersLocation.start();
    }
    
    public void syncObjectivesStatus() {
        Thread syncObjectivesStatus = new Thread() {
            public void run() {
                try {
                    while (running) {
                        // get activated objectives every 10s
                        Thread.sleep(1000 * 10);
                        getGame().syncObjectivesStatus();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        syncObjectivesStatus.start();
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}