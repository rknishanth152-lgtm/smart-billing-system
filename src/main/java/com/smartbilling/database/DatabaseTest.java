package com.smartbilling.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DatabaseTest
 * 
 * Console-based test runner for verifying MySQL JDBC connectivity and PreparedStatement execution.
 * Allows testing the database layer without requiring a GUI.
 */
public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   SMART BILLING SYSTEM - DATABASE LAYER TEST    ");
        System.out.println("=================================================\n");

        System.out.println("Attempting connection to MySQL database 'smart_billing_db'...\n");

        // Use try-with-resources to automatically close Connection and PreparedStatement
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("[SUCCESS] Database Connection Established Successfully!\n");

            // Test PreparedStatement query 1: Count users
            String userQuery = "SELECT COUNT(*) FROM users";
            try (PreparedStatement pstmt = conn.prepareStatement(userQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println(" -> Registered Users Count: " + rs.getInt(1));
                }
            }

            // Test PreparedStatement query 2: Count vendors
            String vendorQuery = "SELECT COUNT(*) FROM vendors";
            try (PreparedStatement pstmt = conn.prepareStatement(vendorQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println(" -> Registered Vendors Count: " + rs.getInt(1));
                }
            }

            // Test PreparedStatement query 3: Count products & low stock items
            String productQuery = "SELECT COUNT(*) AS total, SUM(CASE WHEN quantity <= min_stock_level THEN 1 ELSE 0 END) AS low_stock FROM products";
            try (PreparedStatement pstmt = conn.prepareStatement(productQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println(" -> Total Products Count: " + rs.getInt("total"));
                    System.out.println(" -> Low Stock Alert Products: " + rs.getInt("low_stock"));
                }
            }

            System.out.println("\n[PASSED] Database layer test completed with zero errors!");

        } catch (SQLException e) {
            System.err.println("\n[FAILED] Could not connect to MySQL database!");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State:  " + e.getSQLState());
            System.err.println("Message:    " + e.getMessage());
            System.err.println("\nTroubleshooting Checklist:");
            System.err.println("1. Is MySQL Server running on localhost:3306?");
            System.err.println("2. Have you executed 'schema.sql' in MySQL Workbench?");
            System.err.println("3. Are the username ('root') and password ('root') correct in DatabaseConnection.java?");
        }
    }
}
