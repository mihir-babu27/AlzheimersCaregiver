package com.mihir.alzheimerscaregiver.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.mihir.alzheimerscaregiver.data.AppDatabase;
import com.mihir.alzheimerscaregiver.data.dao.ReminderDao;
import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderRepository {

    private final ReminderDao reminderDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public ReminderRepository(Context context) {
        this.reminderDao = AppDatabase.getInstance(context).reminderDao();
    }

    public LiveData<List<ReminderEntity>> getAllRemindersSortedByDate() {
        return reminderDao.getAllSortedByDate();
    }

    public LiveData<List<ReminderEntity>> search(String query) {
        return reminderDao.search(query);
    }

    public void insert(ReminderEntity reminder) {
        ioExecutor.execute(() -> reminderDao.insert(reminder));
    }

    public void update(ReminderEntity reminder) {
        ioExecutor.execute(() -> reminderDao.update(reminder));
    }

    public void delete(ReminderEntity reminder) {
        ioExecutor.execute(() -> reminderDao.delete(reminder));
    }

    public void markCompleted(long id, boolean completed) {
        ioExecutor.execute(() -> reminderDao.markCompleted(id, completed));
    }
}


