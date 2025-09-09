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
    // Get offense counts for student
    $query = "SELECT violation_type, offense_count FROM student_violation_offense_counts WHERE student_id = ? ORDER BY violation_type";
    $stmt = $conn->prepare($query);
    $stmt->execute([$student_id]);
    
    $offenseCounts = [];
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $offenseCounts[$row['violation_type']] = (int)$row['offense_count'];
    }
    
    sendResponse(true, "Offense counts retrieved successfully", $offenseCounts);
    
} catch(PDOException $exception) {
    error_log("Offense counts error: " . $exception->getMessage());
    sendResponse(false, "Failed to retrieve offense counts");
}
?>