<?php
class Database {
    private $host = "localhost";
    private $port = "3306";
    private $violation_db_name = "student_violation_db";
    private $rfid_db_name = "rfid_system";
    private $username = "root";
    private $password = "";
    
    private $violation_connection;
    private $rfid_connection;
    
    public function getViolationConnection() {
        try {
            if ($this->violation_connection == null) {
                $dsn = "mysql:host=" . $this->host . ";port=" . $this->port . ";dbname=" . $this->violation_db_name;
                $this->violation_connection = new PDO($dsn, $this->username, $this->password);
                $this->violation_connection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            }
            return $this->violation_connection;
        } catch(PDOException $exception) {
            error_log("Violation DB Connection Error: " . $exception->getMessage());
            return null;
        }
    }
    
    public function getRfidConnection() {
        try {
            if ($this->rfid_connection == null) {
                $dsn = "mysql:host=" . $this->host . ";port=" . $this->port . ";dbname=" . $this->rfid_db_name;
                $this->rfid_connection = new PDO($dsn, $this->username, $this->password);
                $this->rfid_connection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            }
            return $this->rfid_connection;
        } catch(PDOException $exception) {
            error_log("RFID DB Connection Error: " . $exception->getMessage());
            return null;
        }
    }
}

// Common response function with optimization
function sendResponse($success, $message, $data = null) {
    // Enable gzip compression
    if (extension_loaded('zlib') && !ob_get_level()) {
        ob_start('ob_gzhandler');
    }
    
    header('Content-Type: application/json; charset=utf-8');
    header('X-Content-Type-Options: nosniff');
    
    $response = array(
        'success' => $success,
        'message' => $message
    );
    
    if ($data !== null) {
        $response['data'] = $data;
    }
    
    echo json_encode($response, JSON_UNESCAPED_UNICODE | JSON_NUMERIC_CHECK);
    exit();
}
?>