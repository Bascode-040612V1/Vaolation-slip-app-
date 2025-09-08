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
    // Get violation history using violation_summary view
    $query = "SELECT * FROM violation_summary WHERE student_id = :student_id ORDER BY recorded_at DESC";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":student_id", $student_id);
    $stmt->execute();
    
    $violations = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, "Student violations retrieved successfully", $violations);
    
} catch(PDOException $exception) {
    sendResponse(false, "Failed to retrieve violations: " . $exception->getMessage());
}
?>