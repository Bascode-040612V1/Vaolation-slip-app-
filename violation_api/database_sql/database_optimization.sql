-- Database optimization script for Violation App
-- Add indexes for better query performance

-- Violation Types optimizations
ALTER TABLE violation_types 
ADD INDEX idx_active_category (is_active, category),
ADD INDEX idx_violation_name (violation_name),
ADD INDEX idx_updated_at (updated_at);

-- Students table optimizations  
ALTER TABLE students
ADD INDEX idx_student_id (student_id),
ADD INDEX idx_student_name (student_name),
ADD INDEX idx_course_year (course, year_level),
ADD INDEX idx_updated_at (updated_at);

-- Violation offense counts optimizations
ALTER TABLE student_violation_offense_counts
ADD INDEX idx_student_violation (student_id, violation_type),
ADD INDEX idx_student_id (student_id),
ADD INDEX idx_violation_type (violation_type),
ADD INDEX idx_offense_count (offense_count);

-- Violations table optimizations
ALTER TABLE violations
ADD INDEX idx_student_id (student_id),
ADD INDEX idx_recorded_by (recorded_by),
ADD INDEX idx_created_at (created_at),
ADD INDEX idx_offense_count (offense_count);

-- Violation details optimizations
ALTER TABLE violation_details
ADD INDEX idx_violation_id (violation_id),
ADD INDEX idx_violation_type (violation_type);

-- Penalty matrix optimizations
ALTER TABLE penalty_matrix
ADD INDEX idx_violation_offense (violation_type, offense_count),
ADD INDEX idx_violation_type (violation_type);

-- Users table optimizations
ALTER TABLE users
ADD INDEX idx_email (email),
ADD INDEX idx_username (username);

-- Add database statistics for query optimization
ANALYZE TABLE violation_types;
ANALYZE TABLE students;
ANALYZE TABLE student_violation_offense_counts;
ANALYZE TABLE violations;
ANALYZE TABLE violation_details;
ANALYZE TABLE penalty_matrix;
ANALYZE TABLE users;

-- Create summary views for faster reporting
CREATE OR REPLACE VIEW student_violation_summary AS
SELECT 
    s.student_id,
    s.student_name,
    s.course,
    s.year_level,
    s.section,
    COUNT(v.id) as total_violations,
    MAX(v.created_at) as last_violation_date,
    MAX(v.offense_count) as highest_offense
FROM students s
LEFT JOIN violations v ON s.student_id = v.student_id
GROUP BY s.student_id, s.student_name, s.course, s.year_level, s.section;

-- Create violation type usage view
CREATE OR REPLACE VIEW violation_type_usage AS
SELECT 
    vt.violation_name,
    vt.category,
    COUNT(vd.id) as usage_count,
    AVG(v.offense_count) as avg_offense_level
FROM violation_types vt
LEFT JOIN violation_details vd ON vt.violation_name = vd.violation_type
LEFT JOIN violations v ON vd.violation_id = v.id
WHERE vt.is_active = 1
GROUP BY vt.violation_name, vt.category
ORDER BY usage_count DESC;

-- Optimize MySQL configuration recommendations
-- Add these to my.cnf for better performance:
/*
[mysqld]
# Buffer pool size (adjust based on RAM)
innodb_buffer_pool_size = 1G

# Query cache
query_cache_type = 1
query_cache_size = 64M

# Connection optimizations
max_connections = 100
wait_timeout = 600
interactive_timeout = 600

# InnoDB optimizations
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M
innodb_flush_log_at_trx_commit = 2

# Table cache
table_open_cache = 2000
table_definition_cache = 1400
*/