package com.mihir.alzheimerscaregiver.repository;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.entity.MedicationEntity;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;

import java.util.ArrayList;
import java.util.List;

public class MedicationRepository {

    private static final String TAG = "MedicationRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public MedicationRepository() {
        try {
            db = FirebaseConfig.getInstance();
            auth = FirebaseAuth.getInstance();
            
            if (db == null) {
                throw new RuntimeException("Firebase Firestore instance is null");
            }
            if (auth == null) {
                throw new RuntimeException("Firebase Auth instance is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MedicationRepository", e);
            throw new RuntimeException("Failed to initialize MedicationRepository: " + e.getMessage(), e);
        }
    }

    private CollectionReference getMedicationsRef() {
        try {
            String patientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "default";
            return db.collection("patients").document(patientId).collection("medications");
        } catch (Exception e) {
            Log.e(TAG, "Error getting medications reference", e);
            throw new RuntimeException("Failed to get medications reference: " + e.getMessage(), e);
        }
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getAll(FirebaseCallback<List<MedicationEntity>> callback) {
        try {
            CollectionReference medicationsRef = getMedicationsRef();
            medicationsRef.orderBy("time")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<MedicationEntity> list = new ArrayList<>();
                            if (queryDocumentSnapshots != null) {
                                for (var doc : queryDocumentSnapshots) {
                                    MedicationEntity entity = doc.toObject(MedicationEntity.class);
                                    if (entity != null) {
                                        entity.id = doc.getId();
                                        list.add(entity);
                                    }
                                }
                            }
                            callback.onSuccess(list);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing medications data", e);
                            callback.onError("Error processing medications data: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching medications", e);
                        callback.onError(e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in getAll", e);
            callback.onError("Failed to fetch medications: " + e.getMessage());
        }
    }

    public void getActiveMedications(FirebaseCallback<List<MedicationEntity>> callback) {
        try {
            CollectionReference medicationsRef = getMedicationsRef();
            medicationsRef.whereEqualTo("isActive", true)
                    .orderBy("time")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<MedicationEntity> list = new ArrayList<>();
                            if (queryDocumentSnapshots != null) {
                                for (var doc : queryDocumentSnapshots) {
                                    MedicationEntity entity = doc.toObject(MedicationEntity.class);
                                    if (entity != null) {
                                        entity.id = doc.getId();
                                        list.add(entity);
                                    }
                                }
                            }
                            callback.onSuccess(list);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing active medications data", e);
                            callback.onError("Error processing active medications data: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching active medications", e);
                        callback.onError(e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in getActiveMedications", e);
            callback.onError("Failed to fetch active medications: " + e.getMessage());
        }
    }

    public void search(String query, FirebaseCallback<List<MedicationEntity>> callback) {
        try {
            CollectionReference medicationsRef = getMedicationsRef();
            medicationsRef.whereGreaterThanOrEqualTo("name", query)
                    .whereLessThanOrEqualTo("name", query + '\uf8ff')
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<MedicationEntity> list = new ArrayList<>();
                            if (queryDocumentSnapshots != null) {
                                for (var doc : queryDocumentSnapshots) {
                                    MedicationEntity entity = doc.toObject(MedicationEntity.class);
                                    if (entity != null) {
                                        entity.id = doc.getId();
                                        list.add(entity);
                                    }
                                }
                            }
                            callback.onSuccess(list);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing search results", e);
                            callback.onError("Error processing search results: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error searching medications", e);
                        callback.onError(e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in search", e);
            callback.onError("Failed to search medications: " + e.getMessage());
        }
    }

    public void insert(MedicationEntity medication, FirebaseCallback<Void> callback) {
        try {
            CollectionReference medicationsRef = getMedicationsRef();
            if (medication.id == null || medication.id.isEmpty()) {
                // Generate a new ID if not present
                DocumentReference docRef = medicationsRef.document();
                medication.id = docRef.getId();
                docRef.set(medication)
                        .addOnSuccessListener(aVoid -> {
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error inserting medication", e);
                            callback.onError(e.getMessage());
                        });
            } else {
                medicationsRef.document(medication.id).set(medication)
                        .addOnSuccessListener(aVoid -> {
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error inserting medication", e);
                            callback.onError(e.getMessage());
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in insert", e);
            callback.onError("Failed to insert medication: " + e.getMessage());
        }
    }

    public void update(MedicationEntity medication, FirebaseCallback<Void> callback) {
        try {
            CollectionReference medicationsRef = getMedicationsRef();
            if (medication.id != null && !medication.id.isEmpty()) {
                medicationsRef.document(medication.id).set(medication)
                        .addOnSuccessListener(aVoid -> {
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating medication", e);
                            callback.onError(e.getMessage());
                        });
            } else {
                callback.onError("Medication ID is required for update");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in update", e);
            callback.onError("Failed to update medication: " + e.getMessage());
        }
    }

    public void delete(MedicationEntity medication, FirebaseCallback<Void> callback) {
        try {
            CollectionReference medicationsRef = getMedicationsRef();
            if (medication.id != null && !medication.id.isEmpty()) {
                medicationsRef.document(medication.id).delete()
                        .addOnSuccessListener(aVoid -> {
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting medication", e);
                            callback.onError(e.getMessage());
                        });
            } else {
                callback.onError("Medication ID is required for deletion");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in delete", e);
            callback.onError("Failed to delete medication: " + e.getMessage());
        }
    }

    public void toggleActive(String id, boolean isActive, FirebaseCallback<Void> callback) {
        try {
            CollectionReference medicationsRef = getMedicationsRef();
            medicationsRef.document(id).update("isActive", isActive)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error toggling medication active status", e);
                        callback.onError(e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in toggleActive", e);
            callback.onError("Failed to toggle medication active status: " + e.getMessage());
        }
    }
}
