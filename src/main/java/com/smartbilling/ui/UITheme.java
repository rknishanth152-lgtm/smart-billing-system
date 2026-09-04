package com.smartbilling.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URL;

/**
 * UITheme
 *
 * Centralized brand visual constants and component factory methods for the
 * Shree Annapurna Edible Oil Company Billing &amp; Inventory System.
 *
 * Phase 1 UI Redesign — presentation layer only, no business logic.
 *
 * Brand Identity:
 *   Primary:  Deep olive green  #2D5016
 *   Accent:   Muted oil gold    #C38214
 *   Surface:  Warm off-white    #FAF8F3
 */
public final class UITheme {

    private UITheme() {}

    // ─── Brand Colors ─────────────────────────────────────────────────────────
    /** Deep olive green — primary brand identity */
    public static final Color PRIMARY        = new Color(45, 80, 22);
    public static final Color PRIMARY_DARK   = new Color(30, 54, 12);
    public static final Color PRIMARY_HOVER  = new Color(55, 95, 30);
    /** Muted oil gold — key accent */
    public static final Color GOLD           = new Color(195, 130, 20);
    public static final Color GOLD_LIGHT     = new Color(218, 158, 45);

    // ─── Surface Colors ───────────────────────────────────────────────────────
    /** Warm off-white background */
    public static final Color BG             = new Color(250, 248, 243);
    public static final Color SURFACE        = Color.WHITE;
    public static final Color BORDER         = new Color(215, 212, 202);
    public static final Color BORDER_LIGHT   = new Color(232, 230, 220);

    // ─── Sidebar Colors ────────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG     = new Color(26, 44, 11);
    public static final Color SIDEBAR_HOVER  = new Color(38, 62, 18);
    public static final Color SIDEBAR_ACTIVE = new Color(45, 78, 20);
    public static final Color SIDEBAR_TEXT   = new Color(192, 210, 168);
    public static final Color SIDEBAR_MUTED  = new Color(120, 142, 100);
    public static final Color SIDEBAR_DIVIDER= new Color(48, 70, 22);

    // ─── Text Colors ──────────────────────────────────────────────────────────
    public static final Color TEXT           = new Color(28, 28, 28);
    public static final Color TEXT_MUTED     = new Color(88, 102, 72);
    public static final Color TEXT_LIGHT     = new Color(128, 138, 115);

    // ─── Status Colors ────────────────────────────────────────────────────────
    public static final Color SUCCESS        = new Color(38, 125, 72);
    public static final Color WARNING        = new Color(185, 108, 15);
    public static final Color ERROR          = new Color(182, 48, 38);
    public static final Color WARNING_BG     = new Color(255, 248, 232);
    public static final Color CRITICAL_BG    = new Color(255, 242, 242);

    // ─── Table Colors ─────────────────────────────────────────────────────────
    public static final Color TBL_HEADER     = new Color(238, 234, 222);
    public static final Color TBL_ALT        = new Color(252, 251, 247);
    public static final Color TBL_SELECT     = new Color(210, 228, 188);
    public static final Color TBL_SELECT_FG  = TEXT;

    // ─── Layout Constants ─────────────────────────────────────────────────────
    public static final int SIDEBAR_W  = 218;
    public static final int TOPBAR_H   = 46;
    public static final int PAD_SM     = 8;
    public static final int PAD_MD     = 14;
    public static final int PAD_LG     = 20;

    // ─── Fonts ────────────────────────────────────────────────────────────────
    private static final String FF = "Segoe UI";

    public static Font f(int style, int size) {
        return new Font(FF, style, size);
    }

    public static final Font F_APP_TITLE   = f(Font.BOLD, 24);
    public static final Font F_PAGE        = f(Font.BOLD, 17);
    public static final Font F_SECTION     = f(Font.BOLD, 14);
    public static final Font F_LABEL       = f(Font.BOLD, 13);
    public static final Font F_BODY        = f(Font.PLAIN, 13);
    public static final Font F_SMALL       = f(Font.PLAIN, 12);
    public static final Font F_SMALL_BOLD  = f(Font.BOLD, 12);
    public static final Font F_NAV         = f(Font.PLAIN, 13);
    public static final Font F_NAV_BOLD    = f(Font.BOLD, 13);
    public static final Font F_TBL_HDR     = f(Font.BOLD, 12);
    public static final Font F_TBL         = f(Font.PLAIN, 13);
    public static final Font F_METRIC_VAL  = f(Font.BOLD, 26);
    public static final Font F_METRIC_LBL  = f(Font.PLAIN, 12);
    public static final Font F_BILL_NO     = f(Font.BOLD, 20);

    // ─── Button Factories ─────────────────────────────────────────────────────

    /** Primary olive-green action button */
    public static JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return b;
    }

    /** Subtle outline secondary button */
    public static JButton secondaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setBackground(SURFACE);
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(7, 16, 7, 16)
        ));
        return b;
    }

    /** Danger button for destructive actions */
    public static JButton dangerBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setBackground(new Color(252, 242, 242));
        b.setForeground(ERROR);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 175, 172), 1),
            BorderFactory.createEmptyBorder(7, 16, 7, 16)
        ));
        return b;
    }

    /** Gold accent button for key sale-complete actions */
    public static JButton goldBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL);
        b.setBackground(GOLD);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 24, 9, 24));
        return b;
    }

    // ─── Input Styling ────────────────────────────────────────────────────────

    public static JTextField inputField() {
        JTextField f = new JTextField();
        styleInputField(f);
        return f;
    }

    public static void styleInputField(JTextField f) {
        f.setFont(F_BODY);
        f.setBackground(SURFACE);
        f.setForeground(TEXT);
        f.setCaretColor(PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void stylePasswordField(JPasswordField f) {
        f.setFont(F_BODY);
        f.setBackground(SURFACE);
        f.setForeground(TEXT);
        f.setCaretColor(PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void styleComboBox(JComboBox<?> c) {
        c.setFont(F_BODY);
        c.setBackground(SURFACE);
        c.setForeground(TEXT);
    }

    // ─── Table Styling ────────────────────────────────────────────────────────

    public static void styleTable(JTable t) {
        t.setFont(F_TBL);
        t.setRowHeight(30);
        t.setGridColor(BORDER_LIGHT);
        t.setSelectionBackground(TBL_SELECT);
        t.setSelectionForeground(TBL_SELECT_FG);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setBackground(SURFACE);
        JTableHeader header = t.getTableHeader();
        header.setFont(F_TBL_HDR);
        header.setBackground(TBL_HEADER);
        header.setForeground(TEXT_MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));
        header.setReorderingAllowed(false);
    }

    // ─── Container Helpers ────────────────────────────────────────────────────

    public static JScrollPane styledScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.setBackground(SURFACE);
        sp.getViewport().setBackground(SURFACE);
        return sp;
    }

    /** Sidebar section category label */
    public static JLabel sidebarSectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font(FF, Font.BOLD, 10));
        l.setForeground(SIDEBAR_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(14, 14, 5, 14));
        return l;
    }

    /** Create a titled section panel (for inner panels) */
    public static JPanel sectionPanel(String title, LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(SURFACE);
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER, 1), title
        );
        tb.setTitleFont(F_SMALL_BOLD);
        tb.setTitleColor(TEXT_MUTED);
        p.setBorder(BorderFactory.createCompoundBorder(tb,
            BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        return p;
    }

    // ─── Logo Loading ─────────────────────────────────────────────────────────

    /**
     * Loads the Shree Annapurna logo from the classpath resource
     * "/images/shree_annapurna_logo.png", scaled to fit within maxW x maxH.
     *
     * Returns null if the file is not found — callers must provide a text fallback.
     * To enable: copy your logo PNG to:
     *   src/main/resources/images/shree_annapurna_logo.png
     */
    public static ImageIcon loadLogo(int maxW, int maxH) {
        try {
            URL url = UITheme.class.getResource("/images/shree_annapurna_logo.png");
            if (url == null) return null;
            ImageIcon raw = new ImageIcon(url);
            int w = raw.getIconWidth();
            int h = raw.getIconHeight();
            if (w <= 0 || h <= 0) return null;
            double scale = Math.min((double) maxW / w, (double) maxH / h);
            Image img = raw.getImage()
                .getScaledInstance((int)(w * scale), (int)(h * scale), Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ignored) {
            return null;
        }
    }

    // ─── FlatLaf Global Defaults ──────────────────────────────────────────────

    /**
     * Applies Shree Annapurna brand defaults to UIManager after FlatLightLaf setup.
     * Call once from Main before opening any window.
     */
    public static void applyGlobalDefaults() {
        UIManager.put("Button.arc", 4);
        UIManager.put("Component.arc", 4);
        UIManager.put("TextComponent.arc", 4);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("Table.selectionBackground", TBL_SELECT);
        UIManager.put("Table.selectionForeground", TBL_SELECT_FG);
        UIManager.put("TableHeader.background", TBL_HEADER);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER, 1));
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Panel.background", BG);
        UIManager.put("TitledBorder.titleColor", TEXT_MUTED);
    }
}
