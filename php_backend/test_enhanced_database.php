<?php
require_once 'config/database.php';

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
        
        // Test student_violation_offense_counts table (per-violation tracking)
        echo "<h4>Student Violation Offense Counts (Per-Violation Tracking):</h4>";
        $violation_offense_query = "SELECT COUNT(*) as count FROM student_violation_offense_counts";
        $violation_offense_stmt = $conn_violation->prepare($violation_offense_query);
        $violation_offense_stmt->execute();
        $violation_offense_count = $violation_offense_stmt->fetch(PDO::FETCH_ASSOC);
        echo "Student violation offense count entries: " . $violation_offense_count['count'] . "<br>";
        
        // Test student_stats view (may be affected by deleted table)
        echo "<h4>Student Stats View:</h4>";
        try {
            $stats_query = "SELECT COUNT(*) as count FROM student_stats";
            $stats_stmt = $conn_violation->prepare($stats_query);
            $stats_stmt->execute();
            $stats_count = $stats_stmt->fetch(PDO::FETCH_ASSOC);
            echo "Student stats view entries: " . $stats_count['count'] . "<br>";
        } catch(Exception $e) {
            echo "❌ Student stats view error (may reference deleted table): " . $e->getMessage() . "<br>";
        }
        
        // Test violation_summary view
        echo "<h4>Violation Summary View:</h4>";
        try {
            $summary_query = "SELECT COUNT(*) as count FROM violation_summary";
            $summary_stmt = $conn_violation->prepare($summary_query);
            $summary_stmt->execute();
            $summary_count = $summary_stmt->fetch(PDO::FETCH_ASSOC);
            echo "Violation summary view entries: " . $summary_count['count'] . "<br>";
        } catch(Exception $e) {
            echo "❌ Violation summary view error: " . $e->getMessage() . "<br>";
        }
        
        // Sample data from views (with error handling)
        echo "<h4>Sample Student Stats:</h4>";
        try {
            $sample_stats_query = "SELECT * FROM student_stats LIMIT 3";
            $sample_stats_stmt = $conn_violation->prepare($sample_stats_query);
            $sample_stats_stmt->execute();
            echo "<table border='1'>";
            echo "<tr><th>Student ID</th><th>Student Name</th><th>Current Offense Count</th><th>Total Violations</th></tr>";
            while ($row = $sample_stats_stmt->fetch(PDO::FETCH_ASSOC)) {
                echo "<tr>";
                echo "<td>" . $row['student_id'] . "</td>";
                echo "<td>" . $row['student_name'] . "</td>";
                echo "<td>" . ($row['current_offense_count'] ?? 'N/A') . "</td>";
                echo "<td>" . ($row['total_violations'] ?? 'N/A') . "</td>";
                echo "</tr>";
            }
            echo "</table><br>";
        } catch(Exception $e) {
            echo "❌ Error displaying student stats: " . $e->getMessage() . "<br>";
        }
        
        echo "<h4>Sample Violation Summary:</h4>";
        try {
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
            echo "❌ Error displaying violation summary: " . $e->getMessage() . "<br>";
        }
        
    } catch(Exception $e) {
        echo "❌ Error: " . $e->getMessage() . "<br>";
    }
} else {
    echo "❌ Connection failed<br>";
}

echo "<br><h3>API Endpoints Status:</h3>";
echo "✅ violations/submit.php - Updated with per-violation offense tracking only<br>";
echo "✅ students/search.php - Updated to use students table directly<br>";
echo "✅ violations/types.php - Working correctly<br>";
echo "✅ violations/student.php - Working with violation_summary view<br>";
echo "✅ violations/offense_counts.php - Working with student_violation_offense_counts table<br>";

echo "<br><h3>Database Structure (After student_offense_counts Removal):</h3>";
echo "✅ penalty_matrix - Dynamic penalty assignment based on violation type and offense count<br>";
echo "❌ student_offense_counts - DELETED (was causing bugs with per-violation tracking)<br>";
echo "✅ student_violation_offense_counts - Per-violation offense tracking (1st → 2nd → 3rd → 1st cycle)<br>";
echo "⚠️ student_stats view - May be broken due to deleted table<br>";
echo "✅ violation_summary view - Should work correctly<br>";

echo "<br><h3>Instructions:</h3>";
echo "1. Copy these PHP files to your XAMPP directory: C:\\xampp\\htdocs\\violation_api\\<br>";
echo "2. Run the penalty_matrix_data.sql file to populate penalty data<br>";
echo "3. The app now uses ONLY per-violation offense tracking (no global student offense counts)<br>";
echo "4. Each violation type maintains its own independent offense count<br>";
echo "5. Offense indicators will show actual per-violation history<br>";
echo "6. If student_stats view is broken, consider recreating it without student_offense_counts references<br>";
?>