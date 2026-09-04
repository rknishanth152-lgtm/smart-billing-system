package com.smartbilling.model;

import java.sql.Timestamp;

/**
 * Simple representation of a completed sale transaction used by the sales entry UI.
 */
public class SimpleSale {
    private int productId;
    private int employeeId;
    private int quantity;
    private double totalAmount;
    private Timestamp saleTimestamp;

    public SimpleSale() {}

    public SimpleSale(int productId, int employeeId, int quantity, double totalAmount) {
        this.productId = productId;
        this.employeeId = employeeId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }

    // Getters and setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public Timestamp getSaleTimestamp() { return saleTimestamp; }
    public void setSaleTimestamp(Timestamp saleTimestamp) { this.saleTimestamp = saleTimestamp; }
}
