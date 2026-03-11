# Freezer Monitoring Microservices Backend

A production-grade Java microservices backend for real-time freezer monitoring with authentication, OTP verification, alert notifications, and data export capabilities.

## Architecture Overview

The system consists of 7 modules:

1. **common-lib**: Shared utilities, DTOs, exceptions, and JWT utilities
2. **eureka-server**: Service registry for service discovery (Port: 8761)
3. **gateway-service**: API Gateway with routing and JWT validation (Port: 8080)
4. **auth-service**: User registration, OTP verification, and JWT token generation (Port: 8081)
5. **freezer-service**: Freezer management and dashboard APIs (Port: 8082)
6. **alert-service**: Temperature monitoring and alert notifications (Port: 8083)
7. **export-service**: CSV and PDF data export (Port: 8084)

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud 2023.0.0** (Kilburn)
- **Spring Cloud Gateway** for API routing
- **Spring Cloud Netflix Eureka** for service discovery
- **Spring Data JPA** with MySQL
- **Spring Security** with JWT authentication
- **Maven** for build management

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- (Optional) Twilio account for SMS notifications
- (Optional) Gmail account for email notifications

## Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE freezer_monitoring;
```

2. Update database credentials in each service's `application.yml`:
   - `DB_USERNAME`: Your MySQL username
   - `DB_PASSWORD`: Your MySQL password

## Configuration

### Environment Variables

Set the following environment variables or update `application.yml` files:

- `JWT_SECRET`: Secret key for JWT token signing (minimum 32 characters)
- `DB_USERNAME`: MySQL username
- `DB_PASSWORD`: MySQL password
- `GMAIL_USERNAME`: Gmail address for sending emails
- `GMAIL_PASSWORD`: Gmail app password (not regular password)
- `TWILIO_ACCOUNT_SID`: Twilio account SID
- `TWILIO_AUTH_TOKEN`: Twilio auth token
- `TWILIO_PHONE_NUMBER`: Twilio phone number

### Gmail SMTP Setup

1. Enable 2-Step Verification on your Google account
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Use the app password in `GMAIL_PASSWORD`

## Building the Project

From the root directory:

```bash
mvn clean install
```

## Running the Services

Start services in the following order:

1. **Eureka Server**:
```bash
cd eureka-server
mvn spring-boot:run
```

2. **Auth Service**:
```bash
cd auth-service
mvn spring-boot:run
```

3. **Freezer Service**:
```bash
cd freezer-service
mvn spring-boot:run
```

4. **Alert Service**:
```bash
cd alert-service
mvn spring-boot:run
```

5. **Export Service**:
```bash
cd export-service
mvn spring-boot:run
```

6. **Gateway Service**:
```bash
cd gateway-service
mvn spring-boot:run
```

## API Endpoints

### Authentication Service (via Gateway: http://localhost:8080)

- `POST /auth/signup` - User registration
- `POST /auth/verify-otp` - OTP verification
- `POST /auth/resend-otp` - Resend OTP
- `POST /auth/login` - User login (returns JWT token)

### Freezer Service (via Gateway: http://localhost:8080)

All endpoints require JWT authentication (Bearer token in Authorization header):

- `GET /freezers` - List all freezers for logged-in user
- `GET /freezers/summary` - Get dashboard summary
- `GET /freezers/{freezerId}/status` - Get latest reading for a freezer
- `GET /freezers/{freezerId}/chart?from=YYYY-MM-DDTHH:mm&to=YYYY-MM-DDTHH:mm` - Get time-series data

### Export Service (via Gateway: http://localhost:8080)

- `GET /export/freezers/{freezerId}/csv?from=YYYY-MM-DD&to=YYYY-MM-DD` - Export CSV
- `GET /export/freezers/{freezerId}/pdf?from=YYYY-MM-DD&to=YYYY-MM-DD` - Export PDF

## Example API Usage

### 1. Sign Up
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "mobileNumber": "+1234567890",
    "password": "Secure@1234"
  }'
```

### 2. Verify OTP
```bash
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "otpCode": "123456"
  }'
```

### 3. Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Secure@1234"
  }'
```

### 4. Get Freezers (with JWT token)
```bash
curl -X GET http://localhost:8080/freezers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Alert Rules

- **RED ALERT**: Temperature < -85°C OR Temperature > -75°C
- When a red alert is detected, SMS and email notifications are sent to the user

## Security Features

- BCrypt password hashing (strength 12)
- JWT-based authentication with 24-hour expiration
- OTP verification with 10-minute expiry and max 3 attempts
- Input validation using Jakarta Bean Validation
- No sensitive data in logs (passwords, full OTP codes)

## Testing

Run tests for a specific service:

```bash
cd auth-service
mvn test
```

## Database Schema

The following tables are auto-created by JPA:

- `users`: User accounts
- `otps`: OTP codes for verification
- `freezers`: Freezer devices
- `freezer_readings`: Temperature and status readings
- `alerts`: Alert records

## Notes

- All services register with Eureka for service discovery
- The gateway validates JWT tokens before routing requests
- Freezer readings trigger alert evaluation automatically
- Export service fetches data from freezer-service via REST

## Future Enhancements

- WebSocket support for real-time updates
- Nextcloud integration (placeholder ready)
- Advanced alert rules and thresholds
- User role management (admin, customer, etc.)
- Audit logging

