package com.mihir.alzheimerscaregiver.repository;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;
import com.mihir.alzheimerscaregiver.data.entity.MmseResult;

import java.util.ArrayList;
import java.util.List;

public class MmseResultRepository {

    private static final String TAG = "MmseResultRepository";
    // private static final String COLLECTION = "mmse_results";

    private final FirebaseFirestore db;

    public MmseResultRepository() {
        try {
            db = FirebaseConfig.getInstance();
            if (db == null) {
                throw new RuntimeException("Firebase Firestore instance is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MmseResultRepository", e);
            throw new RuntimeException("Failed to initialize MmseResultRepository: " + e.getMessage(), e);
        }
    }

    private CollectionReference getCollection(String patientId) {
        return db.collection("patients").document(patientId).collection("mmse_results");
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void save(MmseResult result, FirebaseCallback<Void> callback) {
        try {
            String patientId = result.getPatientId();
            if (patientId == null || patientId.isEmpty()) {
                callback.onError("Missing patientId for MMSE result");
                return;
            }
            CollectionReference ref = getCollection(patientId);
            if (result.getId() == null || result.getId().isEmpty()) {
                DocumentReference docRef = ref.document();
                result.setId(docRef.getId());
                docRef.set(result)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving MMSE result", e);
                            callback.onError(e.getMessage());
                        });
            } else {
                ref.document(result.getId()).set(result)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving MMSE result", e);
                            callback.onError(e.getMessage());
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in save", e);
            callback.onError("Failed to save MMSE result: " + e.getMessage());
        }
    }

    public void getByPatientId(String patientId, FirebaseCallback<List<MmseResult>> callback) {
        try {
            getCollection(patientId)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<MmseResult> list = new ArrayList<>();
                            if (queryDocumentSnapshots != null) {
                                for (var doc : queryDocumentSnapshots) {
                                    MmseResult entity = doc.toObject(MmseResult.class);
                                    if (entity != null) {
                                        entity.setId(doc.getId());
                                        list.add(entity);
                                    }
                                }
                            }
                            callback.onSuccess(list);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing MMSE results", e);
                            callback.onError("Error processing MMSE results: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching MMSE results", e);
                        callback.onError(e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in getByPatientId", e);
            callback.onError("Failed to fetch MMSE results: " + e.getMessage());
        }
    }
}


