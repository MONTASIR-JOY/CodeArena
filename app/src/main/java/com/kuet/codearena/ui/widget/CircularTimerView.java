package com.kuet.codearena.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.kuet.codearena.R;

/**
 * Custom circular progress ring view rendering timer countdowns and daily target completion.
 */
public class CircularTimerView extends View {

    private int ringColor = Color.parseColor("#E2E8F0");
    private int progressColor = Color.parseColor("#2563EB");
    private float strokeWidth = 24f;
    private float progress = 0f;
    private float maxProgress = 100f;
    private String centerText = "";
    private String centerSubtext = "";
    private int textColor = Color.parseColor("#0F172A");
    private float textSize = 48f;

    private Paint ringPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint subtextPaint;
    private final RectF ovalRect = new RectF();

    public CircularTimerView(Context context) {
        super(context);
        init(null);
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularTimerView);
            ringColor = a.getColor(R.styleable.CircularTimerView_ringColor, ringColor);
            progressColor = a.getColor(R.styleable.CircularTimerView_progressColor, progressColor);
            strokeWidth = a.getDimension(R.styleable.CircularTimerView_strokeWidth, strokeWidth);
            progress = a.getFloat(R.styleable.CircularTimerView_progress, progress);
            maxProgress = a.getFloat(R.styleable.CircularTimerView_maxProgress, maxProgress);
            centerText = a.getString(R.styleable.CircularTimerView_centerText);
            if (centerText == null) centerText = "";
            centerSubtext = a.getString(R.styleable.CircularTimerView_centerSubtext);
            if (centerSubtext == null) centerSubtext = "";
            textColor = a.getColor(R.styleable.CircularTimerView_textColor, textColor);
            textSize = a.getDimension(R.styleable.CircularTimerView_textSize, textSize);
            a.recycle();
        }

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(strokeWidth);
        ringPaint.setColor(ringColor);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtextPaint.setColor(Color.parseColor("#64748B"));
        subtextPaint.setTextSize(textSize * 0.45f);
        subtextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = strokeWidth / 2f + 8f;
        ovalRect.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background ring
        canvas.drawOval(ovalRect, ringPaint);

        // Draw progress arc (-90 is 12 o'clock)
        float sweepAngle = maxProgress > 0 ? (progress / maxProgress) * 360f : 0f;
        canvas.drawArc(ovalRect, -90, sweepAngle, false, progressPaint);

        // Draw center text
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        if (centerSubtext.isEmpty()) {
            float textOffset = (textPaint.descent() + textPaint.ascent()) / 2f;
            canvas.drawText(centerText, centerX, centerY - textOffset, textPaint);
        } else {
            float textOffset = (textPaint.descent() + textPaint.ascent()) / 2f;
            canvas.drawText(centerText, centerX, centerY - textOffset - 12f, textPaint);
            canvas.drawText(centerSubtext, centerX, centerY - textOffset + textSize * 0.5f, subtextPaint);
        }
    }

    public void setProgress(float progress) {
        this.progress = Math.min(progress, maxProgress);
        invalidate();
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }

    public void setCenterText(String text) {
        this.centerText = text != null ? text : "";
        invalidate();
    }

    public void setCenterSubtext(String subtext) {
        this.centerSubtext = subtext != null ? subtext : "";
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    public void setRingColor(int color) {
        this.ringColor = color;
        ringPaint.setColor(color);
        invalidate();
    }

    public void setCenterTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
    }
}
