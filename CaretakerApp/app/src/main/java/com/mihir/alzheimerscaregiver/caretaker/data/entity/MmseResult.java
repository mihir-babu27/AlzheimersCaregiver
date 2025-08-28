package com.mihir.alzheimerscaregiver.caretaker.data.entity;

import com.google.firebase.Timestamp;
import java.util.Map;

public class MmseResult {
    public String id;
    public String patientId;
    public String caregiverId;
    public Timestamp dateTaken;
    public Map<String, Integer> sectionScores;
    public Integer totalScore;
    public String interpretation;
    public String notes;

    public MmseResult() {}
}


