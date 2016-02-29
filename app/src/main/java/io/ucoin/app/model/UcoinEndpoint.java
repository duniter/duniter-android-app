package io.ucoin.app.model;

import io.ucoin.app.enumeration.EndpointProtocol;

public interface UcoinEndpoint extends SqlRow {
    Long peerId();

    EndpointProtocol protocol();

    String url();

    String ipv4();

    String ipv6();

    Integer port();
}
