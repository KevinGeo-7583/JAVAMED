package com.javamed.models;

public class User {
    protected int userId;
    protected String username;
    protected String passwordHash; // Kept in model for AuthService to verify
    protected String fullName;
    protected String email;
    protected Role userType;
    protected String registrationStatus;
    protected boolean isApproved;
    private String universalHealthId;
    public User(int userId, String username, String passwordHash, String fullName, 
                String email, Role userType, String registrationStatus, boolean isApproved) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        this.registrationStatus = registrationStatus;
        this.isApproved = isApproved;
    }

    // Standard Getters
    public String getUniversalHealthId(){return universalHealthId;}
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getUserType() { return userType; }
    public String getRegistrationStatus() { return registrationStatus; }
    public boolean isApproved() { return isApproved; }
    //setters
    public void setUniversalHealthId(String universalHealthId){this.universalHealthId = universalHealthId;}
}