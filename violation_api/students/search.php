<?php
require_once '../config/database.php';

// Validate request method
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, "Only GET method is allowed");
}

$student_id = validateInput($_GET['student_id'] ?? '', 'alphanumeric', 20);

if (!$student_id) {
    sendResponse(false, "Valid student ID is required");
}

$database = new Database();
$conn = $database->getRfidConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    // Search student in RFID database
    $query = "SELECT student_id, student_name, year_level, course, section, image FROM students WHERE student_id = :student_id";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":student_id", $student_id);
    $stmt->execute();
    
    if ($stmt->rowCount() > 0) {
        $student = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Get offense counts from violation database
        $violationConn = $database->getViolationConnection();
        if ($violationConn) {
            $offenseQuery = "SELECT violation_type, offense_count FROM student_violation_offense_counts WHERE student_id = ?";
            $offenseStmt = $violationConn->prepare($offenseQuery);
            $offenseStmt->execute([$student_id]);
            
            $offenseCounts = [];
            while ($row = $offenseStmt->fetch(PDO::FETCH_ASSOC)) {
                $offenseCounts[$row['violation_type']] = (int)$row['offense_count'];
            }
            
            $student['offense_counts'] = $offenseCounts;
        }
        
        sendResponse(true, "Student found", $student);
    } else {
        sendResponse(false, "Student not found");
    }
    
} catch(PDOException $exception) {
    error_log("Student search error: " . $exception->getMessage());
    sendResponse(false, "Search failed");
}
?>