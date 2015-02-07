package io.ucoin.app.model;

import java.io.Serializable;

public class Peer implements Serializable {

    private final String host;
    private final int port;
    private final String url;

    public Peer(String host, int port) {
        this.host = host;
        this.port = port;
        this.url = initUrl(host, port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUrl() {
        return url;
    }

    public String toString() {
        return new StringBuilder().append("url=").append(url).append(",")
                .append("host=").append(host).append(",")
                .append("port=").append(port)
                .toString();
    }

    /* -- Internal methods -- */

    protected String initUrl(String host, int port) {
        return String.format("http://%s:%s", host, port);
    }
}
