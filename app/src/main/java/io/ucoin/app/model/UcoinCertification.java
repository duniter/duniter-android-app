package io.ucoin.app.model;

import io.ucoin.app.enumeration.CertificationState;
import io.ucoin.app.enumeration.CertificationType;

public interface UcoinCertification extends SqlRow {
    Long identityId();

    Long memberId();

    CertificationType type();

    Long block();

    Long medianTime();

    String signature();

    UcoinMember member();

    CertificationState state();

    void setState(CertificationState state);

    UcoinIdentity identity();
}