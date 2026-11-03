package com.kuet.codearena.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kuet.codearena.data.db.entity.ReminderEntity;

import java.util.List;

/**
 * Data Access Object managing contest reminder alarm scheduling records.
 */
@Dao
public interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReminderEntity reminder);

    @Update
    void update(ReminderEntity reminder);

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    ReminderEntity getById(long id);

    @Query("SELECT * FROM reminders WHERE contestId = :contestId AND leadTimeMinutes = :leadMinutes LIMIT 1")
    ReminderEntity getByContestAndLead(int contestId, int leadMinutes);

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND reminderTimeMs > :nowMs ORDER BY reminderTimeMs ASC")
    List<ReminderEntity> getFutureActiveReminders(long nowMs);

    @Query("SELECT * FROM reminders WHERE contestId = :contestId")
    LiveData<List<ReminderEntity>> getRemindersForContestLive(int contestId);

    @Query("SELECT * FROM reminders WHERE contestId = :contestId")
    List<ReminderEntity> getRemindersForContestSync(int contestId);

    @Query("SELECT * FROM reminders ORDER BY reminderTimeMs ASC")
    LiveData<List<ReminderEntity>> getAllRemindersLive();

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM reminders WHERE contestId = :contestId AND leadTimeMinutes = :leadMinutes")
    void deleteByContestAndLead(int contestId, int leadMinutes);
}
