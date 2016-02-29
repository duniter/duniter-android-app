package io.ucoin.app.model.sql.sqlite;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinPeer;
import io.ucoin.app.model.UcoinPeers;
import io.ucoin.app.model.http_api.NetworkPeering;
import io.ucoin.app.sqlite.SQLiteTable;

final public class Peers extends Table
        implements UcoinPeers {

    private Long mCurrencyId;

    public Peers(Context context, Long currencyId) {
        this(context, currencyId, SQLiteTable.Peer.CURRENCY_ID + "=?", new String[]{currencyId.toString()});
    }

    private Peers(Context context, Long currencyId, String selection, String[] selectionArgs) {
        this(context, currencyId, selection, selectionArgs, null);
    }

    private Peers(Context context, Long currencyId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.PEER_URI, selection, selectionArgs, sortOrder);
        mCurrencyId = currencyId;
    }

    @Override
    public UcoinPeer add(NetworkPeering networkPeering) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Peer.CURRENCY_ID, mCurrencyId);
        values.put(SQLiteTable.Peer.PUBLIC_KEY, networkPeering.pubkey);
        values.put(SQLiteTable.Peer.SIGNATURE, networkPeering.signature);

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(UcoinUris.PEER_URI)
                .withValues(values)
                .build());

        for (NetworkPeering.Endpoint endpoint : networkPeering.endpoints) {
            values = new ContentValues();
            values.put(SQLiteTable.Endpoint.PROTOCOL, endpoint.protocol.name());
            values.put(SQLiteTable.Endpoint.URL, endpoint.url);
            values.put(SQLiteTable.Endpoint.IPV4, endpoint.ipv4);
            values.put(SQLiteTable.Endpoint.IPV6, endpoint.ipv6);
            values.put(SQLiteTable.Endpoint.PORT, endpoint.port);

            operations.add(ContentProviderOperation.newInsert(UcoinUris.ENDPOINT_URI)
                    .withValues(values)
                    .withValueBackReference(SQLiteTable.Endpoint.PEER_ID, 0)
                    .build());
        }

        ContentProviderResult[] result;
        try {
            result = applyBatch(operations);
        } catch (Exception e) {
            return null;
        }
        return new Peer(mContext, Long.parseLong(result[0].uri.getLastPathSegment()));
    }

    @Override
    public UcoinPeer getById(Long id) {
        return new Peer(mContext, id);
    }

    @Override
    public UcoinPeer at(int position) {
        Iterator<UcoinPeer> it = iterator();
        if (it == null) return null;

        while (position-- > 0) it.next();

        return it.next();
    }

    @Override
    public Iterator<UcoinPeer> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinPeer> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Peer(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}