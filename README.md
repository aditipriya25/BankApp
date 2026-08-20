# 🔐 VaultBank — Bank Locker Management System

> A full-stack, RBI-compliant bank locker management platform built with **Spring Boot 4.0** (Java 17) and **Oracle JET** frontend.
> Implements all mandates from the [RBI Circular on Safe Deposit Lockers (2021)](https://www.rbi.org.in/scripts/NotificationUser.aspx?Id=12146).

---

## ✨ Features

### Customer Portal
- 🔐 Locker Search & Request — Browse lockers by size, view pricing, submit requests
- 💳 Rent Payment Gateway (RBI Para 2.2) — UPI / Card / Net Banking / Offline, full history
- 📋 Nominee Management (RBI Para 5.1) — Add/cancel nominees (Forms SL1, SL1A, SL2, SL3)
- 📄 Locker Agreement (RBI Para 2.1) — View and digitally sign Board-approved agreement
- 🚪 Locker Closure (RBI Part VI) — Normal closure, death claim (15-day settlement)
- 📅 Visit Booking (RBI Para 4) — Schedule visits with OTP-secured entry
- 🛡️ KYC Verification — Submit and track KYC document status
- 🤖 VaultBot AI Chatbot — Powered by Grok xAI

### Employee Portal
- 📊 Dashboard — Real-time stats, revenue, overdue rents with Chart.js charts
- 🏦 Locker Inventory — Full CRUD management
- ✅ Request Management — Approve/reject/payment confirmation
- 🔍 KYC Reviews — Verify customer KYC submissions
- 👥 All Nominees — Searchable list of all nominees
- 📄 Agreements — Generate, view, renew locker agreements
- 💰 Rent Dues Tracker — Overdue dashboard with force-closure (RBI 6.3)
- 🚪 Closure Management — All 5 closure types with inventory recording

---

## 📜 RBI Compliance Coverage

| RBI Section | Requirement | Status |
|---|---|---|
| Para 2.1 | Board-approved stamped agreement, copy to customer | ✅ |
| Para 2.2 | Annual rent with payment tracking | ✅ |
| Para 3.1 | Waitlist management with priority allocation | ✅ |
| Para 4 | Visit logs with OTP-based secure access | ✅ |
| Para 5.1 | Nomination facility (Forms SL1/SL2/SL3) | ✅ |
| Para 5.2 | Joint hirer nomination (Form SL1A) | ✅ |
| Para 5.2.4 | Death claims settled within 15 days | ✅ |
| Para 6.1 | Normal closure on key surrender | ✅ |
| Para 6.2 | Break-open on death with nominee/legal heir | ✅ |
| Para 6.3.1 | Forced closure after 3 years non-payment | ✅ |
| Para 6.3.2 | Dual-witness + video + newspaper notice | ✅ |
| Para 6.4 | Law enforcement / court-ordered opening | ✅ |
| Para 7 | Bank liability capped at 100x annual rent | ✅ |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4.0, Spring Security (JWT) |
| Database | Oracle Database (JPA/Hibernate) |
| AI | Grok xAI API (grok-beta model) |
| Frontend | Oracle JET (AMD/RequireJS), Vanilla CSS |
| Charts | Chart.js (Bar, Doughnut, Line, Radar) |
| Auth | JWT Bearer tokens, BCrypt passwords |

---

## 🚀 Getting Started

### Backend
`ash
cd BankingProject/Project
# Configure application.properties (DB + Grok API key)
./mvnw spring-boot:run
# API available at http://localhost:8080
`

### Frontend
`ash
cd BankingProject/frontend
npm install && npm run dev
# App at http://localhost:3000
`

---

## ⚙️ Configuration (application.properties)

`properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
jwt.secret=your-256-bit-secret
jwt.expiration=86400000
grok.api.key=xai-YOUR_GROK_KEY
grok.api.url=https://api.x.ai/v1/chat/completions
grok.model=grok-beta
`

---

## 📡 Key API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/auth/login | Public | Login |
| GET | /api/nominees/{id} | CUSTOMER | Get nominees |
| POST | /api/nominees/{id} | CUSTOMER | Add nominee |
| DELETE | /api/nominees/nominee/{id} | CUSTOMER | Cancel nominee |
| GET | /api/agreements/{id} | Both | Get agreement |
| POST | /api/agreements/{id}/sign | CUSTOMER | Sign agreement |
| POST | /api/rent/{id}/pay | CUSTOMER | Pay annual rent |
| GET | /api/rent/overdue | EMPLOYEE | Overdue rents |
| POST | /api/closure/{id}/normal | CUSTOMER | Normal closure |
| POST | /api/closure/{id}/death | CUSTOMER | Death claim |
| POST | /api/closure/{id}/non-payment | EMPLOYEE | Force closure (RBI 6.3) |
| PUT | /api/closure/{id}/complete | EMPLOYEE | Complete closure |
| POST | /api/chatbot/message | Authenticated | Chat with VaultBot |

---

## 🤖 VaultBot Setup

1. Get your API key from [x.ai](https://x.ai)
2. Add to pplication.properties: grok.api.key=xai-your-key
3. If key is empty, the built-in rule-based fallback handles common queries automatically.

---

## 📄 License

MIT License — see [LICENSE](LICENSE) file.

**Repository:** [github.com/Digaa2710/BankingProject](https://github.com/Digaa2710/BankingProject)
