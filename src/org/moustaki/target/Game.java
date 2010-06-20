package org.moustaki.target;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class Game {
    
    private String base = null;
    private int playerId;
    private int playerSide;
    
    public Game(String base) {
        this.base = base;
    }
    
    public boolean start(int gameId) {
        return true;
    }
    
    public int getPlayerId() {
        return this.playerId;
    }
    
    public int getPlayerSide() {
        return this.playerSide;
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
    
    public boolean setSide(int side) {
        HashMap<String,String> data = new HashMap<String,String>();
        data.put("side", ""+side);
        JSONObject response = this.postJSON("/players/" + this.playerId, data);
        try {
            this.playerSide = response.getInt("side");
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
