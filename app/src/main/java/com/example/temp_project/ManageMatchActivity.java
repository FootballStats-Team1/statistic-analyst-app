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

    // Screen information
    Spinner matchSpinner;
    TextView homeTeamTxt, awayTeamTxt;
    EditText homeScoreInput, awayScoreInput;
    Button btnUpdateScore, btnFinishMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_match);

        setTitle("Διαχείριση Αγώνα (Analyst)");

        // 1. Linking Java variables to XML IDs
        matchSpinner = findViewById(R.id.matchSpinner);
        homeTeamTxt = findViewById(R.id.homeTeamTxt);
        awayTeamTxt = findViewById(R.id.awayTeamTxt);
        homeScoreInput = findViewById(R.id.homeScoreInput);
        awayScoreInput = findViewById(R.id.awayScoreInput);
        btnUpdateScore = findViewById(R.id.btnUpdateScore);
        btnFinishMatch = findViewById(R.id.btnFinishMatch);

        // 2. What happens when we click the "Update Score" button?
        btnUpdateScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Let's see what numbers the Analyst reported
                String homeScore = homeScoreInput.getText().toString();
                String awayScore = awayScoreInput.getText().toString();

                // For now, we're displaying a message on the screen
                Toast.makeText(ManageMatchActivity.this, "Ενημέρωση: " + homeScore + "-" + awayScore + " (LIVE)", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. What happens when we press the "End Match" button?
        btnFinishMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String homeScore = homeScoreInput.getText().toString();
                String awayScore = awayScoreInput.getText().toString();

                Toast.makeText(ManageMatchActivity.this, "ΛΗΞΗ ΑΓΩΝΑ! Τελικό: " + homeScore + "-" + awayScore, Toast.LENGTH_LONG).show();
            }
        });
    }
}