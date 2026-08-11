# 💰 GrowWise — Investment Goal Management Platform

**GrowWise** is a full-stack investment planning platform that helps users turn financial goals into structured, trackable investment plans.

Users can create financial goals, receive **AI-assisted asset allocation suggestions**, track their portfolio and investment progress, and build consistent investing habits through tasks and streaks.

---

## ✨ Key Features

### 👤 User Features

* 🔐 Email & password authentication
* 🔵 Sign in with Google
* 🎯 Create and track financial goals
* 🤖 AI-assisted investment plan generation
* 📊 Personalized asset allocation suggestions
* ✏️ Manually adjust AI-generated allocations
* 💼 Track investments and portfolio performance
* 📈 Monitor goal progress
* ✅ Manage daily investment tasks
* 🔥 Build and maintain investing streaks

### 🛡️ Admin Features

* 📊 Platform-wide dashboard
* 👥 Monitor platform users
* 💰 View investment statistics
* 📦 Manage the asset catalog
* 📈 Monitor platform performance

---

## 🛠️ Tech Stack

### Backend — `backend/`

* ☕ **Java 21**
* 🌱 **Spring Boot 4.1**
* 🗃️ **Spring Data JPA**
* 🔄 **Hibernate**
* 🐬 **MySQL**
* 🔐 **Spring Security**
* 🎟️ **JWT Authentication (`jjwt`)**
* 🔵 **Google ID Token Verification**
* 🤖 **Spring AI**
* ✨ **Google Gemini API**
* 🐳 **Docker**

### Frontend — `Frontend/`

* 🅰️ **Angular 21**
* 📘 **TypeScript**
* 🧩 **Standalone Components**
* 🌐 **Angular SSR / Express Support**

---

## 🤖 AI-Assisted Investment Planning

One of the main features of GrowWise is its AI-assisted investment planning.

Based on the user's financial goal and available investment amount, the platform uses **Google Gemini through Spring AI** to generate suggested asset allocations.

Users can review the AI-generated plan and manually modify the allocation before proceeding.

> AI suggestions are intended to support investment planning and are not financial advice.

---

## 🏗️ Project Structure

```text
Investment-Goal-Management-Platform/
│
├── backend/
│   └── Spring Boot REST API
│
├── Frontend/
│   └── Angular Web Application
│
├── docker-compose.yml
│
└── README.md
```

### Default Ports

| Service             |   Port |
| ------------------- | -----: |
| Angular Frontend    | `4200` |
| Spring Boot Backend | `8080` |
| MySQL               | `3306` |

---

# 🚀 Getting Started

## 1️⃣ Prerequisites

Make sure the following are installed:

* **JDK 21+**
* **Node.js**
* **npm**
* **Angular CLI**
* **MySQL**
* **Docker** *(optional)*

Check your installations:

```bash
java --version
node --version
npm --version
ng version
```

If Angular CLI is not installed:

```bash
npm install -g @angular/cli
```

---

# ⚙️ Backend Setup

## 2️⃣ Configure Environment Variables

Inside the `backend/` directory, create:

```text
.env
```

Add:

```env
DB_URL=jdbc:mysql://localhost:3306/InvestmentGoalManagementDB?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC

DB_USERNAME=your-username
DB_PASSWORD=your-password

GEMINI_API_KEY=your-gemini-api-key

GOOGLE_CLIENT_ID=your-google-oauth-client-id
```

### Environment Variables

| Variable           | Description                |
| ------------------ | -------------------------- |
| `DB_URL`           | MySQL database connection  |
| `DB_USERNAME`      | MySQL username             |
| `DB_PASSWORD`      | MySQL password             |
| `GEMINI_API_KEY`   | Google Gemini API key      |
| `GOOGLE_CLIENT_ID` | Google OAuth 2.0 client ID |

> ⚠️ Never commit your `.env` file or API keys to GitHub.

---

## 3️⃣ Run the Backend

Open a terminal:

```bash
cd backend
```

### Windows

```bash
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

Once started, the backend API runs on:

```text
http://localhost:8080
```

Hibernate automatically creates or updates the database schema using:

```properties
spring.jpa.hibernate.ddl-auto=update
```

---

# 🅰️ Frontend Setup

## 4️⃣ Install Dependencies

Open another terminal:

```bash
cd Frontend
npm install
```

---

## 5️⃣ Run the Angular Application

Start the development server with:

```bash
ng serve
```

Then open:

```text
http://localhost:4200
```

### Automatically Open the Browser

You can also run:

```bash
ng serve --open
```

or:

```bash
ng serve -o
```

Angular will automatically open the application in your default browser.

> 💡 Keep both the Spring Boot backend and Angular frontend running at the same time.

---

# 🐳 Running with Docker

You can also run the application using Docker.

From the project root:

```bash
docker compose up --build
```

This builds and starts the application containers.

| Container |   Port |
| --------- | -----: |
| Frontend  | `4200` |
| Backend   | `8080` |
| MySQL     | `3306` |

To stop the containers:

```bash
docker compose down
```

---

# 🔐 Authentication

GrowWise supports two authentication methods:

### Email & Password

Users can register and log in using their email and password.

After successful authentication, the backend generates a **JWT token** that is used to authenticate subsequent requests.

### Google Sign-In

Users can also authenticate using their Google account.

The Google ID token is verified by the Spring Boot backend before the application creates or retrieves the corresponding user account.

---

# 🔄 Application Flow

```text
Register / Login
       ↓
Financial Profile
       ↓
Create Financial Goal
       ↓
AI Investment Suggestion
       ↓
Review / Adjust Allocation
       ↓
Create Investment Plan
       ↓
Track Portfolio
       ↓
Monitor Goal Progress
```

---

# 🔌 API

The Angular application communicates with the Spring Boot backend through REST APIs.

```text
Angular
   ↓
REST API
   ↓
Spring Boot
   ↓
Service Layer
   ↓
Spring Data JPA
   ↓
MySQL
```

The backend runs at:

```text
http://localhost:8080
```

If Swagger / Springdoc is enabled, Swagger UI can typically be accessed at:

```text
http://localhost:8080/swagger-ui.html
```

or:

```text
http://localhost:8080/swagger-ui/index.html
```

---

# 🧪 API Testing

Backend endpoints can be tested using:

* **Postman**
* **Swagger UI**

Make sure the Spring Boot backend is running before testing the endpoints.

---

# 🔒 Security

GrowWise uses:

* **Spring Security**
* **JWT-based authentication**
* **Stateless sessions**
* **Role-Based Access Control (RBAC)**
* **Google ID token verification**
* **Protected USER and ADMIN endpoints**

---

# ⚠️ Development Notes

The current configuration is designed primarily for **local development**.

Before production deployment:

* Update CORS configuration
* Replace hardcoded `localhost` API URLs
* Store secrets securely
* Configure production database credentials
* Configure production Google OAuth origins
* Use secure JWT secrets
* Enable HTTPS

---

# 👥 Roles

### USER

Can:

* Manage financial information
* Create financial goals
* Generate investment plans
* Receive AI suggestions
* Manage portfolio investments
* Track goal progress
* Manage tasks and streaks

### ADMIN

Can:

* Access the admin dashboard
* Manage assets
* Monitor users
* View investment statistics
* Monitor platform-wide performance
---

## 💙 GrowWise

**Set your goal. Build your plan. Track your growth.**
