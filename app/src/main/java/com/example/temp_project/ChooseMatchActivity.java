package com.example.temp_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChooseMatchActivity extends AppCompatActivity {

    private Spinner spinnerMatchday;
    private LinearLayout matchesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_match);

        setTitle(getString(R.string.title_matches));

        spinnerMatchday = findViewById(R.id.spinnerMatchday);
        matchesContainer = findViewById(R.id.matchesContainer);

        populateSpinner();
    }

    private void populateSpinner() {
        ArrayList<String> matchdays = new ArrayList<>();
        String prefix = getString(R.string.matchday_prefix);

        for (int i = 1; i <= 9; i++) {
            matchdays.add(prefix + " " + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, matchdays){
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if (position == 3) {
                    tv.setTextColor(android.graphics.Color.RED);
                    tv.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    tv.setTextColor(android.graphics.Color.BLACK);
                    tv.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMatchday.setAdapter(adapter);

        spinnerMatchday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedMatchday = position + 1;

                boolean isLive = loadMatchesForRound(selectedMatchday);
                if (view instanceof TextView) {
                    TextView spinnerTextView = (TextView) view;

                    if (isLive) {
                        spinnerTextView.setTextColor(android.graphics.Color.RED);
                        spinnerTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                    } else {
                        spinnerTextView.setTextColor(android.graphics.Color.BLACK);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private boolean loadMatchesForRound(int matchday) {
        matchesContainer.removeAllViews();

        MatchesList listManager = new MatchesList(matchday);
        ArrayList<Match> matches = listManager.getMatches();

        boolean isRoundLive = false;

        for (Match match : matches) {

            View matchRow = getLayoutInflater().inflate(R.layout.item_match, matchesContainer, false);

            TextView tvHome = matchRow.findViewById(R.id.tvHomeTeam);
            TextView tvAway = matchRow.findViewById(R.id.tvAwayTeam);
            TextView tvScore = matchRow.findViewById(R.id.tvScore);
            ImageView imgHome = matchRow.findViewById(R.id.imgHomeLogo);
            ImageView imgAway = matchRow.findViewById(R.id.imgAwayLogo);

            tvHome.setText(match.getHomeTeam());
            tvAway.setText(match.getAwayTeam());

            if ("live".equalsIgnoreCase(match.getStatus())) {
                isRoundLive = true;
                tvScore.setTextColor(android.graphics.Color.RED);
                tvScore.setText("LIVE\n" + match.getScoreText());
            } else {
                tvScore.setText(match.getScoreText());
            }


            if ("finished".equalsIgnoreCase(match.getStatus()))
            {
                matchRow.setEnabled(false);
                matchRow.setClickable(false);
                matchRow.setBackgroundResource(R.color.divider_gray);
            }



            Picasso.with(getApplicationContext()).load(match.getHomeLogo()).error(R.mipmap.ic_launcher_round).into(imgHome);
            Picasso.with(getApplicationContext()).load(match.getAwayLogo()).error(R.mipmap.ic_launcher_round).into(imgAway);

            matchRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mId = match.getMatchId();
                    boolean hasLineup = matchHasLineup(mId);

                    Intent intent;
                    if (hasLineup) {
                        intent = new Intent(ChooseMatchActivity.this, ManageMatchActivity.class);
                    } else {
                        intent = new Intent(ChooseMatchActivity.this, ChooseLineupsActivity.class);
                    }
                    intent.putExtra("MATCH_ID", mId);
                    startActivity(intent);
                }
            });

            matchesContainer.addView(matchRow);
        }

        return isRoundLive;
    }

    private boolean matchHasLineup(int matchId) {
        final boolean[] result = {false};

        Thread t = new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                String url = Config.BASE_URL + "checkLineup.php?matchId=" + matchId;
                okhttp3.Request req = new okhttp3.Request.Builder().url(url).build();
                okhttp3.Response resp = client.newCall(req).execute();
                if (resp.body() != null) {
                    org.json.JSONObject obj = new org.json.JSONObject(resp.body().string());
                    result[0] = obj.optBoolean("has_lineup", false);
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
        return result[0];
    }
}