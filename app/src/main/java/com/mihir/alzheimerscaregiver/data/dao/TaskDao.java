package com.mihir.alzheimerscaregiver.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mihir.alzheimerscaregiver.data.entity.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TaskEntity task);

    @Update
    int update(TaskEntity task);

    @Delete
    int delete(TaskEntity task);

    @Query("DELETE FROM tasks WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    LiveData<TaskEntity> getById(long id);

    @Query("SELECT * FROM tasks ORDER BY scheduled_time_epoch_millis ASC")
    LiveData<List<TaskEntity>> getAllSortedBySchedule();

    @Query("SELECT * FROM tasks WHERE DATE(scheduled_time_epoch_millis/1000, 'unixepoch', 'localtime') = DATE('now', 'localtime') ORDER BY scheduled_time_epoch_millis ASC")
    LiveData<List<TaskEntity>> getTodayTasks();

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY scheduled_time_epoch_millis ASC")
    LiveData<List<TaskEntity>> getPendingTasks();

    @Query("UPDATE tasks SET is_completed = :completed WHERE id = :id")
    int markCompleted(long id, boolean completed);

    @Query("SELECT * FROM tasks WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY scheduled_time_epoch_millis ASC")
    LiveData<List<TaskEntity>> search(String query);

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY scheduled_time_epoch_millis ASC")
    LiveData<List<TaskEntity>> getByCategory(String category);
}


