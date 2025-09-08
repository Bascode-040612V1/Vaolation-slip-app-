<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Only POST method is allowed");
}

$data = json_decode(file_get_contents("php://input"));

if (!$data || !isset($data->username) || !isset($data->email) || !isset($data->password)) {
    sendResponse(false, "Username, email and password are required");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    // Check if user already exists in violations database
    $query = "SELECT id FROM users WHERE email = :email OR username = :username";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":email", $data->email);
    $stmt->bindParam(":username", $data->username);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        sendResponse(false, "User with this email or username already exists");
    }

    // Insert new user into violations database
    $query = "INSERT INTO users (username, email, password, role) VALUES (:username, :email, :password, :role)";
    $stmt = $conn->prepare($query);
    
    $role = isset($data->role) ? $data->role : 'guard';
    // In production, use password_hash() to hash passwords
    $password = $data->password;

    $stmt->bindParam(":username", $data->username);
    $stmt->bindParam(":email", $data->email);
    $stmt->bindParam(":password", $password);
    $stmt->bindParam(":role", $role);

    if ($stmt->execute()) {
        $user_id = $conn->lastInsertId();
        
        // Also add to RFID system admins table
        $rfid_conn = $database->getRfidConnection();
        $rfid_success = false;
        $rfid_message = "";
        
        if ($rfid_conn) {
            try {
                // Check if username already exists in RFID system
                $rfid_check_query = "SELECT id FROM admins WHERE username = :username";
                $rfid_check_stmt = $rfid_conn->prepare($rfid_check_query);
                $rfid_check_stmt->bindParam(":username", $data->username);
                $rfid_check_stmt->execute();
                
                if ($rfid_check_stmt->rowCount() == 0) {
                    // Insert into RFID system admins table
                    $rfid_query = "INSERT INTO admins (username, password) VALUES (:username, :password)";
                    $rfid_stmt = $rfid_conn->prepare($rfid_query);
                    $rfid_stmt->bindParam(":username", $data->username);
                    $rfid_stmt->bindParam(":password", $password);
                    
                    if ($rfid_stmt->execute()) {
                        $rfid_success = true;
                        $rfid_message = "User also added to RFID system";
                    } else {
                        $rfid_message = "RFID system registration failed";
                    }
                } else {
                    $rfid_message = "Username already exists in RFID system";
                }
            } catch(PDOException $rfid_exception) {
                // Log RFID error but don't fail the main registration
                error_log("RFID system registration error: " . $rfid_exception->getMessage());
                $rfid_message = "RFID system error: " . $rfid_exception->getMessage();
            }
        } else {
            $rfid_message = "RFID database connection failed";
        }
        
        $user_data = array(
            "id" => $user_id,
            "username" => $data->username,
            "email" => $data->email,
            "role" => $role,
            "rfid_admin_created" => $rfid_success,
            "rfid_message" => $rfid_message
        );

        $message = "Registration successful in violations system";
        if ($rfid_success) {
            $message .= " and RFID system";
        } else {
            $message .= " (RFID system: " . $rfid_message . ")";
        }

        sendResponse(true, $message, $user_data);
    } else {
        sendResponse(false, "Registration failed");
    }

} catch(PDOException $exception) {
    sendResponse(false, "Registration failed: " . $exception->getMessage());
}
?>