# Violation API - Complete PHP Backend Setup

## File Structure for C:\xampp\htdocs\violation_api\

```
violation_api/
├── config/
│   └── database.php                    ✅ Database connection configuration
│
├── auth/
│   ├── login.php                       ✅ User login endpoint
│   └── register.php                    ✅ User registration (dual database)
│
├── students/
│   └── search.php                      ✅ Student search using student_stats view
│
├── violations/
│   ├── types.php                       ✅ Get violation types from database
│   ├── submit.php                      ✅ Submit violations with offense tracking
│   ├── student.php                     ✅ Get student violations using violation_summary view
│   └── offense_counts.php              ✅ Get real offense counts per violation type
│
├── test/
│   ├── connection.php                  ✅ Basic database connection test
│   └── test_databases.php             ✅ Dual database connectivity test
│
└── utils/
    └── response_helper.php             (Optional - functions included in database.php)
```

## Setup Instructions

### 1. Copy Files to XAMPP
Copy all the files from this project directory to `C:\xampp\htdocs\violation_api\` maintaining the directory structure:

```bash
# From: c:\Users\Acer\Desktop\01VIOLATIONAPPLIST\
# To: C:\xampp\htdocs\violation_api\

config/database.php              → C:\xampp\htdocs\violation_api\config\database.php
auth/login.php                   → C:\xampp\htdocs\violation_api\auth\login.php
auth/register.php                → C:\xampp\htdocs\violation_api\auth\register.php
students/search.php              → C:\xampp\htdocs\violation_api\students\search.php
violations/types.php             → C:\xampp\htdocs\violation_api\violations\types.php
violations/submit.php            → C:\xampp\htdocs\violation_api\violations\submit.php
violations/student.php           → C:\xampp\htdocs\violation_api\violations\student.php
violations/offense_counts.php    → C:\xampp\htdocs\violation_api\violations\offense_counts.php
test/connection.php              → C:\xampp\htdocs\violation_api\test\connection.php
test_enhanced_database.php       → C:\xampp\htdocs\violation_api\test_enhanced_database.php
```

### 2. Import Database Schema
Import the student_violation_db.sql file into phpMyAdmin or MySQL

### 3. Populate Penalty Matrix
Run the penalty_matrix_data.sql file to populate penalty data:
```sql
-- Execute this in phpMyAdmin for student_violation_db database
SOURCE penalty_matrix_data.sql;
```

### 4. Test Setup
1. Start XAMPP (Apache and MySQL)
2. Test basic connection: http://192.168.1.4:8080/violation_api/test/connection.php
3. Test enhanced database: http://192.168.1.4:8080/violation_api/test_enhanced_database.php

## API Endpoints

### Authentication
- **POST** `/auth/login.php` - User login
- **POST** `/auth/register.php` - User registration (dual database)

### Student Management
- **GET** `/students/search.php?student_id=XXX` - Search student with stats

### Violation Management
- **GET** `/violations/types.php` - Get all violation types
- **POST** `/violations/submit.php` - Submit violations with offense tracking
- **GET** `/violations/student.php?student_id=XXX` - Get student violation history
- **GET** `/violations/offense_counts.php?student_id=XXX` - Get offense counts per violation type

### Testing
- **GET** `/test/connection.php` - Basic connection test
- **GET** `/test_enhanced_database.php` - Complete database feature test

## Database Integration Features

✅ **penalty_matrix** - Dynamic penalty assignment based on violation type and offense count
✅ **student_offense_counts** - Overall student offense progression (1st → 2nd → 3rd → 1st cycle)
✅ **student_violation_offense_counts** - Specific violation type offense tracking per student
✅ **student_stats view** - Enhanced student search with offense statistics
✅ **violation_summary view** - Violation history with grouped violation types

## Android App Integration

The Android app will now:
1. Use real database offense counts instead of random numbers
2. Get dynamic penalties from penalty_matrix
3. Track offense progression properly
4. Display consistent offense indicators
5. Support full CRUD operations for violations

## Next Steps

1. Copy all files to XAMPP directory
2. Import database and penalty data
3. Test all endpoints
4. Configure Android app IP settings to point to your XAMPP server
5. Test violation submission and offense tracking

The offense indicator system will now work with real database data! 🎯