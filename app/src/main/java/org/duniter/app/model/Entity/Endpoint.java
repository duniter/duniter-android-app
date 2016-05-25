package org.duniter.app.model.Entity;

import java.io.Serializable;

/**
 * Created by naivalf27 on 04/04/16.
 */
public class Endpoint implements Serializable{
    private String ipv4;
    private int port;
    private String url;
    private String ipv6;
    private String protocol;
    private long id;
    private Currency currency;
    private String publickKey;
    private String signature;

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIpv6() {
        return ipv6;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setPublickKey(String publickKey) {
        this.publickKey = publickKey;
    }

    public String getPublickKey() {
        return publickKey;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}
