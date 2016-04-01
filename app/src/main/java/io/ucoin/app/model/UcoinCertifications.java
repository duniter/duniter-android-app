package io.ucoin.app.model;

import io.ucoin.app.enumeration.CertificationType;
import io.ucoin.app.model.http_api.WotCertification;

public interface UcoinCertifications extends SqlTable, Iterable<UcoinCertification> {
    UcoinCertifications add(Long currencyId, WotCertification certification, CertificationType type);

    UcoinCertification getById(Long id);

    UcoinCertification getBySignature(String signature);

    UcoinCertifications getByType(CertificationType type);
}