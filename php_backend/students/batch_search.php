<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Only POST method is allowed");
}

$data = json_decode(file_get_contents("php://input"));

if (!$data || !isset($data->student_ids) || !is_array($data->student_ids)) {
    sendResponse(false, "Student IDs array is required");
}

$database = new Database();
$violationConn = $database->getViolationConnection();
$rfidConn = $database->getRfidConnection();

if (!$violationConn || !$rfidConn) {
    sendResponse(false, "Database connection failed");
}

try {
    $studentIds = array_slice($data->student_ids, 0, 20); // Limit to 20 students per batch
    $placeholders = str_repeat('?,', count($studentIds) - 1) . '?';
    
    // Optimized batch query for students
    $query = "SELECT 
                s.student_id,
                s.student_name,
                s.year_level,
                s.course,
                s.section,
                s.image
              FROM students s 
              WHERE s.student_id IN ($placeholders)
              ORDER BY s.student_name";
    
    $stmt = $rfidConn->prepare($query);
    $stmt->execute($studentIds);
    
    $students = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Batch fetch offense counts for all students
    $offenseCounts = [];
    if (!empty($students)) {
        $offenseQuery = "SELECT 
                           student_id,
                           violation_type,
                           offense_count
                         FROM student_violation_offense_counts 
                         WHERE student_id IN ($placeholders)";
        
        $offenseStmt = $violationConn->prepare($offenseQuery);
        $offenseStmt->execute($studentIds);
        
        $offenseResults = $offenseStmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Group offense counts by student
        foreach ($offenseResults as $offense) {
            $offenseCounts[$offense['student_id']][$offense['violation_type']] = (int)$offense['offense_count'];
        }
    }
    
    // Optimize response payload
    $optimizedResponse = [];
    foreach ($students as $student) {
        $studentId = $student['student_id'];
        $optimizedResponse[] = [
            'student_id' => $studentId,
            'name' => $student['student_name'],
            'info' => $student['year_level'] . ' ' . $student['course'] . '-' . $student['section'],
            'image' => $student['image'],
            'offense_counts' => $offenseCounts[$studentId] ?? []
        ];
    }
    
    // Set caching headers for partial caching
    header("Cache-Control: max-age=1800, must-revalidate"); // 30 minutes
    
    sendResponse(true, "Students retrieved successfully", $optimizedResponse);
    
} catch(PDOException $exception) {
    error_log("Batch student query error: " . $exception->getMessage());
    sendResponse(false, "Failed to retrieve students");
}
?>