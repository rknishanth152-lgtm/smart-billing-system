package com.smartbilling.service;

import com.smartbilling.dao.UserDAO;
import com.smartbilling.model.User;

/**
 * AuthService
 * 
 * Business logic service managing user authentication and active session state.
 */
public class AuthService {

    private static User currentUser = null;
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Attempts to log in a user with the provided credentials.
     * 
     * @param username Entered username
     * @param password Entered password
     * @return Authenticated User object if successful, null if authentication fails.
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            currentUser = user; // Store logged in session user
        }
        return user;
    }

    /**
     * Logs out the currently authenticated user.
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Returns the currently logged in user session.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Checks if the currently logged in user has the ADMIN role.
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}
