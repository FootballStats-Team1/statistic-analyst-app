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

    Spinner matchSpinner, spinnerPlayerOut, spinnerPlayerIn, spinnerHomePlayer, spinnerAwayPlayer;
    TextView homeScoreInput, awayScoreInput;
    Button btnUpdateScore, btnFinishMatch, btnSubstitute;

    private okhttp3.OkHttpClient client;
    private String selectedMatchId = "";

    private java.util.ArrayList<String> matchList = new java.util.ArrayList<>();
    private java.util.ArrayList<String> playerList = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<String> matchAdapter;
    private android.widget.ArrayAdapter<String> playerAdapter;

    private java.util.ArrayList<String> homePlayerList = new java.util.ArrayList<>();
    private java.util.ArrayList<String> awayPlayerList = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<String> homePlayerAdapter;
    private android.widget.ArrayAdapter<String> awayPlayerAdapter;

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

        spinnerHomePlayer = findViewById(R.id.spinnerHomePlayer);
        spinnerAwayPlayer = findViewById(R.id.spinnerAwayPlayer);

        homePlayerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, homePlayerList);
        spinnerHomePlayer.setAdapter(homePlayerAdapter);

        awayPlayerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, awayPlayerList);
        spinnerAwayPlayer.setAdapter(awayPlayerAdapter);

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
        setupCounter(R.id.btnHomeTackleFail, "home", "tackles_fail");
        setupCounter(R.id.btnHomeCrossFail, "home", "crosses_fail");
        setupCounter(R.id.btnHomeError, "home", "errors");
        setupCounter(R.id.btnHomeFoulWon, "home", "fouls_won");


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
        setupCounter(R.id.btnAwayTackleFail, "away", "tackles_fail");
        setupCounter(R.id.btnAwayCrossFail, "away", "crosses_fail");
        setupCounter(R.id.btnAwayError, "away", "errors");
        setupCounter(R.id.btnAwayFoulWon, "away", "fouls_won");


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

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerId = getSelectedPlayerId(teamType);
                if (playerId == null) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε παίκτη πρώτα!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentValue = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentValue + 1));
                sendEventToServer(eventType, teamType, "+1", playerId);
            }
        });

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String playerId = getSelectedPlayerId(teamType);
                if (playerId == null) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε παίκτη πρώτα!", Toast.LENGTH_SHORT).show();
                    return true;
                }

                int currentValue = Integer.parseInt(btn.getText().toString());
                if (currentValue > 0) {
                    btn.setText(String.valueOf(currentValue - 1));
                    sendEventToServer(eventType, teamType, "-1", playerId);
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
                String playerId = getSelectedPlayerId(teamType);
                if (playerId == null) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε παίκτη πρώτα!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentStat = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentStat + 1));
                int currentScore = Integer.parseInt(scoreInput.getText().toString());
                scoreInput.setText(String.valueOf(currentScore + 1));

                sendEventToServer(eventType, teamType, "+1", playerId);
            }
        });

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String playerId = getSelectedPlayerId(teamType);
                if (playerId == null) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε παίκτη πρώτα!", Toast.LENGTH_SHORT).show();
                    return true;
                }

                int currentStat = Integer.parseInt(btn.getText().toString());
                int currentScore = Integer.parseInt(scoreInput.getText().toString());
                if (currentStat > 0 && currentScore > 0) {
                    btn.setText(String.valueOf(currentStat - 1));
                    scoreInput.setText(String.valueOf(currentScore - 1));
                    sendEventToServer(eventType, teamType, "-1", playerId);
                }
                return true;
            }
        });
    }


    // SENDING DATA TO THE BACKEND (API CALL)
    private void sendEventToServer(String eventType, String teamType, String value, String playerId) {
        if (selectedMatchId.isEmpty()) return;

        String url = Config.BASE_URL + "insertMatchEvent.php";
        okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
                .add("match_id", selectedMatchId)
                .add("player_id", playerId)  // <--- ΝΕΟ ΠΕΔΙΟ ΓΙΑ ΤΗ ΒΑΣΗ ΔΕΔΟΜΕΝΩΝ!
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
                // Επιτυχής καταγραφή!
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
        String url = Config.BASE_URL + "getPlayersForAnalyst.php?matchId=" + matchId;
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(responseData);

                        // ΣΩΣΤΗ ΑΝΑΓΝΩΣΗ: Διαβάζουμε τα JSON Arrays όπως τα στέλνει το νέο PHP
                        org.json.JSONArray hp = obj.getJSONArray("home_players");
                        org.json.JSONArray ap = obj.getJSONArray("away_players");

                        // 1. Γεμίζουμε το Spinner των Γηπεδούχων
                        homePlayerList.clear();
                        homePlayerList.add("--- Επιλέξτε Παίκτη ---");
                        for (int i=0; i<hp.length(); i++) homePlayerList.add(hp.getString(i));

                        // 2. Γεμίζουμε το Spinner των Φιλοξενούμενων
                        awayPlayerList.clear();
                        awayPlayerList.add("--- Επιλέξτε Παίκτη ---");
                        for (int i=0; i<ap.length(); i++) awayPlayerList.add(ap.getString(i));

                        // 3. Γεμίζουμε και την παλιά γενική λίστα (για να δουλεύουν σωστά οι Αλλαγές!)
                        playerList.clear();
                        for (int i=0; i<hp.length(); i++) playerList.add(hp.getString(i));
                        for (int i=0; i<ap.length(); i++) playerList.add(ap.getString(i));

                        runOnUiThread(() -> {
                            homePlayerAdapter.notifyDataSetChanged();
                            awayPlayerAdapter.notifyDataSetChanged();
                            playerAdapter.notifyDataSetChanged();
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private String getSelectedPlayerId(String teamType) {
        Spinner targetSpinner = teamType.equals("home") ? spinnerHomePlayer : spinnerAwayPlayer;
        if (targetSpinner.getSelectedItem() != null) {
            String selection = targetSpinner.getSelectedItem().toString();
            if (!selection.startsWith("---")) {
                return selection.split(":")[0].trim();
            }
        }
        return null;
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