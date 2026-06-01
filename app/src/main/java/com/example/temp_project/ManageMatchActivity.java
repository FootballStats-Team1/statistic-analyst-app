package com.example.temp_project;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ManageMatchActivity extends AppCompatActivity {

    Spinner matchSpinner, spinnerPlayerOut, spinnerPlayerIn;
    EditText homeScoreInput, awayScoreInput;
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

        // 2. Enabling Statistics Counters (Buttons)
        // Home Team
        setupCounter(R.id.btnHomeGoal);
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
        setupCounter(R.id.btnAwayGoal);
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

    // Takes a button, initializes it to "0", and tells it to increment by 1 with every click
    private void setupCounter(int buttonId) {
        Button btn = findViewById(buttonId);
        btn.setText("0"); // Αρχική τιμή

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // It reads the current number, adds 1 to it, and writes it back
                int currentValue = Integer.parseInt(btn.getText().toString());
                btn.setText(String.valueOf(currentValue + 1));
            }
        });
    }
}