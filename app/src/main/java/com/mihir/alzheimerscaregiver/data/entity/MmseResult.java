package com.mihir.alzheimerscaregiver.data.entity;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.Map;

/**
 * Firestore schema: Collection "mmse_results"
 * Fields per document:
 * - patientId (String)
 * - caregiverId (String, optional)
 * - dateTaken (Timestamp)
 * - sectionScores (Map<String, Integer>)
 * - totalScore (int)
 * - interpretation (String: "Normal", "Mild Impairment", "Moderate", "Severe")
 * - notes (String, optional)
 */
public class MmseResult {

    public String id;

    private String patientId;
    @Nullable
    private String caregiverId;
    private Timestamp dateTaken;
    private Map<String, Integer> sectionScores;
    private int totalScore;
    private String interpretation;
    @Nullable
    private String notes;

    public MmseResult() {}

    public MmseResult(String patientId,
                      @Nullable String caregiverId,
                      Timestamp dateTaken,
                      Map<String, Integer> sectionScores,
                      int totalScore,
                      String interpretation,
                      @Nullable String notes) {
        this.patientId = patientId;
        this.caregiverId = caregiverId;
        this.dateTaken = dateTaken;
        this.sectionScores = sectionScores;
        this.totalScore = totalScore;
        this.interpretation = interpretation;
        this.notes = notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    @Nullable
    public String getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(@Nullable String caregiverId) {
        this.caregiverId = caregiverId;
    }

    public Timestamp getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Timestamp dateTaken) {
        this.dateTaken = dateTaken;
    }

    public Map<String, Integer> getSectionScores() {
        return sectionScores;
    }

    public void setSectionScores(Map<String, Integer> sectionScores) {
        this.sectionScores = sectionScores;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
}


