package com.kuet.codearena.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kuet.codearena.data.db.entity.RunEntity;
import com.kuet.codearena.data.db.entity.RunProblemEntity;
import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.data.repository.VirtualRunRepository;
import com.kuet.codearena.databinding.ActivityPostMortemBinding;
import com.kuet.codearena.ui.adapter.PostMortemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity presenting virtual contest performance metrics, penalty breakdowns, and sharing options.
 */
public class PostMortemActivity extends AppCompatActivity {
    public static final String EXTRA_RUN_ID = "extra_run_id";

    private ActivityPostMortemBinding binding;
    private VirtualRunRepository repository;
    private PostMortemAdapter adapter;

    private long runId;
    private RunEntity currentRun;
    private List<RunProblemEntity> runProblems = new ArrayList<>();

    public static Intent newIntent(Context context, long runId) {
        Intent intent = new Intent(context, PostMortemActivity.class);
        intent.putExtra(EXTRA_RUN_ID, runId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostMortemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
        repository = VirtualRunRepository.getInstance(this);

        setupViews();
        observeRunData();
    }

    private void setupViews() {
        binding.toolbarPostMortem.setNavigationOnClickListener(v -> finish());
        binding.btnDone.setOnClickListener(v -> finish());

        binding.btnShareResult.setOnClickListener(v -> {
            if (currentRun != null) {
                shareResult();
            }
        });
    }

    private void observeRunData() {
        repository.getRunLive(runId).observe(this, run -> {
            if (run != null) {
                currentRun = run;
                binding.tvContestName.setText(run.getContestName());
                binding.tvFinalScore.setText(String.format(Locale.getDefault(), "%.0f pts", run.getScore()));
                binding.tvTotalPenalty.setText(run.getTotalPenaltyMinutes() + "m");
                binding.tvTotalWrong.setText(String.valueOf(run.getTotalWrongAttempts()));

                long durationMins = Math.max(1, run.getDurationMs() / 60000L);
                if (adapter == null) {
                    setupRecyclerView(durationMins);
                }
            }
        });

        repository.getRunProblemsLive(runId).observe(this, problems -> {
            if (problems != null) {
                runProblems = problems;
                int solved = 0;
                for (RunProblemEntity p : problems) {
                    if (p.isSolved()) solved++;
                }
                binding.tvSolvedRatio.setText(solved + " / " + problems.size());

                if (adapter != null) {
                    adapter.submitList(problems);
                }
            }
        });
    }

    private void setupRecyclerView(long durationMinutes) {
        adapter = new PostMortemAdapter(durationMinutes, problem -> {
            if (currentRun != null) {
                // Implicit intent to open problem in browser
                String url = Endpoints.CF_PROBLEM_WEB_BASE + currentRun.getContestId() + "/problem/" + problem.getProblemIndex();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
        binding.rvPostMortemProblems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPostMortemProblems.setAdapter(adapter);
        if (!runProblems.isEmpty()) {
            adapter.submitList(runProblems);
        }
    }

    private void shareResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDFC6 CodeArena Virtual Contest Result\n");
        sb.append("Contest: ").append(currentRun.getContestName()).append("\n");
        sb.append(String.format(Locale.getDefault(), "Score: %.0f points\n", currentRun.getScore()));
        sb.append("Solved: ").append(binding.tvSolvedRatio.getText()).append("\n");
        sb.append("Total Penalty: ").append(currentRun.getTotalPenaltyMinutes()).append(" minutes\n");
        sb.append("Wrong Attempts: ").append(currentRun.getTotalWrongAttempts()).append("\n\n");
        sb.append("Problem Breakdown:\n");

        for (RunProblemEntity p : runProblems) {
            sb.append("• [").append(p.getProblemIndex()).append("] ").append(p.getProblemName()).append(": ");
            if (p.isSolved()) {
                long m = p.getSolveTimeSeconds() / 60;
                long s = p.getSolveTimeSeconds() % 60;
                sb.append(String.format(Locale.getDefault(), "AC at %02d:%02d (+%d)\n", m, s, p.getWrongAttempts()));
            } else {
                sb.append("Unsolved (").append(p.getWrongAttempts()).append(" attempts)\n");
            }
        }
        sb.append("\nTracked with CodeArena - Real-Time Virtual Contest Simulator & CP Coach");

        // Implicit Intent sharing text
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "CodeArena Virtual Contest: " + currentRun.getContestName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Contest Results"));
    }
}
