package com.mihir.alzheimerscaregiver.ui.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;
import com.mihir.alzheimerscaregiver.repository.ReminderRepository;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {

    private static final String TAG = "ReminderViewModel";
    private final ReminderRepository repository;
    private final MutableLiveData<List<ReminderEntity>> allReminders;
    private final MutableLiveData<String> errorMessage;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        try {
            repository = new ReminderRepository();
            allReminders = new MutableLiveData<>();
            errorMessage = new MutableLiveData<>();
            loadReminders();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ReminderViewModel", e);
            throw new RuntimeException("Failed to initialize ReminderViewModel: " + e.getMessage(), e);
        }
    }

    private void loadReminders() {
        try {
            repository.getAllRemindersSortedByDate(new ReminderRepository.FirebaseCallback<List<ReminderEntity>>() {
                @Override
                public void onSuccess(List<ReminderEntity> result) {
                    allReminders.setValue(result);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading reminders: " + error);
                    errorMessage.setValue(error);
                    allReminders.setValue(null);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in loadReminders", e);
            errorMessage.setValue("Failed to load reminders: " + e.getMessage());
            allReminders.setValue(null);
        }
    }

    public LiveData<List<ReminderEntity>> getAllReminders() {
        return allReminders;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void search(String query) {
        try {
            repository.search(query, new ReminderRepository.FirebaseCallback<List<ReminderEntity>>() {
                @Override
                public void onSuccess(List<ReminderEntity> result) {
                    allReminders.setValue(result);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error searching reminders: " + error);
                    errorMessage.setValue(error);
                    allReminders.setValue(null);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in search", e);
            errorMessage.setValue("Failed to search reminders: " + e.getMessage());
            allReminders.setValue(null);
        }
    }

    public void insert(ReminderEntity entity) {
        try {
            repository.insert(entity, new ReminderRepository.FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadReminders(); // Refresh the list
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error inserting reminder: " + error);
                    errorMessage.setValue(error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in insert", e);
            errorMessage.setValue("Failed to insert reminder: " + e.getMessage());
        }
    }

    public void update(ReminderEntity entity) {
        try {
            repository.update(entity, new ReminderRepository.FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadReminders(); // Refresh the list
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error updating reminder: " + error);
                    errorMessage.setValue(error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in update", e);
            errorMessage.setValue("Failed to update reminder: " + e.getMessage());
        }
    }

    public void delete(ReminderEntity entity) {
        try {
            repository.delete(entity, new ReminderRepository.FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadReminders(); // Refresh the list
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error deleting reminder: " + error);
                    errorMessage.setValue(error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in delete", e);
            errorMessage.setValue("Failed to delete reminder: " + e.getMessage());
        }
    }

    public void markCompleted(String id, boolean completed) {
        try {
            repository.markCompleted(id, completed, new ReminderRepository.FirebaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadReminders(); // Refresh the list
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error marking reminder completed: " + error);
                    errorMessage.setValue(error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in markCompleted", e);
            errorMessage.setValue("Failed to mark reminder completed: " + e.getMessage());
        }
    }

    public void refresh() {
        loadReminders();
    }
}


