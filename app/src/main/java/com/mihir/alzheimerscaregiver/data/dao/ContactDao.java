package com.mihir.alzheimerscaregiver.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;

import java.util.List;

@Dao
public interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(ContactEntity contact);

    @Update
    int update(ContactEntity contact);

    @Delete
    int delete(ContactEntity contact);

    @Query("DELETE FROM contacts WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM contacts ORDER BY is_primary DESC, name ASC")
    LiveData<List<ContactEntity>> getAll();

    @Query("UPDATE contacts SET is_primary = CASE WHEN id = :id THEN 1 ELSE 0 END")
    void setPrimary(long id);

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phone_number LIKE '%' || :query || '%' ORDER BY is_primary DESC, name ASC")
    LiveData<List<ContactEntity>> search(String query);
}


