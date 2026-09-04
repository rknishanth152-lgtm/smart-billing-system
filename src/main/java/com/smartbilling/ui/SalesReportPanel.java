package com.smartbilling.ui;

import com.smartbilling.dao.ReportDAO;
import com.smartbilling.model.ProductSaleReport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * SalesReportPanel
 * 
 * Swing Panel for Sales Reports & Graphical Analytics.
 * Displays total revenue metrics, product-wise sales breakdown JTable,
 * and a 2D Bar Chart generated directly from MySQL data.
 */
public class SalesReportPanel extends JPanel {

    private final ReportDAO reportDAO;

    private JLabel totalRevenueLabel;
    private JLabel totalOrdersLabel;
    private JLabel topProductLabel;

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private SalesBarChartPanel chartPanel;

    public SalesReportPanel() {
        this.reportDAO = new ReportDAO();
        initUI();
        loadReportDataFromDatabase();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Header Toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Sales Performance Reports & Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));

        JButton refreshBtn = new JButton("Refresh Report Data");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadReportDataFromDatabase());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Split: Top Cards + Table (Left) & Graphical Bar Chart (Right)
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 15, 0));
        mainContent.setOpaque(false);

        // LEFT COLUMN: Stat Cards & Breakdown JTable
        JPanel leftCol = new JPanel(new BorderLayout(10, 10));
        leftCol.setOpaque(false);

        // Summary Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        cardsGrid.setOpaque(false);

        totalRevenueLabel = new JLabel("₹0.00", SwingConstants.CENTER);
        JPanel revCard = createMiniCard("Total Revenue", totalRevenueLabel, new Color(16, 185, 129));

        totalOrdersLabel = new JLabel("0", SwingConstants.CENTER);
        JPanel orderCard = createMiniCard("Total Sales", totalOrdersLabel, new Color(37, 99, 235));

        topProductLabel = new JLabel("N/A", SwingConstants.CENTER);
        JPanel topCard = createMiniCard("Top Product", topProductLabel, new Color(245, 158, 11));

        cardsGrid.add(revCard);
        cardsGrid.add(orderCard);
        cardsGrid.add(topCard);

        leftCol.add(cardsGrid, BorderLayout.NORTH);

        // Product Sales Breakdown Table
        String[] columns = {"Rank", "Product Name", "Barcode", "Units Sold", "Total Revenue (₹)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportTable.setRowHeight(26);
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane tableScroll = new JScrollPane(reportTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Product Sales Performance Breakdown"));
        leftCol.add(tableScroll, BorderLayout.CENTER);

        mainContent.add(leftCol);

        // RIGHT COLUMN: 2D Graphical Bar Chart Component
        chartPanel = new SalesBarChartPanel();
        mainContent.add(chartPanel);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createMiniCard(String title, JLabel valLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel tLabel = new JLabel(title, SwingConstants.CENTER);
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tLabel.setForeground(new Color(100, 116, 139));

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valLabel.setForeground(color);

        card.add(tLabel, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Fetches live sales statistics from MySQL via ReportDAO and updates UI + Bar Chart.
     */
    public void loadReportDataFromDatabase() {
        // 1. Load Summary Metrics
        double[] summary = reportDAO.getOverallSalesSummary();
        NumberFormat currFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        totalRevenueLabel.setText(currFmt.format(summary[0]));
        totalOrdersLabel.setText(String.valueOf((int) summary[1]));

        // 2. Load Product-wise Breakdown Table
        tableModel.setRowCount(0);
        List<ProductSaleReport> reportList = reportDAO.getProductSalesReport();

        if (!reportList.isEmpty() && reportList.get(0).getTotalQtySold() > 0) {
            topProductLabel.setText(reportList.get(0).getProductName());
        } else {
            topProductLabel.setText("None");
        }

        int rank = 1;
        for (ProductSaleReport r : reportList) {
            Object[] row = {
                "#" + rank++,
                r.getProductName(),
                r.getBarcode(),
                r.getTotalQtySold(),
                String.format("%.2f", r.getTotalRevenue())
            };
            tableModel.addRow(row);
        }

        // 3. Load Top 5 Products into Bar Chart Component
        List<ProductSaleReport> topProducts = reportDAO.getTopSellingProducts(5);
        chartPanel.setChartData(topProducts);
    }
}
