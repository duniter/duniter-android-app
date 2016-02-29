package io.ucoin.app.model;


import io.ucoin.app.enumeration.SourceState;
import io.ucoin.app.enumeration.SourceType;
import io.ucoin.app.model.http_api.TxSources;

public interface UcoinSources extends SqlTable, Iterable<UcoinSource> {
    UcoinSource add(TxSources.Source source);

    UcoinSources set(TxSources sources);

    UcoinSource getById(Long id);

    UcoinSources getByState(SourceState state);

    UcoinSources getByType(SourceType type);

    UcoinSource getByFingerprint(String fingerprint);
}