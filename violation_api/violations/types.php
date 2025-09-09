<?php
require_once '../config/database.php';

// Validate request method
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, "Only GET method is allowed");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    // Get violation types with caching headers
    $query = "SELECT id, violation_name, category, penalty_description FROM violation_types ORDER BY category, violation_name";
    $stmt = $conn->prepare($query);
    $stmt->execute();
    
    $violationTypes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Set caching headers
    header("Cache-Control: max-age=86400, must-revalidate"); // 24 hours
    header("Last-Modified: " . gmdate('D, d M Y H:i:s', time()) . ' GMT');
    
    sendResponse(true, "Violation types retrieved successfully", $violationTypes);
    
} catch(PDOException $exception) {
    error_log("Violation types error: " . $exception->getMessage());
    sendResponse(false, "Failed to retrieve violation types");
}
?>