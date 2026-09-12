package com.javamed.models;

public class Admin extends User {
    private final String adminLevel;
    private final String department;

    public Admin(int userId, String username, String passwordHash, String fullName, String email, 
                 String registrationStatus, boolean isApproved, 
                 String adminLevel, String department) {
        super(userId, username, passwordHash, fullName, email, Role.ADMIN, registrationStatus, isApproved);
        this.adminLevel = adminLevel;
        this.department = department;
    }

    public String getAdminLevel() { return adminLevel; }
    public String getDepartment() { return department; }
}