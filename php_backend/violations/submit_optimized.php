<?php
require_once '../config/database.php';
require_once '../utils/ResponseOptimizer.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    ResponseOptimizer::sendOptimizedResponse(false, "Only POST method allowed");
}

// Enable request compression handling
$input = file_get_contents("php://input");
if (isset($_SERVER['HTTP_CONTENT_ENCODING']) && $_SERVER['HTTP_CONTENT_ENCODING'] === 'gzip') {
    $input = gzinflate(substr($input, 10, -8));
}

$data = json_decode($input);

if (!$data || !isset($data->student_id) || !isset($data->violations) || !isset($data->recorded_by)) {
    ResponseOptimizer::sendOptimizedResponse(false, "Required fields missing");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    ResponseOptimizer::sendOptimizedResponse(false, "Database connection failed");
}

try {
    $conn->beginTransaction();
    
    // Optimized student lookup with minimal fields
    $student_query = "SELECT student_id, student_name, year_level, course, section 
                      FROM students 
                      WHERE student_id = ? LIMIT 1";
    $student_stmt = $conn->prepare($student_query);
    $student_stmt->bindParam(1, $data->student_id);
    $student_stmt->execute();
    
    if ($student_stmt->rowCount() == 0) {
        $conn->rollback();
        ResponseOptimizer::sendOptimizedResponse(false, "Student not found");
    }
    
    $student = $student_stmt->fetch(PDO::FETCH_ASSOC);
    
    // Batch query for all offense counts in single query
    $violations_list = implode(',', array_map(function($v) { return "'$v'"; }, $data->violations));
    $violation_offense_query = "SELECT violation_type, offense_count 
                                FROM student_violation_offense_counts 
                                WHERE student_id = ? AND violation_type IN ($violations_list)";
    $violation_offense_stmt = $conn->prepare($violation_offense_query);
    $violation_offense_stmt->execute([$data->student_id]);
    
    $existing_offenses = [];
    while ($row = $violation_offense_stmt->fetch(PDO::FETCH_ASSOC)) {
        $existing_offenses[$row['violation_type']] = (int)$row['offense_count'];
    }
    
    // Calculate next offense counts
    $highest_violation_offense = 0;
    $violation_offense_counts = [];
    
    foreach ($data->violations as $violation_type) {
        $current_count = $existing_offenses[$violation_type] ?? 0;
        $next_offense_count = ($current_count >= 3) ? 1 : $current_count + 1;
        
        $violation_offense_counts[$violation_type] = $next_offense_count;
        $highest_violation_offense = max($highest_violation_offense, $next_offense_count);
    }
    
    // Get penalty with single query
    $penalty_query = "SELECT penalty_description 
                      FROM penalty_matrix 
                      WHERE violation_type = ? AND offense_count = ? LIMIT 1";
    $penalty_stmt = $conn->prepare($penalty_query);
    $penalty_stmt->execute([$data->violations[0], $highest_violation_offense]);
    
    $penalty = "Warning"; // Default
    if ($penalty_row = $penalty_stmt->fetch(PDO::FETCH_ASSOC)) {
        $penalty = $penalty_row['penalty_description'];
    }
    
    // Insert violation record
    $violation_insert_query = "INSERT INTO violations 
                               (student_id, student_name, year_level, course, section, offense_count, penalty, recorded_by) 
                               VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    $violation_insert_stmt = $conn->prepare($violation_insert_query);
    $violation_insert_stmt->execute([
        $student['student_id'],
        $student['student_name'],
        $student['year_level'],
        $student['course'],
        $student['section'],
        $highest_violation_offense,
        $penalty,
        $data->recorded_by
    ]);
    
    $violation_id = $conn->lastInsertId();
    
    // Batch insert violation details and update offense counts
    $detail_values = [];
    $offense_updates = [];
    
    foreach ($data->violations as $violation_type) {
        $detail_values[] = "($violation_id, '$violation_type')";
        
        $next_count = $violation_offense_counts[$violation_type];
        $offense_updates[] = "('$data->student_id', '$violation_type', $next_count)";
    }
    
    // Batch insert violation details
    if (!empty($detail_values)) {
        $detail_query = "INSERT INTO violation_details (violation_id, violation_type) VALUES " . 
                       implode(',', $detail_values);
        $conn->exec($detail_query);
    }
    
    // Batch upsert offense counts
    if (!empty($offense_updates)) {
        $offense_query = "INSERT INTO student_violation_offense_counts (student_id, violation_type, offense_count) VALUES " .
                        implode(',', $offense_updates) .
                        " ON DUPLICATE KEY UPDATE offense_count = VALUES(offense_count)";
        $conn->exec($offense_query);
    }
    
    $conn->commit();
    
    // Create minimal response
    $response_data = [
        'id' => (int)$violation_id,
        'offense' => $highest_violation_offense,
        'penalty' => $penalty,
        'msg' => $highest_violation_offense . ($highest_violation_offense == 1 ? "st" : 
                ($highest_violation_offense == 2 ? "nd" : "rd")) . " Offense"
    ];
    
    ResponseOptimizer::sendOptimizedResponse(true, "Violation submitted", $response_data);
    
} catch(PDOException $exception) {
    $conn->rollback();
    error_log("Optimized violation submission error: " . $exception->getMessage());
    ResponseOptimizer::sendOptimizedResponse(false, "Submission failed");
}
?>