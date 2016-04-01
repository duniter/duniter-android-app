package io.ucoin.app.model;

public interface UcoinRequirement extends SqlRow {

    Long currencyId();

    Long identityId();

    String publicKey();

    Long expiresIn();

    UcoinCurrency currency();

    UcoinIdentity identity();
}
