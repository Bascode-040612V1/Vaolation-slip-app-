<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, "Only GET method is allowed");
}

$student_id = isset($_GET['student_id']) ? $_GET['student_id'] : '';

if (empty($student_id)) {
    sendResponse(false, "Student ID is required");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    // Get violation offense counts for specific student and violation types
    $query = "SELECT violation_type, offense_count FROM student_violation_offense_counts WHERE student_id = :student_id";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":student_id", $student_id);
    $stmt->execute();
    
    $offenseCounts = array();
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $offenseCounts[$row['violation_type']] = (int)$row['offense_count'];
    }
    
    sendResponse(true, "Offense counts retrieved successfully", $offenseCounts);
    
} catch(PDOException $exception) {
    sendResponse(false, "Failed to retrieve offense counts: " . $exception->getMessage());
}
?>