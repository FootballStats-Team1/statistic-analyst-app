<?php
header('Content-Type: application/json; charset=utf-8');
$conn = new mysqli("localhost", "root", "", "epodb");
$conn->set_charset("utf8mb4");

if ($conn->connect_error) {
    die(json_encode(["error" => "Connection failed"]));
}

// 1. Παίρνουμε τα δεδομένα από το Android App
$match_id   = isset($_POST['match_id'])   ? intval($_POST['match_id']) : 0;
$player_id  = isset($_POST['player_id'])  ? intval($_POST['player_id']) : 0;
$team_type  = isset($_POST['team_type'])  ? $_POST['team_type'] : ''; // "home" ή "away"
$event_type = isset($_POST['event_type']) ? $_POST['event_type'] : '';
$value_str  = isset($_POST['value'])      ? $_POST['value'] : '';

// 2. Μετατροπή του "+1" ή "-1" σε κανονικό νούμερο
$val = 0;
if ($value_str === '+1') $val = 1;
elseif ($value_str === '-1') $val = -1;

// Επιτρεπτά πεδία για τον πίνακα match_events (Whitelist για ασφάλεια)
$allowed_events = [
    'goals', 'shots_on_target', 'shots_off_target',
    'passes_succ', 'passes_fail', 'tackles_succ', 'tackles_fail',
    'crosses_succ', 'crosses_fail', 'assists', 'errors',
    'fouls_won', 'fouls_committed', 'yellow_cards', 'red_cards'
];

if ($match_id > 0 && $val != 0) {
    
    // --- ΠΕΡΙΠΤΩΣΗ Α: Κόρνερ (Πάνε στον πίνακα matches) ---
    if ($event_type === 'corners_won') {
        $column = ($team_type === 'home') ? 'home_corners' : 'away_corners';
        // Το GREATEST(0, ...) εξασφαλίζει ότι το σκορ/κόρνερ δεν θα πάει ποτέ στο μείον (-)
        $sql = "UPDATE matches SET $column = GREATEST(0, $column + ?) WHERE id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ii", $val, $match_id);
        $stmt->execute();
        $stmt->close();
        
        echo json_encode(["status" => "success", "msg" => "Corner updated"]);
        exit;
    }

    // --- ΠΕΡΙΠΤΩΣΗ Β: Κανονικά Ατομικά Στατιστικά (Πάνε στον πίνακα match_events) ---
    if ($player_id > 0 && in_array($event_type, $allowed_events)) {
        
        // Ενημερώνουμε το στατιστικό του συγκεκριμένου παίκτη
        $sql = "UPDATE match_events SET $event_type = GREATEST(0, $event_type + ?) WHERE match_id = ? AND player_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("iii", $val, $match_id, $player_id);
        $stmt->execute();
        $stmt->close();

        // --- ΕΙΔΙΚΗ ΥΠΟ-ΠΕΡΙΠΤΩΣΗ: Αν είναι Γκολ, αυξάνουμε ΚΑΙ το συνολικό σκορ του αγώνα! ---
        if ($event_type === 'goals') {
            $score_col = ($team_type === 'home') ? 'home_score' : 'away_score';
            $sql2 = "UPDATE matches SET $score_col = GREATEST(0, $score_col + ?) WHERE id = ?";
            $stmt2 = $conn->prepare($sql2);
            $stmt2->bind_param("ii", $val, $match_id);
            $stmt2->execute();
            $stmt2->close();
        }

        echo json_encode(["status" => "success"]);
    } else {
        echo json_encode(["error" => "Invalid player_id or event_type"]);
    }
} else {
    echo json_encode(["error" => "Invalid parameters"]);
}

$conn->close();
?>