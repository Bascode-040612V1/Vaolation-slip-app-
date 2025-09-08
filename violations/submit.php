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
    
    // Get or create student offense count
    $offense_query = "SELECT current_offense_count FROM student_offense_counts WHERE student_id = :student_id";
    $offense_stmt = $conn->prepare($offense_query);
    $offense_stmt->bindParam(":student_id", $data->student_id);
    $offense_stmt->execute();
    
    $current_offense_count = 1;
    if ($offense_stmt->rowCount() > 0) {
        $offense_result = $offense_stmt->fetch(PDO::FETCH_ASSOC);
        $current_offense_count = $offense_result['current_offense_count'] + 1;
        
        // Cycle offense count (1 -> 2 -> 3 -> 1)
        if ($current_offense_count > 3) {
            $current_offense_count = 1;
        }
        
        // Update existing offense count
        $update_offense_query = "UPDATE student_offense_counts SET current_offense_count = :count WHERE student_id = :student_id";
        $update_offense_stmt = $conn->prepare($update_offense_query);
        $update_offense_stmt->bindParam(":count", $current_offense_count);
        $update_offense_stmt->bindParam(":student_id", $data->student_id);
        $update_offense_stmt->execute();
    } else {
        // Insert new offense count
        $insert_offense_query = "INSERT INTO student_offense_counts (student_id, current_offense_count) VALUES (:student_id, :count)";
        $insert_offense_stmt = $conn->prepare($insert_offense_query);
        $insert_offense_stmt->bindParam(":student_id", $data->student_id);
        $insert_offense_stmt->bindParam(":count", $current_offense_count);
        $insert_offense_stmt->execute();
    }
    
    // Get penalty from penalty_matrix based on violation type and offense count
    $penalty = "Warning";
    if (!empty($data->violations)) {
        $penalty_query = "SELECT penalty_description FROM penalty_matrix WHERE violation_type = :violation_type AND offense_count = :offense_count";
        $penalty_stmt = $conn->prepare($penalty_query);
        $penalty_stmt->bindParam(":violation_type", $data->violations[0]);
        $penalty_stmt->bindParam(":offense_count", $current_offense_count);
        $penalty_stmt->execute();
        
        if ($penalty_stmt->rowCount() > 0) {
            $penalty_result = $penalty_stmt->fetch(PDO::FETCH_ASSOC);
            $penalty = $penalty_result['penalty_description'];
        } else {
            // Default penalty based on offense count
            switch ($current_offense_count) {
                case 1: $penalty = "Warning"; break;
                case 2: $penalty = "Grounding"; break;
                case 3: $penalty = "Suspension"; break;
                default: $penalty = "Warning"; break;
            }
        }
    }
    
    // Insert violation record
    $violation_insert_query = "INSERT INTO violations (student_id, student_name, year_level, course, section, offense_count, penalty, recorded_by) VALUES (:student_id, :student_name, :year_level, :course, :section, :offense_count, :penalty, :recorded_by)";
    $violation_insert_stmt = $conn->prepare($violation_insert_query);
    $violation_insert_stmt->bindParam(":student_id", $student['student_id']);
    $violation_insert_stmt->bindParam(":student_name", $student['student_name']);
    $violation_insert_stmt->bindParam(":year_level", $student['year_level']);
    $violation_insert_stmt->bindParam(":course", $student['course']);
    $violation_insert_stmt->bindParam(":section", $student['section']);
    $violation_insert_stmt->bindParam(":offense_count", $current_offense_count);
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
        
        // Update student_violation_offense_counts for each violation type
        $violation_offense_query = "SELECT offense_count FROM student_violation_offense_counts WHERE student_id = :student_id AND violation_type = :violation_type";
        $violation_offense_stmt = $conn->prepare($violation_offense_query);
        $violation_offense_stmt->bindParam(":student_id", $data->student_id);
        $violation_offense_stmt->bindParam(":violation_type", $violation_type);
        $violation_offense_stmt->execute();
        
        $violation_offense_count = 1;
        if ($violation_offense_stmt->rowCount() > 0) {
            $violation_offense_result = $violation_offense_stmt->fetch(PDO::FETCH_ASSOC);
            $violation_offense_count = $violation_offense_result['offense_count'] + 1;
            
            // Cycle violation offense count (1 -> 2 -> 3 -> 1)
            if ($violation_offense_count > 3) {
                $violation_offense_count = 1;
            }
            
            // Update existing violation offense count
            $update_violation_offense_query = "UPDATE student_violation_offense_counts SET offense_count = :count WHERE student_id = :student_id AND violation_type = :violation_type";
            $update_violation_offense_stmt = $conn->prepare($update_violation_offense_query);
            $update_violation_offense_stmt->bindParam(":count", $violation_offense_count);
            $update_violation_offense_stmt->bindParam(":student_id", $data->student_id);
            $update_violation_offense_stmt->bindParam(":violation_type", $violation_type);
            $update_violation_offense_stmt->execute();
        } else {
            // Insert new violation offense count
            $insert_violation_offense_query = "INSERT INTO student_violation_offense_counts (student_id, violation_type, offense_count) VALUES (:student_id, :violation_type, :count)";
            $insert_violation_offense_stmt = $conn->prepare($insert_violation_offense_query);
            $insert_violation_offense_stmt->bindParam(":student_id", $data->student_id);
            $insert_violation_offense_stmt->bindParam(":violation_type", $violation_type);
            $insert_violation_offense_stmt->bindParam(":count", $violation_offense_count);
            $insert_violation_offense_stmt->execute();
        }
    }
    
    $conn->commit();
    
    $response_data = array(
        "violation_id" => $violation_id,
        "offense_count" => $current_offense_count,
        "penalty" => $penalty,
        "message" => "Violation submitted successfully"
    );
    
    sendResponse(true, "Violation submitted successfully", $response_data);
    
} catch(PDOException $exception) {
    $conn->rollback();
    sendResponse(false, "Failed to submit violation: " . $exception->getMessage());
}
?>