package com.codearena.ui.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codearena.data.model.CfProblem;
import com.codearena.databinding.ItemLiveProblemBinding;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LiveProblemAdapter extends RecyclerView.Adapter<LiveProblemAdapter.ViewHolder> {

    public interface OnProblemFocusListener {
        void onProblemFocus(CfProblem problem);
    }

    private final List<CfProblem> problems = new ArrayList<>();
    private final Set<String> solvedIndices = new HashSet<>();
    private String currentFocusIndex = "";
    private final OnProblemFocusListener focusListener;

    public LiveProblemAdapter(OnProblemFocusListener focusListener) {
        this.focusListener = focusListener;
    }

    public void setProblems(List<CfProblem> newProblems) {
        problems.clear();
        if (newProblems != null) {
            problems.addAll(newProblems);
            if (!problems.isEmpty() && currentFocusIndex.isEmpty()) {
                currentFocusIndex = problems.get(0).getIndex();
            }
        }
        notifyDataSetChanged();
    }

    public void markSolved(String index) {
        solvedIndices.add(index);
        notifyDataSetChanged();
    }

    public int getSolvedCount() {
        return solvedIndices.size();
    }

    public List<CfProblem> getUnsolvedProblems() {
        List<CfProblem> unsolved = new ArrayList<>();
        for (CfProblem p : problems) {
            if (!solvedIndices.contains(p.getIndex())) {
                unsolved.add(p);
            }
        }
        return unsolved;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLiveProblemBinding binding = ItemLiveProblemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(problems.get(position));
    }

    @Override
    public int getItemCount() {
        return problems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLiveProblemBinding binding;

        ViewHolder(ItemLiveProblemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CfProblem problem) {
            binding.tvLiveIndex.setText(problem.getIndex());
            binding.tvLiveName.setText(problem.getName());

            boolean isSolved = solvedIndices.contains(problem.getIndex());
            boolean isFocused = problem.getIndex().equals(currentFocusIndex);

            if (isSolved) {
                binding.tvLiveStatus.setText("Accepted (AC)");
                binding.tvLiveStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if (isFocused) {
                binding.tvLiveStatus.setText("Currently focused");
                binding.tvLiveStatus.setTextColor(android.graphics.Color.parseColor("#4A90E2"));
            } else {
                binding.tvLiveStatus.setText("Not attempted");
                binding.tvLiveStatus.setTextColor(android.graphics.Color.parseColor("#A0A0A0"));
            }

            binding.btnFocus.setText(isFocused ? "Focused" : "Focus");
            binding.btnFocus.setEnabled(!isFocused);

            binding.btnFocus.setOnClickListener(v -> {
                currentFocusIndex = problem.getIndex();
                notifyDataSetChanged();
                if (focusListener != null) focusListener.onProblemFocus(problem);
            });

            binding.btnOpen.setOnClickListener(v -> {
                String url = "https://codeforces.com/contest/" + problem.getContestId() + "/problem/" + problem.getIndex();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                v.getContext().startActivity(intent);
            });
        }
    }
}

