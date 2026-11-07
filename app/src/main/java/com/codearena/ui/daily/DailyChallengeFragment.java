package com.codearena.ui.daily;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codearena.data.model.AchievementBadge;
import com.codearena.data.model.CfProblem;
import com.codearena.data.model.CfProblemsetResult;
import com.codearena.data.model.CfSubmission;
import com.codearena.data.model.DailyChallengeState;
import com.codearena.data.model.DailyProblemItem;
import com.codearena.data.repository.CodeforcesRepository;
import com.codearena.data.repository.UserPreferencesRepository;
import com.codearena.databinding.FragmentDailyChallengeBinding;
import com.codearena.ui.adapter.AchievementBadgeAdapter;
import com.codearena.ui.adapter.DailyProblemAdapter;
import com.codearena.ui.adapter.StreakDayAdapter;
import com.codearena.util.DailyChallengePlanner;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DailyChallengeFragment extends Fragment {

    private FragmentDailyChallengeBinding binding;
    private CodeforcesRepository cfRepo;
    private UserPreferencesRepository prefsRepo;

    private DailyProblemAdapter problemAdapter;
    private StreakDayAdapter streakDayAdapter;
    private AchievementBadgeAdapter badgeAdapter;

    private DailyChallengeState state;
    private List<CfProblem> cachedProblemset = new ArrayList<>();
    private final Set<String> userSolvedKeys = new HashSet<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDailyChallengeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cfRepo = new CodeforcesRepository(requireContext());
        prefsRepo = new UserPreferencesRepository(requireContext());

        setupRecyclerViews();
        setupControls();
        loadSavedState();
        fetchUserSolvedSubmissions();
        fetchProblemsetAndPopulate();
    }

    private void setupRecyclerViews() {
        // 1. Daily Problems list
        problemAdapter = new DailyProblemAdapter();
        binding.rvDailyProblems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDailyProblems.setAdapter(problemAdapter);

        // 2. 5-Week Streak Grid (7 columns for 7 days per week)
        streakDayAdapter = new StreakDayAdapter();
        binding.rvStreakGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvStreakGrid.setAdapter(streakDayAdapter);

        // 3. Achievements Badges (2 columns)
        badgeAdapter = new AchievementBadgeAdapter();
        binding.rvBadges.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvBadges.setAdapter(badgeAdapter);
    }

    private void setupControls() {
        int currentSize = prefsRepo.getDailySize();
        binding.sliderDailySize.setValue(Math.max(3, Math.min(10, currentSize)));
        binding.tvDailySize.setText("Problems per day: " + currentSize);

        binding.sliderDailySize.addOnChangeListener((slider, value, fromUser) -> {
            int newSize = (int) value;
            binding.tvDailySize.setText("Problems per day: " + newSize);
            prefsRepo.saveDailySize(newSize);

            // If user hasn't solved any problems yet today, resize challenge immediately
            if (state != null && state.getSolvedCount() == 0 && !cachedProblemset.isEmpty()) {
                generateDailyProblems(newSize, false);
                renderAll();
            }
        });

        binding.btnCheckSolved.setOnClickListener(v -> checkSubmissions());
        binding.btnSwap.setOnClickListener(v -> swapProblems());
    }

    private void loadSavedState() {
        userSolvedKeys.clear();
        userSolvedKeys.addAll(prefsRepo.getUserSolvedProblemKeys());

        String json = prefsRepo.getDailyData();
        if (json != null && !json.trim().isEmpty()) {
            try {
                state = new Gson().fromJson(json, DailyChallengeState.class);
            } catch (Exception ignored) {}
        }

        if (state == null) {
            state = new DailyChallengeState();
        }

        String today = dateFormat.format(new Date());
        // Handle day change
        if (!today.equals(state.getDate())) {
            if (!state.getDate().isEmpty() && state.getProblems() != null && !state.getProblems().isEmpty()) {
                String oldDate = state.getDate();
                if (state.isAllSolved()) {
                    state.getHistory().put(oldDate, "ALL");
                } else if (state.getSolvedCount() > 0) {
                    state.getHistory().put(oldDate, "PARTIAL");
                    state.setStreak(0);
                } else {
                    state.getHistory().put(oldDate, "MISSED");
                    state.setStreak(0);
                }
            }
            state.setDate(today);
            state.getProblems().clear();
            saveState();
        }

        renderAll();
    }

    private void fetchUserSolvedSubmissions() {
        String handle = prefsRepo.getHandle();
        if (handle == null || handle.trim().isEmpty()) return;

        cfRepo.fetchUserSubmissions(handle.trim(), 10000, new CodeforcesRepository.Callback<List<CfSubmission>>() {
            @Override
            public void onSuccess(List<CfSubmission> submissions) {
                if (submissions == null || !isAdded()) return;
                Set<String> freshSolved = new HashSet<>();
                for (CfSubmission sub : submissions) {
                    if ("OK".equalsIgnoreCase(sub.getVerdict()) && sub.getProblem() != null) {
                        Long cId = sub.getProblem().getContestId() != null ? sub.getProblem().getContestId() : sub.getContestId();
                        String idx = sub.getProblem().getIndex() != null ? sub.getProblem().getIndex().trim().toUpperCase() : "";
                        if (cId != null && !idx.isEmpty()) {
                            freshSolved.add(cId + idx);
                            freshSolved.add(cId + "_" + idx);
                        }
                    }
                }
                prefsRepo.saveUserSolvedProblemKeys(freshSolved);
                userSolvedKeys.clear();
                userSolvedKeys.addAll(freshSolved);

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null || state == null || state.getProblems() == null) return;
                    // Check if any currently displayed problem was already solved prior to today
                    boolean replacedAny = false;
                    for (DailyProblemItem item : state.getProblems()) {
                        if (!item.isSolved()) {
                            String k = item.getKey().toUpperCase();
                            if (userSolvedKeys.contains(k) || userSolvedKeys.contains(k.replace("_", ""))) {
                                replacedAny = true;
                                break;
                            }
                        }
                    }
                    if (replacedAny && cachedProblemset != null && !cachedProblemset.isEmpty()) {
                        generateDailyProblems(prefsRepo.getDailySize(), true);
                        renderAll();
                    }
                });
            }

            @Override
            public void onError(Exception error) {}
        });
    }

    private void fetchProblemsetAndPopulate() {
        binding.progressDailyLoading.setVisibility(View.VISIBLE);
        binding.tvDailyError.setVisibility(View.GONE);

        cfRepo.fetchProblemset(new CodeforcesRepository.Callback<CfProblemsetResult>() {
            @Override
            public void onSuccess(CfProblemsetResult result) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressDailyLoading.setVisibility(View.GONE);

                    if (result != null && result.getProblems() != null) {
                        cachedProblemset = result.getProblems();
                        // If no problems for today, generate now
                        if (state.getProblems().isEmpty()) {
                            generateDailyProblems(prefsRepo.getDailySize(), false);
                        }
                        renderAll();
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressDailyLoading.setVisibility(View.GONE);
                    if (state.getProblems().isEmpty()) {
                        binding.tvDailyError.setVisibility(View.VISIBLE);
                        binding.tvDailyError.setText("Failed to load daily problems: " +
                                (error != null ? error.getMessage() : "Network error"));
                    }
                });
            }
        });
    }

    private void generateDailyProblems(int count, boolean isSwap) {
        if (cachedProblemset == null || cachedProblemset.isEmpty()) return;

        int userRating = prefsRepo.getUserRating();
        List<Integer> ladderRungs = DailyChallengePlanner.ladder(count, userRating);

        // Keep already solved problems if swapping
        List<DailyProblemItem> existingSolved = new ArrayList<>();
        Set<String> chosenKeys = new HashSet<>();
        if (isSwap && state.getProblems() != null) {
            for (DailyProblemItem item : state.getProblems()) {
                if (item.isSolved()) {
                    existingSolved.add(item);
                    chosenKeys.add(item.getKey());
                }
            }
        }

        long seed = dateFormat.format(new Date()).hashCode() + state.getSwapSeed() * 37L;
        Random random = new Random(seed);

        // Filter usable problems (rating >= 800 and NOT solved by the user on Codeforces)
        List<CfProblem> usable = new ArrayList<>();
        for (CfProblem p : cachedProblemset) {
            if (p.getContestId() != null && p.getRating() != null && p.getRating() >= 800) {
                String k1 = p.getContestId() + (p.getIndex() != null ? p.getIndex().trim().toUpperCase() : "");
                String k2 = p.getContestId() + "_" + (p.getIndex() != null ? p.getIndex().trim().toUpperCase() : "");
                if (!userSolvedKeys.contains(k1) && !userSolvedKeys.contains(k2)) {
                    usable.add(p);
                }
            }
        }
        if (usable.isEmpty()) {
            usable.addAll(cachedProblemset);
        }

        List<DailyProblemItem> newItems = new ArrayList<>(existingSolved);
        int needed = count - newItems.size();
        List<Integer> tolerances = Arrays.asList(0, 100, 200, 300);

        for (int i = newItems.size(); i < count; i++) {
            int targetRung = ladderRungs.get(Math.min(i, ladderRungs.size() - 1));
            CfProblem picked = null;

            for (int tolerance : tolerances) {
                List<CfProblem> candidates = new ArrayList<>();
                for (CfProblem p : usable) {
                    if (Math.abs(p.getRating() - targetRung) <= tolerance) {
                        String key = (p.getContestId() != null ? p.getContestId() : 0) +
                                (p.getIndex() != null ? p.getIndex() : "");
                        if (!chosenKeys.contains(key)) {
                            candidates.add(p);
                        }
                    }
                }
                if (!candidates.isEmpty()) {
                    picked = candidates.get(random.nextInt(candidates.size()));
                    break;
                }
            }

            if (picked != null) {
                String key = (picked.getContestId() != null ? picked.getContestId() : 0) +
                        (picked.getIndex() != null ? picked.getIndex() : "");
                chosenKeys.add(key);
                newItems.add(new DailyProblemItem(picked));
            }
        }

        // Sort problems by rating ascending
        newItems.sort((a, b) -> {
            int rA = (a.getProblem() != null && a.getProblem().getRating() != null) ? a.getProblem().getRating() : 0;
            int rB = (b.getProblem() != null && b.getProblem().getRating() != null) ? b.getProblem().getRating() : 0;
            return Integer.compare(rA, rB);
        });

        state.setProblems(newItems);
        saveState();
    }

    private void swapProblems() {
        if (cachedProblemset == null || cachedProblemset.isEmpty()) {
            Toast.makeText(requireContext(), "Problemset still loading, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        state.setSwapSeed(state.getSwapSeed() + 1);
        int size = prefsRepo.getDailySize();
        int solvedBefore = state.getSolvedCount();

        generateDailyProblems(size, true);
        renderAll();

        if (solvedBefore > 0) {
            Toast.makeText(requireContext(), "Kept your " + solvedBefore + " solved problem(s) and swapped the rest!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Swapped today's challenge with new problems!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSubmissions() {
        String handle = prefsRepo.getHandle();
        if (handle == null || handle.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please set your Codeforces handle in Settings first!", Toast.LENGTH_LONG).show();
            return;
        }

        binding.progressDailyLoading.setVisibility(View.VISIBLE);
        binding.tvDailyError.setVisibility(View.GONE);

        cfRepo.fetchUserSubmissions(handle.trim(), 100, new CodeforcesRepository.Callback<List<CfSubmission>>() {
            @Override
            public void onSuccess(List<CfSubmission> submissions) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressDailyLoading.setVisibility(View.GONE);

                    Set<String> solvedKeys = new HashSet<>();
                    if (submissions != null) {
                        for (CfSubmission sub : submissions) {
                            if ("OK".equalsIgnoreCase(sub.getVerdict()) && sub.getProblem() != null) {
                                String key = (sub.getProblem().getContestId() != null ? sub.getProblem().getContestId() : 0) +
                                        (sub.getProblem().getIndex() != null ? sub.getProblem().getIndex() : "");
                                solvedKeys.add(key);
                            }
                        }
                    }

                    int newlySolved = 0;
                    for (DailyProblemItem item : state.getProblems()) {
                        if (!item.isSolved() && solvedKeys.contains(item.getKey())) {
                            item.setSolved(true);
                            item.setSolvedAtSeconds(System.currentTimeMillis() / 1000);
                            newlySolved++;
                        }
                    }

                    String today = dateFormat.format(new Date());
                    int totalSolved = state.getSolvedCount();
                    int totalProblems = state.getProblems().size();

                    if (state.isAllSolved()) {
                        if (!"ALL".equals(state.getHistory().get(today))) {
                            state.setStreak(state.getStreak() + 1);
                            state.setLongestStreak(Math.max(state.getLongestStreak(), state.getStreak()));
                            state.setTotalCompleted(state.getTotalCompleted() + 1);
                            state.getHistory().put(today, "ALL");
                            Toast.makeText(requireContext(), "🎉 ALL " + totalProblems + " PROBLEMS SOLVED! Streak: " + state.getStreak() + " days!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "All problems for today are already completed! Outstanding!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (totalSolved > 0) {
                        state.getHistory().put(today, "PARTIAL");
                        if (newlySolved > 0) {
                            Toast.makeText(requireContext(), "Great job! " + totalSolved + " of " + totalProblems + " solved.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), totalSolved + " of " + totalProblems + " solved. Keep going!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "No new AC submissions found yet for today's problems.", Toast.LENGTH_SHORT).show();
                    }

                    saveState();
                    renderAll();
                });
            }

            @Override
            public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || binding == null) return;
                    binding.progressDailyLoading.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error checking submissions: " +
                            (error != null ? error.getMessage() : "Network error"), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void renderAll() {
        if (binding == null || state == null) return;

        // 1. Header Subtitle
        int userRating = prefsRepo.getUserRating();
        int minRating = 1000;
        int maxRating = 1400;
        if (state.getProblems() != null && !state.getProblems().isEmpty()) {
            minRating = Integer.MAX_VALUE;
            maxRating = Integer.MIN_VALUE;
            for (DailyProblemItem item : state.getProblems()) {
                if (item.getProblem() != null && item.getProblem().getRating() != null) {
                    int r = item.getProblem().getRating();
                    if (r < minRating) minRating = r;
                    if (r > maxRating) maxRating = r;
                }
            }
            if (minRating == Integer.MAX_VALUE) minRating = 1000;
            if (maxRating == Integer.MIN_VALUE) maxRating = 1400;
        }
        binding.tvDailySubtitle.setText("Ladder from " + minRating + " to " + maxRating + ", matched to your rating (" + userRating + ")");

        // 2. Streak & Stats Card
        binding.tvStreakCount.setText(state.getStreak() + " day streak");
        binding.tvStreakSub.setText("Longest " + state.getLongestStreak() + " days · " + state.getTotalCompleted() + " challenges completed");

        int totalCount = state.getProblems().size();
        int solvedCount = state.getSolvedCount();
        binding.tvTodayProgress.setText("Today: " + solvedCount + " / " + totalCount + " solved");

        int progressPercent = (totalCount > 0) ? (int) Math.round(((double) solvedCount / totalCount) * 100) : 0;
        binding.progressDaily.setProgress(progressPercent);

        // 3. Problem list
        problemAdapter.setItems(state.getProblems());

        // 4. 5-Week Streak Grid (35 days ending today)
        renderStreakGrid();

        // 5. Achievements / Badges
        renderAchievements();
    }

    private void renderStreakGrid() {
        Calendar cal = Calendar.getInstance();
        List<StreakDayAdapter.DayData> gridDays = new ArrayList<>();
        String today = dateFormat.format(cal.getTime());

        for (int i = 34; i >= 0; i--) {
            Calendar dayCal = (Calendar) cal.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, -i);
            String dStr = dateFormat.format(dayCal.getTime());
            String dayNum = String.valueOf(dayCal.get(Calendar.DAY_OF_MONTH));
            boolean isToday = (i == 0);

            String status = state.getHistory().get(dStr);
            if (status == null && isToday) {
                if (state.isAllSolved()) {
                    status = "ALL";
                } else if (state.getSolvedCount() > 0) {
                    status = "PARTIAL";
                } else {
                    status = "IN_PROGRESS";
                }
            }

            gridDays.add(new StreakDayAdapter.DayData(dStr, dayNum, status, isToday));
        }

        streakDayAdapter.setDays(gridDays);
    }

    private void renderAchievements() {
        List<AchievementBadge> badges = new ArrayList<>();

        int streak = state.getStreak();
        int longest = state.getLongestStreak();
        int totalCompleted = state.getTotalCompleted();

        // 1. First Step
        badges.add(new AchievementBadge(
                "first_step",
                "First Step",
                "Complete your first daily challenge",
                Math.min(1, totalCompleted),
                1,
                totalCompleted >= 1
        ));

        // 2. On Fire
        int fireProgress = Math.max(streak, longest);
        badges.add(new AchievementBadge(
                "on_fire",
                "On Fire",
                "Achieve a 3-day challenge streak",
                Math.min(3, fireProgress),
                3,
                fireProgress >= 3
        ));

        // 3. Week Warrior
        badges.add(new AchievementBadge(
                "week_warrior",
                "Week Warrior",
                "Achieve a 7-day challenge streak",
                Math.min(7, fireProgress),
                7,
                fireProgress >= 7
        ));

        // 4. Monthly Master
        badges.add(new AchievementBadge(
                "monthly_master",
                "Monthly Master",
                "Achieve a 30-day challenge streak",
                Math.min(30, fireProgress),
                30,
                fireProgress >= 30
        ));

        // 5. Dedicated Solver
        badges.add(new AchievementBadge(
                "dedicated_solver",
                "Dedicated Solver",
                "Complete 10 daily challenges in total",
                Math.min(10, totalCompleted),
                10,
                totalCompleted >= 10
        ));

        // 6. Century Club
        badges.add(new AchievementBadge(
                "century_club",
                "Century Club",
                "Complete 50 daily challenges in total",
                Math.min(50, totalCompleted),
                50,
                totalCompleted >= 50
        ));

        // 7. Heavy Lifter
        boolean hasHeavy = false;
        if (state.getProblems() != null) {
            for (DailyProblemItem p : state.getProblems()) {
                if (p.isSolved() && p.getProblem() != null &&
                        p.getProblem().getRating() != null && p.getProblem().getRating() >= 1600) {
                    hasHeavy = true;
                    break;
                }
            }
        }
        badges.add(new AchievementBadge(
                "heavy_lifter",
                "Heavy Lifter",
                "Solve a 1600+ rated daily problem",
                hasHeavy ? 1 : 0,
                1,
                hasHeavy
        ));

        // 8. Full House
        boolean hasFullHouse = state.isAllSolved() && state.getProblems() != null && state.getProblems().size() >= 10;
        badges.add(new AchievementBadge(
                "full_house",
                "Full House",
                "Complete a 10-problem challenge",
                hasFullHouse ? 1 : 0,
                1,
                hasFullHouse
        ));

        badgeAdapter.setBadges(badges);
    }

    private void saveState() {
        if (state != null && prefsRepo != null) {
            try {
                String json = new Gson().toJson(state);
                prefsRepo.saveDailyData(json);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
