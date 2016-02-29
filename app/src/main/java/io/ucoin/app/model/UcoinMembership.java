package io.ucoin.app.model;

import io.ucoin.app.enumeration.DayOfWeek;
import io.ucoin.app.enumeration.MembershipState;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.enumeration.Month;

public interface UcoinMembership extends SqlRow{
    Long identityId();

    Long version();

    MembershipType type();

    Long blockNumber();

    String blockHash();

    Long time();

    Integer year();

    Month month();

    Integer day();

    DayOfWeek dayOfWeek();

    String hour();

    Long expirationTime();

    Integer expirationYear();

    Month expirationMonth();

    Integer expirationDay();

    DayOfWeek expirationDayOfWeek();

    String expirationHour();

    Boolean expired();

    MembershipState state();

    UcoinIdentity identity();

    void setState(MembershipState state);
}