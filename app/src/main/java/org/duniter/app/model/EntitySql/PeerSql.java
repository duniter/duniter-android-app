package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Peer;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 20/04/16.
 */
public class PeerSql extends AbstractSql<Peer> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(PeerTable.TABLE_NAME+"/").build();
    public static final int CODE = 60;


    public PeerSql(Context context) {
        super(context,URI);
    }

    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + PeerTable.TABLE_NAME + "(" +
                PeerTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                PeerTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                PeerTable.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                PeerTable.SIGNATURE + TEXT + NOTNULL + UNIQUE + COMMA +
                "FOREIGN KEY (" + PeerTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ") ON DELETE CASCADE" +
                ")";
    }

    @Override
    public Peer fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(PeerTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(PeerTable.CURRENCY_ID);
        int publicKeyIndex = cursor.getColumnIndex(PeerTable.PUBLIC_KEY);
        int signatureIndex = cursor.getColumnIndex(PeerTable.SIGNATURE);

        Peer peer = new Peer();
        peer.setId(cursor.getLong(idIndex));
        peer.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        peer.setPublicKey(cursor.getString(publicKeyIndex));
        peer.setSignature(cursor.getString(signatureIndex));

        return peer;
    }

    @Override
    public ContentValues toContentValues(Peer entity) {
        ContentValues values = new ContentValues();
        values.put(PeerTable.CURRENCY_ID, entity.getCurrencyId());
        values.put(PeerTable.PUBLIC_KEY, entity.getPublicKey());
        values.put(PeerTable.SIGNATURE, entity.getSignature());
        return values;
    }

    public class PeerTable implements BaseColumns {
        public static final String TABLE_NAME = "peer";

        public static final String CURRENCY_ID = "currency_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SIGNATURE = "signature";
    }
}
