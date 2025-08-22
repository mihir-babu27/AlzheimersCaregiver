package com.mihir.alzheimerscaregiver.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReminderEntity reminder);

    @Update
    int update(ReminderEntity reminder);

    @Delete
    int delete(ReminderEntity reminder);

    @Query("DELETE FROM reminders WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    LiveData<ReminderEntity> getById(long id);

    @Query("SELECT * FROM reminders ORDER BY scheduled_time_epoch_millis ASC")
    LiveData<List<ReminderEntity>> getAllSortedByDate();

    @Query("UPDATE reminders SET is_completed = :completed WHERE id = :id")
    int markCompleted(long id, boolean completed);

    @Query("SELECT * FROM reminders WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY scheduled_time_epoch_millis ASC")
    LiveData<List<ReminderEntity>> search(String query);
}


