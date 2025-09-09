# Student Violation Manager 📱

A comprehensive mobile application for managing student disciplinary records in educational institutions. Built with modern Android development practices and a secure PHP backend.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![PHP](https://img.shields.io/badge/php-%23777BB4.svg?style=for-the-badge&logo=php&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)

## 🌟 Features

### 📱 Android App
- **User Authentication** - Secure login for guards, teachers, and administrators
- **Student Search** - Quick student lookup with violation history
- **Violation Recording** - Log multiple violation types with automatic penalty calculation
- **Offense Tracking** - Progressive penalty system (1st → 2nd → 3rd offense per violation type)
- **Profile Pictures** - Upload and manage student profile images stored in RFID database
- **Real-time Sync** - Immediate backend synchronization
- **Configurable Settings** - Custom server configuration with connection testing
- **Modern UI** - Built with Jetpack Compose for responsive design

### 🔧 Backend API
- **RESTful Endpoints** - Clean API structure for all operations
- **Secure Authentication** - Password hashing with backward compatibility
- **Input Validation** - Comprehensive data sanitization and SQL injection protection
- **Dynamic Penalties** - Automatic penalty assignment based on violation type and offense count
- **Image Upload** - Profile picture management with file validation
- **Dual Database Support** - Integration with violation and RFID systems
- **Transaction Safety** - Rollback protection for data integrity

### 🗄️ Database Features
- **Student Management** - Complete student records with course information and profile images
- **Violation Types** - Categorized violations (Dress Code, Minor, Major, Conduct)
- **Penalty Matrix** - Configurable penalties based on violation severity and repeat offenses
- **Offense Tracking** - Per-violation type offense counting with cycling (1→2→3→1)
- **Reporting Views** - Student statistics and violation summaries

## 🏗️ Architecture

### Frontend (Android)
```
app/
├── ui/                     # Jetpack Compose UI components
│   ├── screens/           # Login, Home, Settings, Student Violation
│   ├── theme/             # Material Design theme
│   └── viewmodel/         # MVVM ViewModels
├── data/                  # Data layer
│   ├── api/              # Retrofit API service
│   ├── model/            # Data models
│   ├── repository/       # Repository pattern
│   └── network/          # Network configuration
├── navigation/           # Navigation component
└── utils/               # Utilities and preferences
```

### Backend (PHP)
```
violation_api/
├── auth/                 # Authentication endpoints
├── students/            # Student management & image upload
├── violations/          # Violation CRUD operations
├── config/             # Database configuration
├── test/               # Connection testing
└── uploads/            # Profile image storage
    └── profile_images/ # Student profile pictures
```

## 🚀 Quick Start

### Prerequisites
- **Android Development**: Android Studio, JDK 11+
- **Backend**: XAMPP (Apache + MySQL + PHP)
- **Database**: MySQL 5.7+

### 1. Clone Repository
```bash
git clone https://github.com/Bascode-040612V1/Vaolation-slip-app-
cd Vaolation-slip-app-update
```

### 2. Android App Setup
```bash
# Open in Android Studio
# Gradle will automatically sync dependencies

# Or build via command line
./gradlew assembleDebug
```

### 3. Backend Setup
```bash
# Copy backend to XAMPP
cp -r violation_api/ C:\xampp\htdocs\

# Start XAMPP services
# Import database schema from violation_api/database_sql/
# Run setup scripts (see Backend Setup section)
```

### 4. Database Setup
```sql
-- Import the main schema
SOURCE violation_api/database_sql/student_violation_db.sql;

-- Fix database views
SOURCE fix_database_views.sql;

-- Populate penalty matrix
SOURCE penalty_matrix_data.sql;
```

## 📋 Detailed Setup Guide

### Android App Configuration

1. **Open Project**
   ```bash
   # Open the project root in Android Studio
   # Gradle sync will happen automatically
   ```

2. **Configure Server**
   - Launch the app
   - Go to Settings → Server Configuration
   - Set your server IP address and port (default: 192.168.1.1:8080)
   - Test connection using the built-in connection test
   - Save configuration

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug          # Build APK
   ./gradlew installDebug           # Install on device
   ```

### Backend Setup

1. **Copy Files**
   ```bash
   # Copy the violation_api directory to XAMPP
   violation_api/ → C:\xampp\htdocs\violation_api\
   ```

2. **Database Setup**
   - Start XAMPP (Apache + MySQL)
   - Open phpMyAdmin
   - Create database: `student_violation_db`
   - Create database: `rfid_system`
   - Import: `violation_api/database_sql/student_violation_db.sql`
   - Import: `violation_api/database_sql/rfid_system.sql`
   - Run: `fix_database_views.sql`
   - Run: `penalty_matrix_data.sql`

3. **Setup Image Uploads**
   - Visit: `http://localhost/violation_api/setup_uploads.php`
   - This creates the necessary directories and permissions for profile image uploads

4. **Test Connection**
   ```
   http://localhost/violation_api/test/connection.php
   ```

### Default Users
```
Username: guard1
Email: guard@violationsapp.com
Password: guard123

Username: teacher1  
Email: teacher@violationsapp.com
Password: teacher123
```

## 🔐 Security Features

### ✅ Implemented Security Measures
- **Password Hashing**: PHP `password_hash()` with auto-upgrade for legacy passwords
- **Input Validation**: Comprehensive sanitization and format validation
- **SQL Injection Protection**: Prepared statements throughout
- **File Upload Security**: Image type validation, size limits, secure file handling
- **Transaction Safety**: Database rollback on failures
- **Error Handling**: Secure error messages without system exposure

### 🚨 Security Recommendations
- Use HTTPS in production environments
- Change default passwords immediately
- Regularly update dependencies
- Monitor access logs
- Implement rate limiting for API endpoints
- Secure the uploads directory with proper permissions

## 📊 Database Schema

### Key Tables
- **`students`** (RFID DB) - Student information, enrollment details, and profile images
- **`users`** (Violation DB) - Staff authentication (guards, teachers, admins)
- **`violations`** (Violation DB) - Violation records with offense tracking
- **`violation_types`** (Violation DB) - Predefined violation categories
- **`penalty_matrix`** (Violation DB) - Dynamic penalty rules
- **`student_violation_offense_counts`** (Violation DB) - Per-violation offense tracking

### Views
- **`student_stats`** - Student overview with violation statistics
- **`violation_summary`** - Grouped violation history

## 🛠️ Development

### Tech Stack
- **Frontend**: Kotlin, Jetpack Compose, Retrofit, Navigation Component, Coil (Image Loading)
- **Backend**: PHP 8+, MySQL 5.7+
- **Architecture**: MVVM, Repository Pattern, Clean Architecture
- **Build**: Gradle with Kotlin DSL, Version Catalog

### API Endpoints
```
POST /auth/login.php                    # User authentication
GET  /students/search.php               # Student lookup
POST /students/upload_image.php         # Profile image upload
GET  /violations/types.php              # Available violation types
POST /violations/submit.php             # Submit violations
GET  /violations/student.php            # Student violation history
GET  /violations/offense_counts.php     # Offense statistics
GET  /test/connection.php               # Connection testing
```

## 🧪 Testing

### Backend Testing
```bash
# Test database connection
curl http://localhost/violation_api/test/connection.php

# Test enhanced features
curl http://localhost/violation_api/test_enhanced_database.php

# Test image upload
# Use the Android app or Postman to test image upload functionality
```

### Android Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 🎯 Offense Logic System

### Per-Violation Tracking
Each violation type has its own independent offense count that cycles 1→2→3→1:
- **No ID**: 1st offense → 2nd offense → 3rd offense → 1st offense...
- **Wearing rubber slippers**: 1st offense → 2nd offense → 3rd offense → 1st offense...
- Each violation maintains its own count independently

### Highest Offense Logic
When multiple violations are selected, the system shows the **highest offense count** among all selected violations:

**Example:**
- Student selects "No ID" (currently 1st offense)
- Student selects "Wearing rubber slippers" (currently 2nd offense)
- **Result**: System shows "2nd Offense" message (highest among the two)

### UI Implementation
- **Horizontal Layout**: Offense indicators appear on the right side of violation cards
- **Badge Style**: Colored badges with rounded corners for better visibility
- **Color Coding**: 🟢 1st Offense (Green), 🟠 2nd Offense (Orange), 🔴 3rd Offense (Red)
- **Consistent Positioning**: Proper alignment with violation names

## 📸 Profile Picture Feature

### Upload Process
1. **Access**: Three dots menu in Settings → "Change Profile Picture"
2. **Selection**: Image picker opens for photo selection
3. **Validation**: Backend validates file type (JPG/PNG/GIF) and size (max 5MB)
4. **Storage**: Image saved to `violation_api/uploads/profile_images/`
5. **Database**: Filename stored in `rfid_system.students.image` column
6. **Display**: Circular profile picture replaces default person icon

### Technical Implementation
- **Frontend**: Coil library for image loading, ActivityResultContracts for file picking
- **Backend**: PHP file upload with validation and unique naming
- **Security**: File type validation, size limits, secure directory structure

## 🔧 Recent Fixes & Improvements

### ✅ Major Fixes Applied

1. **Fixed Gradle Version Catalog**
   - Added all missing dependencies to `gradle/libs.versions.toml`
   - Updated build files to use standardized dependency management

2. **Professional Package Structure**
   - Renamed from placeholder to professional package: `com.aics.violationapp`
   - Updated all files with new package declarations

3. **Enhanced Security**
   - Implemented password hashing with backward compatibility
   - Added comprehensive input validation and sanitization
   - Enhanced SQL injection protection

4. **Cleaned File Structure**
   - Consolidated all backend code into single `violation_api/` directory
   - Removed duplicate PHP directories
   - Created clear documentation structure

5. **Fixed Database Issues**
   - Repaired broken views with `fix_database_views.sql`
   - Simplified offense tracking to use only per-violation counts
   - Removed conflicting `student_offense_counts` table

6. **UI/UX Improvements**
   - Moved settings icon to upper-right header
   - Implemented horizontal badge layout for offense indicators
   - Added profile picture upload functionality
   - Enhanced connection testing in settings

### Database Architecture Improvements
- **Simplified Tracking**: Uses only `student_violation_offense_counts` for per-violation tracking
- **Eliminated Conflicts**: Removed dual tracking systems that caused bugs
- **Better Performance**: Cleaner logic flow and easier debugging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙋‍♂️ Support

### Common Issues

**Build Failures**
- Ensure JDK 11+ is installed
- Run `./gradlew clean` then `./gradlew build`
- Check internet connection for dependency downloads

**Connection Issues**
- Verify XAMPP is running (Apache + MySQL)
- Check firewall settings
- Use the built-in connection test in app settings
- Confirm correct IP address configuration

**Database Errors**
- Run `fix_database_views.sql` to repair broken views
- Ensure `penalty_matrix_data.sql` has been imported
- Check MySQL service status
- Verify both databases (`student_violation_db` and `rfid_system`) exist

**Image Upload Issues**
- Run `http://localhost/violation_api/setup_uploads.php` to create directories
- Check file permissions on uploads folder
- Verify file size is under 5MB and format is JPG/PNG/GIF

### Contact
For technical support or questions:
- Create an issue in the repository
- Check existing documentation
- Review the troubleshooting guides

---

**Made with ❤️ for educational institutions**

*Streamlining student conduct management with modern technology*

## 📚 Additional Resources

### File Structure Reference
```
violation_api/
├── config/
│   └── database.php                    ✅ Database connection configuration
├── auth/
│   ├── login.php                       ✅ User login endpoint
│   └── register.php                    ✅ User registration
├── students/
│   ├── search.php                      ✅ Student search
│   └── upload_image.php               ✅ Profile image upload
├── violations/
│   ├── types.php                       ✅ Get violation types
│   ├── submit.php                      ✅ Submit violations
│   ├── student.php                     ✅ Get student violations
│   └── offense_counts.php              ✅ Get offense counts
├── test/
│   └── connection.php                  ✅ Connection test
├── uploads/
│   └── profile_images/                 ✅ Student profile pictures
└── setup_uploads.php                   ✅ Directory setup utility
```

### Development Tips
- Use the connection test feature before submitting violations
- Profile images are stored with format: `profile_{studentId}_{timestamp}.{ext}`
- Offense indicators use horizontal badge layout for consistent UI
- Navigation includes proper error handling with try-catch blocks
- All file uploads include comprehensive validation and security measures

**This comprehensive documentation consolidates all project information into a single, maintainable resource.**
