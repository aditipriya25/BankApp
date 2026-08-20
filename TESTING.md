# TESTING.md — VaultBank API Testing Guide
# Base URL: http://localhost:8080
# All protected routes need: Authorization: Bearer <token>

---

## 1. AUTH

### Register Customer
POST http://localhost:8080/api/auth/signup
Content-Type: application/json
{
  "fullName": "Test Customer",
  "email": "customer@test.com",
  "password": "password123",
  "phone": "9876543210"
}

### Register Employee
POST http://localhost:8080/api/auth/signup
Content-Type: application/json
{
  "name": "Test Employee",
  "email": "employee@test.com",
  "password": "password123",
  "role": "EMPLOYEE"
}

### Login (Customer)
POST http://localhost:8080/api/auth/login
Content-Type: application/json
{
  "email": "customer@test.com",
  "password": "password123"
}
=> SAVE token from response

### Login (Employee)
POST http://localhost:8080/api/auth/login
Content-Type: application/json
{
  "email": "employee@test.com",
  "password": "password123"
}
=> SAVE token from response

---

## 2. LOCKERS (Employee)

### Add Locker
POST http://localhost:8080/api/lockers
Authorization: Bearer <employee_token>
Content-Type: application/json
{
  "lockerNumber": "L001",
  "size": "SMALL",
  "price": 500.00,
  "location": "Branch A - Floor 1",
  "available": true
}

### Get All Lockers
GET http://localhost:8080/api/lockers
Authorization: Bearer <employee_token>

### Get Public Lockers (Customer)
GET http://localhost:8080/api/lockers/all-public
Authorization: Bearer <customer_token>

---

## 3. LOCKER REQUESTS (Customer)

### Request a Locker
POST http://localhost:8080/api/locker-assignments/request
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "lockerId": "<locker_id>"
}
=> SAVE assignmentId

### View My Assignments
GET http://localhost:8080/api/locker-assignments/my-assignments
Authorization: Bearer <customer_token>

---

## 4. LOCKER REQUESTS (Employee)

### View Pending Requests
GET http://localhost:8080/api/locker-assignments/pending
Authorization: Bearer <employee_token>

### Approve Request
PUT http://localhost:8080/api/locker-assignments/<assignmentId>/approve
Authorization: Bearer <employee_token>

### Reject Request
PUT http://localhost:8080/api/locker-assignments/<assignmentId>/reject
Authorization: Bearer <employee_token>

### View Awaiting Payment
GET http://localhost:8080/api/locker-assignments/awaiting-payment
Authorization: Bearer <employee_token>

### Confirm Payment (Employee)
PUT http://localhost:8080/api/locker-assignments/<assignmentId>/confirm-payment
Authorization: Bearer <employee_token>

### View Approved
GET http://localhost:8080/api/locker-assignments/approved
Authorization: Bearer <employee_token>

---

## 5. KYC

### Submit KYC (Customer)
POST http://localhost:8080/api/kyc/submit/<customerId>
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "documentType": "AADHAAR",
  "documentNumber": "1234-5678-9012",
  "documentUrl": "https://example.com/doc.pdf"
}

### Check My KYC Status
GET http://localhost:8080/api/kyc/status/me
Authorization: Bearer <customer_token>

### All Pending KYC (Employee)
GET http://localhost:8080/api/kyc/pending
Authorization: Bearer <employee_token>

### Review KYC (Employee)
PUT http://localhost:8080/api/kyc/<kycId>/review
Authorization: Bearer <employee_token>
Content-Type: application/json
{
  "status": "APPROVED",
  "remarks": "Documents verified"
}

---

## 6. NOMINEES (RBI Para 5.1)

### Add Nominee
POST http://localhost:8080/api/nominees/<assignmentId>
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "name": "Jane Doe",
  "relationship": "Spouse",
  "dateOfBirth": "1990-05-15",
  "phone": "9123456789",
  "email": "jane@test.com",
  "address": "123 Main St",
  "formType": "SL1",
  "isMinor": false
}

### Add Minor Nominee (with Guardian)
POST http://localhost:8080/api/nominees/<assignmentId>
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "name": "Child Doe",
  "relationship": "Child",
  "dateOfBirth": "2015-03-20",
  "formType": "SL1",
  "isMinor": true,
  "guardianName": "Jane Doe"
}

### Get Nominees for My Locker
GET http://localhost:8080/api/nominees/<assignmentId>
Authorization: Bearer <customer_token>

### Cancel Nominee (Form SL2)
DELETE http://localhost:8080/api/nominees/nominee/<nomineeId>
Authorization: Bearer <customer_token>

### Employee - View All Nominees for Assignment
GET http://localhost:8080/api/nominees/employee/<assignmentId>
Authorization: Bearer <employee_token>

---

## 7. LOCKER AGREEMENT (RBI Para 2.1)

### Generate Agreement (Employee)
POST http://localhost:8080/api/agreements/<assignmentId>
Authorization: Bearer <employee_token>
Content-Type: application/json
{}

### Get Agreement
GET http://localhost:8080/api/agreements/<assignmentId>
Authorization: Bearer <customer_token>

### Customer Signs Agreement
POST http://localhost:8080/api/agreements/<assignmentId>/sign
Authorization: Bearer <customer_token>
Content-Type: application/json
{}

### Renew Agreement (Employee)
POST http://localhost:8080/api/agreements/<assignmentId>/renew
Authorization: Bearer <employee_token>
Content-Type: application/json
{}

---

## 8. RENT PAYMENT (RBI Para 2.2)

### Pay Rent — UPI
POST http://localhost:8080/api/rent/<assignmentId>/pay
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "paymentMethod": "UPI",
  "upiId": "customer@upi"
}

### Pay Rent — Card
POST http://localhost:8080/api/rent/<assignmentId>/pay
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "paymentMethod": "CARD",
  "cardNumber": "4111 1111 1111 1111"
}

### Pay Rent — Net Banking
POST http://localhost:8080/api/rent/<assignmentId>/pay
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "paymentMethod": "NETBANKING",
  "bankName": "SBI"
}

### Pay Rent — Offline
POST http://localhost:8080/api/rent/<assignmentId>/pay
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "paymentMethod": "OFFLINE"
}

### View Payment History
GET http://localhost:8080/api/rent/<assignmentId>/history
Authorization: Bearer <customer_token>

### All Overdue Rents (Employee)
GET http://localhost:8080/api/rent/overdue
Authorization: Bearer <employee_token>

---

## 9. LOCKER CLOSURE (RBI Part VI)

### Normal Closure (Customer)
POST http://localhost:8080/api/closure/<assignmentId>/normal
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "reason": "No longer required"
}

### Death Claim (Customer / Nominee)
POST http://localhost:8080/api/closure/<assignmentId>/death
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "deathCertificateUrl": "https://example.com/death-cert.pdf",
  "claimantDetails": "Jane Doe, Spouse"
}

### Non-Payment Forced Closure (Employee — RBI 6.3.1)
POST http://localhost:8080/api/closure/<assignmentId>/non-payment
Authorization: Bearer <employee_token>
Content-Type: application/json
{}

### Law Enforcement Closure (Employee)
POST http://localhost:8080/api/closure/<assignmentId>/law-enforcement
Authorization: Bearer <employee_token>
Content-Type: application/json
{
  "courtOrderDetails": "Court Order No. HC/2024/1234"
}

### Complete Closure (Employee)
PUT http://localhost:8080/api/closure/<closureId>/complete
Authorization: Bearer <employee_token>
Content-Type: application/json
{
  "inventoryDetails": "Gold ornaments, documents",
  "witness1Name": "Ram Kumar",
  "witness2Name": "Suresh Mehta",
  "videoUrl": "https://example.com/video.mp4",
  "newspaperNoticeDetails": "Published in Times of India and Hindu on 2024-01-15"
}

### All Pending Closures (Employee)
GET http://localhost:8080/api/closure/pending
Authorization: Bearer <employee_token>

### All Closures (Employee)
GET http://localhost:8080/api/closure/all
Authorization: Bearer <employee_token>

---

## 10. VISIT BOOKINGS

### Book a Visit Slot (Customer)
POST http://localhost:8080/api/slot-bookings/book
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "assignmentId": "<assignmentId>",
  "visitDate": "2024-12-20",
  "visitTime": "10:00"
}

### My Bookings
GET http://localhost:8080/api/slot-bookings/my-bookings
Authorization: Bearer <customer_token>

### All Visit Logs (Employee)
GET http://localhost:8080/api/visit-logs
Authorization: Bearer <employee_token>

---

## 11. CHATBOT (VaultBot — Groq AI)

### Health Check
GET http://localhost:8080/api/chatbot/health

### Send Message
POST http://localhost:8080/api/chatbot/message
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "message": "How do I add a nominee to my locker?",
  "history": []
}

### Send Message with History
POST http://localhost:8080/api/chatbot/message
Authorization: Bearer <customer_token>
Content-Type: application/json
{
  "message": "What about for a minor nominee?",
  "history": [
    { "role": "user", "content": "How do I add a nominee?" },
    { "role": "assistant", "content": "You can add a nominee using Form SL1..." }
  ]
}

---

## TESTING ORDER (Step-by-step)

1. Register Employee  → Login Employee → save employee_token
2. Add Locker (employee_token)
3. Register Customer → Login Customer → save customer_token
4. Submit KYC (customer_token)
5. Review KYC → APPROVED (employee_token)
6. Request Locker (customer_token) → save assignmentId
7. Approve Request (employee_token)
8. Confirm Payment (employee_token)
9. Generate Agreement (employee_token)
10. Sign Agreement (customer_token)
11. Pay Rent (customer_token)
12. Add Nominee (customer_token)
13. Book Visit (customer_token)
14. Test Chatbot (customer_token)
15. Initiate Closure (customer_token)
16. Complete Closure (employee_token)

---

## COMMON ERRORS

| Error | Cause | Fix |
|---|---|---|
| 401 Unauthorized | Missing/expired token | Re-login, use fresh token |
| 403 Forbidden | Wrong role | Use correct customer/employee token |
| 400 Bad Request | Missing required field | Check request body |
| 404 Not Found | Wrong ID | Verify assignmentId/lockerId |
