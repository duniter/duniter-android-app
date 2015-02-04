package io.ucoin.app.model;

import java.io.Serializable;

public class Peer implements Serializable {

    private String mUrl;
    private String mIPv4;
    private String mIPv6;
    private int mPort;

    //todo filter address
    public Peer(String address, int port) {
        mUrl = address;
        mIPv4= address;
        mIPv6 = address;
        mPort = port;

    }

    public Peer(String url, String IPv4, String IPv6, int port) {
        mUrl = url;
        mIPv4= IPv4;
        mIPv6 = IPv6;
        mPort = port;

    }

    public String getIPv4() {
        return mIPv4;
    }

    public String getIPv6() {
        return mIPv6;
    }

    public String getUrl() {
        return mUrl;
    }

    public int getPort() {
        return mPort;
    }

    public String toString() {
        String string = "url=" + mUrl + "\n" +
        "ipv4=" + mIPv4 + "\n" +
        "ipv6=" + mIPv6 + "\n" +
        "port=" + Integer.toString(mPort);

        return string;
    }
}
