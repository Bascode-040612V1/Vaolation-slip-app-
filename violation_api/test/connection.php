<?php
require_once '../config/database.php';

// Validate request method
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, "Only GET method is allowed");
}

$database = new Database();

try {
    // Test both database connections
    $violationConn = $database->getViolationConnection();
    $rfidConn = $database->getRfidConnection();
    
    $results = [];
    
    if ($violationConn) {
        $results['violation_db'] = 'Connected successfully';
        
        // Test a simple query
        $stmt = $violationConn->prepare("SELECT COUNT(*) as count FROM users");
        $stmt->execute();
        $userCount = $stmt->fetch(PDO::FETCH_ASSOC);
        $results['users_count'] = $userCount['count'];
    } else {
        $results['violation_db'] = 'Connection failed';
    }
    
    if ($rfidConn) {
        $results['rfid_db'] = 'Connected successfully';
        
        // Test a simple query
        $stmt = $rfidConn->prepare("SELECT COUNT(*) as count FROM students");
        $stmt->execute();
        $studentCount = $stmt->fetch(PDO::FETCH_ASSOC);
        $results['students_count'] = $studentCount['count'];
    } else {
        $results['rfid_db'] = 'Connection failed';
    }
    
    $results['server_time'] = date('Y-m-d H:i:s');
    $results['php_version'] = phpversion();
    
    sendResponse(true, "Connection test completed", $results);
    
} catch(Exception $e) {
    error_log("Connection test error: " . $e->getMessage());
    sendResponse(false, "Connection test failed");
}
?>