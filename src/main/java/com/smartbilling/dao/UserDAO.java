package com.smartbilling.dao;

import com.smartbilling.database.DatabaseConnection;
import com.smartbilling.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO
 * 
 * Data Access Object for handling user-related database operations.
 * Uses PreparedStatements for all SQL queries to prevent SQL injection.
 */
public class UserDAO {

    /**
     * Authenticates a user against the 'users' database table.
     * 
     * @param username The entered username
     * @param password The entered password
     * @return User object if credentials are valid and account is ACTIVE; null otherwise.
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT user_id, username, password, full_name, role, status, created_at "
                   // COLLATE utf8mb4_bin makes password comparison case-sensitive (security fix)
                   + "FROM users WHERE username = ? AND password COLLATE utf8mb4_bin = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Bind input parameters safely into prepared statement
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getString("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Return null if authentication failed
    }

    /**
     * Helper method to check if a username already exists in the database.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error checking username existence: " + e.getMessage());
        }
        return false;
    }
}
