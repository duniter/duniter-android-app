package io.ucoin.app.model;

import io.ucoin.app.enumeration.CertificationState;
import io.ucoin.app.enumeration.CertificationType;

public interface UcoinCertification extends SqlRow {
    Long identityId();

    String uid();

    String publicKey();

    Boolean isMember();

    Boolean wasMember();

    CertificationType type();

    Long block();

    Long medianTime();

    Long sigDate();

    String signature();

    Long number();

    String hash();

    CertificationState state();

    void setState(CertificationState state);

    UcoinIdentity identity();
}