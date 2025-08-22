package com.mihir.alzheimerscaregiver.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.mihir.alzheimerscaregiver.data.AppDatabase;
import com.mihir.alzheimerscaregiver.data.dao.ContactDao;
import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactRepository {

    private final ContactDao contactDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public ContactRepository(Context context) {
        this.contactDao = AppDatabase.getInstance(context).contactDao();
    }

    public LiveData<List<ContactEntity>> getAll() {
        return contactDao.getAll();
    }

    public LiveData<List<ContactEntity>> search(String query) {
        return contactDao.search(query);
    }

    public void insert(ContactEntity contact) {
        ioExecutor.execute(() -> contactDao.insert(contact));
    }

    public void update(ContactEntity contact) {
        ioExecutor.execute(() -> contactDao.update(contact));
    }

    public void delete(ContactEntity contact) {
        ioExecutor.execute(() -> contactDao.delete(contact));
    }

    public void setPrimary(long id) {
        ioExecutor.execute(() -> contactDao.setPrimary(id));
    }
}


