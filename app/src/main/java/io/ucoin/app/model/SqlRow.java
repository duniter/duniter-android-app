package io.ucoin.app.model;

import android.content.ContentValues;

public interface SqlRow {
    Long id();

    int delete();

    int update(ContentValues values);
}
