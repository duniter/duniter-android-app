package io.ucoin.app.model;

import io.ucoin.app.enumeration.SelfCertificationState;
import io.ucoin.app.model.http_api.WotLookup;

public interface UcoinSelfCertifications extends SqlTable, Iterable<UcoinSelfCertification> {
    UcoinSelfCertification add(WotLookup.Uid certification);

    UcoinSelfCertification getById(Long id);

    UcoinSelfCertification getBySelf(String self);

    UcoinSelfCertifications getByState(SelfCertificationState state);
}