package com.smartbilling.database;

import com.smartbilling.database.DatabaseConnection;
import com.smartbilling.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for fetching products that are low in stock.
 */
public class StockAlertDAO {

    /**
     * Retrieves all products where current quantity is less than or equal to the minimum stock level.
     * Uses a LEFT JOIN to correctly fetch the vendor name from the vendors table.
     *
     * @return list of low-stock products
     * @throws SQLException if a database access error occurs
     */
    public static List<Product> getLowStockProducts() throws SQLException {
        // FIX: vendor_name does NOT exist as a column in products.
        // Must JOIN vendors table to get vendor name — matches ProductDAO pattern.
        String sql = "SELECT p.product_id, p.barcode, p.name, p.category, p.price, p.cost_price, "
                   + "p.quantity, p.min_stock_level, p.vendor_id, v.name AS vendor_name, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN vendors v ON p.vendor_id = v.vendor_id "
                   + "WHERE p.quantity <= p.min_stock_level "
                   + "ORDER BY p.quantity ASC";
        List<Product> lowStock = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setBarcode(rs.getString("barcode"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setPrice(rs.getDouble("price"));
                p.setCostPrice(rs.getDouble("cost_price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setMinStockLevel(rs.getInt("min_stock_level"));
                p.setVendorId(rs.getObject("vendor_id") != null ? rs.getInt("vendor_id") : null);
                p.setVendorName(rs.getString("vendor_name")); // safely from JOIN
                p.setCreatedAt(rs.getTimestamp("created_at"));
                lowStock.add(p);
            }
        }
        return lowStock;
    }
}
