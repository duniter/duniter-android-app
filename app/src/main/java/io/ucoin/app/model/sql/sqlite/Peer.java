package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinEndpoints;
import io.ucoin.app.model.UcoinPeer;
import io.ucoin.app.sqlite.SQLiteTable;

public class Peer extends Row
        implements UcoinPeer {


    public Peer(Context context, Long peerId) {
        super(context, UcoinUris.PEER_URI, peerId);
    }

    @Override
    public Long currencyId() {
        return getLong(SQLiteTable.Peer.CURRENCY_ID);
    }

    @Override
    public String publicKey() {
        return getString(SQLiteTable.Peer.PUBLIC_KEY);
    }

    @Override
    public String signature() {
        return getString(SQLiteTable.Peer.SIGNATURE);
    }

    @Override
    public UcoinEndpoints endpoints() {
        return new Endpoints(mContext, mId);
    }

    @Override
    public String toString() {
        String s = "PEER id=" + ((id() == null) ? "not in database" : id()) + "\n" +
                "currency_id=" + currencyId() + "\n" +
                "public_key=" + publicKey() + "\n" +
                "signature=" + signature();

        return s;
    }
}