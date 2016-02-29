package io.ucoin.app.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

public interface SqlTable {
    Integer count();

    int delete();

    Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder);

    Uri insert(ContentValues values);
    ContentProviderResult[] applyBatch (ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException;
}
