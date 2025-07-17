# 🍽️ AUT-Food Backend Project

## 📌 Introduction

This is the backend of a **multi-role food ordering system** developed for the **Advanced Programming Course (Spring 1404)** at Amirkabir University of Technology (AUT). The backend supports customer, seller, delivery, and admin roles and is built entirely using **pure Java (Java SE)** with a modular, layered structure.

---

## 🧩 Features

### 🔐 Authentication & User Management
- Register/login for different roles
- Profile editing, including banking info
- Role-based access and dashboards

### 🍽️ Restaurant & Menu Management
- Seller registration with admin approval
- Menu creation and editing
- Food categorization, search, and filter

### 🛍️ Ordering System
- Cart management, order placement
- Order status tracking
- Assign orders to delivery agents

### 💳 Payment & Wallet
- Simulated online payment or wallet deduction
- Full invoice generation (with tax and delivery)
- View transaction history

### 🚚 Delivery System
- Agents receive and update delivery status
- Track history and current assignments

### ⚙️ Admin Panel
- Approve users/restaurants
- Manage system-wide stats, orders, users

### 📈 Advanced Features
- Discount codes
- Ratings and comments on food
- Best restaurants shown on homepage

---

## 🚀 Getting Started

### 🛠️ Requirements

- Java 17+
- Maven
- No external frameworks (pure Java SE)
- File or Redis-based data storage

📦 All dependencies (if any) are declared in `pom.xml`.

---

### ▶️ How to Run

1. Compile the project:

```bash
cd "ap project backend/Food4u"
mvn compile
```

2. Run the main server class:

```bash
mvn exec:java -Dexec.mainClass="com.foodApp.Server"
```

> Make sure Redis is running if your config uses it.

---

## 📚 API Documentation

API design follows **OpenAPI** standards.

- See `aut_food.yaml` in the root folder
- You can view it via [Swagger Editor](https://editor.swagger.io)

---

## 🛠️ Technologies Used

- Java SE 17+
- Maven
- File/Redis-based persistence
- Custom HTTP server using core Java libraries
- DTO-based request/response handling
- Modular package design (e.g., `dto`, `config`, `controller`)

---

## 📁 Project Structure

```
Food4u/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── foodApp/
                    ├── Server.java
                    ├── config/
                    ├── dto/
                    ├── controller/
                    ├── model/
                    ├── service/
                    └── storage/
```

---

## 👨‍🏫 Supervision

Project developed for **Advanced Programming**  
Amirkabir University of Technology 

- Dr. Amir Kalbasi  
- Dr. Hossein Zeynali  

---

## 📣 Notes

- This backend can connect to any frontend (JavaFX, web, etc.) via RESTful HTTP endpoints
- Responses are in JSON
- No external frameworks are used — pure Java implementation
