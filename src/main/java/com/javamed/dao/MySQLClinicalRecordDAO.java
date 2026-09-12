package com.javamed.dao;

import com.javamed.core.DatabaseManager;
import com.javamed.models.ClinicalRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLClinicalRecordDAO implements ClinicalRecordDAO {

    @Override
    public boolean saveRecord(ClinicalRecord record) {
        String sql = "INSERT INTO clinical_records (patient_id, doctor_id, diagnosis, prescription, clinical_notes) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();

        if (conn == null) {
            System.err.println("[MySQLClinicalRecordDAO] Connection is null!");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getPatientId());
            stmt.setInt(2, record.getDoctorId());
            stmt.setString(3, record.getDiagnosis());
            stmt.setString(4, record.getPrescription());
            stmt.setString(5, record.getClinicalNotes());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("[MySQLClinicalRecordDAO] SQL Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ClinicalRecord> getRecordsForPatient(int patientId) {
        List<ClinicalRecord> list = new ArrayList<>();
        String sql = "SELECT record_id, patient_id, doctor_id, diagnosis, prescription, clinical_notes, created_at " +
                     "FROM clinical_records WHERE patient_id = ? ORDER BY created_at DESC";
        Connection conn = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ClinicalRecord(
                        rs.getInt("record_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription"),
                        rs.getString("clinical_notes"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MySQLClinicalRecordDAO] Error fetching records: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}