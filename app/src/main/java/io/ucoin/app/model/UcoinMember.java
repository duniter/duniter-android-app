package io.ucoin.app.model;

public interface UcoinMember extends SqlRow {
    Long identityId();

    String uid();

    String publicKey();

    String self();

    Long timestamp();

    void setSelf(String self);

    void setTimestamp(Long timestamp);

    UcoinIdentity identity();
}

