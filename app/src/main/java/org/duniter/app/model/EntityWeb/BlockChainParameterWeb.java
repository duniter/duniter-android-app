package org.duniter.app.model.EntityWeb;

import android.content.Context;

/**
 * Created by naivalf27 on 19/04/16.
 */
public class BlockChainParameterWeb extends Web{
    private String address;
    private int port;

    public BlockChainParameterWeb(Context context, String address, int port) {
        super(context);
        this.address = address;
        this.port = port;
    }

    @Override
    public String getUrl() {
        return "http://" + address + ":" + port + "/blockchain/parameters/";
    }

    @Override
    public String postUrl() {
        return null;
    }
}
