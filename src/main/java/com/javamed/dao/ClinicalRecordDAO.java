package com.javamed.dao;

import com.javamed.models.ClinicalRecord;
import java.util.List;

public interface ClinicalRecordDAO {
    boolean saveRecord(ClinicalRecord record);
    boolean updateRecord(ClinicalRecord record);
    List<ClinicalRecord> getRecordsForPatient(int patientId);
}
