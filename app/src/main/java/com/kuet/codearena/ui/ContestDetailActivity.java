package com.kuet.codearena.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.kuet.codearena.R;
import com.kuet.codearena.data.db.entity.ProblemEntity;
import com.kuet.codearena.data.remote.Endpoints;
import com.kuet.codearena.data.repository.ContestRepository;
import com.kuet.codearena.databinding.ActivityContestDetailBinding;
import com.kuet.codearena.ui.adapter.ProblemAdapter;

import java.util.Locale;

/**
 * Activity presenting details and problem list of a chosen Codeforces contest with implicit browser launching.
 */
public class ContestDetailActivity extends AppCompatActivity {
    public static final String EXTRA_CONTEST_ID = "extra_contest_id";
    public static final String EXTRA_CONTEST_NAME = "extra_contest_name";
    public static final String EXTRA_CONTEST_DURATION = "extra_contest_duration";
    public static final String EXTRA_CONTEST_DIVISION = "extra_contest_division";

    private ActivityContestDetailBinding binding;
    private ContestRepository repository;
    private ProblemAdapter adapter;

    private int contestId;
    private String contestName;
    private long durationSeconds;
    private String division;

    public static Intent newIntent(Context context, int contestId, String name, long durationSeconds, String division) {
        Intent intent = new Intent(context, ContestDetailActivity.class);
        intent.putExtra(EXTRA_CONTEST_ID, contestId);
        intent.putExtra(EXTRA_CONTEST_NAME, name);
        intent.putExtra(EXTRA_CONTEST_DURATION, durationSeconds);
        intent.putExtra(EXTRA_CONTEST_DIVISION, division);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContestDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        contestId = getIntent().getIntExtra(EXTRA_CONTEST_ID, 0);
        contestName = getIntent().getStringExtra(EXTRA_CONTEST_NAME);
        durationSeconds = getIntent().getLongExtra(EXTRA_CONTEST_DURATION, 7200);
        division = getIntent().getStringExtra(EXTRA_CONTEST_DIVISION);

        repository = ContestRepository.getInstance(this);

        setupViews();
        setupRecyclerView();
        observeProblems();
    }

    private void setupViews() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvContestTitle.setText(contestName != null ? contestName : "Contest " + contestId);
        binding.tvDetailDivision.setText(division != null ? division : "Contest");

        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        binding.tvDetailDuration.setText(String.format(Locale.getDefault(), "%d hr %d min", hours, minutes));

        binding.btnStartVirtualRun.setOnClickListener(v -> {
            Intent runIntent = new Intent(this, VirtualRunActivity.class);
            runIntent.putExtra(VirtualRunActivity.EXTRA_CONTEST_ID, contestId);
            runIntent.putExtra(VirtualRunActivity.EXTRA_CONTEST_NAME, contestName);
            runIntent.putExtra(VirtualRunActivity.EXTRA_DURATION_SECONDS, durationSeconds);
            startActivity(runIntent);
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadRemoteProblems);
        binding.btnRetry.setOnClickListener(v -> loadRemoteProblems());
    }

    private void setupRecyclerView() {
        adapter = new ProblemAdapter(problem -> {
            // Implicit Intent opening the problem in the device's web browser
            String url = Endpoints.CF_PROBLEM_WEB_BASE + contestId + "/problem/" + problem.getProblemIndex();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
        binding.rvProblems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProblems.setAdapter(adapter);
    }

    private void observeProblems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.getProblemsForContest(contestId).observe(this, problems -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (problems != null && !problems.isEmpty()) {
                adapter.submitList(problems);
                binding.emptyView.setVisibility(View.GONE);
                binding.rvProblems.setVisibility(View.VISIBLE);
            } else {
                // If not cached, trigger remote fetch
                loadRemoteProblems();
            }
        });
    }

    private void loadRemoteProblems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.refreshContestProblems(contestId, new ContestRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                if (adapter.getItemCount() == 0) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
                Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadRemoteProblems())
                        .show();
            }
        });
    }
}
