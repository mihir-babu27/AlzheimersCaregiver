package com.mihir.alzheimerscaregiver.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;
import com.mihir.alzheimerscaregiver.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<TaskEntity>> todayTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository();
        todayTasks = repository.getTodayTasks();
    }

    public LiveData<List<TaskEntity>> getTodayTasks() {
        return todayTasks;
    }

    public LiveData<List<TaskEntity>> getAllSortedBySchedule() {
        return repository.getAllSortedBySchedule();
    }

    public LiveData<List<TaskEntity>> getPendingTasks() {
        return repository.getPendingTasks();
    }

    public LiveData<List<TaskEntity>> search(String query) {
        return repository.search(query);
    }

    public LiveData<List<TaskEntity>> getByCategory(String category) {
        return repository.getByCategory(category);
    }

    public void insert(TaskEntity entity) {
        repository.insert(entity);
    }

    public void update(TaskEntity entity) {
        repository.update(entity);
    }

    public void delete(TaskEntity entity) {
        repository.delete(entity);
    }

    public void markCompleted(String id, boolean completed) {
        repository.markCompleted(id, completed);
    }
}


