package com.codearena.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.codearena.data.db.dao.AiCacheDao;
import com.codearena.data.db.dao.ContestDao;
import com.codearena.data.db.dao.NoteDao;
import com.codearena.data.db.dao.ProblemDao;
import com.codearena.data.db.dao.ProblemResultDao;
import com.codearena.data.db.dao.RankEstimateDao;
import com.codearena.data.db.dao.TagXpDao;
import com.codearena.data.db.dao.UpsolveDao;
import com.codearena.data.db.dao.VirtualRunDao;
import com.codearena.data.db.entity.AiCacheEntity;
import com.codearena.data.db.entity.ContestEntity;
import com.codearena.data.db.entity.NoteEntity;
import com.codearena.data.db.entity.ProblemEntity;
import com.codearena.data.db.entity.ProblemResultEntity;
import com.codearena.data.db.entity.RankEstimateEntity;
import com.codearena.data.db.entity.TagXpEntity;
import com.codearena.data.db.entity.UpsolveItemEntity;
import com.codearena.data.db.entity.VirtualRunEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AppDatabase: Room Database providing local persistence over SQLite,
 * isolated per logged-in Firebase user.
 */
@Database(
    entities = {
        ContestEntity.class,
        ProblemEntity.class,
        VirtualRunEntity.class,
        ProblemResultEntity.class,
        UpsolveItemEntity.class,
        NoteEntity.class,
        TagXpEntity.class,
        AiCacheEntity.class,
        RankEstimateEntity.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract ContestDao contestDao();
    public abstract ProblemDao problemDao();
    public abstract VirtualRunDao virtualRunDao();
    public abstract ProblemResultDao problemResultDao();
    public abstract UpsolveDao upsolveDao();
    public abstract NoteDao noteDao();
    public abstract TagXpDao tagXpDao();
    public abstract AiCacheDao aiCacheDao();
    public abstract RankEstimateDao rankEstimateDao();

    private static volatile AppDatabase INSTANCE;
    private static volatile String currentDbName;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static String getDatabaseName() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getUid() != null && !user.getUid().isEmpty()) {
                return "codearena_db_" + user.getUid();
            }
        } catch (Exception ignored) {}
        return "codearena_db_guest";
    }

    public static AppDatabase getInstance(Context context) {
        String dbName = getDatabaseName();
        if (INSTANCE == null || !dbName.equals(currentDbName)) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null || !dbName.equals(currentDbName)) {
                    if (INSTANCE != null) {
                        try {
                            INSTANCE.close();
                        } catch (Exception ignored) {}
                    }
                    currentDbName = dbName;
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        dbName
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
