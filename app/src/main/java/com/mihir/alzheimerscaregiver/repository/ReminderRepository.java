
package com.mihir.alzheimerscaregiver.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;

import java.util.ArrayList;
import java.util.List;

public class ReminderRepository {

    private final FirebaseFirestore db = FirebaseConfig.getInstance();
    private final CollectionReference remindersRef = db.collection("reminders");

    public LiveData<List<ReminderEntity>> getAllRemindersSortedByDate() {
        MutableLiveData<List<ReminderEntity>> liveData = new MutableLiveData<>();
        remindersRef.orderBy("scheduledTimeEpochMillis")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<ReminderEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            ReminderEntity entity = dc.getDocument().toObject(ReminderEntity.class);
                            if (entity != null) {
                                entity.id = dc.getDocument().getId();
                                list.add(entity);
                            }
                        }
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }

    public LiveData<List<ReminderEntity>> search(String query) {
        MutableLiveData<List<ReminderEntity>> liveData = new MutableLiveData<>();
        remindersRef.whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + '\uf8ff')
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<ReminderEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            ReminderEntity entity = dc.getDocument().toObject(ReminderEntity.class);
                            if (entity != null) {
                                entity.id = dc.getDocument().getId();
                                list.add(entity);
                            }
                        }
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }

    public void insert(ReminderEntity reminder) {
        if (reminder.id == null || reminder.id.isEmpty()) {
            // Generate a new ID if not present
            DocumentReference docRef = remindersRef.document();
            reminder.id = docRef.getId();
            docRef.set(reminder)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        } else {
            remindersRef.document(reminder.id).set(reminder)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void update(ReminderEntity reminder) {
        if (reminder.id != null && !reminder.id.isEmpty()) {
            remindersRef.document(reminder.id).set(reminder)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void delete(ReminderEntity reminder) {
        if (reminder.id != null && !reminder.id.isEmpty()) {
            remindersRef.document(reminder.id).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void markCompleted(String id, boolean completed) {
        remindersRef.document(id).update("isCompleted", completed)
                .addOnSuccessListener(aVoid -> {
                    // Success
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}


