package com.javamed.controllers;

import com.javamed.core.UserSession;
import com.javamed.dao.ClinicalRecordDAO;
import com.javamed.dao.MySQLClinicalRecordDAO;
import com.javamed.models.ClinicalRecord;
import com.javamed.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;

public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<ClinicalRecord> recordsTable;
    @FXML private TableColumn<ClinicalRecord, Timestamp> dateCol;
    @FXML private TableColumn<ClinicalRecord, Integer> doctorCol;
    @FXML private TableColumn<ClinicalRecord, String> diagnosisCol;
    @FXML private TableColumn<ClinicalRecord, String> prescriptionCol;
    @FXML private TableColumn<ClinicalRecord, String> notesCol;

    private final ClinicalRecordDAO recordDAO = new MySQLClinicalRecordDAO();
    private final ObservableList<ClinicalRecord> recordObservableList = FXCollections.observableArrayList();

   @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")");
        }

        setupTableColumns();
        loadPatientRecords();
    }

    private void setupTableColumns() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        diagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        prescriptionCol.setCellValueFactory(new PropertyValueFactory<>("prescription"));
        notesCol.setCellValueFactory(new PropertyValueFactory<>("clinicalNotes"));
    }

    @FXML
    public void loadPatientRecords() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<ClinicalRecord> records = recordDAO.getRecordsForPatient(currentUser.getUserId());
            recordObservableList.setAll(records);
            recordsTable.setItems(recordObservableList);
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