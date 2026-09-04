package com.smartbilling.dao;

import com.smartbilling.database.DatabaseConnection;
import com.smartbilling.model.Vendor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VendorDAO
 * 
 * Data Access Object for handling Vendor database operations using PreparedStatements.
 */
public class VendorDAO {

    /**
     * Retrieves all vendors stored in the database.
     * 
     * @return List of Vendor objects sorted by vendor_id ASC.
     */
    public List<Vendor> getAllVendors() {
        List<Vendor> vendors = new ArrayList<>();
        String sql = "SELECT vendor_id, name, contact_person, phone, email, address, created_at "
                   + "FROM vendors ORDER BY vendor_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vendor v = new Vendor();
                v.setVendorId(rs.getInt("vendor_id"));
                v.setName(rs.getString("name"));
                v.setContactPerson(rs.getString("contact_person"));
                v.setPhone(rs.getString("phone"));
                v.setEmail(rs.getString("email"));
                v.setAddress(rs.getString("address"));
                v.setCreatedAt(rs.getTimestamp("created_at"));
                vendors.add(v);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error fetching vendors: " + e.getMessage());
            e.printStackTrace();
        }

        return vendors;
    }

    /**
     * Inserts a new vendor record into MySQL using a PreparedStatement.
     * 
     * @param vendor The vendor object to insert
     * @return true if insertion succeeds, false otherwise
     */
    public boolean addVendor(Vendor vendor) {
        String sql = "INSERT INTO vendors (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vendor.getName().trim());
            pstmt.setString(2, vendor.getContactPerson() != null ? vendor.getContactPerson().trim() : "");
            pstmt.setString(3, vendor.getPhone().trim());
            pstmt.setString(4, vendor.getEmail() != null ? vendor.getEmail().trim() : "");
            pstmt.setString(5, vendor.getAddress() != null ? vendor.getAddress().trim() : "");

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Error adding vendor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing vendor record in MySQL using a PreparedStatement.
     * 
     * @param vendor The vendor object with updated fields
     * @return true if update succeeds, false otherwise
     */
    public boolean updateVendor(Vendor vendor) {
        String sql = "UPDATE vendors SET name = ?, contact_person = ?, phone = ?, email = ?, address = ? WHERE vendor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vendor.getName().trim());
            pstmt.setString(2, vendor.getContactPerson() != null ? vendor.getContactPerson().trim() : "");
            pstmt.setString(3, vendor.getPhone().trim());
            pstmt.setString(4, vendor.getEmail() != null ? vendor.getEmail().trim() : "");
            pstmt.setString(5, vendor.getAddress() != null ? vendor.getAddress().trim() : "");
            pstmt.setInt(6, vendor.getVendorId());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Error updating vendor ID " + vendor.getVendorId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a vendor record from MySQL by vendor_id using a PreparedStatement.
     * Note: Referenced products will have their vendor_id set to NULL due to ON DELETE SET NULL.
     * 
     * @param vendorId The ID of the vendor to delete
     * @return true if deletion succeeds, false otherwise
     */
    public boolean deleteVendor(int vendorId) {
        String sql = "DELETE FROM vendors WHERE vendor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vendorId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ERROR] Error deleting vendor ID " + vendorId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
