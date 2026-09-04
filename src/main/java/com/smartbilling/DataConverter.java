package com.smartbilling;

import com.smartbilling.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataConverter {

    public static void main(String[] args) {
        System.out.println("Starting Phase 2 Domain Conversion...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // 1. Update Vendors
            System.out.println("Converting Vendors...");
            String[] vendorNames = {
                "Annapurna Oils & Foods", 
                "Sri Lakshmi Oil Distributors", 
                "Southern Edible Oils", 
                "Green Harvest Foods", 
                "Tamil Nadu Oil Traders"
            };
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT vendor_id FROM vendors ORDER BY vendor_id")) {
                int index = 0;
                while (rs.next() && index < vendorNames.length) {
                    int vendorId = rs.getInt("vendor_id");
                    String updateSql = "UPDATE vendors SET name = ? WHERE vendor_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, vendorNames[index]);
                        updateStmt.setInt(2, vendorId);
                        updateStmt.executeUpdate();
                    }
                    index++;
                }
                
                // If there are less existing vendors than we need, insert the rest
                while (index < 3) { // Ensure at least 3 vendors exist
                    String insertSql = "INSERT INTO vendors (name, contact_person, phone, email, address) VALUES (?, 'Demo Person', '9876543210', 'demo@example.com', 'Demo Address')";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, vendorNames[index]);
                        insertStmt.executeUpdate();
                    }
                    index++;
                }
            }
            
            // 2. Convert Products
            System.out.println("Converting Products...");
            
            String[][] newProducts = {
                {"OIL1001", "Sunflower Refined Oil - 1L", "Sunflower Oil", "165.00", "140.00", "42", "10", "1"},
                {"OIL1002", "Sunflower Refined Oil - 5L", "Sunflower Oil", "790.00", "700.00", "15", "5", "1"},
                {"OIL2001", "Groundnut Oil - 1L", "Groundnut Oil", "225.00", "195.00", "27", "10", "2"},
                {"OIL2002", "Groundnut Oil - 5L", "Groundnut Oil", "1080.00", "950.00", "8", "3", "2"},
                {"OIL3001", "Coconut Oil - 500ml", "Coconut Oil", "145.00", "120.00", "18", "5", "3"},
                {"OIL3002", "Coconut Oil - 1L", "Coconut Oil", "275.00", "230.00", "12", "5", "3"},
                {"OIL4001", "Sesame Oil - 1L", "Sesame Oil", "290.00", "250.00", "20", "5", "1"},
                {"OIL5001", "Mustard Oil - 1L", "Mustard Oil", "220.00", "180.00", "30", "8", "2"},
                {"OIL6001", "Rice Bran Oil - 1L", "Rice Bran Oil", "200.00", "170.00", "25", "10", "1"},
                {"OIL6002", "Rice Bran Oil - 5L", "Rice Bran Oil", "950.00", "800.00", "10", "3", "1"},
                {"OIL7001", "Soybean Oil - 1L", "Soybean Oil", "205.00", "175.00", "40", "10", "3"},
                {"OIL7002", "Soybean Oil - 5L", "Soybean Oil", "980.00", "850.00", "15", "5", "3"},
                {"OIL8001", "Palmolein Oil - 1L", "Palmolein Oil", "150.00", "130.00", "50", "15", "2"},
                {"OIL8002", "Palmolein Oil - 5L", "Palmolein Oil", "720.00", "630.00", "20", "5", "2"},
                {"OIL9001", "Vegetable Blended Oil - 1L", "Blended Vegetable Oil", "175.00", "150.00", "35", "10", "1"}
            };
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT product_id FROM products ORDER BY product_id")) {
                int index = 0;
                while (rs.next() && index < newProducts.length) {
                    int productId = rs.getInt("product_id");
                    String[] p = newProducts[index];
                    
                    String updateSql = "UPDATE products SET barcode=?, name=?, category=?, price=?, cost_price=?, quantity=?, min_stock_level=?, vendor_id=? WHERE product_id=?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, p[0]);
                        updateStmt.setString(2, p[1]);
                        updateStmt.setString(3, p[2]);
                        updateStmt.setDouble(4, Double.parseDouble(p[3]));
                        updateStmt.setDouble(5, Double.parseDouble(p[4]));
                        updateStmt.setInt(6, Integer.parseInt(p[5]));
                        updateStmt.setInt(7, Integer.parseInt(p[6]));
                        
                        // Handle vendor mapping safely. Just bind to vendor 1 or 2 if existing vendor_id is invalid.
                        // For simplicity in a script, we'll just set it. If foreign key fails, we catch it.
                        try {
                            updateStmt.setInt(8, Integer.parseInt(p[7]));
                            updateStmt.setInt(9, productId);
                            updateStmt.executeUpdate();
                        } catch (Exception e) {
                            System.out.println("Warning: Foreign key issue for product " + p[1] + ". Setting vendor to NULL.");
                            updateStmt.setNull(8, java.sql.Types.INTEGER);
                            updateStmt.setInt(9, productId);
                            updateStmt.executeUpdate();
                        }
                    }
                    index++;
                }
                
                // If there are more new products than existing ones, insert them
                while (index < newProducts.length) {
                    String[] p = newProducts[index];
                    String insertSql = "INSERT INTO products (barcode, name, category, price, cost_price, quantity, min_stock_level, vendor_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, p[0]);
                        insertStmt.setString(2, p[1]);
                        insertStmt.setString(3, p[2]);
                        insertStmt.setDouble(4, Double.parseDouble(p[3]));
                        insertStmt.setDouble(5, Double.parseDouble(p[4]));
                        insertStmt.setInt(6, Integer.parseInt(p[5]));
                        insertStmt.setInt(7, Integer.parseInt(p[6]));
                        insertStmt.setInt(8, Integer.parseInt(p[7]));
                        insertStmt.executeUpdate();
                    } catch (Exception e) {
                         System.out.println("Warning: Could not insert " + p[1] + ": " + e.getMessage());
                         
                         // Try without vendor
                         insertSql = "INSERT INTO products (barcode, name, category, price, cost_price, quantity, min_stock_level, vendor_id) VALUES (?, ?, ?, ?, ?, ?, ?, NULL)";
                         try (PreparedStatement fallbackStmt = conn.prepareStatement(insertSql)) {
                             fallbackStmt.setString(1, p[0]);
                             fallbackStmt.setString(2, p[1]);
                             fallbackStmt.setString(3, p[2]);
                             fallbackStmt.setDouble(4, Double.parseDouble(p[3]));
                             fallbackStmt.setDouble(5, Double.parseDouble(p[4]));
                             fallbackStmt.setInt(6, Integer.parseInt(p[5]));
                             fallbackStmt.setInt(7, Integer.parseInt(p[6]));
                             fallbackStmt.executeUpdate();
                         } catch (Exception ex) {
                             System.out.println("Failed entirely to insert " + p[1]);
                         }
                    }
                    index++;
                }
            }
            
            System.out.println("Data Conversion Complete!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
