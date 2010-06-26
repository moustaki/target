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
    
    private String base = null;
    private int playerId;
    private int gameId;
    private int playerSide;
    private boolean isGameMaster;
    private boolean isStarted = false;
    private ObjectivesOverlay objectives;
    private Target context;
    
    public Game(String base, Target context) {
        this.context = context;
        this.base = base;
    }
    
    public void setObjectives(ObjectivesOverlay objectives) {
        this.objectives = objectives;
    }
    
    public boolean start() {
        this.isStarted = true;
        for(Objective objective : this.objectives.getObjectives()) {
            registerObjective(objective);
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
            }
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
    
    public boolean registerObjective(Objective objective) {
        HashMap<String,String> data = new HashMap<String,String>();
        data.put("game_id", ""+this.gameId);
        data.put("id_in_game", ""+objective.getIdInGame());
        data.put("power", ""+objective.getPower());
        data.put("latitude", ""+objective.getPoint().getLatitudeE6());
        data.put("longitude", ""+objective.getPoint().getLongitudeE6());
        JSONObject response = this.postJSON("/objectives", data);
        try {
            objective.setId(response.getInt("id"));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
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
