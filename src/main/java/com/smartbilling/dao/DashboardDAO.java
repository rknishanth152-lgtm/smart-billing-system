package com.smartbilling.dao;

import com.smartbilling.database.DatabaseConnection;
import com.smartbilling.model.DashboardMetrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DashboardDAO
 * 
 * Data Access Object for fetching real-time aggregate statistics for the Admin Dashboard.
 * All metrics are queried directly from the MySQL database using PreparedStatements.
 */
public class DashboardDAO {

    /**
     * Fetches live aggregate metrics from the database.
     * 
     * @return DashboardMetrics object populated with real database values.
     */
    public DashboardMetrics getMetrics() {
        DashboardMetrics metrics = new DashboardMetrics();

        // SQL Query 1: Total count of active products
        String sqlProducts = "SELECT COUNT(*) AS total_products FROM products";
        
        // SQL Query 2: Count of products where stock <= min_stock_level
        String sqlLowStock = "SELECT COUNT(*) AS low_stock FROM products WHERE quantity <= min_stock_level";
        
        // SQL Query 3: Total sum of sales completed today (CURDATE)
        String sqlTodaySales = "SELECT COALESCE(SUM(total_amount), 0.00) AS today_sum FROM sales WHERE DATE(sale_date) = CURRENT_DATE()";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Fetch Total Products
            try (PreparedStatement pstmt = conn.prepareStatement(sqlProducts);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    metrics.setTotalProducts(rs.getInt("total_products"));
                }
            }

            // 2. Fetch Low Stock Count
            try (PreparedStatement pstmt = conn.prepareStatement(sqlLowStock);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    metrics.setLowStockCount(rs.getInt("low_stock"));
                }
            }

            // 3. Fetch Today's Sales Total
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTodaySales);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    metrics.setTodaysSales(rs.getDouble("today_sum"));
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Error loading dashboard metrics: " + e.getMessage());
            e.printStackTrace();
        }

        return metrics;
    }
}
