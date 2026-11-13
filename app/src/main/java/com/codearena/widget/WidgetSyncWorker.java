package com.codearena.widget;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.codearena.data.model.CfContest;
import com.codearena.data.repository.ContestRepository;
import java.util.List;

/**
 * WidgetSyncWorker: Periodically syncs contest data in the background
 * to keep home screen widgets updated efficiently without battery drain.
 */
public class WidgetSyncWorker extends Worker {
    private static final String TAG = "WidgetSyncWorker";

    public WidgetSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.i(TAG, "Starting background widget synchronization...");
            Context context = getApplicationContext();

            ContestRepository contestRepo = new ContestRepository(context);
            contestRepo.fetchUpcomingContests(new ContestRepository.Callback<List<CfContest>>() {
                @Override
                public void onSuccess(List<CfContest> result) {
                    Log.i(TAG, "Widget background contest sync successful: " + (result != null ? result.size() : 0));
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Widget background contest sync failed", e);
                }
            });

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error in WidgetSyncWorker", e);
            return Result.failure();
        }
    }
}
