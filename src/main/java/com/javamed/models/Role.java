package com.javamed.models;

public enum Role {
    ADMIN("Administrator", "/fxml/admin_console.fxml"),
    DOCTOR("Doctor", "/fxml/doctor_workspace.fxml"),
    PATIENT("Patient", "/fxml/patient_dashboard.fxml");

    private final String displayName;
    private final String dashboardFxml;

    Role(String displayName, String dashboardFxml) {
        this.displayName = displayName;
        this.dashboardFxml = dashboardFxml;
    }

    public String getDisplayName() { return displayName; }
    public String getDashboardFxml() { return dashboardFxml; }
}