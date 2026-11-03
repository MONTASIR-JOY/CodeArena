package com.kuet.codearena.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.kuet.codearena.R;
import com.kuet.codearena.data.db.entity.ContestEntity;
import com.kuet.codearena.data.db.entity.SolveEntity;
import com.kuet.codearena.data.db.entity.UpsolveItemEntity;
import com.kuet.codearena.data.remote.model.Platform;
import com.kuet.codearena.data.repository.CalendarRepository;
import com.kuet.codearena.databinding.FragmentCalendarBinding;
import com.kuet.codearena.ui.adapter.CalendarDayAdapter;
import com.kuet.codearena.ui.adapter.UpcomingContestAdapter;
import com.kuet.codearena.ui.model.CalendarDayModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fragment displaying multi-platform upcoming contest calendars, reminder scheduling, and day agendas.
 */
public class CalendarFragment extends Fragment implements
        CalendarDayAdapter.OnDayClickListener,
        UpcomingContestAdapter.OnUpcomingContestActionListener {

    private FragmentCalendarBinding binding;
    private CalendarRepository repository;

    private CalendarDayAdapter dayAdapter;
    private UpcomingContestAdapter contestAdapter;

    private final Calendar displayedMonth = Calendar.getInstance();
    private String selectedPlatform = "ALL";
    private String selectedDateKey = null;

    private List<ContestEntity> allUpcomingContests = new ArrayList<>();
    private final Set<String> contestDates = new HashSet<>();
    private final Set<String> upsolveDates = new HashSet<>();
    private final Set<String> solveDates = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = CalendarRepository.getInstance(requireContext());

        setupRecyclerViews();
        setupFiltersAndNavigation();
        observeCalendarData();
        refreshRemoteContests();
    }

    private void setupRecyclerViews() {
        // 7-column month grid
        dayAdapter = new CalendarDayAdapter(this);
        binding.rvCalendarGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendarGrid.setAdapter(dayAdapter);

        // Upcoming contests list
        contestAdapter = new UpcomingContestAdapter(this);
        binding.rvUpcomingContests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingContests.setAdapter(contestAdapter);
    }

    private void setupFiltersAndNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> {
            displayedMonth.add(Calendar.MONTH, -1);
            renderCalendarMonth();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            displayedMonth.add(Calendar.MONTH, 1);
            renderCalendarMonth();
        });

        binding.chipGroupPlatform.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPlatformCf) {
                selectedPlatform = Platform.CODEFORCES.name();
            } else if (checkedId == R.id.chipPlatformAtCoder) {
                selectedPlatform = Platform.ATCODER.name();
            } else if (checkedId == R.id.chipPlatformLeetCode) {
                selectedPlatform = Platform.LEETCODE.name();
            } else if (checkedId == R.id.chipPlatformCodeChef) {
                selectedPlatform = Platform.CODECHEF.name();
            } else {
                selectedPlatform = "ALL";
            }
            filterContestList();
        });
    }

    private void observeCalendarData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);

        repository.getUpcomingContests().observe(getViewLifecycleOwner(), contests -> {
            if (contests != null) {
                allUpcomingContests = contests;
                contestDates.clear();
                for (ContestEntity c : contests) {
                    contestDates.add(sdf.format(new Date(c.getStartTimeSeconds() * 1000L)));
                }
                renderCalendarMonth();
                filterContestList();
            }
        });

        repository.getAllUpsolvesLive().observe(getViewLifecycleOwner(), upsolves -> {
            if (upsolves != null) {
                upsolveDates.clear();
                for (UpsolveItemEntity u : upsolves) {
                    if (!u.isSolved()) {
                        upsolveDates.add(sdf.format(new Date(u.getDueDateMs())));
                    }
                }
                renderCalendarMonth();
            }
        });

        repository.getAllSolvesLive().observe(getViewLifecycleOwner(), solves -> {
            if (solves != null) {
                solveDates.clear();
                for (SolveEntity s : solves) {
                    solveDates.add(sdf.format(new Date(s.getSolvedAt())));
                }
                renderCalendarMonth();
            }
        });
    }

    private void renderCalendarMonth() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.tvCurrentMonthYear.setText(monthFormat.format(displayedMonth.getTime()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Calendar cal = (Calendar) displayedMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 2 = Monday...
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<CalendarDayModel> dayModels = new ArrayList<>();

        // Leading padding days from previous month
        cal.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1));
        for (int i = 1; i < firstDayOfWeek; i++) {
            String key = sdf.format(cal.getTime());
            CalendarDayModel model = new CalendarDayModel(cal.get(Calendar.DAY_OF_MONTH), false, key);
            model.setHasContest(contestDates.contains(key));
            model.setHasUpsolve(upsolveDates.contains(key));
            model.setHasSolve(solveDates.contains(key));
            model.setSelected(key.equals(selectedDateKey));
            dayModels.add(model);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Days of current month
        cal.setTime(displayedMonth.getTime());
        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            String key = sdf.format(cal.getTime());
            CalendarDayModel model = new CalendarDayModel(d, true, key);
            model.setHasContest(contestDates.contains(key));
            model.setHasUpsolve(upsolveDates.contains(key));
            model.setHasSolve(solveDates.contains(key));
            model.setSelected(key.equals(selectedDateKey));
            dayModels.add(model);
        }

        // Trailing padding to fill complete grid of 35 or 42 cells
        int remaining = 7 - (dayModels.size() % 7);
        if (remaining < 7) {
            cal.set(Calendar.DAY_OF_MONTH, daysInMonth);
            for (int i = 1; i <= remaining; i++) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                String key = sdf.format(cal.getTime());
                CalendarDayModel model = new CalendarDayModel(cal.get(Calendar.DAY_OF_MONTH), false, key);
                model.setHasContest(contestDates.contains(key));
                model.setHasUpsolve(upsolveDates.contains(key));
                model.setHasSolve(solveDates.contains(key));
                model.setSelected(key.equals(selectedDateKey));
                dayModels.add(model);
            }
        }

        dayAdapter.setDays(dayModels);
    }

    private void filterContestList() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        List<ContestEntity> filtered = new ArrayList<>();

        for (ContestEntity c : allUpcomingContests) {
            boolean matchesPlatform = "ALL".equals(selectedPlatform) || selectedPlatform.equalsIgnoreCase(c.getPlatform());
            boolean matchesDate = selectedDateKey == null || selectedDateKey.equals(sdf.format(new Date(c.getStartTimeSeconds() * 1000L)));

            if (matchesPlatform && matchesDate) {
                filtered.add(c);
            }
        }

        contestAdapter.submitList(filtered);
        binding.tvEmptyCalendar.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvUpcomingContests.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);

        if (selectedDateKey != null) {
            binding.tvAgendaHeader.setText("Contests on Selected Day (" + filtered.size() + ")");
        } else {
            binding.tvAgendaHeader.setText("Upcoming Contests (" + filtered.size() + ")");
        }
    }

    private void refreshRemoteContests() {
        binding.progressCalendar.setVisibility(View.VISIBLE);
        repository.refreshUpcomingContests(new CalendarRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    binding.progressCalendar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    binding.progressCalendar.setVisibility(View.GONE);
                    Snackbar.make(binding.calendarCoordinator, message, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> refreshRemoteContests())
                            .show();
                }
            }
        });
    }

    @Override
    public void onDayClick(CalendarDayModel day) {
        if (day.getDateKey().equals(selectedDateKey)) {
            selectedDateKey = null; // Toggle off selection
        } else {
            selectedDateKey = day.getDateKey();
        }
        renderCalendarMonth();
        filterContestList();
    }

    @Override
    public void onContestClick(ContestEntity contest) {
        if (contest.getUrl() != null && !contest.getUrl().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contest.getUrl()));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onAddToCalendarClick(ContestEntity contest) {
        // Implicit Intent inserting into device calendar via CalendarContract.Events.CONTENT_URI (no permissions needed)
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, contest.getName());
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, contest.getStartTimeSeconds() * 1000L);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, (contest.getStartTimeSeconds() + contest.getDurationSeconds()) * 1000L);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Competitive Programming contest via CodeArena: " + contest.getUrl());
        startActivity(intent);
    }

    @Override
    public void onSetReminderClick(ContestEntity contest) {
        String[] options = {"10 minutes before", "1 hour before", "1 day before"};
        final int[] leadMinutes = {10, 60, 1440};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Set Contest Alarm")
                .setItems(options, (dialog, which) -> {
                    int chosenLead = leadMinutes[which];
                    repository.setReminder(contest, chosenLead);
                    Snackbar.make(binding.calendarCoordinator,
                            "Alarm set for " + options[which] + "!", Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
