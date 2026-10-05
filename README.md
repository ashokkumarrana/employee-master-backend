<h1 align="center">🚀 Employee Master – Backend</h1>

<p align="center">
  <b>Spring Boot microservices backend for centralized employee management.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white" />
</p>

<p align="center">
  Backend of the <b>Employee Master</b> project built using <b>Spring Boot Microservices, Spring Cloud Gateway, Eureka, Config Server, MySQL, Redis, Docker and AWS EC2</b>.
</p>

> 🔗 The React frontend lives in a **separate repository**: `employee-master-ui`.

---

## 📑 Table of Contents

<details open>
<summary><b>Click to expand/collapse</b></summary>

1. [Project Overview](#1-project-overview)
2. [Key Features](#2-key-features)
3. [Technology Stack](#3-technology-stack)
4. [System Architecture](#4-system-architecture)
5. [Request Flow](#5-request-flow)
6. [Authentication Flow](#6-authentication-flow)
7. [Microservices](#7-microservices)
8. [Common Service (JAR Library)](#8-common-service-jar-library)
9. [Employee Flow](#9-employee-flow)
10. [Dropdown Master Data Flow](#10-dropdown-master-data-flow)
11. [Excel Import Flow](#11-excel-import-flow)
12. [Excel Export Flow](#12-excel-export-flow)
13. [Audit & Employee History](#13-audit--employee-history)
14. [Redis & Caching](#14-redis--caching)
15. [Backend Architecture](#15-backend-architecture)
16. [Database](#16-database)
17. [Docker Architecture](#17-docker-architecture)
18. [Project Structure](#18-project-structure)
19. [Local Development](#19-local-development)
20. [Environment Configuration](#20-environment-configuration)
21. [AWS Deployment](#21-aws-deployment)
22. [API Routing](#22-api-routing)
23. [Deployment Verification](#23-deployment-verification)
24. [Security Notes](#24-security-notes)
25. [Related Repositories](#25-related-repositories)
26. [Conclusion](#26-conclusion)

</details>

---

## 1. Project Overview

The **Employee Master Backend** is a microservices-based application that manages employees and related dropdown master data.

It consists of:

- 🚪 API Gateway
- 🔐 Auth Service
- 📚 Dropdown Service
- 👤 Employee Service
- 🧩 Common Service (shared JAR library)
- 🔍 Eureka Server (service discovery)
- ⚙️ Config Server (centralized configuration)
- 🗄️ MySQL database
- ⚡ Redis caching
- 🐳 Docker & Docker Compose
- ☁️ AWS EC2 deployment

> The frontend communicates with the backend only through the **API Gateway**, never directly with individual services.

---

## 2. Key Features

<details>
<summary><b>👤 Employee Management</b></summary>

- Add, update, view and delete employees
- Search, filter, sort and paginate employees (server-side)
- Active/inactive status management

</details>

<details>
<summary><b>🔐 Authentication & Security</b></summary>

- Login authentication
- JWT-based authentication
- Role-based authorization (Admin / Management / HOD / Normal / User)
- JWT validation at the API Gateway
- Protected APIs

</details>

<details>
<summary><b>📚 Dropdown Master Data</b></summary>

- Managed by a dedicated Dropdown Service
- Consumed by Employee Service through OpenFeign

</details>

<details>
<summary><b>📊 Excel Management</b></summary>

- Download employee import template
- Upload employee Excel file
- Validate records, detect duplicates
- Separate valid, invalid and duplicate records
- Import valid employees
- Export employee data (centered header and data)

</details>

<details>
<summary><b>📧 Email Notifications</b></summary>

- Email without attachment
- Email with attachment

</details>

<details>
<summary><b>🧾 Audit & Employee History</b></summary>

- `createdBy`, `updatedBy`, `created_at`, `updated_at`, `status` on records
- Employee history (who changed what and when) maintained inside Employee Service

</details>

<details>
<summary><b>⚡ Redis Caching</b></summary>

- Frequently accessed employee data is cached
- Cache config is written once in common-service and reused

</details>

---

## 3. Technology Stack

| Category | Technologies |
|----------|-------------|
| **Language & Build** | Java, Gradle |
| **Backend** | Spring Boot, Spring Security, JWT, Spring Data JPA, Spring Cloud Gateway, Spring Cloud Netflix Eureka, Spring Cloud Config, OpenFeign |
| **Database & Caching** | MySQL 8, Redis |
| **DevOps & Deployment** | Docker, Docker Compose, AWS EC2, GitHub |

---

## 4. System Architecture

<pre>
                    ┌──────────────────────┐
                    │   React Frontend     │
                    │ (separate repo)      │
                    └──────────┬───────────┘
                               │
                               │ HTTPS / API
                               ▼
                    ┌──────────────────────┐
                    │    API Gateway       │
                    │       :8081          │
                    └──────────┬───────────┘
                               │
             ┌─────────────────┼─────────────────┐
             │                 │                 │
             ▼                 ▼                 ▼
     ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
     │ Auth Service │  │   Dropdown   │  │   Employee   │
     │    :8082     │  │   Service    │  │   Service    │
     │              │  │    :8083     │  │    :8084     │
     └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
            │                 │                 │
            ▼                 ▼                 ▼
         MySQL             MySQL           MySQL + Redis

             ┌─────────────────────────────┐
             │       Eureka Server         │
             │            :8761            │
             └─────────────────────────────┘

             ┌─────────────────────────────┐
             │       Config Server         │
             │            :8888            │
             └─────────────────────────────┘

        common-service  ──(JAR dependency)──►  used by services
</pre>

---

## 5. Request Flow

<pre>
Frontend
  │
  ▼
API Gateway :8081
  │
  ├── Auth APIs ──────────► Auth Service :8082
  │
  ├── Dropdown APIs ──────► Dropdown Service :8083
  │
  └── Employee APIs ──────► Employee Service :8084
</pre>

Services are discovered through **Eureka**; configuration comes from the **Config Server**.

---

## 6. Authentication Flow

<pre>
Login request
  │
  ▼
API Gateway
  │
  ▼
Auth Service ──► Authentication ──► JWT Token
  │
  ▼
Client stores JWT
  │
  ▼
JWT sent with protected requests
  │
  ▼
JWT validated at API Gateway ──► forwarded to target service
</pre>

---

## 7. Microservices

<details>
<summary><b>🚪 API Gateway — Port: 8081</b></summary>

- Single entry point for all client requests
- Routes requests to backend services via Eureka
- JWT validation
- CORS configuration

</details>

<details>
<summary><b>🔐 Auth Service — Port: 8082</b></summary>

- User authentication and login
- JWT generation
- Role handling

</details>

<details>
<summary><b>📚 Dropdown Service — Port: 8083</b></summary>

- Dropdown / master data management
- Serves data used by employee forms and filters

</details>

<details>
<summary><b>👤 Employee Service — Port: 8084</b></summary>

- Employee CRUD
- Search, filter, sort, pagination
- Excel import/export
- Email notifications
- Employee history / audit records
- Redis caching
- Fetches dropdown data from Dropdown Service via Feign

</details>

<details>
<summary><b>⚙️ Config Server — Port: 8888</b></summary>

Centralized configuration for all microservices.

</details>

<details>
<summary><b>🔍 Eureka Server — Port: 8761</b></summary>

Service discovery and registration. A single Eureka server is used (no cluster).

</details>

---

## 8. Common Service (JAR Library)

**common-service** is **not** a running microservice. It is built as a **JAR** and added as a dependency in the services that need it (for example Employee Service and Auth Service).

**Contains:**
- Excel utilities (`ExcelUtil`)
- Email functionality
- Redis / cache configuration
- Shared reusable classes

<pre>
common-service ──► JAR
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
  employee-service  auth-service  other services
</pre>

> It has no port, container or database of its own.
> **Build common-service first**, so the JAR is available to the other services.

---

## 9. Employee Flow

<pre>
Request (search / filter / CRUD)
      │
      ▼
API Gateway
      │
      ▼
Employee Service
      │
      ├── MySQL
      │
      └── Redis
</pre>

---

## 10. Dropdown Master Data Flow

<pre>
Client ──► API Gateway ──► Dropdown Service ──► MySQL

Employee Service ──(OpenFeign)──► Dropdown Service
</pre>

---

## 11. Excel Import Flow

<pre>
Download Template
       │
       ▼
Fill Employee Data
       │
       ▼
Upload Excel
       │
       ▼
Validation ──► VALID / INVALID / DUPLICATE
       │
       ▼
Import Valid Records
       │
       ▼
Employee Database
</pre>

Mandatory fields and duplicates are validated before import. Excel logic is shared via **common-service** (`ExcelUtil`).

---

## 12. Excel Export Flow

<pre>
Export request
     │
     ▼
Employee Service
     │
     ▼
Excel generation (ExcelUtil)
     │
     ▼
Excel file download
</pre>

Template and exported files have **centered header and data**.

---

## 13. Audit & Employee History

### Audit fields

Every master record carries common audit fields:

| Field | Description |
|-------|-------------|
| status | Active / inactive |
| created_at | Creation timestamp |
| updated_at | Last update timestamp |
| createdBy | Who created the record |
| updatedBy | Who last updated the record |

### Employee history

Employee changes are recorded as history inside the **Employee Service**.

<pre>
Add / Update / Status change
          │
          ▼
    Employee Service
          │
          ├── Employee table updated
          │
          └── History record saved
</pre>

**Supported actions:**
- `ADD`
- `UPDATE`
- `STATUS_CHANGE`

**History records contain:**

| Field | Description |
|-------|-------------|
| Employee ID | Which employee was changed |
| Action | ADD / UPDATE / STATUS_CHANGE |
| Performed By | User who made the change |
| Performed At | Timestamp of the change |
| Old Data | Previous state |
| New Data | Updated state |

> History logic belongs to **Employee Service** and is saved together with the employee operation.

---

## 14. Redis & Caching

- Redis caches frequently accessed data (e.g. employee by ID)
- Cache config is written **once in common-service** and reused by consumer services
- Cache entries are **evicted** when data changes

---

## 15. Backend Architecture

<pre>
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
MySQL
</pre>

**Additional components:**
- DTOs and projections
- Feign clients
- Spring Security + JWT
- Global exception handling with proper status codes
- Redis caching
- Service discovery & centralized configuration

---

## 16. Database

**MySQL** is the primary database.

**Databases used:**
- `AuthServiceDatabase`
- `DropdownServiceDatabase`
- `EmployeeServiceDatabase`

> In the deployed environment, data is persisted using **Docker volumes**.

---

## 17. Docker Architecture

| Service | Port |
|---------|-----:|
| API Gateway | 8081 |
| Auth Service | 8082 |
| Dropdown Service | 8083 |
| Employee Service | 8084 |
| Config Server | 8888 |
| Eureka Server | 8761 |
| MySQL | 3307 → 3306 |
| Redis | 6379 |

- Docker Compose manages startup and networking.
- Volumes persist MySQL and Redis data.
- `common-service` has no container (it is packaged inside other service JARs).

---

## 18. Project Structure

<pre>
employee-master-backend/
│
├── api-gateway
├── auth-service
├── common-service        (JAR library)
├── config-server
├── dropdown-service
├── employee-service
├── eureka-server
├── .dockerignore
├── .gitignore
└── docker-compose.yml
</pre>

---

## 19. Local Development

**Prerequisites:** Java, Gradle (wrapper included), MySQL, Redis, Docker (optional).

**1. Build common-service first**

<pre>
cd common-service
./gradlew clean build
</pre>

**2. Build every service**

<pre>
./gradlew clean bootJar
</pre>

**3. Start order**

<pre>
Config Server → Eureka Server → Auth / Dropdown / Employee Service → API Gateway
</pre>

**Or run everything with Docker Compose:**

<pre>
docker-compose up --build
</pre>

Check Eureka dashboard at `http://localhost:8761` to confirm all services are registered.

---

## 20. Environment Configuration

Configuration is externalized using **environment variables** and the **Config Server**.

**Important areas:**
- Database connection
- Redis connection
- JWT secret
- Eureka URL
- Config Server URL
- Mail configuration

> ⚠️ **Never commit passwords or secrets to GitHub.**

---

## 21. AWS Deployment

<pre>
GitHub
   │
   ▼
AWS EC2
   │
   ▼
Git Pull
   │
   ▼
Gradle Build
   │
   ▼
Docker Image Build
   │
   ▼
Docker Compose
   │
   ▼
Running Microservices
</pre>

- The **API Gateway** is the public entry point.
- Internal services talk through the **Docker network** and **Eureka**.

---

## 22. API Routing

| Route | Target Service |
|-------|---------------|
| Auth APIs | Auth Service |
| Dropdown APIs | Dropdown Service |
| Employee APIs | Employee Service |

> Exact route paths are defined in the **Gateway configuration**.

---

## 23. Deployment Verification

- ✅ API Gateway availability
- ✅ CORS configuration
- ✅ Auth Service login through Gateway
- ✅ Eureka registration of all services
- ✅ Config Server connectivity
- ✅ MySQL connectivity
- ✅ Redis availability
- ✅ Docker container health

---

## 24. Security Notes

- 🔒 Do not commit passwords, DB credentials, JWT secrets or mail credentials.
- 🔑 Use environment variables for sensitive configuration.
- 🛡️ Keep internal services away from direct public access.
- 🌐 Expose only the API Gateway publicly.
- 🎫 JWT protects all secured APIs.

---

## 25. Related Repositories

| Repository | Description |
|------------|-------------|
| `employee-master-backend` | Spring Boot microservices (this repo) |
| `employee-master-ui` | React + TypeScript frontend (deployed on Vercel) |

---

## 26. Conclusion

The **Employee Master Backend** provides a complete microservices solution with:

- 🚪 API Gateway with JWT validation
- 🔐 Auth, 📚 Dropdown and 👤 Employee services
- 🧩 Shared common-service JAR
- 🔍 Eureka + ⚙️ Config Server
- 🗄️ MySQL + ⚡ Redis
- 📊 Excel import/export, 📧 email
- 🐳 Docker Compose + ☁️ AWS EC2 deployment

---

<p align="center">
  <b>⭐ If you like this project, give it a star on GitHub! ⭐</b>
</p>