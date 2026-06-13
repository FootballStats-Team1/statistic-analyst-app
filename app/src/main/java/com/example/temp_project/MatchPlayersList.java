package com.example.temp_project;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MatchPlayersList {

    private final ArrayList<LineupPlayer> homePlayers = new ArrayList<>();
    private final ArrayList<LineupPlayer> awayPlayers = new ArrayList<>();

    private String homeTeamName = "";
    private String awayTeamName = "";
    private String homeLogo = "";
    private String awayLogo = "";

    public MatchPlayersList(int matchId) {
        final String url = Config.BASE_URL + "getMatchPlayers.php?matchId=" + matchId;

        Thread t = new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                if (response.body() != null) {
                    parse(response.body().string());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void parse(String json) {
        try {
            JSONObject root = new JSONObject(json);

            homeTeamName = root.optString("home_team", "");
            awayTeamName = root.optString("away_team", "");
            homeLogo = root.optString("home_logo", "");
            awayLogo = root.optString("away_logo", "");

            JSONArray homeArr = root.getJSONArray("home_players");
            for (int i = 0; i < homeArr.length(); i++) {
                homePlayers.add(toPlayer(homeArr.getJSONObject(i)));
            }

            JSONArray awayArr = root.getJSONArray("away_players");
            for (int i = 0; i < awayArr.length(); i++) {
                awayPlayers.add(toPlayer(awayArr.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LineupPlayer toPlayer(JSONObject o) throws Exception {
        return new LineupPlayer(
                o.getInt("player_id"),
                o.getString("name"),
                o.getString("position"),
                o.getString("photo")
        );
    }

    public ArrayList<LineupPlayer> getHomePlayers() { return homePlayers; }
    public ArrayList<LineupPlayer> getAwayPlayers() { return awayPlayers; }
    public String getHomeTeamName() { return homeTeamName; }
    public String getAwayTeamName() { return awayTeamName; }
    public String getHomeLogo() { return homeLogo; }
    public String getAwayLogo() { return awayLogo; }
}