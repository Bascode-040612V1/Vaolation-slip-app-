<?php
require_once '../config/database.php';

// Validate request method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Only POST method is allowed");
}

// Get and validate input
$data = json_decode(file_get_contents("php://input"));

if (!$data || !isset($data->username) || !isset($data->email) || !isset($data->password)) {
    sendResponse(false, "Username, email and password are required");
}

// Validate input format
$username = validateInput($data->username, 'string', 50);
$email = validateInput($data->email, 'email');
$password = validateInput($data->password, 'string', 128);
$role = validateInput($data->role ?? 'guard', 'string', 20);
$rfid = isset($data->rfid) ? validateInput($data->rfid, 'string', 50) : null;

if (!$username || !$email || !$password) {
    sendResponse(false, "Invalid input format");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    // Check if user already exists
    $query = "SELECT id FROM users WHERE email = :email OR username = :username";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":email", $email);
    $stmt->bindParam(":username", $username);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        sendResponse(false, "User with this email or username already exists");
    }

    // Hash password securely
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);

    // Insert new user with RFID
    $query = "INSERT INTO users (username, email, password, role, rfid) VALUES (:username, :email, :password, :role, :rfid)";
    $stmt = $conn->prepare($query);
    
    $stmt->bindParam(":username", $username);
    $stmt->bindParam(":email", $email);
    $stmt->bindParam(":password", $hashed_password);
    $stmt->bindParam(":role", $role);
    $stmt->bindParam(":rfid", $rfid);

    if ($stmt->execute()) {
        $user_id = $conn->lastInsertId();
        
        // Store RFID in admins table in rfid_system database if RFID is provided
        if ($rfid) {
            $rfidConn = $database->getRfidConnection();
            if ($rfidConn) {
                try {
                    // Insert into admins table in rfid_system database
                    $adminQuery = "INSERT INTO admins (username, rfid, password) VALUES (:username, :rfid, :password)";
                    $adminStmt = $rfidConn->prepare($adminQuery);
                    $adminStmt->bindParam(":username", $username);
                    $adminStmt->bindParam(":rfid", $rfid);
                    $adminStmt->bindParam(":password", $hashed_password);
                    $adminStmt->execute();
                    
                    // Mark RFID as used in rfid_admin_scans
                    $updateRfidQuery = "UPDATE rfid_admin_scans SET is_used = 1, admin_username = :username WHERE rfid_number = :rfid AND is_used = 0";
                    $updateRfidStmt = $rfidConn->prepare($updateRfidQuery);
                    $updateRfidStmt->bindParam(":username", $username);
                    $updateRfidStmt->bindParam(":rfid", $rfid);
                    $updateRfidStmt->execute();
                } catch(PDOException $rfid_exception) {
                    error_log("RFID registration error: " . $rfid_exception->getMessage());
                }
            }
        }
        
        $user_data = array(
            "id" => $user_id,
            "username" => $username,
            "email" => $email,
            "role" => $role,
            "rfid" => $rfid
        );

        sendResponse(true, "Registration successful", $user_data);
    } else {
        sendResponse(false, "Registration failed");
    }

} catch(PDOException $exception) {
    error_log("Registration error: " . $exception->getMessage());
    sendResponse(false, "Registration failed");
}
?>