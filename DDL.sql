-- DDL
-- Creating Database
CREATE DATABASE IF NOT EXISTS budget_management;
USE budget_management;

-- USERS table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    username VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100)
);

-- CATEGORIES table (now tied to individual users)
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NULL,  -- NULL for predefined categories
    name VARCHAR(255) NOT NULL,
    is_predefined BOOLEAN DEFAULT FALSE,
    image_path VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
ALTER TABLE categories ADD COLUMN is_custom_image BOOLEAN DEFAULT FALSE;
ALTER TABLE categories ADD COLUMN description VARCHAR(255) DEFAULT NULL;
ALTER TABLE categories ADD COLUMN is_essential BOOLEAN DEFAULT FALSE;
ALTER TABLE categories 
ADD CONSTRAINT uc_user_category UNIQUE (user_id, name);

-- ACCOUNTS table
CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    account_name VARCHAR(100),
    balance DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- EXPENSES table
CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    account_id INT,
    category_id INT,
    expense_date DATE,
    month VARCHAR(20), -- e.g., 'April 2025'
    amount DECIMAL(10, 2),
    type VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- BUDGET table
CREATE TABLE IF NOT EXISTS budget (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    month VARCHAR(20),
    income DECIMAL(10, 2),
    needs_percent DECIMAL(5, 2),
    wants_percent DECIMAL(5, 2),
    savings_percent DECIMAL(5, 2),
    budget_set BOOLEAN DEFAULT FALSE,
    income_confirmed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- CHALLENGES table
CREATE TABLE IF NOT EXISTS user_challenges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    challenge_name VARCHAR(100),
    status ENUM('not_started', 'in_progress', 'completed') DEFAULT 'not_started',
    completion_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
ALTER TABLE user_challenges 
MODIFY COLUMN status ENUM('not_started', 'in_progress', 'completed', 'claimed') DEFAULT 'not_started';

-- BADGES table
CREATE TABLE IF NOT EXISTS badges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    challenge_name VARCHAR(100) UNIQUE,
    badge_image_path VARCHAR(255)
);

-- SUBSCRIPTIONS table
CREATE TABLE subscriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    service_name VARCHAR(100),
    cost DOUBLE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
ALTER TABLE subscriptions ADD COLUMN icon VARCHAR(100);
