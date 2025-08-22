package com.mihir.alzheimerscaregiver.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;
import com.mihir.alzheimerscaregiver.repository.ContactRepository;

import java.util.List;

public class ContactViewModel extends AndroidViewModel {

    private final ContactRepository repository;
    private final LiveData<List<ContactEntity>> contacts;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        repository = new ContactRepository(application);
        contacts = repository.getAll();
    }

    public LiveData<List<ContactEntity>> getContacts() {
        return contacts;
    }

    public LiveData<List<ContactEntity>> search(String query) {
        return repository.search(query);
    }

    public void insert(ContactEntity entity) {
        repository.insert(entity);
    }

    public void update(ContactEntity entity) {
        repository.update(entity);
    }

    public void delete(ContactEntity entity) {
        repository.delete(entity);
    }

    public void setPrimary(long id) {
        repository.setPrimary(id);
    }
}


