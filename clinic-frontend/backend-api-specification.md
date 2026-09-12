# Backend API Specification for Clinic Management System

## Base URL
```
http://localhost:8080/api
```

## Authentication Endpoints

### 1. Login User
**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "emailOrUsername": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "string (JWT token)",
  "role": "string (doctor|patient|staff|admin)",
  "message": "string",
  "status": "SUCCESS|ERROR"
}
```

**Example Success Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "role": "doctor",
  "message": "Login successful",
  "status": "SUCCESS"
}
```

### 2. Register User
**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "email": "string",
  "username": "string (optional)",
  "password": "string",
  "confirmPassword": "string",
  "firstName": "string",
  "lastName": "string",
  "profile": "doctor|patient|staff|admin",
  "phoneNumber": "string (optional)",
  "dateOfBirth": "string (optional, ISO date)",
  "address": "string (optional)",
  "doctorDetails": {
    "licenseNumber": "string",
    "specialization": "string",
    "experience": "number",
    "department": "string",
    "qualifications": ["string"],
    "consultationFee": "number"
  },
  "patientDetails": {
    "emergencyContact": "string",
    "bloodGroup": "string",
    "allergies": ["string"],
    "medicalHistory": ["string"]
  },
  "staffDetails": {
    "employeeId": "string",
    "department": "string",
    "position": "string",
    "shift": "string"
  }
}
```

**Response:** Same as login response

### 3. Refresh Token
**Endpoint:** `POST /auth/refresh`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:** `{}` (empty object)

**Response:**
```json
{
  "token": "string (new JWT token)",
  "role": "string",
  "message": "Token refreshed successfully",
  "status": "SUCCESS|ERROR"
}
```

### 4. Change Password
**Endpoint:** `POST /auth/change-password`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "currentPassword": "string",
  "newPassword": "string"
}
```

**Response:**
```json
{
  "token": "string (optional new token)",
  "role": "string",
  "message": "Password changed successfully",
  "status": "SUCCESS|ERROR"
}
```

### 5. Logout
**Endpoint:** `POST /auth/logout`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:** `{}` (empty object)

**Response:**
```json
{
  "token": "",
  "role": "",
  "message": "Logout successful",
  "status": "SUCCESS"
}
```

### 6. Check Email Exists
**Endpoint:** `GET /auth/check-email/{email}`

**Response:**
```json
{
  "exists": "boolean"
}
```

### 7. Check Username Exists
**Endpoint:** `GET /auth/check-username/{username}`

**Response:**
```json
{
  "exists": "boolean"
}
```

## Error Response Format
All endpoints should return consistent error responses:

```json
{
  "token": "",
  "role": "",
  "message": "Error description",
  "status": "ERROR"
}
```

## HTTP Status Codes
- `200 OK` - Success
- `201 Created` - Resource created (registration)
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Invalid credentials or token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Email/username already exists
- `500 Internal Server Error` - Server error

## Authentication Flow
1. User submits login credentials to `/auth/login`
2. Backend validates credentials and returns JWT token
3. Frontend stores token and user role
4. Frontend includes token in Authorization header for protected routes
5. Backend validates token on each protected request
6. Frontend can refresh token using `/auth/refresh` endpoint
7. User logs out via `/auth/logout` endpoint

## Security Considerations
- JWT tokens should include expiration time
- Passwords should be hashed using bcrypt or similar
- Implement rate limiting on auth endpoints
- Use HTTPS in production
- Validate and sanitize all input data
- Implement proper CORS configuration