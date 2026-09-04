package com.smartbilling.ui;

import com.smartbilling.dao.ProductDAO;
import com.smartbilling.dao.VendorDAO;
import com.smartbilling.model.Product;
import com.smartbilling.model.Vendor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * StockOverviewPanel
 * 
 * Swing Panel for displaying and managing products from the MySQL products table.
 * Includes Add Product, Update Product, Delete Product, and Refresh features.
 */
public class StockOverviewPanel extends JPanel {

    private final ProductDAO productDAO;
    private final VendorDAO vendorDAO;

    private JTable productTable;
    private DefaultTableModel tableModel;
    private List<Product> currentProductList;

    /** When true, Add/Update/Delete controls are hidden (Employee read-only view). */
    private final boolean readOnly;

    /** Admin mode — all controls enabled. */
    public StockOverviewPanel() {
        this(false);
    }

    /** Mode-aware constructor. Pass readOnly=true for Employee stock view. */
    public StockOverviewPanel(boolean readOnly) {
        this.readOnly  = readOnly;
        this.productDAO = new ProductDAO();
        this.vendorDAO = new VendorDAO();
        initUI();
        loadProductsFromDatabase();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Header Toolbar Panel
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setOpaque(false);

        String title = readOnly ? "Product Stock" : "Stock Overview / Product Management";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.F_PAGE);
        titleLabel.setForeground(UITheme.TEXT);

        toolbarPanel.add(titleLabel, BorderLayout.WEST);

        // Admin-only toolbar buttons
        if (!readOnly) {
            JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonGroup.setOpaque(false);

            JButton addBtn    = new JButton("Add Product");
            addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            JButton updateBtn = new JButton("Update Selected");
            updateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            JButton deleteBtn = new JButton("Delete Selected");
            deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            JButton refreshBtn = new JButton("Refresh");
            refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            buttonGroup.add(addBtn);
            buttonGroup.add(updateBtn);
            buttonGroup.add(deleteBtn);
            buttonGroup.add(refreshBtn);

            toolbarPanel.add(buttonGroup, BorderLayout.EAST);

            addBtn.addActionListener(e    -> showAddProductDialog());
            updateBtn.addActionListener(e -> showUpdateProductDialog());
            deleteBtn.addActionListener(e -> deleteSelectedProduct());
            refreshBtn.addActionListener(e -> loadProductsFromDatabase());
        } else {
            // Employee view: single Refresh button only
            JButton refreshBtn = UITheme.secondaryBtn("Refresh");
            refreshBtn.addActionListener(e -> loadProductsFromDatabase());
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(refreshBtn);
            toolbarPanel.add(rightPanel, BorderLayout.EAST);
        }

        add(toolbarPanel, BorderLayout.NORTH);

        // 2. JTable Setup — column set depends on mode
        String[] columnNames = readOnly
            ? new String[]{ "Product Name", "Category", "Selling Price (₹)", "Qty in Stock", "Min Stock", "Status" }
            : new String[]{ "ID", "Barcode", "Product Name", "Category",
                            "Vendor", "Purchase Price (₹)", "Selling Price (₹)",
                            "Quantity", "Min Stock" };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table cells are read-only; edited via dialogs
            }
        };

        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        productTable.setRowHeight(28);
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        productTable.getTableHeader().setBackground(new Color(241, 245, 249));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Highlight low stock rows with soft red text
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row < currentProductList.size()) {
                    Product p = currentProductList.get(row);
                    if (p.isLowStock() && !isSelected) {
                        c.setForeground(new Color(225, 29, 72)); // Red highlight for low stock
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (!isSelected) {
                        c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Event Listeners (Admin-mode buttons already wired above in initUI)
    }

    /**
     * Loads product records from MySQL via ProductDAO and updates JTable.
     */
    public void loadProductsFromDatabase() {
        tableModel.setRowCount(0);
        currentProductList = productDAO.getAllProducts();

        for (Product p : currentProductList) {
            if (readOnly) {
                // Employee view: name, category, selling price, quantity, min stock, status
                String status = p.isLowStock() ? "⚠ Low Stock" : "In Stock";
                Object[] row = {
                    p.getName(),
                    p.getCategory(),
                    String.format("₹%.2f", p.getPrice()),
                    p.getQuantity(),
                    p.getMinStockLevel(),
                    status
                };
                tableModel.addRow(row);
            } else {
                // Admin view: all columns
                Object[] row = {
                    p.getProductId(),
                    p.getBarcode(),
                    p.getName(),
                    p.getCategory(),
                    p.getVendorName(),
                    String.format("%.2f", p.getCostPrice()),
                    String.format("%.2f", p.getPrice()),
                    p.getQuantity(),
                    p.getMinStockLevel()
                };
                tableModel.addRow(row);
            }
        }
    }

    /**
     * Displays dialog for adding a new product.
     */
    private void showAddProductDialog() {
        JTextField barcodeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField costPriceField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField minStockField = new JTextField("5");

        JComboBox<Vendor> vendorCombo = new JComboBox<>();
        vendorCombo.addItem(new Vendor(0, "-- Select Vendor (Optional) --"));
        List<Vendor> vendorList = vendorDAO.getAllVendors();
        for (Vendor v : vendorList) {
            vendorCombo.addItem(v);
        }

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 8, 8));
        formPanel.add(new JLabel("Barcode / SKU:")); formPanel.add(barcodeField);
        formPanel.add(new JLabel("Product Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Category:")); formPanel.add(categoryField);
        formPanel.add(new JLabel("Purchase Price (₹):")); formPanel.add(costPriceField);
        formPanel.add(new JLabel("Selling Price (₹):")); formPanel.add(priceField);
        formPanel.add(new JLabel("Current Quantity:")); formPanel.add(quantityField);
        formPanel.add(new JLabel("Min Stock Alert Level:")); formPanel.add(minStockField);
        formPanel.add(new JLabel("Supplier / Vendor:")); formPanel.add(vendorCombo);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add New Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String barcode = barcodeField.getText().trim();
                String name = nameField.getText().trim();
                String category = categoryField.getText().trim();

                if (barcode.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Barcode and Product Name are required!",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double costPrice = Double.parseDouble(costPriceField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());
                int minStock = Integer.parseInt(minStockField.getText().trim());

                Vendor selectedVendor = (Vendor) vendorCombo.getSelectedItem();
                Integer vendorId = (selectedVendor != null && selectedVendor.getVendorId() > 0) 
                        ? selectedVendor.getVendorId() : null;

                Product newProduct = new Product(0, barcode, name, category, price, costPrice, quantity, minStock, vendorId, null);
                boolean success = productDAO.addProduct(newProduct);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadProductsFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add product. Check if barcode already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values for prices and stock quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Displays dialog for updating selected product.
     */
    private void showUpdateProductDialog() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = currentProductList.get(selectedRow);

        JTextField barcodeField = new JTextField(selectedProduct.getBarcode());
        JTextField nameField = new JTextField(selectedProduct.getName());
        JTextField categoryField = new JTextField(selectedProduct.getCategory());
        JTextField costPriceField = new JTextField(String.valueOf(selectedProduct.getCostPrice()));
        JTextField priceField = new JTextField(String.valueOf(selectedProduct.getPrice()));
        JTextField quantityField = new JTextField(String.valueOf(selectedProduct.getQuantity()));
        JTextField minStockField = new JTextField(String.valueOf(selectedProduct.getMinStockLevel()));

        JComboBox<Vendor> vendorCombo = new JComboBox<>();
        vendorCombo.addItem(new Vendor(0, "-- Select Vendor (Optional) --"));
        List<Vendor> vendorList = vendorDAO.getAllVendors();
        Vendor selectedVendorObj = null;

        for (Vendor v : vendorList) {
            vendorCombo.addItem(v);
            if (selectedProduct.getVendorId() != null && v.getVendorId() == selectedProduct.getVendorId()) {
                selectedVendorObj = v;
            }
        }
        if (selectedVendorObj != null) {
            vendorCombo.setSelectedItem(selectedVendorObj);
        }

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 8, 8));
        formPanel.add(new JLabel("Barcode / SKU:")); formPanel.add(barcodeField);
        formPanel.add(new JLabel("Product Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Category:")); formPanel.add(categoryField);
        formPanel.add(new JLabel("Purchase Price (₹):")); formPanel.add(costPriceField);
        formPanel.add(new JLabel("Selling Price (₹):")); formPanel.add(priceField);
        formPanel.add(new JLabel("Current Quantity:")); formPanel.add(quantityField);
        formPanel.add(new JLabel("Min Stock Alert Level:")); formPanel.add(minStockField);
        formPanel.add(new JLabel("Supplier / Vendor:")); formPanel.add(vendorCombo);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Update Product (ID: " + selectedProduct.getProductId() + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String barcode = barcodeField.getText().trim();
                String name = nameField.getText().trim();
                String category = categoryField.getText().trim();

                if (barcode.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Barcode and Product Name are required!",
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double costPrice = Double.parseDouble(costPriceField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());
                int minStock = Integer.parseInt(minStockField.getText().trim());

                Vendor selectedVendor = (Vendor) vendorCombo.getSelectedItem();
                Integer vendorId = (selectedVendor != null && selectedVendor.getVendorId() > 0) 
                        ? selectedVendor.getVendorId() : null;

                selectedProduct.setBarcode(barcode);
                selectedProduct.setName(name);
                selectedProduct.setCategory(category);
                selectedProduct.setCostPrice(costPrice);
                selectedProduct.setPrice(price);
                selectedProduct.setQuantity(quantity);
                selectedProduct.setMinStockLevel(minStock);
                selectedProduct.setVendorId(vendorId);

                boolean success = productDAO.updateProduct(selectedProduct);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadProductsFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update product.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values for prices and stock quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deletes selected product from MySQL.
     */
    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product selectedProduct = currentProductList.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete product: '" + selectedProduct.getName() + "' (ID: " + selectedProduct.getProductId() + ")?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = productDAO.deleteProduct(selectedProduct.getProductId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProductsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete product. It may be linked to existing sale billing records.", "Database Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
