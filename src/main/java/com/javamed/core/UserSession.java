package com.javamed.core;

import com.javamed.models.User;

public class UserSession {

    // 1. Static instance for the Singleton
    private static UserSession instance;

    // 2. Holds the currently logged-in user in memory
    private User currentUser;

    // 3. Private constructor prevents instantiation outside
    private UserSession() {}

    // 4. Global access point
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public void cleanUserSession() {
        this.currentUser = null;
    }
}