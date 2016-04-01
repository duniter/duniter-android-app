package io.ucoin.app.model;

import io.ucoin.app.enumeration.MembershipType;

public interface UcoinIdentity extends SqlRow {
    Long currencyId();

    String publicKey();

    String uid();

    Long sigDate();


    Long selfCount();

    Boolean isMember();

    Boolean wasMember();

    long nbRequirements();

    MembershipType lastMembership();

    Long expirationTime();

    Long syncBlock();

    void setSigDate(Long sigDate);

    void setSyncBlock(Long block);

    UcoinCurrency currency();

    UcoinCertifications certifications();

    UcoinSelfCertifications selfCertifications();

    UcoinRequirements requirements();

    UcoinMemberships memberships();

    UcoinMembers members();
}
