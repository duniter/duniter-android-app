package io.ucoin.app.model;

import io.ucoin.app.model.http_api.WotCertification;
import io.ucoin.app.model.http_api.WotLookup;

public interface UcoinMembers extends SqlTable, Iterable<UcoinMember> {
    UcoinMember add(WotLookup.Result result);

    UcoinMember add(WotCertification.Certification certification);

    UcoinMember getById(Long id);

    UcoinMember getByPublicKey(String publicKey);

    UcoinMember getBySelf(String self);
}