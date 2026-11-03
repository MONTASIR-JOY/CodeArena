package com.kuet.codearena.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kuet.codearena.data.db.AppDatabase;
import com.kuet.codearena.data.db.dao.UpsolveDao;

/**
 * ContentProvider exposing a read-only cursor interface to the competitive programming upsolve queue.
 */
public class UpsolveContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.kuet.codearena.provider";
    public static final String PATH_UPSOLVE = "upsolve";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_UPSOLVE);

    public static final String MIME_DIR = "vnd.android.cursor.dir/vnd.com.kuet.codearena.provider.upsolve";
    public static final String MIME_ITEM = "vnd.android.cursor.item/vnd.com.kuet.codearena.provider.upsolve";

    private static final int CODE_UPSOLVE_DIR = 1;
    private static final int CODE_UPSOLVE_ITEM = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, PATH_UPSOLVE, CODE_UPSOLVE_DIR);
        URI_MATCHER.addURI(AUTHORITY, PATH_UPSOLVE + "/#", CODE_UPSOLVE_ITEM);
    }

    private UpsolveDao upsolveDao;

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            upsolveDao = AppDatabase.getInstance(getContext()).upsolveDao();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (upsolveDao == null && getContext() != null) {
            upsolveDao = AppDatabase.getInstance(getContext()).upsolveDao();
        }
        if (upsolveDao == null) return null;

        Cursor cursor;
        int match = URI_MATCHER.match(uri);

        switch (match) {
            case CODE_UPSOLVE_DIR:
                cursor = upsolveDao.getUpsolveCursor();
                break;
            case CODE_UPSOLVE_ITEM:
                long id = Long.parseLong(uri.getLastPathSegment());
                cursor = upsolveDao.getUpsolveCursorById(id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null && cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_UPSOLVE_DIR:
                return MIME_DIR;
            case CODE_UPSOLVE_ITEM:
                return MIME_ITEM;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("UpsolveContentProvider is strictly read-only.");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("UpsolveContentProvider is strictly read-only.");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("UpsolveContentProvider is strictly read-only.");
    }
}
