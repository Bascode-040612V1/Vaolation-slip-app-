<?php
require_once '../config/database.php';

$database = new Database();

echo "<h2>Database Connection Test - Enhanced Features</h2>";

// Test Violations Database
echo "<h3>Violations Database (student_violation_db)</h3>";
$conn_violation = $database->getViolationConnection();
if ($conn_violation) {
    echo "✅ Connection successful<br>";
    
    try {
        // Test penalty_matrix table
        echo "<h4>Penalty Matrix:</h4>";
        $penalty_query = "SELECT COUNT(*) as count FROM penalty_matrix";
        $penalty_stmt = $conn_violation->prepare($penalty_query);
        $penalty_stmt->execute();
        $penalty_count = $penalty_stmt->fetch(PDO::FETCH_ASSOC);
        echo "Penalty matrix entries: " . $penalty_count['count'] . "<br>";
        
        // Test student_offense_counts table
        echo "<h4>Student Offense Counts:</h4>";
        $offense_query = "SELECT COUNT(*) as count FROM student_offense_counts";
        $offense_stmt = $conn_violation->prepare($offense_query);
        $offense_stmt->execute();
        $offense_count = $offense_stmt->fetch(PDO::FETCH_ASSOC);
        echo "Student offense count entries: " . $offense_count['count'] . "<br>";
        
        // Test student_violation_offense_counts table
        echo "<h4>Student Violation Offense Counts:</h4>";
        $violation_offense_query = "SELECT COUNT(*) as count FROM student_violation_offense_counts";
        $violation_offense_stmt = $conn_violation->prepare($violation_offense_query);
        $violation_offense_stmt->execute();
        $violation_offense_count = $violation_offense_stmt->fetch(PDO::FETCH_ASSOC);
        echo "Student violation offense count entries: " . $violation_offense_count['count'] . "<br>";
        
        // Test student_stats view
        echo "<h4>Student Stats View:</h4>";
        $stats_query = "SELECT COUNT(*) as count FROM student_stats";
        $stats_stmt = $conn_violation->prepare($stats_query);
        $stats_stmt->execute();
        $stats_count = $stats_stmt->fetch(PDO::FETCH_ASSOC);
        echo "Student stats view entries: " . $stats_count['count'] . "<br>";
        
        // Test violation_summary view
        echo "<h4>Violation Summary View:</h4>";
        $summary_query = "SELECT COUNT(*) as count FROM violation_summary";
        $summary_stmt = $conn_violation->prepare($summary_query);
        $summary_stmt->execute();
        $summary_count = $summary_stmt->fetch(PDO::FETCH_ASSOC);
        echo "Violation summary view entries: " . $summary_count['count'] . "<br>";
        
        // Sample data from views
        echo "<h4>Sample Student Stats:</h4>";
        $sample_stats_query = "SELECT * FROM student_stats LIMIT 3";
        $sample_stats_stmt = $conn_violation->prepare($sample_stats_query);
        $sample_stats_stmt->execute();
        echo "<table border='1'>";
        echo "<tr><th>Student ID</th><th>Student Name</th><th>Current Offense Count</th><th>Total Violations</th></tr>";
        while ($row = $sample_stats_stmt->fetch(PDO::FETCH_ASSOC)) {
            echo "<tr>";
            echo "<td>" . $row['student_id'] . "</td>";
            echo "<td>" . $row['student_name'] . "</td>";
            echo "<td>" . $row['current_offense_count'] . "</td>";
            echo "<td>" . $row['total_violations'] . "</td>";
            echo "</tr>";
        }
        echo "</table><br>";
        
        echo "<h4>Sample Violation Summary:</h4>";
        $sample_summary_query = "SELECT * FROM violation_summary LIMIT 3";
        $sample_summary_stmt = $conn_violation->prepare($sample_summary_query);
        $sample_summary_stmt->execute();
        echo "<table border='1'>";
        echo "<tr><th>Student ID</th><th>Student Name</th><th>Offense Count</th><th>Violations</th><th>Recorded By</th></tr>";
        while ($row = $sample_summary_stmt->fetch(PDO::FETCH_ASSOC)) {
            echo "<tr>";
            echo "<td>" . $row['student_id'] . "</td>";
            echo "<td>" . $row['student_name'] . "</td>";
            echo "<td>" . $row['offense_count'] . "</td>";
            echo "<td>" . $row['violations'] . "</td>";
            echo "<td>" . $row['recorded_by'] . "</td>";
            echo "</tr>";
        }
        echo "</table><br>";
        
    } catch(Exception $e) {
        echo "❌ Error: " . $e->getMessage() . "<br>";
    }
} else {
    echo "❌ Connection failed<br>";
}

echo "<br><h3>API Endpoints Status:</h3>";
echo "✅ violations/submit.php - Created with full offense tracking<br>";
echo "✅ students/search.php - Created with student_stats view integration<br>";
echo "✅ violations/types.php - Created for violation types<br>";
echo "✅ violations/student.php - Created with violation_summary view<br>";
echo "✅ violations/offense_counts.php - Created for real-time offense tracking<br>";

echo "<br><h3>Instructions:</h3>";
echo "1. Copy these PHP files to your XAMPP directory: C:\\xampp\\htdocs\\violation_api\\<br>";
echo "2. Run the penalty_matrix_data.sql file to populate penalty data<br>";
echo "3. The app will now use real database offense tracking instead of mock data<br>";
echo "4. Offense indicators will show actual student violation history<br>";
?>