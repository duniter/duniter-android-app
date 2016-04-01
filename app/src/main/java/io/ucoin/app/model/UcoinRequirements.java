package io.ucoin.app.model;

import io.ucoin.app.model.http_api.WotRequirements;

public interface UcoinRequirements extends SqlTable, Iterable<UcoinRequirement> {

    UcoinRequirement add(long currencyId, long identityId, String publicKey, long expiresIn);

    UcoinRequirements add(long currencyId, WotRequirements wotRequirements);

    void remove();

    UcoinRequirement getById(Long id);
}
