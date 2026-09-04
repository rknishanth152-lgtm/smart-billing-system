package com.smartbilling.model;

/**
 * ProductSaleReport
 * 
 * POJO representing aggregate sales performance for an individual product.
 */
public class ProductSaleReport {

    private String productName;
    private String barcode;
    private int totalQtySold;
    private double totalRevenue;

    public ProductSaleReport() {
    }

    public ProductSaleReport(String productName, String barcode, int totalQtySold, double totalRevenue) {
        this.productName = productName;
        this.barcode = barcode;
        this.totalQtySold = totalQtySold;
        this.totalRevenue = totalRevenue;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getTotalQtySold() {
        return totalQtySold;
    }

    public void setTotalQtySold(int totalQtySold) {
        this.totalQtySold = totalQtySold;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
