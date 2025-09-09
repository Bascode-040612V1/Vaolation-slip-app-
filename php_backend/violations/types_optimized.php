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
    // Check for If-None-Match header for conditional requests
    $clientETag = $_SERVER['HTTP_IF_NONE_MATCH'] ?? '';
    
    // Optimized query with better indexing
    $query = "SELECT 
                vt.id, 
                vt.violation_name, 
                vt.category, 
                vt.penalty_description,
                vt.is_active,
                vt.updated_at
              FROM violation_types vt 
              WHERE vt.is_active = 1 
              ORDER BY vt.category, vt.violation_name";
    
    $stmt = $conn->prepare($query);
    $stmt->execute();
    
    $violationTypes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Generate ETag based on data hash for caching
    $dataHash = md5(json_encode($violationTypes));
    $eTag = '"' . $dataHash . '"';
    
    // Send 304 Not Modified if client has current version
    if ($clientETag === $eTag) {
        http_response_code(304);
        header("ETag: $eTag");
        header("Cache-Control: max-age=86400, must-revalidate"); // 24 hours
        exit();
    }
    
    // Optimize response payload - only essential fields
    $optimizedResponse = array_map(function($vt) {
        return [
            'id' => (int)$vt['id'],
            'name' => $vt['violation_name'],
            'category' => $vt['category'],
            'penalty' => $vt['penalty_description']
        ];
    }, $violationTypes);
    
    // Set caching headers
    header("ETag: $eTag");
    header("Cache-Control: max-age=86400, must-revalidate"); // 24 hours
    header("Last-Modified: " . gmdate('D, d M Y H:i:s', time()) . ' GMT');
    
    sendResponse(true, "Violation types retrieved successfully", $optimizedResponse);
    
} catch(PDOException $exception) {
    error_log("Violation types query error: " . $exception->getMessage());
    sendResponse(false, "Failed to retrieve violation types");
}
?>