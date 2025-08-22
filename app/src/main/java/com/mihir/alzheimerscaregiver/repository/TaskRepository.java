package com.mihir.alzheimerscaregiver.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.mihir.alzheimerscaregiver.data.AppDatabase;
import com.mihir.alzheimerscaregiver.data.dao.TaskDao;
import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public TaskRepository(Context context) {
        this.taskDao = AppDatabase.getInstance(context).taskDao();
    }

    public LiveData<List<TaskEntity>> getAllSortedBySchedule() {
        return taskDao.getAllSortedBySchedule();
    }

    public LiveData<List<TaskEntity>> getTodayTasks() {
        return taskDao.getTodayTasks();
    }

    public LiveData<List<TaskEntity>> getPendingTasks() {
        return taskDao.getPendingTasks();
    }

    public LiveData<List<TaskEntity>> search(String query) {
        return taskDao.search(query);
    }

    public LiveData<List<TaskEntity>> getByCategory(String category) {
        return taskDao.getByCategory(category);
    }

    public void insert(TaskEntity task) {
        ioExecutor.execute(() -> taskDao.insert(task));
    }

    public void update(TaskEntity task) {
        ioExecutor.execute(() -> taskDao.update(task));
    }

    public void delete(TaskEntity task) {
        ioExecutor.execute(() -> taskDao.delete(task));
    }

    public void markCompleted(long id, boolean completed) {
        ioExecutor.execute(() -> taskDao.markCompleted(id, completed));
    }
}


