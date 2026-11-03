package com.kuet.codearena.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kuet.codearena.data.db.dao.ContestDao;
import com.kuet.codearena.data.db.dao.DailyStatDao;
import com.kuet.codearena.data.db.dao.ProblemDao;
import com.kuet.codearena.data.db.dao.ReminderDao;
import com.kuet.codearena.data.db.dao.RunDao;
import com.kuet.codearena.data.db.dao.SolveDao;
import com.kuet.codearena.data.db.dao.UpsolveDao;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.DailyStatEntity;
import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.data.db.entity.ReminderEntity;
import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.data.db.entity.RunProblemEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;

/**
 * Singleton Room database housing all local tables, caches, and metric entities.
 */
@Database(
        entities = {
                ContestEntity.class,
                ProblemEntity.class,
                RunEntity.class,
                RunProblemEntity.class,
                UpsolveItemEntity.class,
                SolveEntity.class,
                DailyStatEntity.class,
                ReminderEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "codearena_db";
    private static volatile AppDatabase INSTANCE;

    public abstract ContestDao contestDao();
    public abstract ProblemDao problemDao();
    public abstract RunDao runDao();
    public abstract UpsolveDao upsolveDao();
    public abstract SolveDao solveDao();
    public abstract DailyStatDao dailyStatDao();
    public abstract ReminderDao reminderDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
