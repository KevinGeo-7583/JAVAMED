package com.javamed.dao;

import com.javamed.models.User;
import java.util.List;

public interface UserDAO {

    /**
     * Retrieves a single user matching the unique username.
     * Used for login verification and profile lookups.
     *
     * @param username The login username
     * @return Populated User object if found, or null if no match exists
     */
    User getUserByUsername(String username);

    /**
     * Persists a new user record into the database.
     *
     * @param user The user entity to insert
     * @return true if the row was successfully inserted, false otherwise
     */
    boolean saveUser(User user);

    /**
     * Retrieves all registered users from the system.
     *
     * @return List of all User objects (empty list if no records exist)
     */
    List<User> getAllUsers();
    List<User> getAllPatients();
    List<User> getPendingUsers();
    boolean approveUser(int userId);
}