package com.javamed.controllers;

import com.javamed.core.UserSession;
import com.javamed.dao.MySQLUserDAO;
import com.javamed.dao.UserDAO;
import com.javamed.models.Role;
import com.javamed.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AdminConsoleController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<User> pendingUsersTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, Role> roleCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private Label feedbackLabel;

    private final UserDAO userDAO = new MySQLUserDAO();
    private final ObservableList<User> pendingList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Administrator: " + currentUser.getFullName());
        }

        setupTableColumns();
        loadPendingUsers();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    @FXML
    public void loadPendingUsers() {
        List<User> pending = userDAO.getPendingUsers();
        pendingList.setAll(pending);
        pendingUsersTable.setItems(pendingList);
    }

    @FXML
    public void handleApproveUser(ActionEvent event) {
        User selected = pendingUsersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            feedbackLabel.setText("Please select a pending user to approve.");
            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            return;
        }

        boolean success = userDAO.approveUser(selected.getUserId());
        if (success) {
            feedbackLabel.setText("User " + selected.getUsername() + " approved successfully!");
            feedbackLabel.setStyle("-fx-text-fill: #16a34a;");
            loadPendingUsers();
        } else {
            feedbackLabel.setText("Failed to approve user.");
            feedbackLabel.setStyle("-fx-text-fill: #dc2626;");
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        navigateTo(event, "/com/javamed/resources/fxml/role_selection.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                resource = getClass().getClassLoader().getResource(
                    fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath
                );
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}