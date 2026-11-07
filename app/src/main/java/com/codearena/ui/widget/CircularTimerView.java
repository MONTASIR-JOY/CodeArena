package com.codearena.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.codearena.R;
import java.util.Locale;

/**
 * CircularTimerView: Custom Canvas-rendered circular timer visualization
 * for active competitive programming virtual contests.
 */
public class CircularTimerView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private Paint timePaint;
    private Paint labelPaint;

    private final RectF arcBounds = new RectF();
    private long totalSeconds = 7200;
    private long remainingSeconds = 7200;
    private boolean isPaused = false;
    private String statusText = "Running";

    private float strokeWidth = 14f;

    public CircularTimerView(Context context) {
        super(context);
        init();
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        strokeWidth = 10f * density;

        int primaryColor;
        int trackColor;
        int textPrimaryColor;
        int textMutedColor;

        try {
            primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
            trackColor = ContextCompat.getColor(getContext(), R.color.stroke_dark);
            textPrimaryColor = ContextCompat.getColor(getContext(), R.color.text_primary);
            textMutedColor = ContextCompat.getColor(getContext(), R.color.text_muted);
        } catch (Exception e) {
            primaryColor = Color.parseColor("#4F46E5");
            trackColor = Color.parseColor("#374151");
            textPrimaryColor = Color.WHITE;
            textMutedColor = Color.LTGRAY;
        }

        // Circular background track paint
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(trackColor);

        // Circular active sweep progress arc paint
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(primaryColor);

        // Center timer text paint
        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setStyle(Paint.Style.FILL);
        timePaint.setTextAlign(Paint.Align.CENTER);
        timePaint.setColor(textPrimaryColor);
        timePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        timePaint.setTextSize(32f * density);

        // Status subtext paint (e.g. "Running", "Paused")
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(textMutedColor);
        labelPaint.setTypeface(Typeface.DEFAULT);
        labelPaint.setTextSize(13f * density);
    }

    public void setTime(long remainingSeconds, long totalSeconds) {
        this.remainingSeconds = Math.max(0, remainingSeconds);
        this.totalSeconds = Math.max(1, totalSeconds);
        invalidate();
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
        this.statusText = paused ? "Paused" : "Running";
        try {
            int activeColor = paused ?
                    ContextCompat.getColor(getContext(), R.color.accent_amber) :
                    ContextCompat.getColor(getContext(), R.color.primary);
            progressPaint.setColor(activeColor);
        } catch (Exception ignored) {}
        invalidate();
    }

    public void setStatusText(String text) {
        this.statusText = text;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = strokeWidth / 2f + 8f;
        float size = Math.min(w, h);
        float left = (w - size) / 2f + padding;
        float top = (h - size) / 2f + padding;
        float right = (w + size) / 2f - padding;
        float bottom = (h + size) / 2f - padding;
        arcBounds.set(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - strokeWidth / 2f - 8f;

        if (radius <= 0) return;

        // 1. Draw circular background track ring using Canvas
        canvas.drawCircle(cx, cy, radius, trackPaint);

        // 2. Calculate remaining fraction and draw sweep progress arc
        float fraction = (float) remainingSeconds / (float) totalSeconds;
        float sweepAngle = Math.max(0f, Math.min(1f, fraction)) * 360f;
        canvas.drawArc(arcBounds, -90f, sweepAngle, false, progressPaint);

        // 3. Format time string (HH:MM:SS or MM:SS)
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        long seconds = remainingSeconds % 60;

        String timeStr;
        if (hours > 0) {
            timeStr = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeStr = String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }

        // Center the timer text vertically
        float textY = cy - ((timePaint.descent() + timePaint.ascent()) / 2f) - 10f;
        canvas.drawText(timeStr, cx, textY, timePaint);

        // Center the status text below timer
        float statusY = cy + 38f;
        canvas.drawText(statusText, cx, statusY, labelPaint);
    }
}

