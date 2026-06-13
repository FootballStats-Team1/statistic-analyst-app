package com.example.temp_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChooseLineupsActivity extends AppCompatActivity {

    private ImageView btnHomeTeam, btnAwayTeam;
    private TextView tvSelectedTeam, tvCounter;
    private LinearLayout playersContainer;
    private Button btnSaveLineups;

    private int matchId;
    private MatchPlayersList playersList;

    private boolean viewingHome = true; // ποια ομάδα βλέπουμε (true = γηπεδούχος)
    private static final int MAX_PLAYERS = 11;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lineups);
        setTitle(R.string.title_choose_lineups);

        matchId = getIntent().getIntExtra("MATCH_ID", -1);

        btnHomeTeam = findViewById(R.id.btnHomeTeam);
        btnAwayTeam = findViewById(R.id.btnAwayTeam);
        tvSelectedTeam = findViewById(R.id.tvSelectedTeam);
        tvCounter = findViewById(R.id.tvCounter);
        playersContainer = findViewById(R.id.playersContainer);
        btnSaveLineups = findViewById(R.id.btnSaveLineups);

        // Κατεβάζουμε ΟΛΟΥΣ τους παίκτες των 2 ομάδων (Thread + join μέσα στην κλάση)
        playersList = new MatchPlayersList(matchId);

        Picasso.with(getApplicationContext())
                .load(playersList.getHomeLogo())
                .error(R.mipmap.ic_launcher_round)
                .into(btnHomeTeam);
        Picasso.with(getApplicationContext())
                .load(playersList.getAwayLogo())
                .error(R.mipmap.ic_launcher_round)
                .into(btnAwayTeam);

        btnHomeTeam.setOnClickListener(v -> { viewingHome = true; renderPlayers(); });
        btnAwayTeam.setOnClickListener(v -> { viewingHome = false; renderPlayers(); });
        btnSaveLineups.setOnClickListener(v -> trySave());

        renderPlayers(); // default: η γηπεδούχος
    }

    private ArrayList<LineupPlayer> currentList() {
        return viewingHome ? playersList.getHomePlayers() : playersList.getAwayPlayers();
    }

    private int countSelected(ArrayList<LineupPlayer> list) {
        int c = 0;
        for (LineupPlayer p : list) {
            if (p.isSelected()) c++;
        }
        return c;
    }

    private void renderPlayers() {
        playersContainer.removeAllViews();

        tvSelectedTeam.setText(viewingHome ? playersList.getHomeTeamName()
                : playersList.getAwayTeamName());

        for (final LineupPlayer player : currentList()) {
            playersContainer.addView(buildPlayerRow(player));
        }
        updateCounter();
    }

    private View buildPlayerRow(final LineupPlayer player) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(12, 12, 12, 12);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 6, 0, 0);
        row.setLayoutParams(rowParams);

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(120, 120);
        imgParams.setMargins(0, 0, 16, 0);
        img.setLayoutParams(imgParams);
        Picasso.with(getApplicationContext())
                .load(player.getPhoto())
                .error(R.mipmap.ic_launcher_round)
                .into(img);
        row.addView(img);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(this);
        tvName.setText(player.getName());
        tvName.setTextSize(16f);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvPos = new TextView(this);
        tvPos.setText(player.getPosition());
        tvPos.setTextSize(13f);

        textCol.addView(tvName);
        textCol.addView(tvPos);
        row.addView(textCol);

        applyRowStyle(row, tvName, tvPos, player.isSelected());

        row.setOnClickListener(v -> {
            if (!player.isSelected()) {
                if (countSelected(currentList()) >= MAX_PLAYERS) {
                    Toast.makeText(this, getString(R.string.lineup_full),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                player.setSelected(true);
            } else {
                player.setSelected(false);
            }
            applyRowStyle(row, tvName, tvPos, player.isSelected());
            updateCounter();
        });

        return row;
    }

    private void applyRowStyle(LinearLayout row, TextView name, TextView pos, boolean selected) {
        if (selected) {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.palette_blue_4));
            name.setTextColor(Color.WHITE);
            pos.setTextColor(Color.WHITE);
        } else {
            row.setBackgroundColor(Color.parseColor("#EEEEEE"));
            name.setTextColor(Color.BLACK);
            pos.setTextColor(Color.DKGRAY);
        }
    }

    private void updateCounter() {
        int selected = countSelected(currentList());
        tvCounter.setText(selected + " / " + MAX_PLAYERS);
        tvCounter.setTextColor(selected == MAX_PLAYERS
                ? Color.parseColor("#2E7D32") : Color.RED);
    }

    private void trySave() {
        int home = countSelected(playersList.getHomePlayers());
        int away = countSelected(playersList.getAwayPlayers());

        if (home != MAX_PLAYERS || away != MAX_PLAYERS) {
            Toast.makeText(this,
                    "Πρέπει 11 + 11 (τώρα " + home + " + " + away + ")",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Τα IDs των 22 βασικών σε ένα comma-separated string
        StringBuilder ids = new StringBuilder();
        for (LineupPlayer p : playersList.getHomePlayers()) {
            if (p.isSelected()) {
                if (ids.length() > 0) ids.append(",");
                ids.append(p.getPlayerId());
            }
        }
        for (LineupPlayer p : playersList.getAwayPlayers()) {
            if (p.isSelected()) {
                if (ids.length() > 0) ids.append(",");
                ids.append(p.getPlayerId());
            }
        }

        saveLineupsToServer(ids.toString());
    }

    private void saveLineupsToServer(String starterIds) {
        String url = Config.BASE_URL + "saveLineups.php";

        RequestBody formBody = new FormBody.Builder()
                .add("match_id", String.valueOf(matchId))
                .add("starter_ids", starterIds)
                .build();

        Request request = new Request.Builder().url(url).post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChooseLineupsActivity.this,
                        "Network Error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = (response.body() != null) ? response.body().string() : "";
                boolean ok = false;
                try {
                    ok = new JSONObject(body).optBoolean("success", false);
                } catch (Exception e) {
                    ok = false;
                }
                final boolean success = ok;

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(ChooseLineupsActivity.this,
                                "Οι ενδεκάδες αποθηκεύτηκαν!", Toast.LENGTH_SHORT).show();

                        // Πάμε στη διαχείριση αγώνα με το ΙΔΙΟ MATCH_ID
                        Intent intent = new Intent(ChooseLineupsActivity.this,
                                ManageMatchActivity.class);
                        intent.putExtra("MATCH_ID", matchId);
                        startActivity(intent);
                        finish(); // να μη γυρνάει με back στην επιλογή 11άδας
                    } else {
                        Toast.makeText(ChooseLineupsActivity.this,
                                "Αποτυχία αποθήκευσης", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}