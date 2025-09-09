<?php
require_once '../config/database.php';

// Validate request method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Only POST method is allowed");
}

// Get and validate input
$data = json_decode(file_get_contents("php://input"));

if (!$data || !isset($data->student_id) || !isset($data->violations) || empty($data->violations)) {
    sendResponse(false, "Student ID and violations are required");
}

// Validate input
$student_id = validateInput($data->student_id, 'alphanumeric', 20);
$recorded_by = validateInput($data->recorded_by ?? 'System', 'string', 100);

if (!$student_id) {
    sendResponse(false, "Invalid student ID");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    $conn->beginTransaction();
    
    // Get student info first
    $rfidConn = $database->getRfidConnection();
    if (!$rfidConn) {
        sendResponse(false, "RFID database connection failed");
    }
    
    $studentQuery = "SELECT student_name, year_level, course, section FROM students WHERE student_id = ?";
    $studentStmt = $rfidConn->prepare($studentQuery);
    $studentStmt->execute([$student_id]);
    $student = $studentStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$student) {
        sendResponse(false, "Student not found");
    }
    
    // Process violations and get highest offense count
    $highest_offense = 1;
    $penalty = "Warning";
    
    foreach ($data->violations as $violation_type) {
        // Get current offense count for this violation type
        $offenseQuery = "SELECT offense_count FROM student_violation_offense_counts WHERE student_id = ? AND violation_type = ?";
        $offenseStmt = $conn->prepare($offenseQuery);
        $offenseStmt->execute([$student_id, $violation_type]);
        
        $current_offense = 1;
        if ($offenseStmt->rowCount() > 0) {
            $offense_data = $offenseStmt->fetch(PDO::FETCH_ASSOC);
            $current_offense = ($offense_data['offense_count'] % 3) + 1; // Cycle 1->2->3->1
        }
        
        // Update offense count
        $upsertQuery = "INSERT INTO student_violation_offense_counts (student_id, violation_type, offense_count) 
                       VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE offense_count = ?";
        $upsertStmt = $conn->prepare($upsertQuery);
        $upsertStmt->execute([$student_id, $violation_type, $current_offense, $current_offense]);
        
        // Track highest offense
        if ($current_offense > $highest_offense) {
            $highest_offense = $current_offense;
        }
    }
    
    // Get penalty based on highest offense
    $penaltyQuery = "SELECT penalty_description FROM penalty_matrix WHERE offense_number = ? LIMIT 1";
    $penaltyStmt = $conn->prepare($penaltyQuery);
    $penaltyStmt->execute([$highest_offense]);
    if ($penaltyStmt->rowCount() > 0) {
        $penalty_data = $penaltyStmt->fetch(PDO::FETCH_ASSOC);
        $penalty = $penalty_data['penalty_description'];
    }
    
    // Insert violation record
    $violationQuery = "INSERT INTO violations (student_id, student_name, year_level, course, section, offense_count, penalty, recorded_by) 
                      VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    $violationStmt = $conn->prepare($violationQuery);
    $violationStmt->execute([
        $student_id,
        $student['student_name'],
        $student['year_level'],
        $student['course'],
        $student['section'],
        $highest_offense,
        $penalty,
        $recorded_by
    ]);
    
    $violation_id = $conn->lastInsertId();
    
    // Insert violation details
    foreach ($data->violations as $violation_type) {
        $detailQuery = "INSERT INTO violation_details (violation_id, violation_type) VALUES (?, ?)";
        $detailStmt = $conn->prepare($detailQuery);
        $detailStmt->execute([$violation_id, $violation_type]);
    }
    
    $conn->commit();
    
    $response_data = [
        'id' => (int)$violation_id,
        'offense' => $highest_offense,
        'penalty' => $penalty,
        'message' => $highest_offense . ($highest_offense == 1 ? "st" : ($highest_offense == 2 ? "nd" : "rd")) . " Offense"
    ];
    
    sendResponse(true, "Violation submitted successfully", $response_data);
    
} catch(PDOException $exception) {
    $conn->rollback();
    error_log("Violation submission error: " . $exception->getMessage());
    sendResponse(false, "Submission failed");
}
?>