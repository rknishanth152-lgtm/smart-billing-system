package com.smartbilling.dao;

import com.smartbilling.database.DatabaseConnection;
import com.smartbilling.model.ProductSaleReport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportDAO
 * 
 * Data Access Object for fetching sales report statistics and graph data from MySQL.
 */
public class ReportDAO {

    /**
     * Retrieves overall sales metrics: total revenue and total orders count.
     * 
     * @return double array: [0] = Total Revenue, [1] = Total Orders Count
     */
    public double[] getOverallSalesSummary() {
        double[] summary = new double[]{0.0, 0.0};
        String sql = "SELECT COALESCE(SUM(total_amount), 0.00) AS total_revenue, COUNT(*) AS total_orders FROM sales";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                summary[0] = rs.getDouble("total_revenue");
                summary[1] = rs.getDouble("total_orders");
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error fetching sales summary: " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }

    /**
     * Retrieves sales performance for all products (quantity sold & revenue generated).
     * 
     * @return List of ProductSaleReport objects sorted by quantity sold DESC.
     */
    public List<ProductSaleReport> getProductSalesReport() {
        List<ProductSaleReport> reports = new ArrayList<>();
        String sql = "SELECT p.name AS product_name, p.barcode, "
                   + "COALESCE(SUM(si.quantity), 0) AS total_qty_sold, "
                   + "COALESCE(SUM(si.subtotal), 0.00) AS total_revenue "
                   + "FROM products p "
                   + "LEFT JOIN sale_items si ON p.product_id = si.product_id "
                   + "GROUP BY p.product_id, p.name, p.barcode "
                   + "ORDER BY total_qty_sold DESC, total_revenue DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ProductSaleReport r = new ProductSaleReport();
                r.setProductName(rs.getString("product_name"));
                r.setBarcode(rs.getString("barcode"));
                r.setTotalQtySold(rs.getInt("total_qty_sold"));
                r.setTotalRevenue(rs.getDouble("total_revenue"));
                reports.add(r);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Error fetching product sales report: " + e.getMessage());
            e.printStackTrace();
        }

        return reports;
    }

    /**
     * Retrieves top-selling products ranked by units sold for graph generation.
     * 
     * @param limit Number of top products to fetch (e.g. 5)
     * @return List of ProductSaleReport objects
     */
    public List<ProductSaleReport> getTopSellingProducts(int limit) {
        List<ProductSaleReport> list = new ArrayList<>();
        String sql = "SELECT p.name AS product_name, p.barcode, "
                   + "SUM(si.quantity) AS total_qty_sold, "
                   + "SUM(si.subtotal) AS total_revenue "
                   + "FROM sale_items si "
                   + "JOIN products p ON si.product_id = p.product_id "
                   + "GROUP BY p.product_id, p.name, p.barcode "
                   + "ORDER BY total_qty_sold DESC "
                   + "LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProductSaleReport r = new ProductSaleReport();
                    r.setProductName(rs.getString("product_name"));
                    r.setBarcode(rs.getString("barcode"));
                    r.setTotalQtySold(rs.getInt("total_qty_sold"));
                    r.setTotalRevenue(rs.getDouble("total_revenue"));
                    list.add(r);
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Error fetching top-selling products: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
