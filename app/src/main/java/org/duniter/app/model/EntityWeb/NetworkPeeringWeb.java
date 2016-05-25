package org.duniter.app.model.EntityWeb;


import android.content.Context;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class NetworkPeeringWeb extends Web{

    private final String address;
    private final int port;

    public NetworkPeeringWeb(Context context, String address, int port){
        super(context);
        this.address = address;
        this.port = port;
    }

    @Override
    public String getUrl() {
        return "http://" + address + ":" + port + "/network/peering/";
    }

    @Override
    public String postUrl() {
        return null;
    }
}
