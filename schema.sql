-- ========================================================
-- Smart Billing System - Database Schema Setup Script
-- Database Management System: MySQL
-- ========================================================

-- 1. Create Database if it does not already exist
CREATE DATABASE IF NOT EXISTS smart_billing_db;
USE smart_billing_db;

-- 2. Drop existing tables in reverse dependency order (to allow clean script re-execution)
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS vendors;
DROP TABLE IF EXISTS users;

-- 3. Users Table (Stores Admin and Employee credentials & role)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'EMPLOYEE') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Vendors Table (Stores supplier/vendor details)
CREATE TABLE vendors (
    vendor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Products Table (Stores inventory items & low-stock alert thresholds)
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 5,
    vendor_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Sales Table (Stores main billing header information)
CREATE TABLE sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(50) NOT NULL UNIQUE,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_mode ENUM('CASH', 'CARD', 'UPI') DEFAULT 'CASH',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Sale Items Table (Stores individual line items billed in a sale)
CREATE TABLE sale_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========================================================
-- Initial Seed Data
-- ========================================================

-- Insert Default Admin Account (Username: admin / Password: admin123)
INSERT INTO users (username, password, full_name, role, status)
VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN', 'ACTIVE');

-- Insert Default Employee Account (Username: emp1 / Password: emp123)
INSERT INTO users (username, password, full_name, role, status)
VALUES ('emp1', 'emp123', 'John Cashier', 'EMPLOYEE', 'ACTIVE');

-- Insert Sample Vendors
INSERT INTO vendors (name, contact_person, phone, email, address)
VALUES 
('Annapurna Oils & Foods', 'Rajesh Kumar', '9876543210', 'contact@annapurnaoils.com', '123 Market Street, Block A'),
('Sri Lakshmi Oil Distributors', 'Venkat Raman', '9123456789', 'orders@srilakshmioil.com', '45 Industrial Estate, Phase II'),
('Southern Edible Oils', 'Priya Sharma', '9443322110', 'sales@southernoils.in', '78 Harvest Road');

-- Insert Sample Products
INSERT INTO products (barcode, name, category, price, cost_price, quantity, min_stock_level, vendor_id)
VALUES 
('OIL1001', 'Sunflower Refined Oil - 1L', 'Sunflower Oil', 165.00, 140.00, 42, 10, 1),
('OIL1002', 'Sunflower Refined Oil - 5L', 'Sunflower Oil', 790.00, 700.00, 15, 5, 1),
('OIL2001', 'Groundnut Oil - 1L', 'Groundnut Oil', 225.00, 195.00, 27, 10, 2),
('OIL2002', 'Groundnut Oil - 5L', 'Groundnut Oil', 1080.00, 950.00, 8, 3, 2),
('OIL3001', 'Coconut Oil - 500ml', 'Coconut Oil', 145.00, 120.00, 18, 5, 3),
('OIL3002', 'Coconut Oil - 1L', 'Coconut Oil', 275.00, 230.00, 12, 5, 3),
('OIL4001', 'Sesame Oil - 1L', 'Sesame Oil', 290.00, 250.00, 20, 5, 1),
('OIL5001', 'Mustard Oil - 1L', 'Mustard Oil', 220.00, 180.00, 30, 8, 2),
('OIL6001', 'Rice Bran Oil - 1L', 'Rice Bran Oil', 200.00, 170.00, 25, 10, 1),
('OIL6002', 'Rice Bran Oil - 5L', 'Rice Bran Oil', 950.00, 800.00, 10, 3, 1),
('OIL7001', 'Soybean Oil - 1L', 'Soybean Oil', 205.00, 175.00, 40, 10, 3),
('OIL7002', 'Soybean Oil - 5L', 'Soybean Oil', 980.00, 850.00, 15, 5, 3),
('OIL8001', 'Palmolein Oil - 1L', 'Palmolein Oil', 150.00, 130.00, 50, 15, 2),
('OIL8002', 'Palmolein Oil - 5L', 'Palmolein Oil', 720.00, 630.00, 20, 5, 2),
('OIL9001', 'Vegetable Blended Oil - 1L', 'Blended Vegetable Oil', 175.00, 150.00, 35, 10, 1);
