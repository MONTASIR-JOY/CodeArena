package com.codearena.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;
import com.codearena.data.db.AppDatabase;

/**
 * CodeArenaContentProvider: Provides structured inter-app data sharing
 * for competitive programming contests, problems, and virtual run records.
 */
public class CodeArenaContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.codearena.provider";

    public static final Uri CONTENT_URI_CONTESTS = Uri.parse("content://" + AUTHORITY + "/contests");
    public static final Uri CONTENT_URI_PROBLEMS = Uri.parse("content://" + AUTHORITY + "/problems");
    public static final Uri CONTENT_URI_VIRTUAL_RUNS = Uri.parse("content://" + AUTHORITY + "/virtual_runs");
    public static final Uri CONTENT_URI_UPSOLVE = Uri.parse("content://" + AUTHORITY + "/upsolve");

    private static final int CODE_CONTESTS = 1;
    private static final int CODE_CONTEST_ID = 2;
    private static final int CODE_PROBLEMS = 3;
    private static final int CODE_VIRTUAL_RUNS = 4;
    private static final int CODE_UPSOLVE = 5;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, "contests", CODE_CONTESTS);
        URI_MATCHER.addURI(AUTHORITY, "contests/#", CODE_CONTEST_ID);
        URI_MATCHER.addURI(AUTHORITY, "problems", CODE_PROBLEMS);
        URI_MATCHER.addURI(AUTHORITY, "virtual_runs", CODE_VIRTUAL_RUNS);
        URI_MATCHER.addURI(AUTHORITY, "upsolve", CODE_UPSOLVE);
    }

    private AppDatabase database;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            database = AppDatabase.getInstance(getContext());
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (database == null && getContext() != null) {
            database = AppDatabase.getInstance(getContext());
        }
        if (database == null) return null;

        SupportSQLiteDatabase db = database.getOpenHelper().getReadableDatabase();
        String table;
        String finalSelection = selection;

        switch (URI_MATCHER.match(uri)) {
            case CODE_CONTESTS:
                table = "contests";
                break;
            case CODE_CONTEST_ID:
                table = "contests";
                long contestId = ContentUris.parseId(uri);
                finalSelection = "id = " + contestId + (selection != null ? " AND (" + selection + ")" : "");
                break;
            case CODE_PROBLEMS:
                table = "problems";
                break;
            case CODE_VIRTUAL_RUNS:
                table = "virtual_runs";
                break;
            case CODE_UPSOLVE:
                table = "upsolve_items";
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SupportSQLiteQueryBuilder queryBuilder = SupportSQLiteQueryBuilder.builder(table)
                .columns(projection)
                .selection(finalSelection, selectionArgs)
                .orderBy(sortOrder);

        Cursor cursor = db.query(queryBuilder.create());
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_CONTESTS:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".contests";
            case CODE_CONTEST_ID:
                return "vnd.android.cursor.item/" + AUTHORITY + ".contests";
            case CODE_PROBLEMS:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".problems";
            case CODE_VIRTUAL_RUNS:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".virtual_runs";
            case CODE_UPSOLVE:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".upsolve";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (database == null && getContext() != null) {
            database = AppDatabase.getInstance(getContext());
        }
        if (database == null || values == null) return null;

        SupportSQLiteDatabase db = database.getOpenHelper().getWritableDatabase();
        String table;
        switch (URI_MATCHER.match(uri)) {
            case CODE_CONTESTS:
                table = "contests";
                break;
            case CODE_PROBLEMS:
                table = "problems";
                break;
            case CODE_VIRTUAL_RUNS:
                table = "virtual_runs";
                break;
            case CODE_UPSOLVE:
                table = "upsolve_items";
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        long id = db.insert(table, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, values);
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return itemUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (database == null && getContext() != null) {
            database = AppDatabase.getInstance(getContext());
        }
        if (database == null) return 0;

        SupportSQLiteDatabase db = database.getOpenHelper().getWritableDatabase();
        String table;
        switch (URI_MATCHER.match(uri)) {
            case CODE_CONTESTS:
                table = "contests";
                break;
            case CODE_PROBLEMS:
                table = "problems";
                break;
            case CODE_VIRTUAL_RUNS:
                table = "virtual_runs";
                break;
            case CODE_UPSOLVE:
                table = "upsolve_items";
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        int count = db.delete(table, selection, selectionArgs);
        if (count > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        if (database == null && getContext() != null) {
            database = AppDatabase.getInstance(getContext());
        }
        if (database == null || values == null) return 0;

        SupportSQLiteDatabase db = database.getOpenHelper().getWritableDatabase();
        String table;
        switch (URI_MATCHER.match(uri)) {
            case CODE_CONTESTS:
                table = "contests";
                break;
            case CODE_PROBLEMS:
                table = "problems";
                break;
            case CODE_VIRTUAL_RUNS:
                table = "virtual_runs";
                break;
            case CODE_UPSOLVE:
                table = "upsolve_items";
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        int count = db.update(table, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, values, selection, selectionArgs);
        if (count > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}

