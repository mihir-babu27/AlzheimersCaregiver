package com.mihir.alzheimerscaregiver.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;
import com.mihir.alzheimerscaregiver.repository.ContactRepository;

import java.util.List;

public class ContactViewModel extends AndroidViewModel {

    private final ContactRepository repository;
    private final MutableLiveData<List<ContactEntity>> contacts;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        repository = new ContactRepository();
        contacts = new MutableLiveData<>();
        loadContacts();
    }

    private void loadContacts() {
        repository.getAll(new ContactRepository.FirebaseCallback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.setValue(result);
            }

            @Override
            public void onError(String error) {
                contacts.setValue(null);
            }
        });
    }

    public LiveData<List<ContactEntity>> getContacts() {
        return contacts;
    }

    public void search(String query) {
        repository.search(query, new ContactRepository.FirebaseCallback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.setValue(result);
            }

            @Override
            public void onError(String error) {
                contacts.setValue(null);
            }
        });
    }

    public void insert(ContactEntity entity) {
        repository.insert(entity, new ContactRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts(); // Refresh the list
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void update(ContactEntity entity) {
        repository.update(entity, new ContactRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts(); // Refresh the list
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void delete(ContactEntity entity) {
        repository.delete(entity, new ContactRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts(); // Refresh the list
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void setPrimary(String id) {
        repository.setPrimary(id, new ContactRepository.FirebaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts(); // Refresh the list
            }

            @Override
            public void onError(String error) {
                // Handle error
            }
        });
    }

    public void refresh() {
        loadContacts();
    }
}


