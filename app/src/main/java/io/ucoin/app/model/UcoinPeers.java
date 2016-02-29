package io.ucoin.app.model;

import io.ucoin.app.model.http_api.NetworkPeering;

public interface UcoinPeers extends SqlTable, Iterable<UcoinPeer> {
    UcoinPeer add(NetworkPeering networkPeering);

    UcoinPeer getById(Long id);

    UcoinPeer at(int position);
}
