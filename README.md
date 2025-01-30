# ⛏ Banking System (CLI Project)

A simple **Bank Management System** built using **Java** and **MySQL (JDBC)** for secure banking operations like **Deposit, Withdraw, Transfer Money**, and **Transaction History**.

## ✨ Features

- 🏦 **User Account Management** (Sign-up, Sign-in, Security PIN)
- 💰 **Deposit, Withdraw, and Transfer Money**
- 🔐 **Security** using **MessageDigest (SHA-256)** for PIN encryption
- 🗃 **Database Interaction** via JDBC
- 📜 **Transaction History** (Statement)
- ✅ **Error Handling & Input Validation**

---

## ⚙ Requirements

- **Java 17+**
- **MySQL Database**
- **JDBC MySQL Connector** (⚠️ Must be added manually)
- **Git** (for version control)

---

## ♻ Setup Instructions

### 1️⃣ Clone the Repository

```sh
$ git clone https://github.com/mygithub/bank-management-system.git
$ cd bank-management-system
```

### 2️⃣ Install MySQL and Create Database
~~~~sql
  CREATE DATABASE bank_management;
  USE bank_management;

~~~~

### 3️⃣ Create Required Tables
#### customer Table
~~~~sql
 CREATE TABLE customer (
    aadhar_id VARCHAR(12) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(10) NOT NULL,
    date_of_birth  DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    security_pin VARCHAR(255) NOT NULL
);

~~~~

#### Accounts Table
~~~~sql
 CREATE TABLE accounts (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(12) NOT NULL,
    account_number VARCHAR(10) NOT NULL UNIQUE,
    balance DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(aadhar_id) ON DELETE CASCADE
);

~~~~

#### Transactions Table
~~~~sql
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    related_account VARCHAR(20),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

~~~~

## 🔧 Database Configuration
Before running the project, ensure that you have a `dbconfig.properties` file in the `resources/` folder. This file should store the MySQL database credentials and table names.

### 1️⃣ Create `dbconfig.properties`
Inside the `resources/` folder, create a file named **`dbconfig.properties`** and add the following:

```properties
DB_URL=jdbc:mysql://localhost:3306/bank_management
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
CUSTOMER_TABLE=customers
ACCOUNT_TABLE=accounts
TRANSACTION_TABLE=transactions
```

### 2️⃣ Update DBConnection Class

Ensure that your DBConnection class reads from this file to establish a database connection.

## ⚡ Running the Project

### 1️⃣ Add MySQL [Connector JAR](https://dev.mysql.com/downloads/connector/j/)

## ⛔ Security & Best Practices
  - MessageDigest (SHA-256) is used to encrypt the security PIN.
  - Database instance ensures a single connection for efficiency.
  - InputHandler class validates user inputs and prevents SQL Injection.
  - Resources Folder (resources/) stores configuration files safely.

## ✨ Contributing

Feel free to contribute! Fork the repo, create a new branch, and submit a Pull Request. 🚀

## ✨ Author
Developed by 'Chandan Kumar Singh' 😊
