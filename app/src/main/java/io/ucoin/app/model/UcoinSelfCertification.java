package io.ucoin.app.model;

import io.ucoin.app.enumeration.SelfCertificationState;

public interface UcoinSelfCertification extends SqlRow {
    Long identityId();

    Long timestamp();

    String self();

    SelfCertificationState state();

    void setState(SelfCertificationState state);

    UcoinIdentity identity();
}