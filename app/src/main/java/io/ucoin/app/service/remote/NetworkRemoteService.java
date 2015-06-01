package io.ucoin.app.service.remote;

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.remote.NetworkPeerResults;
import io.ucoin.app.model.remote.NetworkPeering;

/**
 * Created by eis on 05/02/15.
 */
public class NetworkRemoteService extends BaseRemoteService{


    private static final String TAG = "NetworkRemoteService";

    public static final String URL_BASE = "/network";

    public static final String URL_PEERING = URL_BASE + "/peering";

    public static final String URL_PEERING_PEERS = URL_PEERING + "/peers";

    public static final String URL_PEERING_PEERS_LEAF = URL_PEERING + "/peers?leaf=";

    public NetworkRemoteService() {
        super();
    }

    public NetworkPeering getPeering(Peer peer) {
        NetworkPeering result = httpService.executeRequest(peer, URL_PEERING, NetworkPeering.class);
        return result;
    }

    public List<Peer> getPeers(Peer peer) {
        List<Peer> result = new ArrayList<Peer>();

        NetworkPeering peering = getPeering(peer);

        NetworkPeerResults firstResult = httpService.executeRequest(peer, URL_PEERING_PEERS, NetworkPeerResults.class);

        // TODO : get some depth. depth=2 only ?

        return result;
    }


    /* -- Internal methods -- */

}
