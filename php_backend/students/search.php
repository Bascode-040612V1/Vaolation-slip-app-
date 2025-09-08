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
    // Search student from students table (since student_stats view may reference deleted table)
    $query = "SELECT * FROM students WHERE student_id = :student_id";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":student_id", $student_id);
    $stmt->execute();
    
    if ($stmt->rowCount() > 0) {
        $student = $stmt->fetch(PDO::FETCH_ASSOC);
        sendResponse(true, "Student found", $student);
    } else {
        sendResponse(false, "Student not found");
    }
    
} catch(PDOException $exception) {
    sendResponse(false, "Search failed: " . $exception->getMessage());
}
?>