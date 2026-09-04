package com.smartbilling.ui;

import com.smartbilling.model.ProductSaleReport;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SalesBarChartPanel
 * 
 * Custom Swing component that renders a 2D Graphical Bar Chart using Vanilla Java Graphics2D.
 * Dynamically scales and draws bar charts for Top-Selling Products based on real database records.
 */
public class SalesBarChartPanel extends JPanel {

    private List<ProductSaleReport> chartData;

    public SalesBarChartPanel() {
        this.chartData = new ArrayList<>();
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
    }

    public void setChartData(List<ProductSaleReport> data) {
        this.chartData = data != null ? data : new ArrayList<>();
        repaint(); // Re-trigger paintComponent to redraw graph with fresh data
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable Anti-Aliasing for crisp, smooth bar rendering and text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 1. Draw Section Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(new Color(15, 23, 42));
        g2.drawString("Top Selling Products Performance (Revenue)", 20, 30);

        if (chartData == null || chartData.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.setColor(new Color(148, 163, 184));
            g2.drawString("No sales transactions recorded yet. Complete sales in POS Billing to generate performance graph.", 20, height / 2);
            return;
        }

        // 2. Chart Layout Margins
        int paddingLeft = 60;
        int paddingRight = 30;
        int paddingTop = 60;
        int paddingBottom = 60;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        // 3. Find Maximum Value for Scaling Bar Heights
        double maxVal = 0.0;
        for (ProductSaleReport item : chartData) {
            if (item.getTotalRevenue() > maxVal) {
                maxVal = item.getTotalRevenue();
            }
        }
        if (maxVal == 0.0) maxVal = 100.0; // Avoid divide by zero

        // 4. Draw X and Y Axes
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(1.5f));

        // Y-Axis line
        g2.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight);
        // X-Axis line
        g2.drawLine(paddingLeft, paddingTop + chartHeight, paddingLeft + chartWidth, paddingTop + chartHeight);

        // 5. Draw Horizontal Grid Lines
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f}, 0.0f));
        int gridDivisions = 4;
        NumberFormat currFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        for (int i = 0; i <= gridDivisions; i++) {
            int y = paddingTop + chartHeight - (int) ((chartHeight / (double) gridDivisions) * i);
            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(paddingLeft, y, paddingLeft + chartWidth, y);

            // Y-Axis Value Label
            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            double val = (maxVal / gridDivisions) * i;
            String label = currFmt.format(val);
            g2.drawString(label, 5, y + 4);
        }

        // 6. Draw Bars for Top Products
        int itemCount = chartData.size();
        int slotWidth = chartWidth / itemCount;
        int barWidth = Math.min(60, slotWidth - 30);

        Color[] barColors = {
            new Color(37, 99, 235),  // Royal Blue
            new Color(16, 185, 129), // Emerald Green
            new Color(245, 158, 11), // Amber Yellow
            new Color(139, 92, 246), // Purple
            new Color(236, 72, 153)  // Pink
        };

        for (int i = 0; i < itemCount; i++) {
            ProductSaleReport report = chartData.get(i);

            int x = paddingLeft + (i * slotWidth) + (slotWidth - barWidth) / 2;
            int barHeight = (int) ((report.getTotalRevenue() / maxVal) * chartHeight);
            int y = paddingTop + chartHeight - barHeight;

            // Draw Bar Rect
            g2.setColor(barColors[i % barColors.length]);
            g2.fillRect(x, y, barWidth, barHeight);

            // Draw Bar Border
            g2.setColor(g2.getColor().darker());
            g2.drawRect(x, y, barWidth, barHeight);

            // Draw Top Value Text Label (e.g. ₹1,997.00)
            g2.setColor(new Color(15, 23, 42));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String valStr = currFmt.format(report.getTotalRevenue());
            int valWidth = g2.getFontMetrics().stringWidth(valStr);
            g2.drawString(valStr, x + (barWidth - valWidth) / 2, y - 8);

            // Draw Units Sold Tag
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(100, 116, 139));
            String qtyStr = report.getTotalQtySold() + " sold";
            int qtyWidth = g2.getFontMetrics().stringWidth(qtyStr);
            g2.drawString(qtyStr, x + (barWidth - qtyWidth) / 2, y - 22);

            // Draw Product Name Label on X-Axis
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(new Color(30, 41, 59));
            String nameStr = report.getProductName();
            if (nameStr.length() > 14) {
                nameStr = nameStr.substring(0, 12) + "..";
            }
            int nameWidth = g2.getFontMetrics().stringWidth(nameStr);
            g2.drawString(nameStr, x + (barWidth - nameWidth) / 2, paddingTop + chartHeight + 20);
        }
    }
}
