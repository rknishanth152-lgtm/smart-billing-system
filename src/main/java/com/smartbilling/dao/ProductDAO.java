package com.smartbilling.dao;

import com.smartbilling.database.DatabaseConnection;
import com.smartbilling.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO
 * 
 * Data Access Object for handling product stock operations using PreparedStatements.
 */
public class ProductDAO {

    /**
     * Retrieves all products from the products table joined with vendor names.
     * 
     * @return List of Product objects.
     */
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.barcode, p.name, p.category, p.price, p.cost_price, "
                   + "p.quantity, p.min_stock_level, p.vendor_id, v.name AS vendor_name, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN vendors v ON p.vendor_id = v.vendor_id "
                   + "ORDER BY p.product_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Error fetching products: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * CORE SMART FEATURE: Retrieves products where current quantity <= min_stock_level.
     * 
     * @return List of low-stock Product objects sorted by quantity ASC.
     */
    public List<Product> getLowStockProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.barcode, p.name, p.category, p.price, p.cost_price, "
                   + "p.quantity, p.min_stock_level, p.vendor_id, v.name AS vendor_name, p.created_at "
                   + "FROM products p "
                   + "LEFT JOIN vendors v ON p.vendor_id = v.vendor_id "
                   + "WHERE p.quantity <= p.min_stock_level "
                   + "ORDER BY p.quantity ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Error fetching low-stock products: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Helper method to quickly restock/add stock quantity for a product.
     */
    public boolean addStockQuantity(int productId, int additionalQuantity) {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, additionalQuantity);
            pstmt.setInt(2, productId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Error updating stock quantity for ID " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts a new product record into MySQL using a PreparedStatement.
     */
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (barcode, name, category, price, cost_price, quantity, min_stock_level, vendor_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getBarcode().trim());
            pstmt.setString(2, product.getName().trim());
            pstmt.setString(3, product.getCategory() != null ? product.getCategory().trim() : "General");
            pstmt.setDouble(4, product.getPrice());
            pstmt.setDouble(5, product.getCostPrice());
            pstmt.setInt(6, product.getQuantity());
            pstmt.setInt(7, product.getMinStockLevel());

            if (product.getVendorId() != null && product.getVendorId() > 0) {
                pstmt.setInt(8, product.getVendorId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Error adding product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing product record in MySQL using a PreparedStatement.
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET barcode = ?, name = ?, category = ?, price = ?, "
                   + "cost_price = ?, quantity = ?, min_stock_level = ?, vendor_id = ? "
                   + "WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getBarcode().trim());
            pstmt.setString(2, product.getName().trim());
            pstmt.setString(3, product.getCategory() != null ? product.getCategory().trim() : "General");
            pstmt.setDouble(4, product.getPrice());
            pstmt.setDouble(5, product.getCostPrice());
            pstmt.setInt(6, product.getQuantity());
            pstmt.setInt(7, product.getMinStockLevel());

            if (product.getVendorId() != null && product.getVendorId() > 0) {
                pstmt.setInt(8, product.getVendorId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            pstmt.setInt(9, product.getProductId());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Error updating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a product record by its ID using a PreparedStatement.
     */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Error deleting product ID " + productId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setBarcode(rs.getString("barcode"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setPrice(rs.getDouble("price"));
        p.setCostPrice(rs.getDouble("cost_price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setMinStockLevel(rs.getInt("min_stock_level"));

        int vId = rs.getInt("vendor_id");
        if (!rs.wasNull()) {
            p.setVendorId(vId);
        } else {
            p.setVendorId(null);
        }

        p.setVendorName(rs.getString("vendor_name"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
