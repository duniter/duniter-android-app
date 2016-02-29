package io.ucoin.app.model;

import io.ucoin.app.enumeration.EndpointProtocol;
import io.ucoin.app.model.http_api.NetworkPeering;

public interface UcoinEndpoints extends SqlTable, Iterable<UcoinEndpoint> {
    UcoinEndpoint add(NetworkPeering.Endpoint endpoint);

    UcoinEndpoint getById(Long id);

    UcoinEndpoints getByProtocol(EndpointProtocol protocol);

    UcoinEndpoint at(int position);
}
