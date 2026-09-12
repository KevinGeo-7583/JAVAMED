package com.javamed.services;

import com.javamed.core.UserSession;
import com.javamed.dao.MySQLUserDAO;
import com.javamed.dao.UserDAO;
import com.javamed.models.User;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new MySQLUserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("[AuthService] Empty username or password submitted.");
            return false;
        }

        System.out.println("[AuthService] Querying database for: '" + username.trim() + "'");
        User user = userDAO.getUserByUsername(username.trim());

        if (user == null) {
            System.err.println("[AuthService] FAILED: User not found in database.");
            return false;
        }

        System.out.println("[AuthService] User found: " + user.getUsername() + ", Role: " + user.getUserType() + ", Approved: " + user.isApproved());

        if (!user.isApproved()) {
            System.err.println("[AuthService] FAILED: Account is not approved (is_approved = false).");
            return false;
        }

        // Compare values directly with visible boundaries to catch whitespace/hash discrepancies
        String dbHash = user.getPasswordHash();
        System.out.println("[AuthService] Comparing stored password [" + dbHash + "] against input [" + password + "]");

        if (dbHash != null && dbHash.trim().equals(password.trim())) {
            UserSession.getInstance().setCurrentUser(user);
            System.out.println("[AuthService] Authentication successful for: " + user.getUsername());
            return true;
        }

        System.err.println("[AuthService] FAILED: Password mismatch.");
        return false;
    }

    public void logout() {
        UserSession.getInstance().cleanUserSession();
    }
}