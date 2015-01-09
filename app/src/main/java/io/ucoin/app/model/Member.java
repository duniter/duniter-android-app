package io.ucoin.app.model;

public class Member extends Identity{

    private static final long serialVersionUID = 8448049949323699700L;

    private String number;

    private String hash;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
