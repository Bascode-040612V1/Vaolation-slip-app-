<?php
require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Only POST method is allowed");
}

$data = json_decode(file_get_contents("php://input"));

if (!$data || !isset($data->email) || !isset($data->password)) {
    sendResponse(false, "Email and password are required");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    $query = "SELECT * FROM users WHERE email = :email";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":email", $data->email);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // In production, use password_verify() for hashed passwords
        if ($user['password'] === $data->password) {
            unset($user['password']); // Don't send password back
            sendResponse(true, "Login successful", $user);
        } else {
            sendResponse(false, "Invalid credentials");
        }
    } else {
        sendResponse(false, "User not found");
    }
    
} catch(PDOException $exception) {
    sendResponse(false, "Login failed: " . $exception->getMessage());
}
?>