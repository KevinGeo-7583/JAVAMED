package com.javamed.controllers;

import com.javamed.core.UserSession;
import com.javamed.models.Role;
import com.javamed.models.User;
import com.javamed.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private Label portalTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();
    private Role selectedRole = null;

    /**
     * Called by RoleSelectionController to configure portal-specific state before login screen is rendered.
     */
    public void setSelectedRole(Role role) {
        this.selectedRole = role;
        if (portalTitleLabel != null) {
            switch (role) {
                case DOCTOR -> portalTitleLabel.setText("Physician & Clinician Portal");
                case PATIENT -> portalTitleLabel.setText("Patient Health Portal");
                case ADMIN -> portalTitleLabel.setText("System Administrator Console");
            }
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        System.out.println("\n========== LOGIN ATTEMPT START ==========");
        String username = (usernameField != null) ? usernameField.getText() : null;
        String password = (passwordField != null) ? passwordField.getText() : null;

        System.out.println("[Trace 1] Reading UI fields -> Username: '" + username + "' | Password length: " + (password != null ? password.length() : 0));

        if (errorLabel != null) {
            errorLabel.setText("");
        }

        boolean authResult = authService.authenticate(username, password);
        System.out.println("[Trace 2] authService.authenticate() returned: " + authResult);

        if (authResult) {
            System.out.println("[Trace 3] Entered SUCCESS branch.");
            User user = UserSession.getInstance().getCurrentUser();
            
            if (user == null) {
                System.err.println("[Trace 3-ERROR] User is NULL in UserSession despite authResult=true!");
                if (errorLabel != null) errorLabel.setText("Session error: User not set.");
                return;
            }

            System.out.println("[Trace 4] User from session: " + user.getUsername() + " | Account Role: " + user.getUserType() + " | Selected Portal: " + selectedRole);

            // Role guard verification
            if (selectedRole != null && user.getUserType() != selectedRole) {
                System.err.println("[Trace 4-FAIL] Role Guard Mismatch! Logging out.");
                authService.logout();
                if (errorLabel != null) {
                    errorLabel.setText("Access denied: Not a " + selectedRole + " account.");
                }
                return;
            }

            System.out.println("[Trace 5] Role check passed. Calling routeByRole...");
            routeByRole(event, user.getUserType());

        } else {
            System.err.println("[Trace 3-FAIL] Entered FAILURE branch. Setting errorLabel text.");
            if (errorLabel != null) {
                errorLabel.setText("Invalid credentials or unapproved account.");
            }
        }
        System.out.println("========== LOGIN ATTEMPT END ==========\n");
    }

    @FXML
    public void handleBackToRoleSelect(ActionEvent event) {
        switchScene(event, "/com/javamed/resources/fxml/role_selection.fxml");
    }

    private void routeByRole(ActionEvent event, Role role) {
        switch (role) {
            case DOCTOR -> switchScene(event, "/com/javamed/resources/fxml/doctor_workspace.fxml");
            case PATIENT -> switchScene(event, "/com/javamed/resources/fxml/patient_dashboard.fxml");
            case ADMIN -> switchScene(event, "/com/javamed/resources/fxml/admin_console.fxml");
        }
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            if (errorLabel != null) errorLabel.setText("Failed to load view: " + fxmlPath);
        }
    }
}