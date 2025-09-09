<?php
require_once '../config/database.php';

// Validate request method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, "Only POST method is allowed");
}

// Get and validate input
$data = json_decode(file_get_contents("php://input"));

if (!$data || !isset($data->email) || !isset($data->password)) {
    sendResponse(false, "Email and password are required");
}

// Validate input format
$email = validateInput($data->email, 'email');
$password = validateInput($data->password, 'string', 128);

if (!$email || !$password) {
    sendResponse(false, "Invalid email or password format");
}

$database = new Database();
$conn = $database->getViolationConnection();

if (!$conn) {
    sendResponse(false, "Database connection failed");
}

try {
    $query = "SELECT * FROM users WHERE email = :email";
    $stmt = $conn->prepare($query);
    $stmt->bindParam(":email", $email);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Secure password verification with backward compatibility
        $password_valid = false;
        
        // Check if password is already hashed (starts with $2y$)
        if (password_get_info($user['password'])['algo'] !== null) {
            // Password is hashed, use password_verify
            $password_valid = password_verify($password, $user['password']);
        } else {
            // Legacy plaintext password, verify and upgrade
            if ($user['password'] === $password) {
                $password_valid = true;
                
                // Upgrade to hashed password
                $hashed_password = password_hash($password, PASSWORD_DEFAULT);
                $update_query = "UPDATE users SET password = :password WHERE id = :id";
                $update_stmt = $conn->prepare($update_query);
                $update_stmt->bindParam(":password", $hashed_password);
                $update_stmt->bindParam(":id", $user['id']);
                $update_stmt->execute();
            }
        }
        
        if ($password_valid) {
            unset($user['password']); // Don't send password back
            sendResponse(true, "Login successful", $user);
        } else {
            sendResponse(false, "Invalid credentials");
        }
    } else {
        sendResponse(false, "User not found");
    }
    
} catch(PDOException $exception) {
    error_log("Login error: " . $exception->getMessage());
    sendResponse(false, "Login failed");
}
?>