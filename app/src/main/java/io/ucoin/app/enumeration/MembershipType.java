package io.ucoin.app.enumeration;


public enum MembershipType {
    IN("IN"),
    OUT("OUT"),
    UNKNOWN(null);

    MembershipType(String m) {
    }

    public static MembershipType fromString(String s) {
        if (s == null)
            return UNKNOWN;
        else
            return valueOf(s);
    }
}