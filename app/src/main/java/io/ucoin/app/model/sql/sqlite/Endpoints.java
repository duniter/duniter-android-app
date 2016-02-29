package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.EndpointProtocol;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinEndpoints;
import io.ucoin.app.model.http_api.NetworkPeering;
import io.ucoin.app.sqlite.SQLiteTable;

final public class Endpoints extends Table
        implements UcoinEndpoints {

    private Long mPeerId;

    public Endpoints(Context context, Long peerId) {
        this(context, peerId, SQLiteTable.Endpoint.PEER_ID + "=?",new String[]{peerId.toString()});
    }

    private Endpoints(Context context, Long peerId, String selection, String[] selectionArgs) {
        this(context, peerId, selection, selectionArgs, null);
    }

    private Endpoints(Context context, Long peerId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.ENDPOINT_URI, selection, selectionArgs, sortOrder);
        mPeerId = peerId;
    }

    @Override
    public UcoinEndpoint add(NetworkPeering.Endpoint endpoint) {

        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Endpoint.PEER_ID, mPeerId);
        values.put(SQLiteTable.Endpoint.PROTOCOL, endpoint.protocol.name());
        values.put(SQLiteTable.Endpoint.URL, endpoint.url);
        values.put(SQLiteTable.Endpoint.IPV4, endpoint.ipv4);
        values.put(SQLiteTable.Endpoint.IPV6, endpoint.ipv6);
        values.put(SQLiteTable.Endpoint.PORT, endpoint.port);

        Uri uri = insert(values);
        return new Endpoint(mContext, Long.parseLong(uri.getLastPathSegment()));
    }

    @Override
    public UcoinEndpoint getById(Long id) {
        return new Endpoint(mContext, id);
    }

    @Override
    public Iterator<UcoinEndpoint> iterator() {
        Cursor cursor = fetch();
        if(cursor != null) {
            ArrayList<UcoinEndpoint> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Endpoint(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }

    @Override
    public UcoinEndpoints getByProtocol(EndpointProtocol protocol) {
        String selection = SQLiteTable.Endpoint.PEER_ID + "=? AND " +
                SQLiteTable.Endpoint.PROTOCOL + "=?";
        String[] selectionArgs = new String[]{mPeerId.toString(), protocol.name()};
        return new Endpoints(mContext, mPeerId, selection, selectionArgs);
    }

    @Override
    public UcoinEndpoint at(int position) {
        Iterator<UcoinEndpoint> it = iterator();
        if(it == null) return null;

        while(position-- > 0) it.next();
        return it.next();
    }
}