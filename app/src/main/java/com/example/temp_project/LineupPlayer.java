package com.example.temp_project;

public class LineupPlayer {
    private final int playerId;
    private final String name;
    private final String position;
    private final String photo;
    private boolean selected; // αν τον διάλεξε ο υπεύθυνος για τη βασική 11άδα

    public LineupPlayer(int playerId, String name, String position, String photo) {
        this.playerId = playerId;
        this.name = name;
        this.position = position;
        this.photo = photo;
        this.selected = false;
    }

    public int getPlayerId() { return playerId; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public String getPhoto() { return photo; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}