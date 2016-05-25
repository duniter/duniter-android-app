package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Endpoint;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 20/04/16.
 */
public class EndpointSql extends AbstractSql<Endpoint> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(EndpointTable.TABLE_NAME+"/").build();
    public static final int CODE = 40;



    public EndpointSql(Context context) {
        super(context,URI);
    }

    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + EndpointTable.TABLE_NAME + "(" +
                EndpointTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                EndpointTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                EndpointTable.PUBLIC_KEY + TEXT + COMMA +
                EndpointTable.SIGNATURE + TEXT + COMMA +
                EndpointTable.PROTOCOL + TEXT + COMMA +
                EndpointTable.URL + TEXT + COMMA +
                EndpointTable.IPV4 + TEXT + COMMA +
                EndpointTable.IPV6 + TEXT + COMMA +
                EndpointTable.PORT + INTEGER + COMMA +
                "FOREIGN KEY (" + EndpointTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ") ON DELETE CASCADE " + COMMA +
                " UNIQUE (" + EndpointTable.CURRENCY_ID + COMMA + EndpointTable.IPV4 + COMMA + EndpointTable.PORT + ")" +
                ")";
    }

    @Override
    public Endpoint fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(EndpointTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(EndpointTable.CURRENCY_ID);
        int publicKeyIndex = cursor.getColumnIndex(EndpointTable.PUBLIC_KEY);
        int signatureIndex = cursor.getColumnIndex(EndpointTable.SIGNATURE);
        int protocolIndex = cursor.getColumnIndex(EndpointTable.PROTOCOL);
        int urlIndex = cursor.getColumnIndex(EndpointTable.URL);
        int ipv4Index = cursor.getColumnIndex(EndpointTable.IPV4);
        int ipv6Index = cursor.getColumnIndex(EndpointTable.IPV6);
        int portIndex = cursor.getColumnIndex(EndpointTable.PORT);

        Endpoint endpoint = new Endpoint();
        endpoint.setId(cursor.getLong(idIndex));
        endpoint.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        endpoint.setPublickKey(cursor.getString(publicKeyIndex));
        endpoint.setSignature(cursor.getString(signatureIndex));
        endpoint.setProtocol(cursor.getString(protocolIndex));
        endpoint.setUrl(cursor.getString(urlIndex));
        endpoint.setIpv4(cursor.getString(ipv4Index));
        endpoint.setIpv6(cursor.getString(ipv6Index));
        endpoint.setPort(cursor.getInt(portIndex));

        return endpoint;
    }

    @Override
    public ContentValues toContentValues(Endpoint entity) {
        ContentValues values = new ContentValues();
        values.put(EndpointTable.CURRENCY_ID, entity.getCurrency().getId());
        values.put(EndpointTable.PUBLIC_KEY,entity.getPublickKey());
        values.put(EndpointTable.SIGNATURE,entity.getSignature());
        values.put(EndpointTable.PROTOCOL, entity.getProtocol());
        values.put(EndpointTable.URL, entity.getUrl());
        values.put(EndpointTable.IPV4, entity.getIpv4());
        values.put(EndpointTable.IPV6, entity.getIpv6());
        values.put(EndpointTable.PORT, entity.getPort());
        return values;
    }

    public class EndpointTable implements BaseColumns {
        public static final String TABLE_NAME = "endpoint";

        public static final String CURRENCY_ID = "currency_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String SIGNATURE = "signature";
        public static final String PROTOCOL = "protocol";
        public static final String URL = "url";
        public static final String IPV4 = "ipv4";
        public static final String IPV6 = "ipv6";
        public static final String PORT = "port";

    }
}
