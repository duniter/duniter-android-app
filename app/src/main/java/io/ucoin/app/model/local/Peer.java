package io.ucoin.app.model.local;

import java.io.Serializable;

public class Peer implements Serializable {

    private Long id;
    private Long currencyId;
    private String host;
    private int port;
    private String url;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public String toString() {
        return new StringBuilder().append("url=").append(url).append(",")
                .append("host=").append(host).append(",")
                .append("port=").append(port)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (id != null && o instanceof Peer) {
            return id.equals(((Peer)o).getId());
        }
        return super.equals(o);
    }

    /* -- Internal methods -- */

    protected String initUrl(String host, int port) {
        return String.format("http://%s:%s", host, port);
    }
}
