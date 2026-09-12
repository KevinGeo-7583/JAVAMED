package com.javamed.controllers;

import com.javamed.core.UserSession;
import com.javamed.dao.ClinicalRecordDAO;
import com.javamed.dao.MySQLClinicalRecordDAO;
import com.javamed.dao.MySQLUserDAO;
import com.javamed.dao.UserDAO;
import com.javamed.models.ClinicalRecord;
import com.javamed.models.User;
import com.javamed.services.PDFExportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;

public class DoctorWorkspaceController {

    @FXML private Label welcomeLabel;

    // Patient Directory Table
    @FXML private TableView<User> patientTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, String> emailCol;

    // Records History & Search
    @FXML private Label selectedPatientNameLabel;
    @FXML private TextField searchRecordField;
    @FXML private TableView<ClinicalRecord> clinicalRecordsTable;
    @FXML private TableColumn<ClinicalRecord, Timestamp> recordDateCol;
    @FXML private TableColumn<ClinicalRecord, String> recordDiagnosisCol;
    @FXML private TableColumn<ClinicalRecord, String> recordPrescriptionCol;
    @FXML private TableColumn<ClinicalRecord, String> recordNotesCol;

    // Input Form
    @FXML private TextField diagnosisField;
    @FXML private TextArea prescriptionArea;
    @FXML private TextArea clinicalNotesArea;
    @FXML private Label feedbackLabel;

    // DAOs and Services
    private final UserDAO userDAO = new MySQLUserDAO();
    private final ClinicalRecordDAO recordDAO = new MySQLClinicalRecordDAO();
    private final PDFExportService pdfService = new PDFExportService();

    // Data lists
    private final ObservableList<User> patientList = FXCollections.observableArrayList();
    private final ObservableList<ClinicalRecord> patientRecordsList = FXCollections.observableArrayList();
    private FilteredList<ClinicalRecord> filteredRecords;
    private User selectedPatient = null;

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Dr. " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")");
        }

        setupPatientTable();
        setupClinicalRecordsTable();
        setupSearchFilter();
        loadPatientData();
    }

    private void setupPatientTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal;
                selectedPatientNameLabel.setText(newVal.getFullName() + " (ID: " + newVal.getUserId() + ")");
                loadRecordsForSelectedPatient();
            }
        });
    }

    private void setupClinicalRecordsTable() {
        recordDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        recordDiagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        recordPrescriptionCol.setCellValueFactory(new PropertyValueFactory<>("prescription"));
        recordNotesCol.setCellValueFactory(new PropertyValueFactory<>("clinicalNotes"));

        // When a past record is clicked, load its data into the form for easy reading
        clinicalRecordsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                diagnosisField.setText(newVal.getDiagnosis());
                prescriptionArea.setText(newVal.getPrescription());
                clinicalNotesArea.setText(newVal.getClinicalNotes());
                setFeedback("Loaded record from " + newVal.getCreatedAt(), "#0284c7");
            }
        });
    }

    private void setupSearchFilter() {
        filteredRecords = new FilteredList<>(patientRecordsList, p -> true);
        searchRecordField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredRecords.setPredicate(record -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }
                String lowerFilter = newValue.toLowerCase();
                boolean matchDiagnosis = record.getDiagnosis() != null && record.getDiagnosis().toLowerCase().contains(lowerFilter);
                boolean matchNotes = record.getClinicalNotes() != null && record.getClinicalNotes().toLowerCase().contains(lowerFilter);
                boolean matchRx = record.getPrescription() != null && record.getPrescription().toLowerCase().contains(lowerFilter);

                return matchDiagnosis || matchNotes || matchRx;
            });
        });
        clinicalRecordsTable.setItems(filteredRecords);
    }

    @FXML
    public void loadPatientData() {
        patientList.setAll(userDAO.getAllPatients());
        patientTable.setItems(patientList);
    }

    private void loadRecordsForSelectedPatient() {
        if (selectedPatient != null) {
            List<ClinicalRecord> records = recordDAO.getRecordsForPatient(selectedPatient.getUserId());
            patientRecordsList.setAll(records);
            searchRecordField.clear();
        }
    }

    @FXML
    public void handleSaveRecord(ActionEvent event) {
        if (selectedPatient == null) {
            setFeedback("Please select a patient from the left table first.", "#dc2626");
            return;
        }

        String diagnosis = diagnosisField.getText().trim();
        if (diagnosis.isEmpty()) {
            setFeedback("Diagnosis is required before saving.", "#dc2626");
            return;
        }

        User currentDoctor = UserSession.getInstance().getCurrentUser();
        ClinicalRecord record = new ClinicalRecord(
                selectedPatient.getUserId(),
                currentDoctor.getUserId(),
                diagnosis,
                prescriptionArea.getText().trim(),
                clinicalNotesArea.getText().trim()
        );

        if (recordDAO.saveRecord(record)) {
            setFeedback("Record saved! Added to patient history table above.", "#16a34a");
            loadRecordsForSelectedPatient(); // Instantly updates the history table
        } else {
            setFeedback("Database error: Could not save record.", "#dc2626");
        }
    }

    @FXML
    public void handleExportPdf(ActionEvent event) {
        if (selectedPatient == null) {
            setFeedback("Please select a patient first.", "#dc2626");
            return;
        }

        // Prioritize the record selected from the table; if none selected, use what is currently typed
        ClinicalRecord targetRecord = clinicalRecordsTable.getSelectionModel().getSelectedItem();
        if (targetRecord == null) {
            String diag = diagnosisField.getText().trim();
            if (diag.isEmpty()) {
                setFeedback("Select a past record from the table or type a diagnosis to export.", "#dc2626");
                return;
            }
            User currentDoctor = UserSession.getInstance().getCurrentUser();
            targetRecord = new ClinicalRecord(
                    selectedPatient.getUserId(),
                    currentDoctor.getUserId(),
                    diag,
                    prescriptionArea.getText().trim(),
                    clinicalNotesArea.getText().trim()
            );
        }

        User currentDoctor = UserSession.getInstance().getCurrentUser();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Clinical Summary Report");
        fileChooser.setInitialFileName("Summary_" + selectedPatient.getUsername() + "_" + System.currentTimeMillis() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents (*.pdf)", "*.pdf"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File targetFile = fileChooser.showSaveDialog(stage);

        if (targetFile != null) {
            try {
                pdfService.generateClinicalReport(selectedPatient, currentDoctor, targetRecord, targetFile);
                setFeedback("Exported: " + targetFile.getName(), "#16a34a");
            } catch (Exception e) {
                setFeedback("PDF Error: " + e.getMessage(), "#dc2626");
            }
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        navigateTo(event, "/com/javamed/resources/fxml/role_selection.fxml");
    }

    private void setFeedback(String message, String hexColor) {
        if (feedbackLabel != null) {
            feedbackLabel.setText(message);
            feedbackLabel.setStyle("-fx-text-fill: " + hexColor + "; -fx-font-weight: bold;");
        }
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