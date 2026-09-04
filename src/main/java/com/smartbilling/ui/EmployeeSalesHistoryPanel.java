package com.smartbilling.ui;

import com.smartbilling.dao.SaleDAO;
import com.smartbilling.model.Sale;
import com.smartbilling.service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * EmployeeSalesHistoryPanel
 *
 * Displays the currently logged-in employee's own completed bills.
 *
 * Reads: sales.invoice_no, sales.sale_date, sales.total_amount, sales.payment_mode
 * Filters by: sales.user_id = AuthService.getCurrentUser().getUserId()
 * Sort: sale_date DESC (handled by SaleDAO.getSalesByUserId)
 *
 * Phase 4 — no schema changes required.
 * Phase 3 bill numbers (BILL-XXXXXX) are read directly from the database.
 */
public class EmployeeSalesHistoryPanel extends JPanel {

    private final SaleDAO saleDAO;

    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private static final String[] COLUMNS = {
        "Bill No", "Date / Time", "Total Amount (\u20b9)", "Payment Mode"
    };

    public EmployeeSalesHistoryPanel() {
        this.saleDAO = new SaleDAO();
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("My Sales History");
        titleLabel.setFont(UITheme.F_PAGE);
        titleLabel.setForeground(UITheme.TEXT);
        header.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = UITheme.secondaryBtn("\u21BA  Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(refreshBtn);
        header.add(btnWrap, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Status / empty-state label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.F_BODY);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusLabel.setVisible(false);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);

        JScrollPane scroll = UITheme.styledScroll(table);

        JPanel centerPane = new JPanel(new BorderLayout(0, 8));
        centerPane.setOpaque(false);
        centerPane.add(statusLabel, BorderLayout.NORTH);
        centerPane.add(scroll,      BorderLayout.CENTER);
        add(centerPane, BorderLayout.CENTER);
    }

    /**
     * Fetches the current employee's sales from MySQL and populates the table.
     * Called on initial construction and whenever Refresh is clicked.
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        statusLabel.setVisible(false);

        if (AuthService.getCurrentUser() == null) {
            statusLabel.setText("Session error: no logged-in user.");
            statusLabel.setVisible(true);
            return;
        }

        int userId = AuthService.getCurrentUser().getUserId();
        List<Sale> sales = saleDAO.getSalesByUserId(userId);

        if (sales.isEmpty()) {
            statusLabel.setText(
                "No sales found. Complete a billing transaction to see your history here.");
            statusLabel.setForeground(UITheme.TEXT_MUTED);
            statusLabel.setVisible(true);
            return;
        }

        SimpleDateFormat dtFmt = new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss");
        for (Sale s : sales) {
            String dateStr = (s.getSaleDate() != null)
                    ? dtFmt.format(s.getSaleDate())
                    : "\u2014";
            Object[] row = {
                s.getInvoiceNo(),
                dateStr,
                String.format("%.2f", s.getTotalAmount()),
                s.getPaymentMode()
            };
            tableModel.addRow(row);
        }
    }
}
