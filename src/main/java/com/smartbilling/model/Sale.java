package com.smartbilling.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale
 * 
 * Model POJO representing a complete billing transaction header.
 */
public class Sale {

    private int saleId;
    private String invoiceNo;
    private Timestamp saleDate;
    private int userId;
    private String userName;
    private double totalAmount;
    private String paymentMode; // "CASH", "CARD", "UPI"
    private List<SaleItem> items;

    public Sale() {
        this.items = new ArrayList<>();
    }

    public Sale(int userId, String paymentMode) {
        this.userId = userId;
        this.paymentMode = paymentMode;
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
        calculateTotal();
    }

    public void addItem(SaleItem item) {
        this.items.add(item);
        calculateTotal();
    }

    public void calculateTotal() {
        double sum = 0.0;
        for (SaleItem item : items) {
            sum += item.getSubtotal();
        }
        this.totalAmount = sum;
    }
}
