package com.smartbilling.dao;

import com.smartbilling.database.DatabaseConnection;
import com.smartbilling.model.Sale;
import com.smartbilling.model.SaleItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SaleDAO
 * 
 * Handles billing transaction saving into MySQL.
 * Uses atomic Database Transactions (setAutoCommit(false), commit, rollback)
 * to ensure sales record insertion, sale line items, and stock deductions
 * occur together without data inconsistency.
 */
public class SaleDAO {

    /**
     * Processes and records a complete billing sale transaction atomically.
     * 
     * @param sale The sale object containing user_id, payment mode, and cart line items.
     * @return Generated Invoice Number if sale succeeds; null if transaction fails/rolls back.
     */
    public String processTransaction(Sale sale) {
        if (sale == null || sale.getItems() == null || sale.getItems().isEmpty()) {
            System.err.println("[ERROR] Transaction failed: Sale cart is empty!");
            return null;
        }

        Connection conn = null;
        PreparedStatement pstmtSale = null;
        PreparedStatement pstmtItem = null;
        PreparedStatement pstmtStock = null;
        ResultSet rsKeys = null;

        String sqlInsertSale = "INSERT INTO sales (invoice_no, user_id, total_amount, payment_mode) VALUES (?, ?, ?, ?)";
        String sqlInsertItem = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlDeductStock = "UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";

        try {
            conn = DatabaseConnection.getConnection();

            // 1. DISABLE AUTO-COMMIT TO START DATABASE TRANSACTION
            conn.setAutoCommit(false);

            // 1a. Generate Sequential Bill Number securely within the transaction
            String sqlGetMaxBill = "SELECT invoice_no FROM sales WHERE invoice_no LIKE 'BILL-%' ORDER BY invoice_no DESC LIMIT 1 FOR UPDATE";
            try (Statement stmt = conn.createStatement();
                 ResultSet rsSeq = stmt.executeQuery(sqlGetMaxBill)) {
                 int nextSeq = 1;
                 if (rsSeq.next()) {
                     String lastInvoice = rsSeq.getString(1); // e.g. "BILL-000024"
                     try {
                         String numPart = lastInvoice.substring(5);
                         nextSeq = Integer.parseInt(numPart) + 1;
                     } catch (Exception e) {
                         System.err.println("[WARNING] Could not parse previous bill number: " + lastInvoice);
                     }
                 }
                 String invoiceNo = String.format("BILL-%06d", nextSeq);
                 sale.setInvoiceNo(invoiceNo);
            }

            // 2. Insert Sale Header
            pstmtSale = conn.prepareStatement(sqlInsertSale, Statement.RETURN_GENERATED_KEYS);
            pstmtSale.setString(1, sale.getInvoiceNo());
            pstmtSale.setInt(2, sale.getUserId());
            pstmtSale.setDouble(3, sale.getTotalAmount());
            pstmtSale.setString(4, sale.getPaymentMode());

            int affectedSales = pstmtSale.executeUpdate();
            if (affectedSales == 0) {
                throw new SQLException("Creating sale record failed, no rows affected.");
            }

            // Retrieve generated sale_id PRIMARY KEY
            rsKeys = pstmtSale.getGeneratedKeys();
            int generatedSaleId = 0;
            if (rsKeys.next()) {
                generatedSaleId = rsKeys.getInt(1);
                sale.setSaleId(generatedSaleId);
            } else {
                throw new SQLException("Creating sale record failed, no generated ID obtained.");
            }

            // 3. Prepare Statements for Sale Items & Stock Deduction
            pstmtItem = conn.prepareStatement(sqlInsertItem);
            pstmtStock = conn.prepareStatement(sqlDeductStock);

            // Loop through each item in customer's cart
            for (SaleItem item : sale.getItems()) {
                // A. Insert into sale_items table
                pstmtItem.setInt(1, generatedSaleId);
                pstmtItem.setInt(2, item.getProductId());
                pstmtItem.setInt(3, item.getQuantity());
                pstmtItem.setDouble(4, item.getUnitPrice());
                pstmtItem.setDouble(5, item.getSubtotal());
                pstmtItem.executeUpdate();

                // B. Deduct quantity from products table (safeguarded: WHERE quantity >= ?)
                pstmtStock.setInt(1, item.getQuantity());
                pstmtStock.setInt(2, item.getProductId());
                pstmtStock.setInt(3, item.getQuantity());

                int updatedStockRows = pstmtStock.executeUpdate();
                if (updatedStockRows == 0) {
                    // Stock insufficient! Trigger transaction rollback
                    throw new SQLException("Insufficient stock available for product: '" 
                            + item.getProductName() + "' (ID: " + item.getProductId() + "). Transaction aborted.");
                }
            }

            // 4. ALL STEPS SUCCESSFUL -> COMMIT TRANSACTION
            conn.commit();
            System.out.println("[SUCCESS] Transaction committed successfully. Bill No: " + sale.getInvoiceNo());
            return sale.getInvoiceNo();

        } catch (SQLException e) {
            System.err.println("[TRANSACTION ERROR] " + e.getMessage());
            // ROLLBACK TRANSACTION ON FAILURE TO KEEP DATABASE CONSISTENT
            if (conn != null) {
                try {
                    System.err.println("[TRANSACTION ROLLBACK] Rolling back all database changes...");
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[ROLLBACK ERROR] Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            return null;
        } finally {
            // Restore auto-commit state and close resources
            try {
                if (rsKeys != null) rsKeys.close();
                if (pstmtSale != null) pstmtSale.close();
                if (pstmtItem != null) pstmtItem.close();
                if (pstmtStock != null) pstmtStock.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset connection auto-commit
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("[CLEANUP WARNING] Error closing transaction connection: " + ex.getMessage());
            }
        }
    }

    /**
     * Retrieves all completed sales for a specific employee, sorted newest first.
     *
     * Used by EmployeeSalesHistoryPanel to show only the logged-in employee's bills.
     *
     * @param userId The user_id of the currently logged-in employee.
     * @return List of Sale objects belonging to that employee (may be empty).
     */
    public List<Sale> getSalesByUserId(int userId) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT sale_id, invoice_no, sale_date, total_amount, payment_mode "
                   + "FROM sales "
                   + "WHERE user_id = ? "
                   + "ORDER BY sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale s = new Sale();
                    s.setSaleId(rs.getInt("sale_id"));
                    s.setInvoiceNo(rs.getString("invoice_no"));
                    s.setSaleDate(rs.getTimestamp("sale_date"));
                    s.setTotalAmount(rs.getDouble("total_amount"));
                    s.setPaymentMode(rs.getString("payment_mode"));
                    s.setUserId(userId);
                    list.add(s);
                }
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] getSalesByUserId failed for userId=" + userId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
