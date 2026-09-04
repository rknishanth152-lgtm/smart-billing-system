package com.smartbilling.ui;

import com.smartbilling.dao.ProductDAO;
import com.smartbilling.dao.SaleDAO;
import com.smartbilling.model.Product;
import com.smartbilling.model.Sale;
import com.smartbilling.model.SaleItem;
import com.smartbilling.service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BillingPanel
 * 
 * Swing Panel for POS Billing operations.
 * Allows selecting products, adding to cart, validating available stock,
 * and saving complete sale transactions into MySQL atomically.
 */
public class BillingPanel extends JPanel {

    private final ProductDAO productDAO;
    private final SaleDAO saleDAO;

    private JComboBox<ProductComboItem> productComboBox;
    private JTextField qtyField;
    private JLabel availableStockLabel;
    private JLabel unitPriceLabel;

    private JTable cartTable;
    private DefaultTableModel tableModel;
    private List<SaleItem> cartItems;

    private JLabel grandTotalLabel;
    private JComboBox<String> paymentModeCombo;
    private JButton completeSaleBtn;
    private JButton removeSelectedBtn;

    private Product selectedProduct;

    public BillingPanel() {
        this.productDAO = new ProductDAO();
        this.saleDAO = new SaleDAO();
        this.cartItems = new ArrayList<>();
        initUI();
        loadProductList();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // -------------------------------------------------------------
        // 1. TOP HEADER & ITEM SELECTION PANEL
        // -------------------------------------------------------------
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        JLabel titleLabel = new JLabel("Point of Sale (POS) - New Billing Entry");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));
        topContainer.add(titleLabel, BorderLayout.NORTH);

        // Input Form Box
        JPanel selectionBox = new JPanel(new GridBagLayout());
        selectionBox.setBackground(new Color(248, 250, 252));
        selectionBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Select Product
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        selectionBox.add(new JLabel("Select Product:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.45;
        productComboBox = new JComboBox<>();
        productComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        selectionBox.add(productComboBox, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.2;
        availableStockLabel = new JLabel("Stock: 0");
        availableStockLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        availableStockLabel.setForeground(new Color(37, 99, 235));
        selectionBox.add(availableStockLabel, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.2;
        unitPriceLabel = new JLabel("Price: ₹0.00");
        unitPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        unitPriceLabel.setForeground(new Color(16, 185, 129));
        selectionBox.add(unitPriceLabel, gbc);

        // Row 2: Quantity & Add to Cart
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        selectionBox.add(new JLabel("Enter Quantity:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.45;
        qtyField = new JTextField("1");
        qtyField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        selectionBox.add(qtyField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.4;
        JButton addToCartBtn = new JButton("Add to Bill Cart");
        addToCartBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addToCartBtn.setBackground(new Color(37, 99, 235));
        addToCartBtn.setForeground(Color.BLACK);
        selectionBox.add(addToCartBtn, gbc);

        topContainer.add(selectionBox, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // 2. CENTER: LIVE CART TABLE
        // -------------------------------------------------------------
        String[] columns = {"Barcode", "Product Name", "Unit Price (₹)", "Quantity", "Subtotal (₹)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        cartTable = new JTable(tableModel);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cartTable.setRowHeight(28);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createTitledBorder("Customer Billing Cart"));
        add(cartScroll, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // 3. SOUTH: SUMMARY TOTAL & COMPLETE TRANSACTION PANEL
        // -------------------------------------------------------------
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 10));
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Left Controls: Remove Item
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);
        removeSelectedBtn = new JButton("Remove Selected Item");
        removeSelectedBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftControls.add(removeSelectedBtn);

        // Right Controls: Grand Total, Payment Mode, Submit Bill
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightControls.setOpaque(false);

        rightControls.add(new JLabel("Payment Mode:"));
        paymentModeCombo = new JComboBox<>(new String[]{"CASH", "CARD", "UPI"});
        paymentModeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rightControls.add(paymentModeCombo);

        grandTotalLabel = new JLabel("Grand Total: ₹0.00");
        grandTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        grandTotalLabel.setForeground(new Color(16, 185, 129));
        rightControls.add(grandTotalLabel);

        completeSaleBtn = new JButton("Complete Sale & Save Bill");
        completeSaleBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        completeSaleBtn.setBackground(new Color(16, 185, 129));
        completeSaleBtn.setForeground(Color.BLACK);
        rightControls.add(completeSaleBtn);

        bottomPanel.add(leftControls, BorderLayout.WEST);
        bottomPanel.add(rightControls, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // -------------------------------------------------------------
        // 4. EVENT LISTENERS
        // -------------------------------------------------------------
        productComboBox.addActionListener(e -> updateSelectedProductDetails());
        addToCartBtn.addActionListener(e -> addItemToCart());
        removeSelectedBtn.addActionListener(e -> removeSelectedItemFromCart());
        completeSaleBtn.addActionListener(e -> processCompleteSale());
    }

    /**
     * Loads available products from MySQL database into the dropdown selector.
     */
    public void loadProductList() {
        productComboBox.removeAllItems();
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            productComboBox.addItem(new ProductComboItem(p));
        }
        updateSelectedProductDetails();
    }

    private void updateSelectedProductDetails() {
        ProductComboItem selectedItem = (ProductComboItem) productComboBox.getSelectedItem();
        if (selectedItem != null) {
            this.selectedProduct = selectedItem.getProduct();
            availableStockLabel.setText("Available Stock: " + selectedProduct.getQuantity());
            unitPriceLabel.setText(String.format("Unit Price: ₹%.2f", selectedProduct.getPrice()));
            
            if (selectedProduct.getQuantity() <= 0) {
                availableStockLabel.setForeground(new Color(225, 29, 72)); // Red for Out of Stock
            } else {
                availableStockLabel.setForeground(new Color(37, 99, 235));
            }
        } else {
            this.selectedProduct = null;
            availableStockLabel.setText("Stock: 0");
            unitPriceLabel.setText("Price: ₹0.00");
        }
    }

    private void addItemToCart() {
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int inputQty = Integer.parseInt(qtyField.getText().trim());

            if (inputQty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check existing quantity already in cart for this product
            int currentCartQty = 0;
            for (SaleItem item : cartItems) {
                if (item.getProductId() == selectedProduct.getProductId()) {
                    currentCartQty += item.getQuantity();
                }
            }

            int totalRequestedQty = currentCartQty + inputQty;

            // PREVENT SELLING MORE QUANTITY THAN AVAILABLE STOCK
            if (totalRequestedQty > selectedProduct.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot add item! Requested quantity (" + totalRequestedQty + ") exceeds available stock (" + selectedProduct.getQuantity() + ").",
                    "Insufficient Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if item already exists in cart -> update quantity
            boolean found = false;
            for (SaleItem item : cartItems) {
                if (item.getProductId() == selectedProduct.getProductId()) {
                    item.setQuantity(item.getQuantity() + inputQty);
                    found = true;
                    break;
                }
            }

            if (!found) {
                cartItems.add(new SaleItem(
                    selectedProduct.getProductId(),
                    selectedProduct.getName(),
                    selectedProduct.getBarcode(),
                    inputQty,
                    selectedProduct.getPrice()
                ));
            }

            refreshCartTable();
            qtyField.setText("1");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedItemFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the cart to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cartItems.remove(selectedRow);
        refreshCartTable();
    }

    private void refreshCartTable() {
        tableModel.setRowCount(0);
        double grandTotal = 0.0;

        for (SaleItem item : cartItems) {
            Object[] row = {
                item.getBarcode(),
                item.getProductName(),
                String.format("%.2f", item.getUnitPrice()),
                item.getQuantity(),
                String.format("%.2f", item.getSubtotal())
            };
            tableModel.addRow(row);
            grandTotal += item.getSubtotal();
        }

        NumberFormat currFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        grandTotalLabel.setText("Grand Total: " + currFormat.format(grandTotal));
    }

    private void processCompleteSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Billing cart is empty! Add products before completing the sale.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // SECURITY FIX: abort if session is invalid — do not fall back to userId=1
        if (AuthService.getCurrentUser() == null) {
            JOptionPane.showMessageDialog(this,
                "Session error: No logged-in user found. Please log in again.",
                "Session Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int currentUserId = AuthService.getCurrentUser().getUserId();
        String paymentMode = (String) paymentModeCombo.getSelectedItem();

        Sale sale = new Sale(currentUserId, paymentMode);
        for (SaleItem item : cartItems) {
            sale.addItem(item);
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format("Confirm Bill Transaction?\nTotal Amount: ₹%.2f\nPayment Mode: %s\nTotal Items: %d",
                    sale.getTotalAmount(), paymentMode, cartItems.size()),
            "Confirm Sale",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Process Sale Transaction via SaleDAO (Atomic Database Transaction)
            String generatedInvoice = saleDAO.processTransaction(sale);

            if (generatedInvoice != null) {
                // Display Clear Success Invoice Message
                JOptionPane.showMessageDialog(this,
                    "Sale Completed Successfully!\n\n"
                    + "Bill No: " + generatedInvoice + "\n"
                    + "Total Amount Paid: ₹" + String.format("%.2f", sale.getTotalAmount()) + "\n"
                    + "Payment Method: " + paymentMode + "\n\n"
                    + "Product stock quantities have been updated in MySQL.",
                    "Billing Transaction Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear cart and reload stock from database
                cartItems.clear();
                refreshCartTable();
                loadProductList();

            } else {
                JOptionPane.showMessageDialog(this,
                    "Transaction Failed!\nDatabase rolled back changes. Check if stock levels were altered concurrently.",
                    "Transaction Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Helper Wrapper class for JComboBox rendering
    private static class ProductComboItem {
        private final Product product;

        public ProductComboItem(Product product) {
            this.product = product;
        }

        public Product getProduct() {
            return product;
        }

        @Override
        public String toString() {
            return product.getName() + " [Code: " + product.getBarcode() + "] - ₹" + String.format("%.2f", product.getPrice())
                    + " (In Stock: " + product.getQuantity() + ")";
        }
    }
}
