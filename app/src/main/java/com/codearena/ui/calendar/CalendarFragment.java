package com.codearena.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.R;
import com.codearena.data.model.CfContest;
import com.codearena.data.repository.ContestRepository;
import com.codearena.databinding.FragmentCalendarBinding;
import com.codearena.ui.adapter.CalendarContestAdapter;
import com.codearena.ui.adapter.CalendarDayAdapter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment implements CalendarDayAdapter.OnDayClickListener {

    private FragmentCalendarBinding binding;
    private ContestRepository contestRepo;
    private CalendarDayAdapter dayAdapter;
    private CalendarContestAdapter contestAdapter;

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private String selectedPlatformFilter = null;
    private final List<CfContest> allContests = new ArrayList<>();

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
        contestRepo = new ContestRepository(requireContext());

        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();

        setupMonthNavigation();
        setupFilterChips();
        setupCalendarGrid();
        setupSelectedDayList();
        setupSwipeRefresh();

        // 1. Instant display from local cache
        List<CfContest> cached = contestRepo.getCachedContests();
        if (!cached.isEmpty()) {
            allContests.clear();
            allContests.addAll(cached);
            renderCalendar();
        }

        // 2. Fetch fresh multi-platform contests
        loadContests();
    }

    private void setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            renderCalendar();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            renderCalendar();
        });

        binding.btnToday.setOnClickListener(v -> {
            currentMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            renderCalendar();
        });
    }

    private void setupFilterChips() {
        binding.chipGroupCalendar.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = binding.chipGroupCalendar.getCheckedChipId();
            if (checkedId == R.id.chipCalCF) {
                selectedPlatformFilter = "codeforces";
            } else if (checkedId == R.id.chipCalLC) {
                selectedPlatformFilter = "leetcode";
            } else if (checkedId == R.id.chipCalCC) {
                selectedPlatformFilter = "codechef";
            } else if (checkedId == R.id.chipCalAC) {
                selectedPlatformFilter = "atcoder";
            } else if (checkedId == R.id.chipCalHR) {
                selectedPlatformFilter = "hackerrank";
            } else {
                selectedPlatformFilter = null;
            }
            renderCalendar();
        });
    }

    private void setupCalendarGrid() {
        dayAdapter = new CalendarDayAdapter(this);
        binding.rvCalendarGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendarGrid.setAdapter(dayAdapter);
    }

    private void setupSelectedDayList() {
        contestAdapter = new CalendarContestAdapter();
        binding.rvDayContests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDayContests.setAdapter(contestAdapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshCalendar.setColorSchemeResources(R.color.primary);
        binding.swipeRefreshCalendar.setOnRefreshListener(this::loadContests);
    }

    private void loadContests() {
        binding.swipeRefreshCalendar.setRefreshing(true);

        contestRepo.fetchUpcomingContests(new ContestRepository.Callback<List<CfContest>>() {
            @Override
            public void onSuccess(List<CfContest> result) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.swipeRefreshCalendar.setRefreshing(false);
                    allContests.clear();
                    if (result != null) {
                        allContests.addAll(result);
                    }
                    renderCalendar();
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.swipeRefreshCalendar.setRefreshing(false);
                    renderCalendar();
                });
            }
        });
    }

    private void renderCalendar() {
        if (binding == null) return;

        // Month & Year Title
        DateTimeFormatter monthYearFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        binding.tvMonthYear.setText(currentMonth.format(monthYearFmt));

        // Build grid of days
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek1 = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        LocalDate gridStart = firstOfMonth.minusDays(dayOfWeek1 - 1);

        int totalDays = 35;
        // If the month needs 6 rows (e.g. starts on Sun and has 31 days)
        if (firstOfMonth.lengthOfMonth() + (dayOfWeek1 - 1) > 35) {
            totalDays = 42;
        }

        List<CalendarDayAdapter.DayItem> dayItems = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < totalDays; i++) {
            LocalDate date = gridStart.plusDays(i);
            boolean isCurMonth = (date.getMonth() == currentMonth.getMonth() && date.getYear() == currentMonth.getYear());
            boolean isToday = date.equals(today);
            boolean isSelected = date.equals(selectedDate);

            CalendarDayAdapter.DayItem item = new CalendarDayAdapter.DayItem(date, isCurMonth, isToday, isSelected);

            // Populate contests on this date
            for (CfContest c : allContests) {
                if (c.getStartTimeSeconds() != null) {
                    LocalDate cDate = Instant.ofEpochSecond(c.getStartTimeSeconds())
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    if (cDate.equals(date)) {
                        if (selectedPlatformFilter == null) {
                            item.contests.add(c);
                        } else {
                            String type = c.getType() != null ? c.getType().toLowerCase() : "";
                            if (type.contains(selectedPlatformFilter)) {
                                item.contests.add(c);
                            }
                        }
                    }
                }
            }

            dayItems.add(item);
        }

        dayAdapter.setDays(dayItems);

        // Update selected day view
        renderSelectedDayContests();
    }

    @Override
    public void onDayClick(CalendarDayAdapter.DayItem dayItem) {
        selectedDate = dayItem.date;
        renderCalendar();
    }

    private void renderSelectedDayContests() {
        if (binding == null || selectedDate == null) return;

        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEEE, MMM d");
        binding.tvSelectedDayTitle.setText("Contests on " + selectedDate.format(dayFmt));

        List<CfContest> dayContests = new ArrayList<>();
        for (CfContest c : allContests) {
            if (c.getStartTimeSeconds() != null) {
                LocalDate cDate = Instant.ofEpochSecond(c.getStartTimeSeconds())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                if (cDate.equals(selectedDate)) {
                    if (selectedPlatformFilter == null) {
                        dayContests.add(c);
                    } else {
                        String type = c.getType() != null ? c.getType().toLowerCase() : "";
                        if (type.contains(selectedPlatformFilter)) {
                            dayContests.add(c);
                        }
                    }
                }
            }
        }

        if (dayContests.isEmpty()) {
            binding.tvSelectedDaySubtitle.setText("No contests scheduled on this day");
            binding.tvDayEmptyState.setVisibility(View.VISIBLE);
            binding.rvDayContests.setVisibility(View.GONE);
        } else {
            binding.tvSelectedDaySubtitle.setText(dayContests.size() +
                    (dayContests.size() == 1 ? " contest scheduled" : " contests scheduled"));
            binding.tvDayEmptyState.setVisibility(View.GONE);
            binding.rvDayContests.setVisibility(View.VISIBLE);
            contestAdapter.setContests(dayContests);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

