package org.duniter.app.model.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by naivalf27 on 04/04/16.
 */
public class Peer implements Serializable{
    public List<Endpoint> endpoints;
    private long id;
    private Currency currency;
    private String publicKey;
    private String signature;
    private long currencyId;

    public Peer(long id) {
        this.id =id;
    }
    public Peer(){}

    public List<Endpoint> getEndpoints() {
        return endpoints==null ? new ArrayList<Endpoint>() : endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
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
        this.currencyId = currency.getId();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(long currencyId) {
        this.currencyId = currencyId;
    }

    public void addEndpoint(Endpoint endpoint) {
        if(endpoints==null){
            endpoints = new ArrayList<>();
        }
        endpoints.add(endpoint);
    }
}