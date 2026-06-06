package com.example.temp_project;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ManageMatchActivity extends AppCompatActivity {

    Spinner matchSpinner, spinnerPlayerOut, spinnerPlayerIn;
    TextView homeScoreInput, awayScoreInput;
    Button btnUpdateScore, btnFinishMatch, btnSubstitute;

    private okhttp3.OkHttpClient client;
    private String selectedMatchId = "";

    private java.util.ArrayList<String> matchList = new java.util.ArrayList<>();
    private java.util.ArrayList<String> playerList = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<String> matchAdapter;
    private android.widget.ArrayAdapter<String> playerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_match);
        setTitle("Διαχείριση Αγώνα (Analyst)");

        // 1. Connecting the basic UI elements
        matchSpinner = findViewById(R.id.matchSpinner);
        homeScoreInput = findViewById(R.id.homeScoreInput);
        awayScoreInput = findViewById(R.id.awayScoreInput);
        btnUpdateScore = findViewById(R.id.btnUpdateScore);
        btnFinishMatch = findViewById(R.id.btnFinishMatch);

        spinnerPlayerOut = findViewById(R.id.spinnerPlayerOut);
        spinnerPlayerIn = findViewById(R.id.spinnerPlayerIn);
        btnSubstitute = findViewById(R.id.btnSubstitute);


        // Network initialization & adapters
        client = new okhttp3.OkHttpClient();

        matchAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, matchList);
        matchSpinner.setAdapter(matchAdapter);

        playerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, playerList);
        spinnerPlayerOut.setAdapter(playerAdapter);
        spinnerPlayerIn.setAdapter(playerAdapter);

        // Retrieve matches from PHP
        fetchMatches();

        // What happens when a match is selected
        matchSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selection = matchList.get(position);
                    selectedMatchId = selection.split(":")[0].trim();
                    fetchPlayers(selectedMatchId); // Retrieves the players for this match
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });


        // 2. Enabling Statistics Counters (Buttons)
        // Home Team
        setupGoalCounter(R.id.btnHomeGoal, homeScoreInput, "home", "goals");
        setupCounter(R.id.btnHomeShotOn, "home", "shots_on_target");
        setupCounter(R.id.btnHomeShotOff, "home", "shots_off_target");
        setupCounter(R.id.btnHomePassSucc, "home", "passes_succ");
        setupCounter(R.id.btnHomePassFail, "home", "passes_fail");
        setupCounter(R.id.btnHomeTackleSucc, "home", "tackles_succ");
        setupCounter(R.id.btnHomeCross, "home", "crosses_succ");
        setupCounter(R.id.btnHomeAssist, "home", "assists");
        setupCounter(R.id.btnHomeFoul, "home", "fouls_committed");
        setupCounter(R.id.btnHomeCorner, "home", "corners_won");
        setupCounter(R.id.btnHomeYellow, "home", "yellow_cards");
        setupCounter(R.id.btnHomeRed, "home", "red_cards");

        // Away
        setupGoalCounter(R.id.btnAwayGoal, awayScoreInput, "away", "goals");
        setupCounter(R.id.btnAwayShotOn, "away", "shots_on_target");
        setupCounter(R.id.btnAwayShotOff, "away", "shots_off_target");
        setupCounter(R.id.btnAwayPassSucc, "away", "passes_succ");
        setupCounter(R.id.btnAwayPassFail, "away", "passes_fail");
        setupCounter(R.id.btnAwayTackleSucc, "away", "tackles_succ");
        setupCounter(R.id.btnAwayCross, "away", "crosses_succ");
        setupCounter(R.id.btnAwayAssist, "away", "assists");
        setupCounter(R.id.btnAwayFoul, "away", "fouls_committed");
        setupCounter(R.id.btnAwayCorner, "away", "corners_won");
        setupCounter(R.id.btnAwayYellow, "away", "yellow_cards");
        setupCounter(R.id.btnAwayRed, "away", "red_cards");

        // 3. Function of the Score and Change Buttons
        btnUpdateScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String homeScore = homeScoreInput.getText().toString();
                String awayScore = awayScoreInput.getText().toString();
                Toast.makeText(ManageMatchActivity.this, "Σκορ: " + homeScore + "-" + awayScore, Toast.LENGTH_SHORT).show();
            }
        });

        btnFinishMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ManageMatchActivity.this, "Ο αγώνας έληξε!", Toast.LENGTH_LONG).show();
            }
        });

        btnSubstitute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedMatchId.isEmpty()) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε αγώνα!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(spinnerPlayerOut.getSelectedItem() == null || spinnerPlayerIn.getSelectedItem() == null) {
                    Toast.makeText(ManageMatchActivity.this, "Δεν υπάρχουν παίκτες!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String playerOut = spinnerPlayerOut.getSelectedItem().toString();
                String playerIn = spinnerPlayerIn.getSelectedItem().toString();
                sendSubstitutionToServer(playerOut, playerIn);
            }
        });
    }


    // +1 with a click, -1 with a long click
    private void setupCounter(int buttonId, String teamType, String eventType) {
        Button btn = findViewById(buttonId);
        btn.setText("0");

        // Single click -> Add (+1)
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentValue + 1));

                // SEND THE DATA TO PHP
                sendEventToServer(eventType, teamType, "+1");
            }
        });

        // Long press -> Subtract (-1)
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentValue = Integer.parseInt(btn.getText().toString());
                if (currentValue > 0) {
                    btn.setText(String.valueOf(currentValue - 1));

                    // SEND THE SUBTRACTION TO PHP
                    sendEventToServer(eventType, teamType, "-1");
                }
                return true;
            }
        });
    }

    // +1/-1 and in the main score
    private void setupGoalCounter(int buttonId, TextView scoreInput, String teamType, String eventType) {
        Button btn = findViewById(buttonId);
        btn.setText("0");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentStat = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentStat + 1));

                int currentScore = Integer.parseInt(scoreInput.getText().toString());
                scoreInput.setText(String.valueOf(currentScore + 1));

                // SEND GOAL TO BACKEND
                sendEventToServer(eventType, teamType, "+1");
            }
        });

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentStat = Integer.parseInt(btn.getText().toString());
                int currentScore = Integer.parseInt(scoreInput.getText().toString());

                if (currentStat > 0 && currentScore > 0) {
                    btn.setText(String.valueOf(currentStat - 1));
                    scoreInput.setText(String.valueOf(currentScore - 1));

                    // SUBTRACT GOAL FROM THE BACKEND
                    sendEventToServer(eventType, teamType, "-1");
                }
                return true;
            }
        });
    }


    // SENDING DATA TO THE BACKEND (API CALL)
    private void sendEventToServer(String eventType, String teamType, String value) {
        if (selectedMatchId.isEmpty()) return; // If no match has been selected, do nothing

        String url = Config.BASE_URL + "insertMatchEvent.php";

        okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
                .add("match_id", selectedMatchId)
                .add("team_type", teamType)
                .add("event_type", eventType)
                .add("value", value)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(ManageMatchActivity.this, "Network Error", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                // Success!
            }
        });
    }

    private void fetchMatches() {
        String url = Config.BASE_URL + "getMatches.php";
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(responseData);
                        matchList.clear();
                        matchList.add("--- Επιλέξτε Αγώνα ---");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);
                            matchList.add(obj.getString("match_id") + ": " + obj.getString("home_team") + " vs " + obj.getString("away_team"));
                        }
                        runOnUiThread(() -> matchAdapter.notifyDataSetChanged());
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void fetchPlayers(String matchId) {
        String url = Config.BASE_URL + "getMatchLineups.php?matchId=" + matchId;
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        // Reading the JSON object as returned by getMatchLineups.php
                        org.json.JSONObject obj = new org.json.JSONObject(responseData);
                        org.json.JSONArray hs = obj.getJSONArray("home_starters");
                        org.json.JSONArray as = obj.getJSONArray("away_starters");
                        org.json.JSONArray hsub = obj.getJSONArray("home_subs");
                        org.json.JSONArray asub = obj.getJSONArray("away_subs");

                        playerList.clear();
                        playerList.add("--- Επιλέξτε Παίκτη ---");

                        // Add all the players to the Spinner
                        for (int i=0; i<hs.length(); i++) playerList.add(hs.getString(i));
                        for (int i=0; i<as.length(); i++) playerList.add(as.getString(i));
                        for (int i=0; i<hsub.length(); i++) playerList.add(hsub.getString(i));
                        for (int i=0; i<asub.length(); i++) playerList.add(asub.getString(i));

                        runOnUiThread(() -> playerAdapter.notifyDataSetChanged());
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void sendSubstitutionToServer(String playerOutStr, String playerInStr) {
        String playerOutId = playerOutStr.split(":")[0].trim();
        String playerInId = playerInStr.split(":")[0].trim();
        String url = Config.BASE_URL + "insertSubstitution.php";

        okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
                .add("match_id", selectedMatchId)
                .add("player_out_id", playerOutId)
                .add("player_in_id", playerInId)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if(response.isSuccessful()){
                    runOnUiThread(() -> Toast.makeText(ManageMatchActivity.this, "Η αλλαγή καταγράφηκε!", Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}