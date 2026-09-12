package com.javamed.dao;

import com.javamed.core.DatabaseManager;
import com.javamed.models.User;
import com.javamed.models.Doctor;
import com.javamed.models.Patient;
import com.javamed.models.Admin;
import com.javamed.models.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQLUserDAO implements UserDAO {

    @Override
    public User getUserByUsername(String username) {
        // Explicitly selecting your schema columns
        String sql = "SELECT user_id, username, password_hash, full_name, email, user_type, " +
                     "registration_status, is_approved, universal_health_id, license_id, " +
                     "specialization, department, position, admin_level, created_at " +
                     "FROM users WHERE username = ?";

        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MySQLUserDAO] Error querying user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean saveUser(User user) {
        // Base insert for common user fields
        String sql = "INSERT INTO users (username, password_hash, full_name, email, user_type, " +
                     "registration_status, is_approved) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getUserType().name());
            stmt.setString(6, user.getRegistrationStatus());
            stmt.setBoolean(7, user.isApproved());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[MySQLUserDAO] Error inserting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        Connection conn = DatabaseManager.getInstance().getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = mapRowToUser(rs);
                if (user != null) {
                    userList.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.println("[MySQLUserDAO] Error fetching all users: " + e.getMessage());
            e.printStackTrace();
        }

        return userList;
    }
    @Override
    public List<User> getPendingUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_approved = 0 ORDER BY created_at DESC";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            list.add(mapRowToUser(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}

@Override
public boolean approveUser(int userId) {
    String sql = "UPDATE users SET is_approved = 1, registration_status = 'ACTIVE' WHERE user_id = ?";
    Connection conn = DatabaseManager.getInstance().getConnection();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, userId);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    @Override
    public List<User> getAllPatients() {
        List<User> patients = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, email, user_type, " +
                     "registration_status, is_approved, universal_health_id, license_id, " +
                     "specialization, department, position, admin_level, created_at " +
                     "FROM users WHERE user_type = 'PATIENT' ORDER BY full_name ASC";

        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                patients.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MySQLUserDAO] Error fetching patients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }
    /**
     * Helper method to map a ResultSet row into a User object (Polymorphism in action).
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String uname = rs.getString("username");
        String passHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String typeStr = rs.getString("user_type");
        String regStatus = rs.getString("registration_status");
        boolean approved = rs.getBoolean("is_approved");

        // Parse Role safely
        Role role;
        try {
            role = Role.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            role = Role.PATIENT; // Safe fallback default
        }

        // Return the instantiated User model
        return new User(id, uname, passHash, fullName, email, role, regStatus, approved);
    }
}