-- ============================================================
-- Shree Annapurna Billing System
-- MySQL Dedicated Instance: ShreeAnnapurnaDB
-- Initial Security Setup Script
-- 
-- PURPOSE:
-- Run ONCE immediately after mysqld --initialize-insecure.
-- Sets root password on the fresh dedicated instance.
-- 
-- SAFETY:
-- This script ONLY runs against the dedicated ShreeAnnapurnaDB
-- instance on port 33066.
-- It NEVER touches MySQL80 or any other installation.
-- ============================================================

-- Set root password for dedicated instance
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';

-- Apply immediately without server restart
FLUSH PRIVILEGES;
