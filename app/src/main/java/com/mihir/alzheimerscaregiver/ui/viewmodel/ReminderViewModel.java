package com.mihir.alzheimerscaregiver.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;
import com.mihir.alzheimerscaregiver.repository.ReminderRepository;

import java.util.List;

public class ReminderViewModel extends AndroidViewModel {

    private final ReminderRepository repository;
    private final LiveData<List<ReminderEntity>> allReminders;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        repository = new ReminderRepository();
        allReminders = repository.getAllRemindersSortedByDate();
    }

    public LiveData<List<ReminderEntity>> getAllReminders() {
        return allReminders;
    }

    public LiveData<List<ReminderEntity>> search(String query) {
        return repository.search(query);
    }

    public void insert(ReminderEntity entity) {
        repository.insert(entity);
    }

    public void update(ReminderEntity entity) {
        repository.update(entity);
    }

    public void delete(ReminderEntity entity) {
        repository.delete(entity);
    }

    public void markCompleted(String id, boolean completed) {
        repository.markCompleted(id, completed);
    }
}


