package com.javamed.models;

import java.sql.Timestamp;

public class ClinicalRecord {
    private int recordId;
    private int patientId;
    private int doctorId;
    private String diagnosis;
    private String prescription;
    private String clinicalNotes;
    private Timestamp createdAt;

    // Full constructor (for reading from DB)
    public ClinicalRecord(int recordId, int patientId, int doctorId, 
                          String diagnosis, String prescription, String clinicalNotes, 
                          Timestamp createdAt) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.clinicalNotes = clinicalNotes;
        this.createdAt = createdAt;
    }

    // Insert constructor (before recordId and createdAt are generated)
    public ClinicalRecord(int patientId, int doctorId, String diagnosis, String prescription, String clinicalNotes) {
        this(0, patientId, doctorId, diagnosis, prescription, clinicalNotes, null);
    }

    public int getRecordId() { return recordId; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }
    public String getClinicalNotes() { return clinicalNotes; }
    public Timestamp getCreatedAt() { return createdAt; }
}