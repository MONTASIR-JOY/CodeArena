package com.codearena.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.codearena.data.db.entity.RankEstimateEntity;
import com.codearena.data.db.entity.VirtualRunEntity;
import java.io.File;
import java.io.FileOutputStream;

public class ShareCardRenderer {

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 620;

    public static Bitmap render(VirtualRunEntity run, RankEstimateEntity estimate) {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#0B0F19"));

        int accent = Color.parseColor("#00E5FF");
        int white = Color.parseColor("#F1F5F9");
        int muted = Color.parseColor("#94A3B8");

        Paint brand = createPaint(accent, 40f, true);
        Paint title = createPaint(white, 46f, true);
        Paint sub = createPaint(muted, 30f, false);
        Paint big = createPaint(white, 120f, true);
        Paint caption = createPaint(muted, 28f, false);

        Paint bar = new Paint();
        bar.setColor(accent);
        canvas.drawRect(0f, 0f, 16f, (float) HEIGHT, bar);

        canvas.drawText("CodeArena · Virtual Contest", 60f, 90f, brand);
        canvas.drawText(ellipsize(run.getContestName(), title, WIDTH - 120f), 60f, 170f, title);
        canvas.drawText(
            run.getMode() + " mode · " + TimeUtils.formatDurationHours(run.getDurationSeconds()),
            60f, 220f, sub
        );

        String solvedText = run.getSolvedCount() + "/" + run.getTotalProblemsCount();
        big.setColor(accent);
        canvas.drawText(solvedText, 60f, 400f, big);
        canvas.drawText("problems solved", 60f, 450f, caption);

        String rankText = estimate != null ? "~" + estimate.getEstimatedRank() : "—";
        big.setColor(white);
        canvas.drawText(rankText, 460f, 400f, big);
        canvas.drawText("estimated rank", 460f, 450f, caption);

        String perfText = run.getEstimatedPerformance() > 0 ? String.valueOf(run.getEstimatedPerformance()) : "—";
        big.setColor(run.getEstimatedPerformance() > 0 ? RankColors.getColorForRating(run.getEstimatedPerformance()) : white);
        canvas.drawText(perfText, 800f, 400f, big);
        canvas.drawText("performance", 800f, 450f, caption);

        canvas.drawText("Penalty " + run.getPenaltyMinutes() + " min", 60f, 540f, sub);
        return bitmap;
    }

    public static Intent shareIntent(Context context, VirtualRunEntity run, RankEstimateEntity estimate) {
        Bitmap bitmap = render(run, estimate);
        File dir = new File(context.getCacheDir(), "share_cards");
        dir.mkdirs();
        File file = new File(dir, "run_" + run.getRunId() + ".png");

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception ignored) {}

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("image/png");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.putExtra(Intent.EXTRA_TEXT, "I solved " + run.getSolvedCount() + "/" + run.getTotalProblemsCount() + " in " + run.getContestName() + " on CodeArena!");
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(send, "Share result card");
    }

    private static Paint createPaint(int color, float sizePx, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(sizePx);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return p;
    }

    private static String ellipsize(String text, Paint paint, float maxWidth) {
        if (text == null) return "";
        if (paint.measureText(text) <= maxWidth) return text;
        int end = text.length();
        while (end > 1 && paint.measureText(text, 0, end) + paint.measureText("…") > maxWidth) {
            end--;
        }
        return text.substring(0, end) + "…";
    }
}

