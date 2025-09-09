<?php
// Response optimization and compression utilities

class ResponseOptimizer {
    
    public static function enableCompression() {
        // Enable gzip compression if supported
        if (extension_loaded('zlib') && !ob_get_level()) {
            ob_start('ob_gzhandler');
        }
        
        // Set compression headers
        header('Content-Encoding: gzip');
        header('Vary: Accept-Encoding');
    }
    
    public static function setOptimalHeaders($cacheTime = 3600, $etag = null) {
        // Security headers
        header('X-Content-Type-Options: nosniff');
        header('X-Frame-Options: DENY');
        header('X-XSS-Protection: 1; mode=block');
        
        // Performance headers
        header('Connection: keep-alive');
        
        // Cache headers
        if ($cacheTime > 0) {
            header("Cache-Control: max-age=$cacheTime, must-revalidate");
            header('Expires: ' . gmdate('D, d M Y H:i:s', time() + $cacheTime) . ' GMT');
        }
        
        if ($etag) {
            header("ETag: \"$etag\"");
        }
    }
    
    public static function compressData($data) {
        return gzcompress(json_encode($data), 6);
    }
    
    public static function checkClientCache($etag) {
        $clientETag = $_SERVER['HTTP_IF_NONE_MATCH'] ?? '';
        return $clientETag === "\"$etag\"";
    }
    
    public static function send304NotModified($etag, $cacheTime = 3600) {
        http_response_code(304);
        self::setOptimalHeaders($cacheTime, $etag);
        exit();
    }
    
    public static function optimizeViolationTypes($violationTypes) {
        // Minimize field names and remove unnecessary data
        return array_map(function($vt) {
            return [
                'i' => (int)$vt['id'],                    // id
                'n' => $vt['violation_name'],             // name
                'c' => $vt['category'],                   // category  
                'p' => $vt['penalty_description'] ?? ''   // penalty
            ];
        }, $violationTypes);
    }
    
    public static function optimizeStudent($student) {
        return [
            'i' => $student['student_id'],          // id
            'n' => $student['student_name'],        // name
            'y' => $student['year_level'],          // year
            'c' => $student['course'],              // course
            's' => $student['section'],             // section
            'img' => $student['image'] ?? null      // image
        ];
    }
    
    public static function optimizeOffenseCounts($offenseCounts) {
        // Use shorter keys to reduce payload size
        $optimized = [];
        foreach ($offenseCounts as $violation => $count) {
            // Use hash of violation name as key to reduce size
            $key = substr(md5($violation), 0, 8);
            $optimized[$key] = $count;
        }
        return $optimized;
    }
    
    public static function createMinimalResponse($success, $message, $data = null, $metadata = []) {
        $response = [
            's' => $success,                // success
            'm' => $message                 // message
        ];
        
        if ($data !== null) {
            $response['d'] = $data;         // data
        }
        
        if (!empty($metadata)) {
            $response['meta'] = $metadata;  // metadata
        }
        
        return $response;
    }
    
    public static function sendOptimizedResponse($success, $message, $data = null, $cacheTime = 0, $etag = null) {
        // Enable compression
        self::enableCompression();
        
        // Create minimal response
        $response = self::createMinimalResponse($success, $message, $data);
        
        // Generate ETag if not provided
        if (!$etag && $data) {
            $etag = md5(json_encode($response));
        }
        
        // Check if client has cached version
        if ($etag && self::checkClientCache($etag)) {
            self::send304NotModified($etag, $cacheTime);
        }
        
        // Set optimal headers
        self::setOptimalHeaders($cacheTime, $etag);
        
        // Send JSON response
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_NUMERIC_CHECK);
        exit();
    }
}

// Utility function for backwards compatibility
function sendOptimizedResponse($success, $message, $data = null, $cacheTime = 0) {
    ResponseOptimizer::sendOptimizedResponse($success, $message, $data, $cacheTime);
}
?>