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
2. Thread-Safe Fund Transfers
To protect against double-spending and dynamic balance updates in multi-threaded environments, accounts are queried with write-level locks before modifying balances:

@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Account> findByAccountNumber(String accountNumber);
```
## 💻 Local Setup & Installation

### Prerequisites

Before running the project, ensure you have the following installed:

- ☕ Java 17 JDK or later
- 📦 Maven 3.8+ (or use the included Maven Wrapper `./mvnw`)
- 🐘 PostgreSQL (for production) or H2 Database (for local development)
- Git

---

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/banking-api.git
cd banking-api
```

---

### 2️⃣ Build the Application

Using Maven Wrapper:

```bash
./mvnw clean package -DskipTests
```

Or if Maven is installed globally:

```bash
mvn clean package -DskipTests
```

---

### 3️⃣ Run the Application

Using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Or:

```bash
mvn spring-boot:run
```

The application will start at:

```
http://localhost:8081
```

---

### 📖 API Documentation (Swagger)

After the application starts, open:

```
http://localhost:8081/swagger-ui.html
```

or (SpringDoc v2)

```
http://localhost:8081/swagger-ui/index.html
```

---

### 🗄️ H2 Database Console (Local Development)

Access the H2 Console:

```
http://localhost:8081/h2-console
```

Database Configuration:

| Property | Value |
|----------|-------|
| JDBC URL | `jdbc:h2:file:./data/bankdb` |
| Username | `sa` |
| Password | *(leave blank)* |

---

# 🌐 Production Environment Variables

Configure the following environment variables when deploying to platforms such as **Render**, **Railway**, **AWS**, or **Azure**.

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL Connection URL | `jdbc:postgresql://db-host:5432/bankdb` |
| `SPRING_DATASOURCE_USERNAME` | Database Username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database Password | `your_secure_password` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate Schema Strategy | `update` |
| `PORT` | Server Port | `8080` |

---

# 🧪 Default Test Credentials

When running the project for the first time, the `DataInitializer` automatically seeds the database with sample users.

| Role | Username | Password | Purpose |
|------|----------|----------|---------|
| 👑 Administrator | `admin_boss` | `admin123` | Manage users, accounts, and transactions |
| 👤 Standard User | `alice_user` | `password123` | Customer testing (Initial Balance: **$1,000**) |
| 👤 Standard User | `bob_user` | `password123` | Customer testing (Initial Balance: **$500**) |

---

# 🚀 Running Tests

Run all unit tests:

```bash
./mvnw test
```

Or

```bash
mvn test
```

---

# 📦 Build Executable JAR

```bash
./mvnw clean package
```

The generated JAR will be available in:

```
target/
```

Run it using:

```bash
java -jar target/banking-api-0.0.1-SNAPSHOT.jar
```

---

# 📄 License

This project is licensed under the **MIT License**.

Feel free to use, modify, and distribute this project in accordance with the license terms.

---

## ❤️ Support

If you found this project useful, consider giving it a ⭐ on GitHub.
Contributions, issues, and feature requests are always welcome!

Happy Coding! 🚀
