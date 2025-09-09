<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, "Only GET method is allowed");
}

$student_id = $_GET['student_id'] ?? '';
$include_offense_counts = $_GET['include_offense_counts'] ?? 'true';
$use_cache = $_GET['use_cache'] ?? 'true';

if (empty($student_id)) {
    sendResponse(false, "Student ID is required");
}

$database = new Database();
$violationConn = $database->getViolationConnection();
$rfidConn = $database->getRfidConnection();

if (!$violationConn || !$rfidConn) {
    sendResponse(false, "Database connection failed");
}

try {
    // Check for cached response
    $cacheKey = "student_" . $student_id . "_" . ($include_offense_counts === 'true' ? '1' : '0');
    $clientETag = $_SERVER['HTTP_IF_NONE_MATCH'] ?? '';
    
    // Optimized student query with single join
    $query = "SELECT 
                s.student_id,
                s.student_name,
                s.year_level,
                s.course,
                s.section,
                s.image,
                s.updated_at
              FROM students s 
              WHERE s.student_id = ? 
              LIMIT 1";
    
    $stmt = $rfidConn->prepare($query);
    $stmt->execute([$student_id]);
    
    if ($stmt->rowCount() == 0) {
        sendResponse(false, "Student not found");
    }
    
    $student = $stmt->fetch(PDO::FETCH_ASSOC);
    
    // Optimized response payload
    $response = [
        'student_id' => $student['student_id'],
        'name' => $student['student_name'],
        'info' => $student['year_level'] . ' ' . $student['course'] . '-' . $student['section'],
        'image' => $student['image']
    ];
    
    // Conditionally include offense counts
    if ($include_offense_counts === 'true') {
        $offenseQuery = "SELECT violation_type, offense_count 
                         FROM student_violation_offense_counts 
                         WHERE student_id = ?";
        
        $offenseStmt = $violationConn->prepare($offenseQuery);
        $offenseStmt->execute([$student_id]);
        
        $offenseCounts = [];
        while ($row = $offenseStmt->fetch(PDO::FETCH_ASSOC)) {
            $offenseCounts[$row['violation_type']] = (int)$row['offense_count'];
        }
        
        $response['offense_counts'] = $offenseCounts;
    }
    
    // Generate ETag for caching
    $eTag = '"' . md5(json_encode($response)) . '"';
    
    // Send 304 if client has current version
    if ($use_cache === 'true' && $clientETag === $eTag) {
        http_response_code(304);
        header("ETag: $eTag");
        header("Cache-Control: max-age=1800, must-revalidate"); // 30 minutes
        exit();
    }
    
    // Set caching headers
    header("ETag: $eTag");
    header("Cache-Control: max-age=1800, must-revalidate"); // 30 minutes
    header("Last-Modified: " . gmdate('D, d M Y H:i:s', strtotime($student['updated_at'])) . ' GMT');
    
    sendResponse(true, "Student found", $response);
    
} catch(PDOException $exception) {
    error_log("Optimized student search error: " . $exception->getMessage());
    sendResponse(false, "Failed to retrieve student");
}
?>