package com.smartbilling.ui;

import com.smartbilling.dao.VendorDAO;
import com.smartbilling.model.Vendor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * VendorManagementPanel
 * 
 * Swing Panel for managing suppliers/vendors in smart_billing_db.
 * Includes Add Vendor, Update Vendor, Delete Vendor, and Refresh features.
 */
public class VendorManagementPanel extends JPanel {

    private final VendorDAO vendorDAO;

    private JTable vendorTable;
    private DefaultTableModel tableModel;
    private List<Vendor> currentVendorList;

    public VendorManagementPanel() {
        this.vendorDAO = new VendorDAO();
        initUI();
        loadVendorsFromDatabase();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Header Toolbar Panel
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Vendor & Supplier Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        JButton addBtn = new JButton("Add Vendor");
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

        toolbarPanel.add(titleLabel, BorderLayout.WEST);
        toolbarPanel.add(buttonGroup, BorderLayout.EAST);
        add(toolbarPanel, BorderLayout.NORTH);

        // 2. JTable Setup
        String[] columnNames = {"Vendor ID", "Vendor Name", "Contact Person", "Phone", "Email", "Address"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only cells
            }
        };

        vendorTable = new JTable(tableModel);
        vendorTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        vendorTable.setRowHeight(28);
        vendorTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        vendorTable.getTableHeader().setBackground(new Color(241, 245, 249));
        vendorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(vendorTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Event Listeners
        addBtn.addActionListener(e -> showAddVendorDialog());
        updateBtn.addActionListener(e -> showUpdateVendorDialog());
        deleteBtn.addActionListener(e -> deleteSelectedVendor());
        refreshBtn.addActionListener(e -> loadVendorsFromDatabase());
    }

    /**
     * Fetches vendor records from MySQL via VendorDAO and populates JTable.
     */
    public void loadVendorsFromDatabase() {
        tableModel.setRowCount(0);
        currentVendorList = vendorDAO.getAllVendors();

        for (Vendor v : currentVendorList) {
            Object[] row = {
                v.getVendorId(),
                v.getName(),
                v.getContactPerson() != null ? v.getContactPerson() : "-",
                v.getPhone(),
                v.getEmail() != null ? v.getEmail() : "-",
                v.getAddress() != null ? v.getAddress() : "-"
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Displays form dialog to add a new vendor.
     */
    private void showAddVendorDialog() {
        JTextField nameField = new JTextField();
        JTextField contactPersonField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.add(new JLabel("Vendor Name:*")); formPanel.add(nameField);
        formPanel.add(new JLabel("Contact Person:")); formPanel.add(contactPersonField);
        formPanel.add(new JLabel("Phone Number:*")); formPanel.add(phoneField);
        formPanel.add(new JLabel("Email Address:")); formPanel.add(emailField);
        formPanel.add(new JLabel("Office Address:")); formPanel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Add New Vendor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vendor Name and Phone Number are required!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Vendor newVendor = new Vendor(0, name, contactPerson, phone, email, address);
            boolean success = vendorDAO.addVendor(newVendor);

            if (success) {
                JOptionPane.showMessageDialog(this, "Vendor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVendorsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add vendor.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Displays form dialog to update selected vendor.
     */
    private void showUpdateVendorDialog() {
        int selectedRow = vendorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a vendor from the table to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vendor selectedVendor = currentVendorList.get(selectedRow);

        JTextField nameField = new JTextField(selectedVendor.getName());
        JTextField contactPersonField = new JTextField(selectedVendor.getContactPerson());
        JTextField phoneField = new JTextField(selectedVendor.getPhone());
        JTextField emailField = new JTextField(selectedVendor.getEmail());
        JTextField addressField = new JTextField(selectedVendor.getAddress());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.add(new JLabel("Vendor Name:*")); formPanel.add(nameField);
        formPanel.add(new JLabel("Contact Person:")); formPanel.add(contactPersonField);
        formPanel.add(new JLabel("Phone Number:*")); formPanel.add(phoneField);
        formPanel.add(new JLabel("Email Address:")); formPanel.add(emailField);
        formPanel.add(new JLabel("Office Address:")); formPanel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, "Update Vendor (ID: " + selectedVendor.getVendorId() + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String contactPerson = contactPersonField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vendor Name and Phone Number are required!",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            selectedVendor.setName(name);
            selectedVendor.setContactPerson(contactPerson);
            selectedVendor.setPhone(phone);
            selectedVendor.setEmail(email);
            selectedVendor.setAddress(address);

            boolean success = vendorDAO.updateVendor(selectedVendor);

            if (success) {
                JOptionPane.showMessageDialog(this, "Vendor updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVendorsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update vendor.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Deletes selected vendor from MySQL.
     */
    private void deleteSelectedVendor() {
        int selectedRow = vendorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a vendor from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vendor selectedVendor = currentVendorList.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete vendor: '" + selectedVendor.getName() + "' (ID: " + selectedVendor.getVendorId() + ")?\n"
            + "Note: Any products currently supplied by this vendor will have their vendor reference set to N/A.",
            "Confirm Vendor Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = vendorDAO.deleteVendor(selectedVendor.getVendorId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Vendor deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVendorsFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete vendor.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
