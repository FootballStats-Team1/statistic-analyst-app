package com.example.temp_project;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ManageMatchActivity extends AppCompatActivity {

    Spinner spinnerPlayerOut, spinnerPlayerIn, spinnerHomePlayer, spinnerAwayPlayer;
    TextView homeScoreInput, awayScoreInput, homeTeamTxt, awayTeamTxt;
    ImageView imgHomeLogo, imgAwayLogo;
    Button btnUpdateScore, btnFinishMatch, btnSubstitute;

    private okhttp3.OkHttpClient client;

    private int matchId = -1;
    private String selectedMatchId = "";

    private java.util.ArrayList<String> playerList = new java.util.ArrayList<>();
    private java.util.ArrayList<String> playerIds = new java.util.ArrayList<>();
    private java.util.ArrayList<String> playerTeams = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<String> playerAdapter;

    private java.util.ArrayList<String> subInList = new java.util.ArrayList<>();
    private java.util.ArrayList<String> subInIds = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<String> subInAdapter;

    private java.util.ArrayList<String> homePlayerList = new java.util.ArrayList<>();
    private java.util.ArrayList<String> awayPlayerList = new java.util.ArrayList<>();
    private java.util.ArrayList<String> homePlayerIds = new java.util.ArrayList<>();
    private java.util.ArrayList<String> awayPlayerIds = new java.util.ArrayList<>();
    private android.widget.ArrayAdapter<String> homePlayerAdapter;
    private android.widget.ArrayAdapter<String> awayPlayerAdapter;

    private java.util.HashSet<String> sentOff = new java.util.HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_match);
        setTitle("Διαχείριση Αγώνα (Analyst)");

        matchId = getIntent().getIntExtra("MATCH_ID", -1);
        selectedMatchId = (matchId > 0) ? String.valueOf(matchId) : "";

        imgHomeLogo = findViewById(R.id.imgHomeLogo);
        imgAwayLogo = findViewById(R.id.imgAwayLogo);
        homeTeamTxt = findViewById(R.id.homeTeamTxt);
        awayTeamTxt = findViewById(R.id.awayTeamTxt);
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

        client = new okhttp3.OkHttpClient();

        playerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, playerList);
        spinnerPlayerOut.setAdapter(playerAdapter);

        subInList.add("--- Επιλέξτε Παίκτη ---");
        subInIds.add("");
        subInAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subInList);
        spinnerPlayerIn.setAdapter(subInAdapter);

        spinnerPlayerOut.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rebuildSubInList(position);
                updateSubPreview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerPlayerIn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSubPreview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fetchMatchHeader();
        if (!selectedMatchId.isEmpty()) {
            fetchPlayers(selectedMatchId);
        }

        setupGoalCounter(R.id.btnHomeGoal, homeScoreInput, "home", "goals");
        setupCounter(R.id.btnHomeShotOn, "home", "shots_on_target");
        setupCounter(R.id.btnHomeShotOff, "home", "shots_off_target");
        setupCounter(R.id.btnHomePassSucc, "home", "passes_succ");
        setupCounter(R.id.btnHomePassFail, "home", "passes_fail");
        setupCounter(R.id.btnHomeTackleSucc, "home", "tackles_succ");
        setupCounter(R.id.btnHomeCross, "home", "crosses_succ");
        setupCounter(R.id.btnHomeFoul, "home", "fouls_committed");
        setupCounter(R.id.btnHomeCorner, "home", "corners_won");
        setupCounter(R.id.btnHomeYellow, "home", "yellow_cards");
        setupRedCardCounter(R.id.btnHomeRed, "home");
        setupCounter(R.id.btnHomeTackleFail, "home", "tackles_fail");
        setupCounter(R.id.btnHomeCrossFail, "home", "crosses_fail");
        setupCounter(R.id.btnHomeError, "home", "errors");
        setupCounter(R.id.btnHomeFoulWon, "home", "fouls_won");

        setupGoalCounter(R.id.btnAwayGoal, awayScoreInput, "away", "goals");
        setupCounter(R.id.btnAwayShotOn, "away", "shots_on_target");
        setupCounter(R.id.btnAwayShotOff, "away", "shots_off_target");
        setupCounter(R.id.btnAwayPassSucc, "away", "passes_succ");
        setupCounter(R.id.btnAwayPassFail, "away", "passes_fail");
        setupCounter(R.id.btnAwayTackleSucc, "away", "tackles_succ");
        setupCounter(R.id.btnAwayCross, "away", "crosses_succ");
        setupCounter(R.id.btnAwayFoul, "away", "fouls_committed");
        setupCounter(R.id.btnAwayCorner, "away", "corners_won");
        setupCounter(R.id.btnAwayYellow, "away", "yellow_cards");
        setupRedCardCounter(R.id.btnAwayRed, "away");
        setupCounter(R.id.btnAwayTackleFail, "away", "tackles_fail");
        setupCounter(R.id.btnAwayCrossFail, "away", "crosses_fail");
        setupCounter(R.id.btnAwayError, "away", "errors");
        setupCounter(R.id.btnAwayFoulWon, "away", "fouls_won");

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
                if (selectedMatchId.isEmpty()) {
                    Toast.makeText(ManageMatchActivity.this, "Δεν υπάρχει αγώνας!", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendStatusToServer("finished");
            }
        });

        btnSubstitute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMatchId.isEmpty()) {
                    Toast.makeText(ManageMatchActivity.this, "Δεν υπάρχει αγώνας!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int outPos = spinnerPlayerOut.getSelectedItemPosition();
                int inPos = spinnerPlayerIn.getSelectedItemPosition();

                if (outPos <= 0 || outPos >= playerIds.size()) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε παίκτη που βγαίνει!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (inPos <= 0 || inPos >= subInIds.size()) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε παίκτη που μπαίνει!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String outId = playerIds.get(outPos);
                String inId = subInIds.get(inPos);

                if (outId.isEmpty() || inId.isEmpty()) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε παίκτες!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (outId.equals(inId)) {
                    Toast.makeText(ManageMatchActivity.this, "Επιλέξτε διαφορετικούς παίκτες!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (sentOff.contains(outId) || sentOff.contains(inId)) {
                    Toast.makeText(ManageMatchActivity.this, "Αποβληθείς παίκτης δεν μπορεί να μπει σε αλλαγή!", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendSubstitutionToServer(outId, inId);
            }
        });
    }

    private void rebuildSubInList(int outPos) {
        subInList.clear();
        subInIds.clear();
        subInList.add("--- Επιλέξτε Παίκτη ---");
        subInIds.add("");

        if (outPos > 0 && outPos < playerTeams.size()) {
            String team = playerTeams.get(outPos);
            String outId = playerIds.get(outPos);

            java.util.ArrayList<String> names = team.equals("home") ? homePlayerList : awayPlayerList;
            java.util.ArrayList<String> ids = team.equals("home") ? homePlayerIds : awayPlayerIds;

            for (int i = 1; i < ids.size(); i++) {
                String pid = ids.get(i);
                if (pid.equals(outId)) continue;
                if (sentOff.contains(pid)) continue;
                subInList.add(names.get(i));
                subInIds.add(pid);
            }
        }

        subInAdapter.notifyDataSetChanged();
    }

    private void updateSubPreview() {
        int outPos = spinnerPlayerOut.getSelectedItemPosition();
        int inPos = spinnerPlayerIn.getSelectedItemPosition();

        if (outPos > 0 && outPos < playerList.size() && inPos > 0 && inPos < subInList.size()) {
            btnSubstitute.setText("ΕΞΩ: " + playerList.get(outPos) + "   ➜   ΜΕΣΑ: " + subInList.get(inPos));
        } else {
            btnSubstitute.setText("Καταχώρηση Αλλαγής");
        }
    }

    private void fetchMatchHeader() {
        if (selectedMatchId.isEmpty()) return;

        String url = Config.BASE_URL + "getMatchDetails.php?matchId=" + selectedMatchId;
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(data);

                        final String homeTeam = obj.getString("home_team");
                        final String awayTeam = obj.getString("away_team");
                        final String homeLogo = obj.getString("home_logo").trim();
                        final String awayLogo = obj.getString("away_logo").trim();

                        int hs = obj.getInt("home_score");
                        int as = obj.getInt("away_score");
                        final int homeScore = (hs < 0) ? 0 : hs;
                        final int awayScore = (as < 0) ? 0 : as;

                        runOnUiThread(() -> {
                            homeTeamTxt.setText(homeTeam);
                            awayTeamTxt.setText(awayTeam);
                            homeScoreInput.setText(String.valueOf(homeScore));
                            awayScoreInput.setText(String.valueOf(awayScore));

                            Picasso.with(getApplicationContext())
                                    .load(homeLogo)
                                    .error(R.mipmap.ic_launcher_round)
                                    .into(imgHomeLogo);
                            Picasso.with(getApplicationContext())
                                    .load(awayLogo)
                                    .error(R.mipmap.ic_launcher_round)
                                    .into(imgAwayLogo);
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

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
                if (sentOff.contains(playerId)) {
                    Toast.makeText(ManageMatchActivity.this, "Ο παίκτης έχει αποβληθεί!", Toast.LENGTH_SHORT).show();
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
                if (sentOff.contains(playerId)) {
                    Toast.makeText(ManageMatchActivity.this, "Ο παίκτης έχει αποβληθεί!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentStat = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentStat + 1));
                int currentScore = Integer.parseInt(scoreInput.getText().toString());
                scoreInput.setText(String.valueOf(currentScore + 1));

                sendEventToServer(eventType, teamType, "+1", playerId);
                askForAssist(teamType, playerId);
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

    private void setupRedCardCounter(int buttonId, String teamType) {
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
                if (sentOff.contains(playerId)) {
                    Toast.makeText(ManageMatchActivity.this, "Ο παίκτης έχει ήδη αποβληθεί!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int currentValue = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentValue + 1));
                sendEventToServer("red_cards", teamType, "+1", playerId);
                sentOff.add(playerId);
                Toast.makeText(ManageMatchActivity.this, "Αποβολή! Ο παίκτης δεν μπορεί να συνεχίσει.", Toast.LENGTH_SHORT).show();
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
                if (!sentOff.contains(playerId)) {
                    Toast.makeText(ManageMatchActivity.this, "Ο παίκτης δεν έχει κόκκινη.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                int currentValue = Integer.parseInt(btn.getText().toString());
                if (currentValue > 0) {
                    btn.setText(String.valueOf(currentValue - 1));
                    sendEventToServer("red_cards", teamType, "-1", playerId);
                    sentOff.remove(playerId);
                    Toast.makeText(ManageMatchActivity.this, "Η αποβολή αναιρέθηκε.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    private void askForAssist(String teamType, String scorerId) {
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        final java.util.ArrayList<String> ids = new java.util.ArrayList<>();

        java.util.ArrayList<String> srcNames = teamType.equals("home") ? homePlayerList : awayPlayerList;
        java.util.ArrayList<String> srcIds = teamType.equals("home") ? homePlayerIds : awayPlayerIds;

        for (int i = 1; i < srcIds.size(); i++) {
            String id = srcIds.get(i);
            if (id.equals(scorerId)) continue;
            if (sentOff.contains(id)) continue;
            ids.add(id);
            names.add(srcNames.get(i));
        }

        if (ids.isEmpty()) return;

        final String[] nameArr = names.toArray(new String[0]);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ασίστ;")
                .setMessage("Είχε ασίστ το γκολ;")
                .setPositiveButton("Ναι", (dialog, which) ->
                        new androidx.appcompat.app.AlertDialog.Builder(ManageMatchActivity.this)
                                .setTitle("Ποιος έδωσε την ασίστ;")
                                .setItems(nameArr, (d2, pos) -> {
                                    String assistId = ids.get(pos);
                                    sendEventToServer("assists", teamType, "+1", assistId);
                                    Toast.makeText(ManageMatchActivity.this, "Ασίστ: " + nameArr[pos], Toast.LENGTH_SHORT).show();
                                })
                                .show())
                .setNegativeButton("Όχι", null)
                .show();
    }

    private void sendEventToServer(String eventType, String teamType, String value, String playerId) {
        if (selectedMatchId.isEmpty()) return;

        String url = Config.BASE_URL + "insertMatchEvent.php";
        okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
                .add("match_id", selectedMatchId)
                .add("player_id", playerId)
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
            }
        });
    }

    private void sendStatusToServer(String status) {
        if (selectedMatchId.isEmpty()) return;

        String url = Config.BASE_URL + "updateMatchStatus.php";
        okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
                .add("match_id", selectedMatchId)
                .add("status", status)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(ManageMatchActivity.this, "Network Error", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ManageMatchActivity.this, "Ο αγώνας έληξε!", Toast.LENGTH_LONG).show());
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

                        org.json.JSONArray hp = obj.getJSONArray("home_players");
                        org.json.JSONArray ap = obj.getJSONArray("away_players");

                        homePlayerList.clear();
                        homePlayerIds.clear();
                        homePlayerList.add("--- Επιλέξτε Παίκτη ---");
                        homePlayerIds.add("");
                        for (int i = 0; i < hp.length(); i++) {
                            String entry = hp.getString(i);
                            int sep = entry.indexOf(":");
                            String id = entry.substring(0, sep).trim();
                            String name = entry.substring(sep + 1).trim();
                            homePlayerList.add(name);
                            homePlayerIds.add(id);
                        }

                        awayPlayerList.clear();
                        awayPlayerIds.clear();
                        awayPlayerList.add("--- Επιλέξτε Παίκτη ---");
                        awayPlayerIds.add("");
                        for (int i = 0; i < ap.length(); i++) {
                            String entry = ap.getString(i);
                            int sep = entry.indexOf(":");
                            String id = entry.substring(0, sep).trim();
                            String name = entry.substring(sep + 1).trim();
                            awayPlayerList.add(name);
                            awayPlayerIds.add(id);
                        }

                        playerList.clear();
                        playerIds.clear();
                        playerTeams.clear();
                        playerList.add("--- Επιλέξτε Παίκτη ---");
                        playerIds.add("");
                        playerTeams.add("");
                        for (int i = 0; i < hp.length(); i++) {
                            String entry = hp.getString(i);
                            int sep = entry.indexOf(":");
                            playerList.add(entry.substring(sep + 1).trim());
                            playerIds.add(entry.substring(0, sep).trim());
                            playerTeams.add("home");
                        }
                        for (int i = 0; i < ap.length(); i++) {
                            String entry = ap.getString(i);
                            int sep = entry.indexOf(":");
                            playerList.add(entry.substring(sep + 1).trim());
                            playerIds.add(entry.substring(0, sep).trim());
                            playerTeams.add("away");
                        }

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
        java.util.ArrayList<String> ids = teamType.equals("home") ? homePlayerIds : awayPlayerIds;

        int pos = targetSpinner.getSelectedItemPosition();
        if (pos <= 0 || pos >= ids.size()) return null;

        String id = ids.get(pos);
        return (id == null || id.isEmpty()) ? null : id;
    }

    private void sendSubstitutionToServer(String playerOutId, String playerInId) {
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
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ManageMatchActivity.this, "Η αλλαγή καταγράφηκε!", Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}