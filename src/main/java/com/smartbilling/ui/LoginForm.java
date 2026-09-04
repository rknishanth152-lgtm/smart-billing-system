package com.smartbilling.ui;

import com.smartbilling.model.User;
import com.smartbilling.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * LoginForm
 *
 * Phase 1 UI Redesign — Shree Annapurna Edible Oil Company.
 *
 * Visual design:
 *  - Warm off-white window background
 *  - Centred login card with logo, company name, tagline
 *  - Olive green Sign In button
 *  - Clean field styling via UITheme
 *
 * Business logic unchanged (performLogin / clearForm / authService).
 */
public class LoginForm extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginButton;
    private JButton        clearButton;

    private final AuthService authService;

    public LoginForm() {
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Shree Annapurna — Smart Billing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 530);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Root — warm off-white ──────────────────────────────────────────
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UITheme.BG);
        setContentPane(root);

        // ── Login Card ────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(36, 44, 32, 44)
        ));

        // ── Logo / Brand header ───────────────────────────────────────────
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon logo = UITheme.loadLogo(300, 140);
        if (logo != null) {
            JLabel logoLabel = new JLabel(logo);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            brandPanel.add(logoLabel);
            brandPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        } else {
            // Text fallback when logo file not yet placed in resources
            JLabel cname = new JLabel("SHREE ANNAPURNA");
            cname.setFont(UITheme.F_APP_TITLE);
            cname.setForeground(UITheme.PRIMARY);
            cname.setAlignmentX(Component.CENTER_ALIGNMENT);
            brandPanel.add(cname);

            JLabel csub = new JLabel("EDIBLE OIL COMPANY");
            csub.setFont(UITheme.f(Font.PLAIN, 12));
            csub.setForeground(UITheme.GOLD);
            csub.setAlignmentX(Component.CENTER_ALIGNMENT);
            brandPanel.add(csub);
            brandPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        JLabel tagline = new JLabel("Billing & Inventory System");
        tagline.setFont(UITheme.f(Font.PLAIN, 12));
        tagline.setForeground(UITheme.TEXT_MUTED);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(tagline);

        card.add(brandPanel);
        card.add(Box.createRigidArea(new Dimension(0, 22)));

        // ── Divider ───────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // ── Form ──────────────────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.insets    = new Insets(0, 0, 6, 0);
        gbc.gridwidth = 1;
        gbc.weightx   = 1.0;

        // Username label
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(UITheme.F_LABEL);
        userLbl.setForeground(UITheme.TEXT);
        formPanel.add(userLbl, gbc);

        // Username field
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(310, 34));
        UITheme.styleInputField(usernameField);
        formPanel.add(usernameField, gbc);

        // Password label
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UITheme.F_LABEL);
        passLbl.setForeground(UITheme.TEXT);
        formPanel.add(passLbl, gbc);

        // Password field
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 14, 0);
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(310, 34));
        UITheme.stylePasswordField(passwordField);
        formPanel.add(passwordField, gbc);

        // Error label
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 14, 0);
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.F_SMALL_BOLD);
        errorLabel.setForeground(UITheme.ERROR);
        formPanel.add(errorLabel, gbc);

        // Sign In button (full width, olive green)
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        loginButton = new JButton("Sign In");
        loginButton.setFont(UITheme.F_LABEL);
        loginButton.setBackground(UITheme.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setPreferredSize(new Dimension(310, 36));
        loginButton.setMaximumSize(new Dimension(310, 36));
        loginButton.setMinimumSize(new Dimension(310, 36));
        formPanel.add(loginButton, gbc);

        // Clear button (secondary, smaller)
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        clearButton = UITheme.secondaryBtn("Clear");
        clearButton.setFont(UITheme.F_SMALL);
        clearButton.setPreferredSize(new Dimension(310, 30));
        formPanel.add(clearButton, gbc);

        card.add(formPanel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // ── Footer caption ────────────────────────────────────────────────
        JLabel footer = new JLabel("Shree Annapurna Edible Oil Company  \u2022  Internal Use Only");
        footer.setFont(UITheme.f(Font.PLAIN, 10));
        footer.setForeground(UITheme.TEXT_LIGHT);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(footer);

        // ── Add card to root ──────────────────────────────────────────────
        GridBagConstraints rootGbc = new GridBagConstraints();
        rootGbc.anchor = GridBagConstraints.CENTER;
        root.add(card, rootGbc);

        // ── Event Listeners ───────────────────────────────────────────────
        loginButton.addActionListener(e -> performLogin());
        clearButton.addActionListener(e -> clearForm());

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocus();
            }
        });
    }

    // ── Business Logic — UNCHANGED ─────────────────────────────────────────────

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        errorLabel.setText(" "); // Clear previous errors

        User user = authService.login(username, password);

        if (user == null) {
            errorLabel.setText("Invalid username/password or account inactive.");
            passwordField.setText("");
            return;
        }

        this.dispose();

        if (user.isAdmin()) {
            SwingUtilities.invokeLater(() -> new AdminDashboard(user).setVisible(true));
        } else if (user.isEmployee()) {
            SwingUtilities.invokeLater(() -> new EmployeeDashboard(user).setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this, "Unknown role assigned to user: " + user.getRole(),
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        usernameField.requestFocus();
    }
}
