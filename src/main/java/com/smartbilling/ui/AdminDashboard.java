package com.smartbilling.ui;

import com.smartbilling.dao.DashboardDAO;
import com.smartbilling.model.DashboardMetrics;
import com.smartbilling.model.User;
import com.smartbilling.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * AdminDashboard
 *
 * Phase 1 UI Redesign — Shree Annapurna Edible Oil Company.
 *
 * Visual design:
 *  - Dark olive sidebar (SIDEBAR_BG) with logo, section label, nav items
 *  - Warm off-white content background
 *  - Top bar with page breadcrumb + user info + refresh
 *  - 3 metric cards (Total Products / Low Stock / Today's Sales)
 *  - CardLayout module container for sub-panels (UNCHANGED architecture)
 *
 * Business logic unchanged:
 *  - loadRealDatabaseMetrics() queries DashboardDAO
 *  - switchTab() switches CardLayout + refreshes sub-panels
 *  - performLogout() calls AuthService.logout()
 */
public class AdminDashboard extends JFrame {

    private final User         currentUser;
    private final DashboardDAO dashboardDAO;

    // Metric card value labels — updated by loadRealDatabaseMetrics()
    private JLabel totalProductsValLabel;
    private JLabel lowStockValLabel;
    private JLabel todaySalesValLabel;

    // Navigation
    private JButton[] navButtons;

    // Page title in topbar
    private JLabel pageTitle;

    // CardLayout module container — existing sub-panel architecture preserved
    private JPanel              moduleContainer;
    private StockOverviewPanel  stockOverviewPanel;
    private VendorManagementPanel vendorManagementPanel;
    private StockAlertPanel     stockAlertPanel;
    private SalesReportPanel    salesReportPanel;

    public AdminDashboard(User user) {
        if (user == null || !user.isAdmin()) {
            JOptionPane.showMessageDialog(null,
                "Access Denied: Administrative privileges required.",
                "Security Warning", JOptionPane.ERROR_MESSAGE);
            throw new SecurityException("Unauthorized access attempt to Admin Dashboard.");
        }
        this.currentUser  = user;
        this.dashboardDAO = new DashboardDAO();
        initUI();
        loadRealDatabaseMetrics();
    }

    private void initUI() {
        setTitle("Shree Annapurna — Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.BG);

        root.add(buildSidebar(),   BorderLayout.WEST);
        root.add(buildMainArea(),  BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Sidebar ────────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(UITheme.SIDEBAR_W, 0));

        sidebar.add(buildBrandArea());

        // NAVIGATION section label
        JLabel navLabel = UITheme.sidebarSectionLabel("Modules");
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navLabel);

        // Nav items
        String[] labels = {
            "  Dashboard",
            "  Stock Overview",
            "  Vendors",
            "  Low Stock Alert",
            "  Sales Report"
        };
        navButtons = new JButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = makeNavBtn(labels[i]);
            navButtons[i] = btn;
            btn.addActionListener(e -> onNavClick(idx));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(btn);
        }
        setActiveNav(0);

        sidebar.add(Box.createVerticalGlue());

        // Divider
        JSeparator div = new JSeparator();
        div.setForeground(UITheme.SIDEBAR_DIVIDER);
        div.setBackground(UITheme.SIDEBAR_DIVIDER);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(div);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        // Logout
        JButton logoutBtn = makeNavBtn("  Logout");
        logoutBtn.setForeground(new Color(210, 148, 128));
        logoutBtn.addActionListener(e -> performLogout());
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoutBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        return sidebar;
    }

    private JPanel buildBrandArea() {
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(UITheme.SIDEBAR_BG);
        brand.setBorder(BorderFactory.createEmptyBorder(20, 12, 12, 12));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(UITheme.SIDEBAR_W, 140));

        ImageIcon logo = UITheme.loadLogo(168, 80);
        if (logo != null) {
            JLabel logoLbl = new JLabel(logo);
            logoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(logoLbl);
            brand.add(Box.createRigidArea(new Dimension(0, 6)));
        } else {
            JLabel name = new JLabel("SHREE ANNAPURNA");
            name.setFont(UITheme.f(Font.BOLD, 13));
            name.setForeground(Color.WHITE);
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(name);

            JLabel sub = new JLabel("Edible Oil Company");
            sub.setFont(UITheme.f(Font.PLAIN, 10));
            sub.setForeground(UITheme.GOLD_LIGHT);
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(sub);
            brand.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Gold accent separator
        JSeparator gs = new JSeparator();
        gs.setForeground(UITheme.GOLD);
        gs.setBackground(UITheme.GOLD);
        gs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        brand.add(gs);

        return brand;
    }

    private JButton makeNavBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(UITheme.F_NAV);
        b.setForeground(UITheme.SIDEBAR_TEXT);
        b.setBackground(UITheme.SIDEBAR_BG);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(UITheme.SIDEBAR_W, 38));
        b.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        return b;
    }

    private void setActiveNav(int index) {
        if (navButtons == null) return;
        for (int i = 0; i < navButtons.length; i++) {
            if (i == index) {
                navButtons[i].setBackground(UITheme.SIDEBAR_ACTIVE);
                navButtons[i].setForeground(Color.WHITE);
                navButtons[i].setFont(UITheme.F_NAV_BOLD);
            } else {
                navButtons[i].setBackground(UITheme.SIDEBAR_BG);
                navButtons[i].setForeground(UITheme.SIDEBAR_TEXT);
                navButtons[i].setFont(UITheme.F_NAV);
            }
        }
    }

    private void onNavClick(int idx) {
        switch (idx) {
            case 1: switchTab("Stock Overview"); break;
            case 2: switchTab("Vendor Details"); break;
            case 3: switchTab("Stock Alert");    break;
            case 4: switchTab("Sales Report");   break;
            default: switchTab("Dashboard");     break;
        }
    }

    // ── Main Content Area ──────────────────────────────────────────────────────

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0, 0));
        main.setBackground(UITheme.BG);
        main.add(buildTopBar(),      BorderLayout.NORTH);
        main.add(buildContentArea(), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 0));
        bar.setBackground(UITheme.SURFACE);
        bar.setPreferredSize(new Dimension(0, UITheme.TOPBAR_H));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER));

        pageTitle = new JLabel("   Dashboard");
        pageTitle.setFont(UITheme.F_PAGE);
        pageTitle.setForeground(UITheme.TEXT);
        bar.add(pageTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 9));
        right.setOpaque(false);

        JLabel userLbl = new JLabel(currentUser.getFullName() + "  \u2022  ADMIN");
        userLbl.setFont(UITheme.F_SMALL_BOLD);
        userLbl.setForeground(UITheme.TEXT_MUTED);
        right.add(userLbl);

        JButton refreshBtn = UITheme.secondaryBtn("\u21BA  Refresh");
        refreshBtn.setFont(UITheme.F_SMALL_BOLD);
        refreshBtn.addActionListener(e -> loadRealDatabaseMetrics());
        right.add(refreshBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildContentArea() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(UITheme.BG);

        content.add(buildMetricBar(), BorderLayout.NORTH);

        moduleContainer = new JPanel(new CardLayout());
        moduleContainer.setBackground(UITheme.BG);
        moduleContainer.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        // Default dashboard welcome card
        moduleContainer.add(buildWelcomeCard(), "SUMMARY");

        // Sub-panels (existing classes, unchanged logic)
        stockOverviewPanel    = new StockOverviewPanel();
        vendorManagementPanel = new VendorManagementPanel();
        stockAlertPanel       = new StockAlertPanel();
        salesReportPanel      = new SalesReportPanel();

        moduleContainer.add(stockOverviewPanel,    "STOCK_OVERVIEW");
        moduleContainer.add(vendorManagementPanel, "VENDOR_DETAILS");
        moduleContainer.add(stockAlertPanel,       "STOCK_ALERT");
        moduleContainer.add(salesReportPanel,      "SALES_REPORT");

        content.add(moduleContainer, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildMetricBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 12, 0));
        bar.setBackground(UITheme.BG);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));
        bar.setPreferredSize(new Dimension(0, 96));

        totalProductsValLabel = new JLabel("0", SwingConstants.RIGHT);
        bar.add(buildMetricCard("Total Products", totalProductsValLabel,
                UITheme.PRIMARY,  UITheme.SURFACE));

        lowStockValLabel = new JLabel("0", SwingConstants.RIGHT);
        bar.add(buildMetricCard("Low Stock Alerts", lowStockValLabel,
                UITheme.WARNING,  UITheme.WARNING_BG));

        todaySalesValLabel = new JLabel("\u20B90.00", SwingConstants.RIGHT);
        bar.add(buildMetricCard("Today's Sales", todaySalesValLabel,
                UITheme.SUCCESS,  new Color(236, 250, 242)));

        return bar;
    }

    private JPanel buildMetricCard(String title, JLabel valLabel,
                                    Color accent, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.F_METRIC_LBL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);
        card.add(titleLbl, BorderLayout.NORTH);

        valLabel.setFont(UITheme.F_METRIC_VAL);
        valLabel.setForeground(accent);
        card.add(valLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildWelcomeCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(26, 26, 26, 26)
        ));

        JLabel hello = new JLabel("Welcome back, " + currentUser.getFullName());
        hello.setFont(UITheme.F_SECTION);
        hello.setForeground(UITheme.TEXT);
        card.add(hello, BorderLayout.NORTH);

        JLabel desc = new JLabel(
            "<html><body style='font-family:Segoe UI; font-size:13px; color:#586648;'>" +
            "Select a module from the sidebar to manage <b>Stock</b>, <b>Vendors</b>, " +
            "review <b>Low Stock Alerts</b>, or view <b>Sales Reports</b>.<br><br>" +
            "Metrics displayed above are fetched live from the MySQL database." +
            "</body></html>"
        );
        card.add(desc, BorderLayout.CENTER);

        return card;
    }

    // ── Business Logic — UNCHANGED ─────────────────────────────────────────────

    /**
     * Queries MySQL via DashboardDAO to populate the three metric cards.
     * Also triggers data refresh in all sub-panels if already instantiated.
     */
    private void loadRealDatabaseMetrics() {
        DashboardMetrics metrics = dashboardDAO.getMetrics();

        totalProductsValLabel.setText(String.valueOf(metrics.getTotalProducts()));
        lowStockValLabel.setText(String.valueOf(metrics.getLowStockCount()));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        todaySalesValLabel.setText(currencyFormat.format(metrics.getTodaysSales()));

        if (stockOverviewPanel    != null) stockOverviewPanel.loadProductsFromDatabase();
        if (vendorManagementPanel != null) vendorManagementPanel.loadVendorsFromDatabase();
        if (stockAlertPanel       != null) stockAlertPanel.loadLowStockProducts();
        if (salesReportPanel      != null) salesReportPanel.loadReportDataFromDatabase();
    }

    private void switchTab(String tabName) {
        CardLayout cl = (CardLayout) moduleContainer.getLayout();
        if (tabName.contains("Stock Overview")) {
            setActiveNav(1);
            pageTitle.setText("   Stock Overview");
            stockOverviewPanel.loadProductsFromDatabase();
            loadRealDatabaseMetrics();
            cl.show(moduleContainer, "STOCK_OVERVIEW");
        } else if (tabName.contains("Vendor Details")) {
            setActiveNav(2);
            pageTitle.setText("   Vendors");
            vendorManagementPanel.loadVendorsFromDatabase();
            cl.show(moduleContainer, "VENDOR_DETAILS");
        } else if (tabName.contains("Stock Alert")) {
            setActiveNav(3);
            pageTitle.setText("   Low Stock Alert");
            stockAlertPanel.loadLowStockProducts();
            loadRealDatabaseMetrics();
            cl.show(moduleContainer, "STOCK_ALERT");
        } else if (tabName.contains("Sales Report")) {
            setActiveNav(4);
            pageTitle.setText("   Sales Report");
            salesReportPanel.loadReportDataFromDatabase();
            loadRealDatabaseMetrics();
            cl.show(moduleContainer, "SALES_REPORT");
        } else {
            setActiveNav(0);
            pageTitle.setText("   Dashboard");
            loadRealDatabaseMetrics();
            cl.show(moduleContainer, "SUMMARY");
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of Admin Dashboard?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        }
    }
}
