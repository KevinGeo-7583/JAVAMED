package com.javamed.models;

public class Doctor extends User {
    private final String licenseId;
    private final String specialization;
    private final String department;
    private final String position;

    // Added passwordHash to parameters
    public Doctor(int userId, String username, String passwordHash, String fullName, String email, 
                  String registrationStatus, boolean isApproved, 
                  String licenseId, String specialization, String department, String position) {
        
        // Forward passwordHash and Role.DOCTOR to parent User constructor
        super(userId, username, passwordHash, fullName, email, Role.DOCTOR, registrationStatus, isApproved);
        
        this.licenseId = licenseId;
        this.specialization = specialization;
        this.department = department;
        this.position = position;
    }

    public String getLicenseId() { return licenseId; }
    public String getSpecialization() { return specialization; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
}