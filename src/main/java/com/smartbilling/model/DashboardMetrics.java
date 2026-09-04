package com.smartbilling.model;

/**
 * DashboardMetrics
 * 
 * POJO representing aggregate real-time metrics displayed on the Admin Dashboard.
 */
public class DashboardMetrics {

    private int totalProducts;
    private int lowStockCount;
    private double todaysSales;

    public DashboardMetrics() {
        this.totalProducts = 0;
        this.lowStockCount = 0;
        this.todaysSales = 0.0;
    }

    public DashboardMetrics(int totalProducts, int lowStockCount, double todaysSales) {
        this.totalProducts = totalProducts;
        this.lowStockCount = lowStockCount;
        this.todaysSales = todaysSales;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public double getTodaysSales() {
        return todaysSales;
    }

    public void setTodaysSales(double todaysSales) {
        this.todaysSales = todaysSales;
    }
}
