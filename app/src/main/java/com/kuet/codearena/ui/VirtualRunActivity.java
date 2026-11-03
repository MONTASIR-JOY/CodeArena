package com.kuet.codearena.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.kuet.codearena.data.db.entity.RunProblemEntity;
import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.data.repository.VirtualRunRepository;
import com.kuet.codearena.databinding.ActivityVirtualRunBinding;
import com.kuet.codearena.service.VirtualContestService;
import com.kuet.codearena.ui.adapter.RunProblemAdapter;
import com.kuet.codearena.util.PreferenceHelper;

import java.util.List;
import java.util.Locale;

/**
 * Activity hosting the live virtual contest simulator with lifecycle logging and real-time timer.
 */
public class VirtualRunActivity extends AppCompatActivity implements RunProblemAdapter.OnRunProblemClickListener {
    private static final String TAG = "VirtualRunActivity";

    public static final String EXTRA_CONTEST_ID = "extra_contest_id";
    public static final String EXTRA_CONTEST_NAME = "extra_contest_name";
    public static final String EXTRA_DURATION_SECONDS = "extra_duration_seconds";
    public static final String EXTRA_RUN_ID = "extra_run_id";

    private static final String KEY_STATE_RUN_ID = "state_run_id";
    private static final String KEY_STATE_CONTEST_ID = "state_contest_id";
    private static final String KEY_STATE_CONTEST_NAME = "state_contest_name";
    private static final String KEY_STATE_DURATION_MS = "state_duration_ms";
    private static final String KEY_STATE_START_TIME_MS = "state_start_time_ms";

    private ActivityVirtualRunBinding binding;
    private VirtualRunRepository repository;
    private PreferenceHelper preferenceHelper;
    private RunProblemAdapter adapter;

    private long runId = -1;
    private int contestId;
    private String contestName;
    private long durationMs;
    private long startTimeMs;

    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && VirtualContestService.ACTION_TICK_BROADCAST.equals(intent.getAction())) {
                long remainingMs = intent.getLongExtra(VirtualContestService.EXTRA_REMAINING_MS, 0);
                long totalMs = intent.getLongExtra(VirtualContestService.EXTRA_DURATION_MS, durationMs);
                String alert = intent.getStringExtra(VirtualContestService.EXTRA_ALERT);

                updateTimerDisplay(remainingMs, totalMs);

                if ("HALFWAY".equals(alert)) {
                    Snackbar.make(binding.virtualRunCoordinator, "Halfway point reached!", Snackbar.LENGTH_LONG).show();
                } else if ("TEN_MINUTES".equals(alert)) {
                    Snackbar.make(binding.virtualRunCoordinator, "10 minutes remaining! Finish strong!", Snackbar.LENGTH_LONG).show();
                } else if ("FINISHED".equals(alert)) {
                    openPostMortem();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Lifecycle: onCreate()");
        binding = ActivityVirtualRunBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = VirtualRunRepository.getInstance(this);
        preferenceHelper = new PreferenceHelper(this);

        if (savedInstanceState != null) {
            // Restore state after rotation and process death
            runId = savedInstanceState.getLong(KEY_STATE_RUN_ID, -1);
            contestId = savedInstanceState.getInt(KEY_STATE_CONTEST_ID, -1);
            contestName = savedInstanceState.getString(KEY_STATE_CONTEST_NAME);
            durationMs = savedInstanceState.getLong(KEY_STATE_DURATION_MS, 7200000L);
            startTimeMs = savedInstanceState.getLong(KEY_STATE_START_TIME_MS, System.currentTimeMillis());
            Log.d(TAG, "Restored state after process death/rotation for runId: " + runId);
        } else {
            runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
            contestId = getIntent().getIntExtra(EXTRA_CONTEST_ID, -1);
            contestName = getIntent().getStringExtra(EXTRA_CONTEST_NAME);
            long durationSeconds = getIntent().getLongExtra(EXTRA_DURATION_SECONDS, 7200);
            durationMs = durationSeconds * 1000L;
            startTimeMs = System.currentTimeMillis();
        }

        setupViews();
        setupRecyclerView();

        if (runId == -1) {
            // New run: initialize in Room and start ForegroundService
            startNewRun();
        } else {
            // Reconnected to existing run
            observeRunData();
        }
    }

    private void setupViews() {
        binding.toolbarRun.setNavigationOnClickListener(v -> confirmFinishOrExit());
        binding.tvRunContestTitle.setText(contestName != null ? contestName : "Contest " + contestId);
        binding.tvSyncHandleIndicator.setText("Checking submissions every 15s for @" + preferenceHelper.getCfHandle());

        binding.btnFinishRunEarly.setOnClickListener(v -> confirmFinishOrExit());
    }

    private void setupRecyclerView() {
        adapter = new RunProblemAdapter(this);
        binding.rvRunProblems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRunProblems.setAdapter(adapter);
    }

    private void startNewRun() {
        repository.startRun(contestId, contestName, durationMs / 1000L, new VirtualRunRepository.RunCallback<Long>() {
            @Override
            public void onSuccess(Long newRunId) {
                runId = newRunId;
                startTimeMs = System.currentTimeMillis();

                // Start Foreground Service
                Intent serviceIntent = VirtualContestService.createStartIntent(
                        VirtualRunActivity.this, runId, contestId, contestName, startTimeMs, durationMs
                );
                ContextCompat.startForegroundService(VirtualRunActivity.this, serviceIntent);

                observeRunData();
            }

            @Override
            public void onError(String error) {
                Snackbar.make(binding.virtualRunCoordinator, "Failed to start run: " + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void observeRunData() {
        repository.getRunLive(runId).observe(this, run -> {
            if (run != null) {
                binding.tvLivePenalty.setText(run.getTotalPenaltyMinutes() + "m");
                binding.tvLiveScore.setText(String.format(Locale.getDefault(), "%.0f", run.getScore()));
            }
        });

        repository.getRunProblemsLive(runId).observe(this, problems -> {
            if (problems != null) {
                adapter.submitList(problems);
                int solved = 0;
                for (RunProblemEntity p : problems) {
                    if (p.isSolved()) solved++;
                }
                binding.tvLiveSolved.setText(solved + " / " + problems.size());
            }
        });
    }

    private void updateTimerDisplay(long remainingMs, long totalMs) {
        long hours = remainingMs / 3600000;
        long minutes = (remainingMs % 3600000) / 60000;
        long seconds = (remainingMs % 60000) / 1000;
        String formatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        binding.circularTimer.setCenterText(formatted);
        float progress = totalMs > 0 ? ((float) remainingMs / (float) totalMs) * 100f : 0f;
        binding.circularTimer.setProgress(progress);
    }

    private void confirmFinishOrExit() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Finish Virtual Contest?")
                .setMessage("Are you sure you want to end this contest run early? Your current score and penalties will be recorded.")
                .setPositiveButton("Finish & Post-Mortem", (dialog, which) -> {
                    Intent stopIntent = VirtualContestService.createStopIntent(this);
                    startService(stopIntent);

                    repository.finishRun(runId, new VirtualRunRepository.RunCallback<com.kuet.codearena.data.db.entity.RunEntity>() {
                        @Override
                        public void onSuccess(com.kuet.codearena.data.db.entity.RunEntity result) {
                            openPostMortem();
                        }

                        @Override
                        public void onError(String error) {
                            openPostMortem();
                        }
                    });
                })
                .setNegativeButton("Continue Run", null)
                .show();
    }

    private void openPostMortem() {
        Intent intent = new Intent(this, PostMortemActivity.class);
        intent.putExtra(PostMortemActivity.EXTRA_RUN_ID, runId);
        startActivity(intent);
        finish();
    }

    @Override
    public void onProblemClick(RunProblemEntity problem) {
        // Implicit intent to open problem in web browser
        String url = Endpoints.CF_PROBLEM_WEB_BASE + contestId + "/problem/" + problem.getProblemIndex();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Lifecycle: onStart()");
        IntentFilter filter = new IntentFilter(VirtualContestService.ACTION_TICK_BROADCAST);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(tickReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(tickReceiver, filter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Lifecycle: onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Lifecycle: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Lifecycle: onStop()");
        try {
            unregisterReceiver(tickReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Lifecycle: onDestroy()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Lifecycle: onRestart()");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Lifecycle: onSaveInstanceState()");
        outState.putLong(KEY_STATE_RUN_ID, runId);
        outState.putInt(KEY_STATE_CONTEST_ID, contestId);
        outState.putString(KEY_STATE_CONTEST_NAME, contestName);
        outState.putLong(KEY_STATE_DURATION_MS, durationMs);
        outState.putLong(KEY_STATE_START_TIME_MS, startTimeMs);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "Lifecycle: onRestoreInstanceState()");
    }
}
