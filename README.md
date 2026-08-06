# 🏦 Banking API — Production-Ready RESTful Financial Services

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%7C%20H2-blue.svg)](https://www.postgresql.org/)
[![Deployment](https://img.shields.io/badge/Deployment-Render-purple.svg)](https://render.com/)

A secure, transactional, and scalable Banking REST API built with **Spring Boot 3**, **Spring Security**, and **Spring Data JPA**. Designed with robust financial controls including pessimistic database locking, strict account ownership validation, transactional rollback protection, and audit logging.

---

## 🚀 Key Features

* **Role-Based Access Control (RBAC):** Fine-grained permission system distinguishing standard account holders (`ROLE_USER`) from administrators (`ROLE_ADMIN`).
* **Strict Ownership Validation:** Users can only view details, initiate transfers, or inspect transaction logs for accounts they explicitly own.
* **Concurrency & Race Condition Control:** Uses JPA **Pessimistic Locking** (`PESSIMISTIC_WRITE`) during transfers to guarantee thread-safe balance updates under high load.
* **Transactional Audit Trail:** Every transaction (Deposit, Transfer, Withdrawal) logs `SUCCESS` or `FAILED` states along with failure reasons for full compliance reporting.
* **Dual Persistence Strategy:**
  * **Local Development:** Persistent file-based H2 database (`./data/bankdb.mv.db`) surviving application restarts.
  * **Production:** Automatically binds to cloud-hosted **PostgreSQL** on Render via environment variables.
* **Interactive API Documentation:** Full OpenAPI 3.0 integration exposed via Swagger UI.

---

## 🛠️ Tech Stack

| Domain | Technology |
| :--- | :--- |
| **Language & Framework** | Java 17+, Spring Boot 3.x |
| **Security** | Spring Security (Authentication, RBAC, Data Ownership Verification) |
| **Persistence** | Spring Data JPA / Hibernate, H2 Database (Local), PostgreSQL (Production) |
| **Validation & Utilities** | Jakarta Bean Validation, Lombok |
| **API Documentation** | Springdoc OpenAPI (Swagger UI) |
| **Deployment** | Docker, Render Cloud Platform |

---

## 📡 Key API Endpoints

### 🔑 User Management
* `POST /api/v1/users` — Register a new customer account.
* `GET /api/v1/users/me` — Retrieve the authenticated user profile.

### 💳 Account Operations
* `POST /api/v1/accounts` — Open a new bank account with an initial deposit.
* `GET /api/v1/accounts/my-accounts` — Fetch all active accounts belonging to the authenticated user.
* `GET /api/v1/accounts` — *(Admin Only)* List all registered bank accounts across the platform.

### 💸 Transactions & Transfers
* `POST /api/v1/transactions/transfer` — Perform atomic fund transfer between two valid accounts.
* `GET /api/v1/transactions/{accountNumber}` — Retrieve complete transactional ledger/history for an account.

---

## 🔐 Core Security & Concurrency Design

### 1. Account Ownership Safeguard
To prevent unauthorized debiting, every incoming transaction request resolves the logged-in user's identity from Spring Security's context:

```java
if (!fromAccount.getUser().getUsername().equals(authenticatedUsername)) {
    throw new IllegalArgumentException("Unauthorized: You do not own source account " + fromAccountNumber);
}
