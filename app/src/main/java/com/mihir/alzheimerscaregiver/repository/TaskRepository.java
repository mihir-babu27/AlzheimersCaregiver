
package com.mihir.alzheimerscaregiver.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private final FirebaseFirestore db = FirebaseConfig.getInstance();
    private final CollectionReference tasksRef = db.collection("tasks");

    public LiveData<List<TaskEntity>> getAllSortedBySchedule() {
        MutableLiveData<List<TaskEntity>> liveData = new MutableLiveData<>();
        tasksRef.orderBy("scheduledTimeEpochMillis")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<TaskEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            TaskEntity entity = dc.getDocument().toObject(TaskEntity.class);
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

    public LiveData<List<TaskEntity>> getTodayTasks() {
        MutableLiveData<List<TaskEntity>> liveData = new MutableLiveData<>();
        long startOfDay = getStartOfDay();
        long endOfDay = getEndOfDay();
        
        tasksRef.whereGreaterThanOrEqualTo("scheduledTimeEpochMillis", startOfDay)
                .whereLessThanOrEqualTo("scheduledTimeEpochMillis", endOfDay)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<TaskEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            TaskEntity entity = dc.getDocument().toObject(TaskEntity.class);
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

    public LiveData<List<TaskEntity>> getPendingTasks() {
        MutableLiveData<List<TaskEntity>> liveData = new MutableLiveData<>();
        tasksRef.whereEqualTo("isCompleted", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<TaskEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            TaskEntity entity = dc.getDocument().toObject(TaskEntity.class);
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

    public LiveData<List<TaskEntity>> search(String query) {
        MutableLiveData<List<TaskEntity>> liveData = new MutableLiveData<>();
        tasksRef.whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<TaskEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            TaskEntity entity = dc.getDocument().toObject(TaskEntity.class);
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

    public LiveData<List<TaskEntity>> getByCategory(String category) {
        MutableLiveData<List<TaskEntity>> liveData = new MutableLiveData<>();
        tasksRef.whereEqualTo("category", category)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<TaskEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            TaskEntity entity = dc.getDocument().toObject(TaskEntity.class);
                            list.add(entity);
                        }
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }

    public void insert(TaskEntity task) {
        if (task.id == null || task.id.isEmpty()) {
            DocumentReference docRef = tasksRef.document();
            task.id = docRef.getId();
            docRef.set(task)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        } else {
            tasksRef.document(task.id).set(task)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void update(TaskEntity task) {
        if (task.id != null && !task.id.isEmpty()) {
            tasksRef.document(task.id).set(task)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void delete(TaskEntity task) {
        if (task.id != null && !task.id.isEmpty()) {
            tasksRef.document(task.id).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void markCompleted(String id, boolean completed) {
        tasksRef.document(id).update("isCompleted", completed)
                .addOnSuccessListener(aVoid -> {
                    // Success
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    // Helper methods for date calculations
    private long getStartOfDay() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private long getEndOfDay() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return today.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}


