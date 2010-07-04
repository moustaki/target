package org.moustaki.target;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

public class Game {
    
    private static final int HUMAN = 0;
    private static final int ALIEN = 1;
    
    private String base = null;
    private int playerId;
    private int gameId;
    private int playerSide;
    private boolean isGameMaster;
    private boolean isStarted = false;
    private ObjectivesOverlay objectives;
    private ObjectivesOverlay guns;
    private ObjectivesOverlay bombs;
    private PlayersOverlay humanPlayers;
    private PlayersOverlay alienPlayers;
    private Target context;
    
    public Game(String base, Target context) {
        this.context = context;
        this.base = base;
    }
    
    public boolean isHuman() {
        return (this.getPlayerSide() == Game.HUMAN);
    }
    
    public boolean isAlien() {
        return (this.getPlayerSide() == Game.ALIEN);
    }
    
    public void setHumanPlayers(PlayersOverlay players) {
        this.humanPlayers = players;
    }
    
    public void setAlienPlayers(PlayersOverlay players) {
        this.alienPlayers = players;
    }
    
    public void setObjectives(ObjectivesOverlay objectives) {
        this.objectives = objectives;
    }
    
    public void setGuns(ObjectivesOverlay guns) {
        this.guns = guns;
    }
    
    public void setBombs(ObjectivesOverlay bombs) {
        this.bombs = bombs;
    }
    
    public boolean start() {
        this.isStarted = true;
        // @todo - make sure other clients don't try to register in the middle of that
        if (this.isGameMaster) {
            for(Objective objective : this.objectives.getObjectives()) {
                registerObjective(objective, "objective");
            }
            for(Objective gun : this.guns.getObjectives()) {
                registerObjective(gun, "gun");
            }
            for(Objective bomb : this.bombs.getObjectives()) {
                registerObjective(bomb, "bomb");
            }
        }
        return true;
    }
    
    public boolean isStarted() {
        return this.isStarted;
    }
    
    public int getPlayerId() {
        return this.playerId;
    }
    
    public int getPlayerSide() {
        return this.playerSide;
    }
    
    public boolean isGameMaster() {
        return this.isGameMaster;
    }
    
    public boolean activate(Objective objective) {
        objective.setActivated(true);
        HashMap<String,String> data = new HashMap<String,String>();
        if (objective.isActivated()) {
            data.put("activated", "true");
        } else {
            data.put("activated", "false");
        }
        JSONObject response = this.postJSON("/objectives/" + objective.getId(), data);
        try {
            objective.setId(response.getInt("id"));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public int register() {
        JSONObject response = this.getJSON("/register");
        try {
            this.playerId = response.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.playerId;
    }
    
    public int registerNewGame() {
        JSONObject response = this.getJSON("/start");
        try {
            gameId = response.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gameId;
    }
    
    public boolean joinGame(int gameId) {
        this.gameId = gameId;
        HashMap<String,String> data = new HashMap<String,String>();
        data.put("player_id", ""+this.playerId);
        JSONObject response = this.postJSON("/games/" + gameId, data);
        try { 
            int masterPlayerId = response.getJSONArray("players").getJSONObject(0).getInt("id");
            if (this.playerId == masterPlayerId) {
                this.isGameMaster = true;
                // We stop here, so that the game master can set up objectives
                return true;
            }
            if (response.getJSONArray("objectives").length() == 0) {
                try {
                    // we wait for 5 seconds and get the game data again
                    // @todo display something while sleeping
                    Thread.sleep(5000);
                    this.joinGame(gameId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < response.getJSONArray("objectives").length(); i++) {
                JSONObject o = response.getJSONArray("objectives").getJSONObject(i);
                String type = o.getString("type");
                if (type.equals("objective")) {
                    int latitude = o.getInt("latitude");
                    int longitude = o.getInt("longitude");
                    GeoPoint point = new GeoPoint(latitude, longitude);
                    int power = o.getInt("power");
                    int idInGame = o.getInt("id_in_game");
                    int id = o.getInt("id");
                    Objective objective = new Objective(id, idInGame, point, power,
                            this.context.getString(R.string.objective) + " "+ Integer.toString(i), 
                            power + " " + this.context.getString(R.string.objective_unit));
                    this.objectives.addObjective(objective);
                } else if (type.equals("gun")) {
                    int latitude = o.getInt("latitude");
                    int longitude = o.getInt("longitude");
                    GeoPoint point = new GeoPoint(latitude, longitude);
                    int idInGame = o.getInt("id_in_game");
                    int id = o.getInt("id");
                    Objective objective = new Objective(id, idInGame, point, "Gun " + idInGame);
                    this.guns.addObjective(objective);
                } else if (type.equals("bomb")) {
                    int latitude = o.getInt("latitude");
                    int longitude = o.getInt("longitude");
                    GeoPoint point = new GeoPoint(latitude, longitude);
                    int idInGame = o.getInt("id_in_game");
                    int id = o.getInt("id");
                    Objective objective = new Objective(id, idInGame, point, "Bomb " + idInGame);
                    this.bombs.addObjective(objective);
                }
            }
            this.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean setSide(int side) {
        HashMap<String,String> data = new HashMap<String,String>();
        data.put("side", ""+(side+1)); // @todo - 0 gets dropped by appengine, for some reason
        JSONObject response = this.postJSON("/players/" + this.playerId, data);
        try {
            this.playerSide = response.getInt("side") - 1;
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean registerObjective(Objective objective, String type) {
        HashMap<String,String> data = new HashMap<String,String>();
        data.put("game_id", ""+this.gameId);
        data.put("id_in_game", ""+objective.getIdInGame());
        data.put("power", ""+objective.getPower());
        data.put("latitude", ""+objective.getPoint().getLatitudeE6());
        data.put("longitude", ""+objective.getPoint().getLongitudeE6());
        data.put("type", type);
        JSONObject response = this.postJSON("/objectives", data);
        try {
            objective.setId(response.getInt("id"));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean postPlayerLocation() {
        GeoPoint point = this.context.getLocationListener().getCurrentLocation();
        if (point != null) {
            HashMap<String,String> data = new HashMap<String,String>();
            data.put("latitude", ""+point.getLatitudeE6());
            data.put("longitude", ""+point.getLongitudeE6());
            // setting geo location
            JSONObject response = this.postJSON("/players/" + this.playerId, data);
            if (response == null) return false;
        }
        return true;
    }
    
    public boolean updatePlayersLocations() {
        // getting geo locations
        JSONObject response = this.getJSON("/games/" + this.gameId);
        try {
            this.alienPlayers.clear();
            this.humanPlayers.clear();
            for (int i = 0; i < response.getJSONArray("players").length(); i++) {
                JSONObject p = response.getJSONArray("players").getJSONObject(i);
                int id = p.getInt("id");
                if (id != this.playerId) {
                    int latitude = p.getInt("latitude");
                    int longitude = p.getInt("longitude");
                    int side = p.getInt("side") - 1; // @todo weird side = 0 bug
                    GeoPoint playerPoint = new GeoPoint(latitude, longitude);
                    Player player = new Player(p.getInt("id"), side, playerPoint, ""+p.getInt("id"), ""); 
                    if (side == 0) this.humanPlayers.addPlayer(player);
                    if (side == 1) this.alienPlayers.addPlayer(player);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    private JSONObject getJSON(String path) {
        try {
            URL url = new URL(this.base + path);
            URLConnection connection = url.openConnection();
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String response = builder.toString();
            return new JSONObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private JSONObject postJSON(String path, HashMap<String,String> data) {
        try {
            String post = "";
            for (String key : data.keySet()) {
                if (post != "") {
                    post += "&";
                }
                post += URLEncoder.encode(key, "UTF-8") + '=' + URLEncoder.encode(data.get(key), "UTF-8");
            }
            URL url = new URL(this.base + path);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream()); 
            wr.write(post);
            wr.flush();
            
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String response = builder.toString();
            return new JSONObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
