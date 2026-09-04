package com.smartbilling.model;

import java.sql.Timestamp;

/**
 * Product
 * 
 * Model POJO representing an inventory product item in smart_billing_db.
 */
public class Product {

    private int productId;
    private String barcode;
    private String name;
    private String category;
    private double price;       // Selling Price
    private double costPrice;   // Purchase/Cost Price
    private int quantity;       // Available stock count
    private int minStockLevel;  // Low-stock alert threshold
    private Integer vendorId;   // Foreign key (can be null if vendor deleted)
    private String vendorName;  // Display name from joined vendors table
    private Timestamp createdAt;

    public Product() {
    }

    public Product(int productId, String barcode, String name, String category, double price, 
                   double costPrice, int quantity, int minStockLevel, Integer vendorId, String vendorName) {
        this.productId = productId;
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        this.price = price;
        this.costPrice = costPrice;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    /**
    * Decreases the stock quantity by the specified amount.
    * @param amount the amount to decrease
    * @throws IllegalArgumentException if amount exceeds current quantity
    */
    public void decreaseStock(int amount) {
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Insufficient stock: requested " + amount + ", available " + this.quantity);
        }
        this.quantity -= amount;
    }


    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName != null ? vendorName : "N/A";
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isLowStock() {
        return this.quantity <= this.minStockLevel;
    }
}
