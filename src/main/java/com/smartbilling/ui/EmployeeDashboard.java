package com.smartbilling.ui;

import com.smartbilling.model.User;
import com.smartbilling.service.AuthService;

import javax.swing.*;
import java.awt.*;

/**
 * EmployeeDashboard
 *
 * Phase 1 UI Redesign — Shree Annapurna Edible Oil Company.
 *
 * Visual design:
 *  - Dark olive sidebar matching AdminDashboard palette
 *  - Role-restricted navigation: active buttons (billing/stock) + greyed-out admin items
 *  - Warm off-white content background
 *  - Consistent topbar + layout with AdminDashboard
 *
 * Business logic unchanged:
 *  - billingBtn / viewProductsBtn → show BillingPanel
 *  - salesHistoryBtn → information dialog (unchanged message)
 *  - userAdminBtn / reportsAdminBtn → disabled
 *  - performLogout() → AuthService.logout()
 */
public class EmployeeDashboard extends JFrame {

    private final User currentUser;

    private JPanel                  contentContainer;
    private BillingPanel            billingPanel;
    private StockOverviewPanel      stockPanel;
    private EmployeeSalesHistoryPanel salesHistoryPanel;

    public EmployeeDashboard(User user) {
        if (user == null) {
            JOptionPane.showMessageDialog(null,
                "Access Denied: Valid login session required.",
                "Security Warning", JOptionPane.ERROR_MESSAGE);
            throw new SecurityException("Unauthorized access attempt to Employee Dashboard.");
        }
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setTitle("Shree Annapurna — Billing Desk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 660);
        setMinimumSize(new Dimension(860, 560));
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

        // BILLING section label
        JLabel billingLabel = UITheme.sidebarSectionLabel("Billing");
        billingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(billingLabel);

        // Active buttons (employee has access)
        JButton billingBtn     = makeNavBtn("  New Billing / Sale",  true);
        JButton viewProductsBtn = makeNavBtn("  View Product Stock", true);
        JButton salesHistoryBtn = makeNavBtn("  My Sales History",   true);

        billingBtn.setBackground(UITheme.SIDEBAR_ACTIVE);
        billingBtn.setForeground(Color.WHITE);
        billingBtn.setFont(UITheme.F_NAV_BOLD);

        sidebar.add(billingBtn);      sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(viewProductsBtn); sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(salesHistoryBtn); sidebar.add(Box.createRigidArea(new Dimension(0, 14)));

        // ADMIN section label (restricted)
        JLabel adminLabel = UITheme.sidebarSectionLabel("Admin (Restricted)");
        adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(adminLabel);

        // Disabled admin buttons (visual role restriction)
        JButton userAdminBtn    = makeNavBtn("  User Controls",  false);
        JButton reportsAdminBtn = makeNavBtn("  Sales Reports",  false);

        sidebar.add(userAdminBtn);    sidebar.add(Box.createRigidArea(new Dimension(0, 2)));
        sidebar.add(reportsAdminBtn);

        sidebar.add(Box.createVerticalGlue());

        // Divider + logout
        JSeparator div = new JSeparator();
        div.setForeground(UITheme.SIDEBAR_DIVIDER);
        div.setBackground(UITheme.SIDEBAR_DIVIDER);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(div);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton logoutBtn = makeNavBtn("  Logout", true);
        logoutBtn.setForeground(new Color(210, 148, 128));
        logoutBtn.addActionListener(e -> performLogout());
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoutBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // ── Action listeners ──────────────────────────────────────────────
        billingBtn.addActionListener(e     -> showBillingPanel());
        viewProductsBtn.addActionListener(e -> showStockPanel());
        salesHistoryBtn.addActionListener(e -> showSalesHistoryPanel());

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

        JSeparator gs = new JSeparator();
        gs.setForeground(UITheme.GOLD);
        gs.setBackground(UITheme.GOLD);
        gs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        brand.add(gs);

        return brand;
    }

    private JButton makeNavBtn(String text, boolean enabled) {
        JButton b = new JButton(text);
        b.setFont(UITheme.F_NAV);
        b.setForeground(enabled ? UITheme.SIDEBAR_TEXT : UITheme.SIDEBAR_MUTED);
        b.setBackground(UITheme.SIDEBAR_BG);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setEnabled(enabled);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(enabled
            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            : Cursor.getDefaultCursor());
        b.setMaximumSize(new Dimension(UITheme.SIDEBAR_W, 38));
        b.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
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

        JLabel title = new JLabel("   Billing Desk");
        title.setFont(UITheme.F_PAGE);
        title.setForeground(UITheme.TEXT);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 9));
        right.setOpaque(false);

        JLabel userLbl = new JLabel(currentUser.getFullName() + "  \u2022  EMPLOYEE");
        userLbl.setFont(UITheme.F_SMALL_BOLD);
        userLbl.setForeground(UITheme.TEXT_MUTED);
        right.add(userLbl);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildContentArea() {
        contentContainer = new JPanel(new CardLayout());
        contentContainer.setBackground(UITheme.BG);

        // Welcome card
        JPanel welcomeCard = buildWelcomeCard();
        billingPanel       = new BillingPanel();
        stockPanel         = new StockOverviewPanel(true);  // read-only Employee mode
        salesHistoryPanel  = new EmployeeSalesHistoryPanel();

        contentContainer.add(welcomeCard,      "WELCOME");
        contentContainer.add(billingPanel,     "BILLING");
        contentContainer.add(stockPanel,       "STOCK");
        contentContainer.add(salesHistoryPanel, "SALES_HISTORY");

        return contentContainer;
    }

    private JPanel buildWelcomeCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel welcomeTitle = new JLabel("Welcome to Billing Desk");
        welcomeTitle.setFont(UITheme.f(Font.BOLD, 20));
        welcomeTitle.setForeground(UITheme.TEXT);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeDesc = new JLabel("You have access to point-of-sale billing and product stock lookup.");
        welcomeDesc.setFont(UITheme.F_BODY);
        welcomeDesc.setForeground(UITheme.TEXT_MUTED);
        welcomeDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel accessNotice = new JLabel(
            "\u26A0  Administrative settings and sales reports are restricted to Admin users.");
        accessNotice.setFont(UITheme.F_SMALL);
        accessNotice.setForeground(UITheme.WARNING);
        accessNotice.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startBtn = UITheme.primaryBtn("Start New Billing");
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> showBillingPanel());

        inner.add(welcomeTitle);
        inner.add(Box.createRigidArea(new Dimension(0, 10)));
        inner.add(welcomeDesc);
        inner.add(Box.createRigidArea(new Dimension(0, 18)));
        inner.add(accessNotice);
        inner.add(Box.createRigidArea(new Dimension(0, 24)));
        inner.add(startBtn);

        card.add(inner);
        return card;
    }

    // ── Navigation helpers ─────────────────────────────────────────────────────

    private void showBillingPanel() {
        billingPanel.loadProductList();
        CardLayout cl = (CardLayout) contentContainer.getLayout();
        cl.show(contentContainer, "BILLING");
    }

    private void showStockPanel() {
        stockPanel.loadProductsFromDatabase();
        CardLayout cl = (CardLayout) contentContainer.getLayout();
        cl.show(contentContainer, "STOCK");
    }

    private void showSalesHistoryPanel() {
        salesHistoryPanel.refreshData();
        CardLayout cl = (CardLayout) contentContainer.getLayout();
        cl.show(contentContainer, "SALES_HISTORY");
    }

    // ── Business Logic — UNCHANGED ─────────────────────────────────────────────

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
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
