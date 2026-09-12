package com.javamed.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // --- CONFIGURATION ---
    private static final String URL = "jdbc:mysql://localhost:3306/javamed_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";       // Replace with your MySQL username
    private static final String PASSWORD = "r00t123"; // Replace with your MySQL password

    // 1. The static instance of the DatabaseManager (The Singleton)
    private static DatabaseManager instance;

    // 2. The database connection object
    private Connection connection;

    // 3. Private constructor: Runs ONCE when getInstance() is first called
    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DatabaseManager] Database connection established.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] MySQL Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Failed to connect to MySQL!");
            e.printStackTrace();
        }
    }

    // 4. The getInstance() method DAOs are calling
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // 5. The getConnection() method called on that instance
    public Connection getConnection() {
        try {
            // Self-healing: if MySQL timed out or closed, reopen it
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Reconnection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    // 6. Clean shutdown helper
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DatabaseManager] Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}