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


        // Populating the Match Menu
        setupMatchSpinner();


        // 2. Enabling Statistics Counters (Buttons)
        // Home Team
        setupGoalCounter(R.id.btnHomeGoal, homeScoreInput);
        setupCounter(R.id.btnHomeShotOn);
        setupCounter(R.id.btnHomeShotOff);
        setupCounter(R.id.btnHomePassSucc);
        setupCounter(R.id.btnHomePassFail);
        setupCounter(R.id.btnHomeTackleSucc);
        setupCounter(R.id.btnHomeCross);
        setupCounter(R.id.btnHomeAssist);
        setupCounter(R.id.btnHomeFoul);
        setupCounter(R.id.btnHomeCorner);
        setupCounter(R.id.btnHomeYellow);
        setupCounter(R.id.btnHomeRed);

        // Away
        setupGoalCounter(R.id.btnAwayGoal, awayScoreInput);
        setupCounter(R.id.btnAwayShotOn);
        setupCounter(R.id.btnAwayShotOff);
        setupCounter(R.id.btnAwayPassSucc);
        setupCounter(R.id.btnAwayPassFail);
        setupCounter(R.id.btnAwayTackleSucc);
        setupCounter(R.id.btnAwayCross);
        setupCounter(R.id.btnAwayAssist);
        setupCounter(R.id.btnAwayFoul);
        setupCounter(R.id.btnAwayCorner);
        setupCounter(R.id.btnAwayYellow);
        setupCounter(R.id.btnAwayRed);

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
                Toast.makeText(ManageMatchActivity.this, "Καταγραφή Αλλαγής...", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // +1 with a click, -1 with a long click
    private void setupCounter(int buttonId) {
        Button btn = findViewById(buttonId);
        btn.setText("0");

        // Single tap -> Add
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentValue + 1));
            }
        });

        // Long press -> Subtraction
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentValue = Integer.parseInt(btn.getText().toString());
                if (currentValue > 0) { // To prevent negative numbers (-1)
                    btn.setText(String.valueOf(currentValue - 1));
                }
                return true; // Indicates that the action was completed
            }
        });
    }

    // +1/-1 and in the main score
    private void setupGoalCounter(int buttonId, TextView scoreInput) {
        Button btn = findViewById(buttonId);
        btn.setText("0");

        // Single tap -> Add Goal and Score
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentStat = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentStat + 1));

                int currentScore = Integer.parseInt(scoreInput.getText().toString());
                scoreInput.setText(String.valueOf(currentScore + 1));
            }
        });

        // Long press -> Subtract Goals and Score
        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int currentStat = Integer.parseInt(btn.getText().toString());
                int currentScore = Integer.parseInt(scoreInput.getText().toString());

                if (currentStat > 0 && currentScore > 0) {
                    btn.setText(String.valueOf(currentStat - 1));
                    scoreInput.setText(String.valueOf(currentScore - 1));
                }
                return true;
            }
        });
    }

    // --- METHOD FOR THE MATCH SPINNER ---
    private void setupMatchSpinner() {
        // List of mock matches (Mock Data) until the API is ready
        String[] dummyMatches = {
                "--- Select Match ---",
                "121: Olympiacos - PAOK (PENDING)",
                "122: AEK - Panathinaikos (LIVE)",
                "123: Aris - OFI (LIVE)"
        };

        // The Adapter is responsible for populating the Spinner with the Strings
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                dummyMatches
        );
        matchSpinner.setAdapter(adapter);

        // What happens when the user selects a match?
        matchSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                // If they didn't select "--- Select Match ---" (which is at position 0)
                if (position > 0) {
                    Toast.makeText(ManageMatchActivity.this, "Loading: " + dummyMatches[position], Toast.LENGTH_SHORT).show();
                    // TODO: In the future, we will reset the statistics here or load the existing ones from the database.
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    // SENDING DATA TO THE BACKEND (API CALL)
    private void sendEventToServer(String eventType, String teamType, String value) {
        // Get the selected match (for now, we're using a dummy ID, e.g., 121)
        String matchId = "121";

        // The PHP URL that the team will create
        String url = Config.BASE_URL + "insertMatchEvent.php";

        // We build the "package" with the data we will send (POST request)
        okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
                .add("match_id", matchId)
                .add("team_type", teamType) // "home" or "away"
                .add("event_type", eventType) // e.g., "shot_on", "yellow_card"
                .add("value", value) // "+1" or "-1" (if a mistake was made)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url (url)
                .post(formBody)
                .build();

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        // Execute the call in the background so the app doesn't freeze
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    // Successful write to the backend!
                    System.out.println("Success: The event was sent!");
                }
            }
        });
    }
}