package io.ucoin.app.model;

import io.ucoin.app.enumeration.MembershipState;
import io.ucoin.app.enumeration.MembershipType;

public interface UcoinMembership extends SqlRow{
    Long identityId();

    Long version();

    MembershipType type();

    Long blockNumber();

    String blockHash();

    Long time();

    Long expirationTime();

    Boolean expired();

    MembershipState state();

    UcoinIdentity identity();

    void setState(MembershipState state);
}