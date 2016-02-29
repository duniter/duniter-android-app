package io.ucoin.app.model;


public interface UcoinTxSignatures extends SqlTable, Iterable<UcoinTxSignature> {
    UcoinTxSignature add(String signature, Integer sortOrder);

    UcoinTxSignature getById(Long id);
}