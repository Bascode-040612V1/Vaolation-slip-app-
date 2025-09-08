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

// Common response function
function sendResponse($success, $message, $data = null) {
    header('Content-Type: application/json');
    echo json_encode(array(
        'success' => $success,
        'message' => $message,
        'data' => $data
    ));
    exit();
}
?>