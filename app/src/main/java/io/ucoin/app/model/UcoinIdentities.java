package io.ucoin.app.model;

import io.ucoin.app.technical.crypto.AddressFormatException;

public interface UcoinIdentities extends SqlTable, Iterable<UcoinIdentity> {
    UcoinIdentity add(String uid, String publicKey) throws AddressFormatException;

    UcoinIdentity addWallet(String uid, String publicKey, Long walletId) throws AddressFormatException;

    UcoinIdentity getById(Long id);

    UcoinIdentity getIdentity();

    UcoinIdentity getIdentityByWallet(Long id);
}
