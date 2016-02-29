package io.ucoin.app.model;

import io.ucoin.app.enumeration.CertificationType;
import io.ucoin.app.model.http_api.WotCertification;

public interface UcoinCertifications extends SqlTable, Iterable<UcoinCertification> {
    UcoinCertification add(UcoinMember member, CertificationType type, WotCertification.Certification certification);

    UcoinCertification getById(Long id);

    UcoinCertification getBySignature(String signature);

    UcoinCertifications getByType(CertificationType type);
}