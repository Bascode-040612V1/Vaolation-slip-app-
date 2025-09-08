<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, "Only GET method is allowed");
}

$database = new Database();

try {
    $conn = $database->getViolationConnection();
    if ($conn) {
        sendResponse(true, "Database connection successful");
    } else {
        sendResponse(false, "Database connection failed");
    }
} catch(Exception $e) {
    sendResponse(false, "Connection test failed: " . $e->getMessage());
}
?>