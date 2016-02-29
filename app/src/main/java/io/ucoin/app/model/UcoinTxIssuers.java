package io.ucoin.app.model;


public interface UcoinTxIssuers extends SqlTable, Iterable<UcoinTxIssuer> {
    UcoinTxIssuer add(String publicKey, Integer sortOrder);

    UcoinTxIssuer getById(Long id);
}