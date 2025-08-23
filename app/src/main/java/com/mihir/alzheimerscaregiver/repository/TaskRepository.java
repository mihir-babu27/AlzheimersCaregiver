
package com.mihir.alzheimerscaregiver.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private final FirebaseFirestore db = FirebaseConfig.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private CollectionReference getTasksRef() {
        String patientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "default";
        return db.collection("patients").document(patientId).collection("tasks");
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getAllSortedBySchedule(FirebaseCallback<List<TaskEntity>> callback) {
        getTasksRef().orderBy("scheduledTimeEpochMillis")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TaskEntity> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots) {
                            TaskEntity entity = doc.toObject(TaskEntity.class);
                            if (entity != null) {
                                entity.id = doc.getId();
                                list.add(entity);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getTodayTasks(FirebaseCallback<List<TaskEntity>> callback) {
        long startOfDay = getStartOfDay();
        long endOfDay = getEndOfDay();
        
        getTasksRef().whereGreaterThanOrEqualTo("scheduledTimeEpochMillis", startOfDay)
                .whereLessThanOrEqualTo("scheduledTimeEpochMillis", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TaskEntity> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots) {
                            TaskEntity entity = doc.toObject(TaskEntity.class);
                            if (entity != null) {
                                entity.id = doc.getId();
                                list.add(entity);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getPendingTasks(FirebaseCallback<List<TaskEntity>> callback) {
        getTasksRef().whereEqualTo("isCompleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TaskEntity> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots) {
                            TaskEntity entity = doc.toObject(TaskEntity.class);
                            if (entity != null) {
                                entity.id = doc.getId();
                                list.add(entity);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void search(String query, FirebaseCallback<List<TaskEntity>> callback) {
        getTasksRef().whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TaskEntity> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots) {
                            TaskEntity entity = doc.toObject(TaskEntity.class);
                            if (entity != null) {
                                entity.id = doc.getId();
                                list.add(entity);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void getByCategory(String category, FirebaseCallback<List<TaskEntity>> callback) {
        getTasksRef().whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TaskEntity> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots) {
                            TaskEntity entity = doc.toObject(TaskEntity.class);
                            if (entity != null) {
                                entity.id = doc.getId();
                                list.add(entity);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void insert(TaskEntity task, FirebaseCallback<Void> callback) {
        if (task.id == null || task.id.isEmpty()) {
            DocumentReference docRef = getTasksRef().document();
            task.id = docRef.getId();
            docRef.set(task)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        } else {
            getTasksRef().document(task.id).set(task)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        }
    }

    public void update(TaskEntity task, FirebaseCallback<Void> callback) {
        if (task.id != null && !task.id.isEmpty()) {
            getTasksRef().document(task.id).set(task)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        } else {
            callback.onError("Task ID is required for update");
        }
    }

    public void delete(TaskEntity task, FirebaseCallback<Void> callback) {
        if (task.id != null && !task.id.isEmpty()) {
            getTasksRef().document(task.id).delete()
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        } else {
            callback.onError("Task ID is required for deletion");
        }
    }

    public void markCompleted(String id, boolean completed, FirebaseCallback<Void> callback) {
        getTasksRef().document(id).update("isCompleted", completed)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
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


