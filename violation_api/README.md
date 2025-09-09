# Violation API - Consolidated Production Backend

## 📁 Installation

1. **Copy to XAMPP**: Copy the entire `violation_api` folder to your XAMPP `htdocs` directory:
   ```
   C:\xampp\htdocs\violation_api\
   ```

2. **Database Setup**:
   - Import the database schemas from `database_sql/`:
     - `student_violation_db.sql` - Main violation database
     - `rfid_system.sql` - Student information database
     - `penalty_matrix_data.sql` - Penalty rules
     - `database_optimization.sql` - Performance optimizations

3. **Environment Configuration**: 
   - Copy `.env.example` to `.env` in the `config` folder
   - Edit `.env` with your database credentials:
     ```
     DB_HOST=localhost
     DB_PORT=3306
     DB_USERNAME=root
     DB_PASSWORD=your_password_here
     FORCE_HTTPS=true  # Set to true for production
     ```

4. **Test Connection**: Visit `http://localhost/violation_api/test/connection.php` to verify setup

## 📊 **Complete Database Setup**

### Step-by-step Database Installation:

1. **Start XAMPP** (Apache + MySQL)
2. **Open phpMyAdmin** (`http://localhost/phpmyadmin`)
3. **Create Databases**:
   - Create database: `student_violation_db`
   - Create database: `rfid_system`
4. **Import SQL Files** (in this order):
   ```sql
   -- 1. Import main schemas
   SOURCE violation_api/database_sql/student_violation_db.sql;
   SOURCE violation_api/database_sql/rfid_system.sql;
   
   -- 2. Add penalty data
   SOURCE violation_api/database_sql/penalty_matrix_data.sql;
   
   -- 3. Apply performance optimizations
   SOURCE violation_api/database_sql/database_optimization.sql;
   ```

## 🔒 Security Features

- ✅ **Password Hashing**: All passwords use `password_hash()` and `password_verify()`
- ✅ **Environment Variables**: Database credentials are externalized
- ✅ **HTTPS Enforcement**: Configurable HTTPS redirection
- ✅ **Input Validation**: All inputs are sanitized and validated
- ✅ **Security Headers**: X-Frame-Options, CSP, etc.
- ✅ **Error Logging**: Secure error handling without information disclosure

## 📋 API Endpoints

### Authentication
- `POST /auth/login.php` - User login with secure password verification
- `POST /auth/register.php` - User registration with password hashing

### Students
- `GET /students/search.php?student_id=ID` - Search student by ID

### Violations
- `GET /violations/types.php` - Get all violation types (cached)
- `POST /violations/submit.php` - Submit student violations
- `GET /violations/student.php?student_id=ID` - Get student violation history
- `GET /violations/offense_counts.php?student_id=ID` - Get student offense counts

### System
- `GET /test/connection.php` - Test database connections

## 🚀 Production Deployment

1. **HTTPS Setup**: Set `FORCE_HTTPS=true` in `.env`
2. **Error Reporting**: Set `SHOW_ERRORS=false` for production
3. **File Permissions**: Ensure proper file permissions on the server
4. **Database Security**: Use strong database passwords
5. **Regular Updates**: Keep PHP and dependencies updated

## 📊 Performance Features

- **Response Compression**: Automatic GZip compression
- **Caching Headers**: Proper cache control for static data
- **Optimized Queries**: Prepared statements with proper indexing
- **Error Logging**: Comprehensive logging without security leaks

## 🔧 Maintenance

- Monitor error logs regularly
- Update database credentials in `.env` only
- Test API endpoints after any changes
- Backup database before major updates