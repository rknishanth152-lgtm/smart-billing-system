package com.smartbilling;

import com.formdev.flatlaf.FlatLightLaf;
import com.smartbilling.ui.LoginForm;
import com.smartbilling.ui.UITheme;

import javax.swing.SwingUtilities;

/**
 * Main
 *
 * Application Entry Point for Shree Annapurna Edible Oil Company
 * Smart Billing &amp; Inventory System.
 *
 * Applies FlatLightLaf for modern, crisp Swing rendering and then
 * overlays the Shree Annapurna brand color defaults before launching
 * the Login window on the Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {
        // Phase 1 UI: Apply FlatLaf for modern Swing rendering
        FlatLightLaf.setup();
        // Apply Shree Annapurna brand color overrides
        UITheme.applyGlobalDefaults();

        // Launch LoginForm on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }
}
