package com.codearena.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codearena.data.model.CfContest;
import com.codearena.data.repository.ContestRepository;
import com.codearena.data.repository.UserPreferencesRepository;

import java.util.List;

/** Restores saved contest reminders after device boot or an app update. */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !"android.intent.action.QUICKBOOT_POWERON".equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        UserPreferencesRepository prefs = new UserPreferencesRepository(context);
        List<CfContest> contests = new ContestRepository(context).getCachedContests();
        long nowSeconds = System.currentTimeMillis() / 1000L;
        for (CfContest contest : contests) {
            if (contest == null || contest.getStartTimeSeconds() == null
                    || contest.getStartTimeSeconds() <= nowSeconds
                    || !prefs.isContestReminderSet(contest.getId())) {
                continue;
            }

            ContestAlarmReceiver.scheduleContestReminder(
                    context,
                    contest.getId(),
                    contest.getName(),
                    contest.getStartTimeSeconds() * 1000L);
        }

        Log.i(TAG, "Restored saved contest reminders after " + action);
    }
}
