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
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    // Get student violations
    $query = "SELECT v.*, vd.violation_type 
              FROM violations v 
              LEFT JOIN violation_details vd ON v.id = vd.violation_id 
              WHERE v.student_id = ? 
              ORDER BY v.recorded_at DESC";
    $stmt = $conn->prepare($query);
    $stmt->execute([$student_id]);
    
    $violations = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, "Student violations retrieved successfully", $violations);
    
} catch(PDOException $exception) {
    error_log("Student violations error: " . $exception->getMessage());
    sendResponse(false, "Failed to retrieve student violations");
}
?>