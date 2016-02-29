package io.ucoin.app.model;

import io.ucoin.app.enumeration.DayOfWeek;
import io.ucoin.app.enumeration.MembershipType;

public interface UcoinIdentity extends SqlRow {
    Long currencyId();

    String publicKey();

    String uid();

    Long sigDate();


    Long selfCount();

    Boolean isMember();

    Boolean wasMember();

    MembershipType lastMembership();

    Long expirationTime();

    Integer expirationYear();

    Integer expirationMonth();

    Integer expirationDay();

    DayOfWeek expirationDayOfWeek();

    String expirationHour();

    Long syncBlock();

    void setSigDate(Long sigDate);

    void setSyncBlock(Long block);

    UcoinCurrency currency();

    UcoinCertifications certifications();

    UcoinSelfCertifications selfCertifications();

    UcoinMemberships memberships();

    UcoinMembers members();
}
