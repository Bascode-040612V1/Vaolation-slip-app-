<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, "Only GET method is allowed");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    $query = "SELECT * FROM violation_types WHERE is_active = 1 ORDER BY category, violation_name";
    $stmt = $conn->prepare($query);
    $stmt->execute();
    
    $violationTypes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    sendResponse(true, "Violation types retrieved successfully", $violationTypes);
    
} catch(PDOException $exception) {
    sendResponse(false, "Failed to retrieve violation types: " . $exception->getMessage());
}
?>