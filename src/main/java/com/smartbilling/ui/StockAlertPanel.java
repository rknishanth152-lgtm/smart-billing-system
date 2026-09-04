package com.smartbilling.ui;

import com.smartbilling.database.StockAlertDAO;
import com.smartbilling.model.Product;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel that displays products whose quantity is at or below the minimum stock level.
 * It automatically refreshes every 30 seconds to reflect any stock changes.
 */
public class StockAlertPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel totalLabel;
    private final Timer refreshTimer;

    public StockAlertPanel() {
        setLayout(new BorderLayout(10, 10));

        // Table columns
        String[] columnNames = {"Product Name", "Current Qty", "Minimum Stock", "Vendor"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read‑only
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom label showing total count
        totalLabel = new JLabel();
        add(totalLabel, BorderLayout.SOUTH);

        // Initial load
        loadData();

        // Auto‑refresh every 30 seconds (30000 ms)
        refreshTimer = new Timer(30000, e -> loadData());
        refreshTimer.start();
    }

    /**
     * Public entry point called by AdminDashboard to trigger an immediate refresh.
     */
    public void loadLowStockProducts() {
        loadData();
    }

    /**
     * Loads low‑stock products from the database and populates the table.
     */
    private void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Product> lowStock = StockAlertDAO.getLowStockProducts();
                tableModel.setRowCount(0);
                for (Product p : lowStock) {
                    tableModel.addRow(new Object[]{p.getName(), p.getQuantity(), p.getMinStockLevel(), p.getVendorName()});
                }
                totalLabel.setText("Total low‑stock products: " + lowStock.size());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to load low‑stock data: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Stops the periodic refresh timer when the panel is disposed.
     */
    public void disposePanel() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }
}
