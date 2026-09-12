package com.javamed.controllers;

import com.javamed.models.Role;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RoleSelectionController {

    @FXML
    public void handleSelectDoctor(ActionEvent event) {
        System.out.println("DOCTOR BUTTON CLICKED");
        navigateToLogin(event, Role.DOCTOR);
    }

    @FXML
    public void handleSelectPatient(ActionEvent event) {
        System.out.println("PATIENT BUTTON CLICKED");
        navigateToLogin(event, Role.PATIENT);
    }

    @FXML
    public void handleSelectAdmin(ActionEvent event) {
        System.out.println("ADMIN BUTTON CLICKED");
        navigateToLogin(event, Role.ADMIN);
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        navigateToLogin(event, null); // Generic login without a preset role lock
    }

   private void navigateToLogin(ActionEvent event, Role targetRole) {
        System.out.println("[Step 1] Attempting to find login.fxml...");

        java.net.URL resource = getClass().getResource("/com/javamed/resources/fxml/login.fxml");
        if (resource == null) {
            resource = getClass().getResource("/resources/fxml/login.fxml");
        }
        if (resource == null) {
            resource = getClass().getClassLoader().getResource("com/javamed/resources/fxml/login.fxml");
        }
        if (resource == null) {
            resource = getClass().getClassLoader().getResource("login.fxml");
        }

        if (resource == null) {
            System.err.println("[FAILED AT STEP 1] Could NOT find login.fxml anywhere on classpath!");
            System.err.println("Search attempted: /com/javamed/views/login.fxml, /views/login.fxml, login.fxml");
            return;
        }

        System.out.println("[Step 2] Found resource at: " + resource);

        try {
            System.out.println("[Step 3] Initializing FXMLLoader...");
            FXMLLoader loader = new FXMLLoader(resource);
            
            System.out.println("[Step 4] Calling loader.load()...");
            Parent root = FXMLLoader.load(getClass().getResource("/com/javamed/resources/fxml/login.fxml"));
            System.out.println("[Step 4 SUCCESS] FXML hierarchy loaded.");

            LoginController controller = loader.getController();
            if (controller != null && targetRole != null) {
                System.out.println("[Step 5] Injecting role: " + targetRole);
                controller.setSelectedRole(targetRole);
            }

            System.out.println("[Step 6] Grabbing current Window/Stage...");
            Node sourceNode = (Node) event.getSource();
            Scene currentScene = sourceNode.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            System.out.println("[Step 7] Setting new Scene...");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
            System.out.println("[Step 7 SUCCESS] Redirect complete!");

        } catch (Throwable t) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("[CRASH INSIDE NAVIGATE]: " + t.getClass().getName() + " -> " + t.getMessage());
            t.printStackTrace(System.err);
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
}