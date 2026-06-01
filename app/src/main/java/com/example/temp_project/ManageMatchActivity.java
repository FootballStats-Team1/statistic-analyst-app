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

    // Δήλωση των στοιχείων της οθόνης
    Spinner matchSpinner;
    TextView homeTeamTxt, awayTeamTxt;
    EditText homeScoreInput, awayScoreInput;
    Button btnUpdateScore, btnFinishMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ΑΥΤΗ Η ΓΡΑΜΜΗ ΣΥΝΔΕΕΙ ΤΗ JAVA ΜΕ ΤΟ XML ΠΟΥ ΕΦΤΙΑΞΕΣ ΣΤΟ ΒΗΜΑ 1!
        setContentView(R.layout.activity_manage_match);

        setTitle("Διαχείριση Αγώνα (Analyst)");

        // 1. Σύνδεση των μεταβλητών Java με τα ID του XML
        matchSpinner = findViewById(R.id.matchSpinner);
        homeTeamTxt = findViewById(R.id.homeTeamTxt);
        awayTeamTxt = findViewById(R.id.awayTeamTxt);
        homeScoreInput = findViewById(R.id.homeScoreInput);
        awayScoreInput = findViewById(R.id.awayScoreInput);
        btnUpdateScore = findViewById(R.id.btnUpdateScore);
        btnFinishMatch = findViewById(R.id.btnFinishMatch);

        // 2. Τι γίνεται όταν πατάμε το κουμπί "Ενημέρωση Σκορ"
        btnUpdateScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Διαβάζουμε τι νούμερα έγραψε ο Αναλυτής
                String homeScore = homeScoreInput.getText().toString();
                String awayScore = awayScoreInput.getText().toString();

                // Προς το παρόν βγάζουμε ένα μήνυμα στην οθόνη
                Toast.makeText(ManageMatchActivity.this, "Ενημέρωση: " + homeScore + "-" + awayScore + " (LIVE)", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Τι γίνεται όταν πατάμε το κουμπί "Λήξη Αγώνα"
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