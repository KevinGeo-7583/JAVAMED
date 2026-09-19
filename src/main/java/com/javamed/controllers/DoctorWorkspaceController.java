package com.javamed.controllers;

import com.javamed.core.UserSession;
import com.javamed.dao.*;
import com.javamed.models.*;
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
import java.sql.Timestamp;

public class DoctorWorkspaceController {

    @FXML private Label welcomeLabel, selectedPatientNameLabel, feedbackLabel;
    @FXML private TextField searchRecordField, diagnosisField;
    @FXML private TextArea prescriptionArea, clinicalNotesArea;
    
    @FXML private TableView<User> patientTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> nameCol, emailCol;

    @FXML private TableView<ClinicalRecord> clinicalRecordsTable;
    @FXML private TableColumn<ClinicalRecord, Timestamp> recordDateCol;
    @FXML private TableColumn<ClinicalRecord, String> recordDiagnosisCol, recordPrescriptionCol, recordNotesCol;

    private final UserDAO userDAO = new MySQLUserDAO();
    private final ClinicalRecordDAO recordDAO = new MySQLClinicalRecordDAO();
    private final PDFExportService pdfService = new PDFExportService();

    private final ObservableList<ClinicalRecord> recordList = FXCollections.observableArrayList();
    private FilteredList<ClinicalRecord> filteredRecords;
    private User selectedPatient;
    private ClinicalRecord editingRecord;

    @FXML
    public void initialize() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText( user.getFullName() + " (" + user.getUsername() + ")");
        }

        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        recordDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        recordDiagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        recordPrescriptionCol.setCellValueFactory(new PropertyValueFactory<>("prescription"));
        recordNotesCol.setCellValueFactory(new PropertyValueFactory<>("clinicalNotes"));

        filteredRecords = new FilteredList<>(recordList, p -> true);
        clinicalRecordsTable.setItems(filteredRecords);

        searchRecordField.textProperty().addListener((obs, oldV, q) -> {
            String filter = (q == null) ? "" : q.trim().toLowerCase();
            filteredRecords.setPredicate(r -> filter.isEmpty() ||
                (r.getDiagnosis() != null && r.getDiagnosis().toLowerCase().contains(filter)) ||
                (r.getClinicalNotes() != null && r.getClinicalNotes().toLowerCase().contains(filter)) ||
                (r.getPrescription() != null && r.getPrescription().toLowerCase().contains(filter)));
        });

        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, patient) -> {
            if (patient == null) return;
            selectedPatient = patient;
            selectedPatientNameLabel.setText(patient.getFullName() + " (ID: " + patient.getUserId() + ")");
            clearForm();
            refreshRecords();
        });

        clinicalRecordsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, record) -> {
            if (record == null) return;
            editingRecord = record;
            diagnosisField.setText(record.getDiagnosis());
            prescriptionArea.setText(record.getPrescription());
            clinicalNotesArea.setText(record.getClinicalNotes());
            setFeedback("Editing record #" + record.getRecordId(), "#0284c7");
        });

        patientTable.setItems(FXCollections.observableArrayList(userDAO.getAllPatients()));
    }

    private void refreshRecords() {
        if (selectedPatient != null) {
            recordList.setAll(recordDAO.getRecordsForPatient(selectedPatient.getUserId()));
            searchRecordField.clear();
        }
    }

    @FXML
    public void handleSaveRecord(ActionEvent event) {
        if (selectedPatient == null) {
            setFeedback("Select a patient first.", "#dc2626");
            return;
        }

        String diag = diagnosisField.getText().trim();
        if (diag.isEmpty()) {
            setFeedback("Diagnosis is required.", "#dc2626");
            return;
        }

        int docId = UserSession.getInstance().getCurrentUser().getUserId();
        String rx = prescriptionArea.getText().trim();
        String notes = clinicalNotesArea.getText().trim();

        boolean ok;
        if (editingRecord != null) {
            ok = recordDAO.updateRecord(new ClinicalRecord(
                editingRecord.getRecordId(), selectedPatient.getUserId(), docId, diag, rx, notes, editingRecord.getCreatedAt()
            ));
        } else {
            ok = recordDAO.saveRecord(new ClinicalRecord(selectedPatient.getUserId(), docId, diag, rx, notes));
        }

        if (ok) {
            setFeedback(editingRecord != null ? "Record updated." : "Record saved.", "#16a34a");
            refreshRecords();
            clearForm();
        } else {
            setFeedback("Database error occurred.", "#dc2626");
        }
    }

    @FXML
    public void handleExportPdf(ActionEvent event) {
        if (selectedPatient == null) {
            setFeedback("Select a patient first.", "#dc2626");
            return;
        }

        ClinicalRecord target = clinicalRecordsTable.getSelectionModel().getSelectedItem();
        if (target == null && editingRecord != null) target = editingRecord;
        if (target == null) {
            setFeedback("Select a record from the table to export.", "#dc2626");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Export Medical Record");
        fc.setInitialFileName("Record_" + selectedPatient.getUsername() + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            try {
                pdfService.generateClinicalReport(selectedPatient, UserSession.getInstance().getCurrentUser(), target, file);
                setFeedback("Saved: " + file.getName(), "#16a34a");
            } catch (Exception e) {
                setFeedback("Export failed: " + e.getMessage(), "#dc2626");
            }
        }
    }
    @FXML
    public void loadPatientData(ActionEvent event) {
        patientTable.setItems(FXCollections.observableArrayList(userDAO.getAllPatients()));
        setFeedback("Patient list refreshed.", "#0284c7");
    }

    // Overload with no arguments in case initialize() or FXML calls it without parameters
    public void loadPatientData() {
        patientTable.setItems(FXCollections.observableArrayList(userDAO.getAllPatients()));
    }
    @FXML
    public void handleClearForm(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        diagnosisField.clear();
        prescriptionArea.clear();
        clinicalNotesArea.clear();
        clinicalRecordsTable.getSelectionModel().clearSelection();
        editingRecord = null;
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/javamed/resources/fxml/role_selection.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setFeedback(String msg, String color) {
        if (feedbackLabel != null) {
            feedbackLabel.setText(msg);
            feedbackLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        }
    }
}