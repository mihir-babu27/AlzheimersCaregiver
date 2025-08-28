package com.mihir.alzheimerscaregiver.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;
import com.mihir.alzheimerscaregiver.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<List<TaskEntity>> todayTasks;
    private final MutableLiveData<List<TaskEntity>> allTasks;
    private final MutableLiveData<List<TaskEntity>> pendingTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository();
        todayTasks = new MutableLiveData<>();
        allTasks = new MutableLiveData<>();
        pendingTasks = new MutableLiveData<>();
        loadTasks();
    }

    private void loadTasks() {
        loadTodayTasks();
        loadAllTasks();
        loadPendingTasks();
    }

    private void loadTodayTasks() {
        repository.getTodayTasks(new TaskRepository.FirebaseCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> result) {
                todayTasks.setValue(result);
            }

            @Override
            public void onError(String error) {
                todayTasks.setValue(null);
            }
        });
    }

    private void loadAllTasks() {
        repository.getAllSortedBySchedule(new TaskRepository.FirebaseCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> result) {
                allTasks.setValue(result);
            }

            @Override
            public void onError(String error) {
                allTasks.setValue(null);
            }
        });
    }

    private void loadPendingTasks() {
        repository.getPendingTasks(new TaskRepository.FirebaseCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> result) {
                pendingTasks.setValue(result);
            }

            @Override
            public void onError(String error) {
                pendingTasks.setValue(null);
            }
        });
    }

    public LiveData<List<TaskEntity>> getTodayTasks() {
        return todayTasks;
    }

    public LiveData<List<TaskEntity>> getAllSortedBySchedule() {
        return allTasks;
    }

    public LiveData<List<TaskEntity>> getPendingTasks() {
        return pendingTasks;
    }

    public void search(String query) {
        repository.search(query, new TaskRepository.FirebaseCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> result) {
                allTasks.setValue(result);
            }

            @Override
            public void onError(String error) {
                allTasks.setValue(null);
            }
        });
    }

    public void getByCategory(String category) {
        repository.getByCategory(category, new TaskRepository.FirebaseCallback<List<TaskEntity>>() {
            @Override
            public void onSuccess(List<TaskEntity> result) {
                allTasks.setValue(result);
            }

            @Override
            public void onError(String error) {
                allTasks.setValue(null);
            }
        });
    }

    public void insert(TaskEntity entity) {
        repository.insert(entity, new TaskRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadTasks(); // Refresh all lists
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void update(TaskEntity entity) {
        repository.update(entity, new TaskRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadTasks(); // Refresh all lists
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void delete(TaskEntity entity) {
        repository.delete(entity, new TaskRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadTasks(); // Refresh all lists
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void markCompleted(String id, boolean completed) {
        repository.markCompleted(id, completed, new TaskRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadTasks(); // Refresh all lists
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void refresh() {
        loadTasks();
    }
}


