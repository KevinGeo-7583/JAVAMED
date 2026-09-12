package com.javamed.models;

public class Patient extends User {
    private final String universalHealthId;

    public Patient(int userId, String username, String passwordHash, String fullName, String email, 
                   String registrationStatus, boolean isApproved, String universalHealthId) {
        super(userId, username, passwordHash, fullName, email, Role.PATIENT, registrationStatus, isApproved);
        this.universalHealthId = universalHealthId;
    }

    public String getUniversalHealthId() {
        return universalHealthId;
    }
}