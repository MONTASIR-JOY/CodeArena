package com.codearena.ui.widget;

import android.content.Context;
import android.widget.TextView;
import com.codearena.R;
import com.codearena.data.model.CfRatingChange;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.List;

public class TrajectoryMarkerView extends MarkerView {

    private final TextView tvRating;
    private final TextView tvContest;
    private List<CfRatingChange> history;

    public TrajectoryMarkerView(Context context, List<CfRatingChange> history) {
        super(context, R.layout.layout_chart_marker);
        this.history = history;
        tvRating = findViewById(R.id.tvMarkerRating);
        tvContest = findViewById(R.id.tvMarkerContest);
    }

    public void setHistory(List<CfRatingChange> history) {
        this.history = history;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();
        if (history != null && index >= 0 && index < history.size()) {
            CfRatingChange change = history.get(index);
            int diff = change.getNewRating() - change.getOldRating();
            String diffStr = (diff >= 0 ? "+" : "") + diff;
            tvRating.setText(change.getNewRating() + " (" + diffStr + ")");
            String cName = change.getContestName() != null ? change.getContestName() : ("Contest #" + change.getContestId());
            tvContest.setText(cName);
        } else {
            tvRating.setText(String.valueOf((int) e.getY()));
            tvContest.setText("");
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 15f);
    }
}

