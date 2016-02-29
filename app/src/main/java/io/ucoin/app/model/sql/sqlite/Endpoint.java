package io.ucoin.app.model.sql.sqlite;

import android.content.Context;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.EndpointProtocol;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.sqlite.SQLiteTable;

public class Endpoint extends Row
        implements UcoinEndpoint {

    public Endpoint(Context context, Long endpointId) {
        super(context, UcoinUris.ENDPOINT_URI, endpointId);
    }

    @Override
    public Long peerId() {
        return getLong(SQLiteTable.Endpoint.PEER_ID);
    }

    @Override
    public EndpointProtocol protocol() {
        return EndpointProtocol.valueOf(getString(SQLiteTable.Endpoint.PROTOCOL));
    }

    @Override
    public String ipv4() {
        return getString(SQLiteTable.Endpoint.IPV4);
    }

    @Override
    public String ipv6() {
        return getString(SQLiteTable.Endpoint.IPV6);
    }

    @Override
    public String url() {
        return getString(SQLiteTable.Endpoint.URL);
    }

    @Override
    public Integer port() {
        return getInt(SQLiteTable.Endpoint.PORT);
    }

    @Override
    public String toString() {
        return "ENDPOINT id=" + ((id() == null) ? "not in database" : id()) + "\n" +
                "peer_id=" + peerId() + "\n" +
                "protocol=" + protocol() + "\n" +
                "url=" + url() + "\n" +
                "ipv4=" + ipv4() + "\n" +
                "ipv6=" + ipv6() + "\n" +
                "port=" + port();
    }
}