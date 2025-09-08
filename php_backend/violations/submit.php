<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Only POST method is allowed");
}

$data = json_decode(file_get_contents("php://input"));

if (!$data || !isset($data->student_id) || !isset($data->violations) || !isset($data->recorded_by)) {
    sendResponse(false, "Student ID, violations, and recorded_by are required");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    $conn->beginTransaction();
    
    // Get student information
    $student_query = "SELECT * FROM students WHERE student_id = :student_id";
    $student_stmt = $conn->prepare($student_query);
    $student_stmt->bindParam(":student_id", $data->student_id);
    $student_stmt->execute();
    
    if ($student_stmt->rowCount() == 0) {
        $conn->rollback();
        sendResponse(false, "Student not found");
    }
    
    $student = $student_stmt->fetch(PDO::FETCH_ASSOC);
    
    // Find the highest violation offense count among selected violations
    $highest_violation_offense = 0;
    $violation_offense_counts = array();
    
    foreach ($data->violations as $violation_type) {
        // Get current offense count for this specific violation type
        $violation_offense_query = "SELECT offense_count FROM student_violation_offense_counts WHERE student_id = :student_id AND violation_type = :violation_type";
        $violation_offense_stmt = $conn->prepare($violation_offense_query);
        $violation_offense_stmt->bindParam(":student_id", $data->student_id);
        $violation_offense_stmt->bindParam(":violation_type", $violation_type);
        $violation_offense_stmt->execute();
        
        $next_offense_count = 1; // Default for first time
        if ($violation_offense_stmt->rowCount() > 0) {
            $violation_offense_result = $violation_offense_stmt->fetch(PDO::FETCH_ASSOC);
            $next_offense_count = $violation_offense_result['offense_count'] + 1;
            
            // Cycle violation offense count (1 -> 2 -> 3 -> 1)
            if ($next_offense_count > 3) {
                $next_offense_count = 1;
            }
        }
        
        $violation_offense_counts[$violation_type] = $next_offense_count;
        
        // Track the highest violation offense count among all submitted violations
        if ($next_offense_count > $highest_violation_offense) {
            $highest_violation_offense = $next_offense_count;
        }
    }
    
    // Get penalty based on highest violation offense count
    $penalty = "Warning"; // Default
    if ($highest_violation_offense > 0 && !empty($data->violations)) {
        // Try to get penalty for the first violation type with highest offense count
        $penalty_query = "SELECT penalty_description FROM penalty_matrix WHERE violation_type = :violation_type AND offense_count = :offense_count";
        $penalty_stmt = $conn->prepare($penalty_query);
        $penalty_stmt->bindParam(":violation_type", $data->violations[0]);
        $penalty_stmt->bindParam(":offense_count", $highest_violation_offense);
        $penalty_stmt->execute();
        
        if ($penalty_stmt->rowCount() > 0) {
            $penalty_result = $penalty_stmt->fetch(PDO::FETCH_ASSOC);
            $penalty = $penalty_result['penalty_description'];
        } else {
            // Default penalty based on highest violation offense count
            switch ($highest_violation_offense) {
                case 1: $penalty = "Warning"; break;
                case 2: $penalty = "Grounding"; break;
                case 3: $penalty = "Suspension"; break;
                default: $penalty = "Warning"; break;
            }
        }
    }
    
    // Insert violation record using highest offense count
    $violation_insert_query = "INSERT INTO violations (student_id, student_name, year_level, course, section, offense_count, penalty, recorded_by) VALUES (:student_id, :student_name, :year_level, :course, :section, :offense_count, :penalty, :recorded_by)";
    $violation_insert_stmt = $conn->prepare($violation_insert_query);
    $violation_insert_stmt->bindParam(":student_id", $student['student_id']);
    $violation_insert_stmt->bindParam(":student_name", $student['student_name']);
    $violation_insert_stmt->bindParam(":year_level", $student['year_level']);
    $violation_insert_stmt->bindParam(":course", $student['course']);
    $violation_insert_stmt->bindParam(":section", $student['section']);
    $violation_insert_stmt->bindParam(":offense_count", $highest_violation_offense);
    $violation_insert_stmt->bindParam(":penalty", $penalty);
    $violation_insert_stmt->bindParam(":recorded_by", $data->recorded_by);
    $violation_insert_stmt->execute();
    
    $violation_id = $conn->lastInsertId();
    
    // Insert violation details and update individual violation offense counts
    foreach ($data->violations as $violation_type) {
        // Insert violation detail
        $detail_insert_query = "INSERT INTO violation_details (violation_id, violation_type) VALUES (:violation_id, :violation_type)";
        $detail_insert_stmt = $conn->prepare($detail_insert_query);
        $detail_insert_stmt->bindParam(":violation_id", $violation_id);
        $detail_insert_stmt->bindParam(":violation_type", $violation_type);
        $detail_insert_stmt->execute();
        
        // Get the pre-calculated offense count for this violation
        $next_offense_count = $violation_offense_counts[$violation_type];
        
        // Update or insert the violation offense count
        $check_existing_query = "SELECT offense_count FROM student_violation_offense_counts WHERE student_id = :student_id AND violation_type = :violation_type";
        $check_existing_stmt = $conn->prepare($check_existing_query);
        $check_existing_stmt->bindParam(":student_id", $data->student_id);
        $check_existing_stmt->bindParam(":violation_type", $violation_type);
        $check_existing_stmt->execute();
        
        if ($check_existing_stmt->rowCount() > 0) {
            // Update existing violation offense count
            $update_violation_offense_query = "UPDATE student_violation_offense_counts SET offense_count = :count WHERE student_id = :student_id AND violation_type = :violation_type";
            $update_violation_offense_stmt = $conn->prepare($update_violation_offense_query);
            $update_violation_offense_stmt->bindParam(":count", $next_offense_count);
            $update_violation_offense_stmt->bindParam(":student_id", $data->student_id);
            $update_violation_offense_stmt->bindParam(":violation_type", $violation_type);
            $update_violation_offense_stmt->execute();
        } else {
            // Insert new violation offense count
            $insert_violation_offense_query = "INSERT INTO student_violation_offense_counts (student_id, violation_type, offense_count) VALUES (:student_id, :violation_type, :count)";
            $insert_violation_offense_stmt = $conn->prepare($insert_violation_offense_query);
            $insert_violation_offense_stmt->bindParam(":student_id", $data->student_id);
            $insert_violation_offense_stmt->bindParam(":violation_type", $violation_type);
            $insert_violation_offense_stmt->bindParam(":count", $next_offense_count);
            $insert_violation_offense_stmt->execute();
        }
    }
    
    $conn->commit();
    
    $response_data = array(
        "violation_id" => $violation_id,
        "offense_count" => $highest_violation_offense,
        "penalty" => $penalty,
        "message" => "Violation submitted successfully - " . $highest_violation_offense . ($highest_violation_offense == 1 ? "st" : ($highest_violation_offense == 2 ? "nd" : "rd")) . " Offense"
    );
    
    sendResponse(true, "Violation submitted successfully", $response_data);
    
} catch(PDOException $exception) {
    $conn->rollback();
    sendResponse(false, "Failed to submit violation: " . $exception->getMessage());
}
?>