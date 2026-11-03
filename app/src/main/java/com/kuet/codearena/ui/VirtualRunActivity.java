package com.kuet.codearena.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity hosting the live virtual contest simulator with lifecycle logging and real-time timer.
 */
public class VirtualRunActivity extends AppCompatActivity {
    public static final String EXTRA_CONTEST_ID = "extra_contest_id";
    public static final String EXTRA_CONTEST_NAME = "extra_contest_name";
    public static final String EXTRA_DURATION_SECONDS = "extra_duration_seconds";
    public static final String EXTRA_RUN_ID = "extra_run_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
